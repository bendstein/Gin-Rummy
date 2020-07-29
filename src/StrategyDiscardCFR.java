import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class StrategyDiscardCFR extends StrategyDiscard {
    private NavigableMap<String, ConcurrentSkipListSet<int[]>> infosets;

    /**
     * In the default strategy, we are not training
     */
    public StrategyDiscardCFR(boolean training) {
        super(training);
        infosets = new ConcurrentSkipListMap<>();
    }

    @Override
    public ActionDiscard[] getStrategy(GameState state) {

        //The card that we drew
        int drawn = MyGinRummyUtil.bitstringToIDArray(state.getPreviousState().getCurrentPlayerCards() ^ state.getCurrentPlayerCards())[0];

        //All cards that we haven't seen yet
        ArrayList<Card> unaccounted =
                GinRummyUtil.bitstringToCards(MyGinRummyUtil
                        .removeAll(MyGinRummyUtil
                                .cardsToBitstring(new ArrayList<>(Arrays.asList(Card.allCards))), state
                                .getCurrentPlayerSeenCards()));

        //All cards under the face-up card
        long buried = state.getCurrentPlayerSeenCards();
        buried = GinRummyAndTonic_v2.MyGinRummyUtil.removeAll(buried, state.getCurrentPlayerCards());
        buried = GinRummyAndTonic_v2.MyGinRummyUtil.removeAll(buried, state.getPlayerCards(state.getCurrentPlayer() == 0? 1 : 0));
        buried = GinRummyAndTonic_v2.MyGinRummyUtil.removeAll(buried, state.getFaceUpCard());

        //Don't discard cards that are in melds
        long candidateCards = state.getCurrentPlayerCards();
        candidateCards = GinRummyAndTonic_v2.MyGinRummyUtil.removeAll(candidateCards, MyGinRummyUtil.getMelded(candidateCards, 0L));

        //If we drew the face up card, we can't discard it.
        if(state.getPreviousState().getFaceUpCard() == drawn)
            candidateCards = GinRummyAndTonic_v2.MyGinRummyUtil.remove(candidateCards, drawn);

        //If all cards are melded, find the meld set we would use to knock and choose the best discard from a meld of size > 3.
        if(candidateCards == 0L) {
            ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(state.getCurrentPlayerCardsAsList());
            ArrayList<ArrayList<Card>> bestMeldSet = null;
            double minExpectedLayoff = Double.MAX_VALUE;

            for(ArrayList<ArrayList<Card>> meldSet : bestMeldSets) {
                ArrayList<Card> layoff = new ArrayList<>();

                // Add all cards to layoff who could be inserted into our hand
                for (ArrayList<Card> meld : meldSet) {
                    // Meld of cards of same rank
                    if (meld.get(0).getRank() == meld.get(1).getRank()) {
                        layoff.addAll(
                                GinRummyAndTonic_v2.MyGinRummyUtil.getSameRank(GinRummyAndTonic_v2.MyGinRummyUtil.bitstringToCards(
                                        GinRummyAndTonic_v2.MyGinRummyUtil.addAll(state.getPlayerCards(state.getCurrentPlayer() == 0? 1 : 0), GinRummyUtil.cardsToBitstring(unaccounted))), meld.get(0)));
                    }

                    // Cards of same suit
                    else {
                        layoff.addAll(GinRummyAndTonic_v2.MyGinRummyUtil.getSameSuit(GinRummyAndTonic_v2.MyGinRummyUtil.bitstringToCards(
                                GinRummyAndTonic_v2.MyGinRummyUtil.addAll(state.getPlayerCards(state.getCurrentPlayer() == 0? 1 : 0), GinRummyUtil.cardsToBitstring(unaccounted))),
                                meld.get(0), 1));
                        layoff.addAll(GinRummyAndTonic_v2.MyGinRummyUtil.getSameSuit(GinRummyAndTonic_v2.MyGinRummyUtil.bitstringToCards(
                                GinRummyAndTonic_v2.MyGinRummyUtil.addAll(state.getPlayerCards(state.getCurrentPlayer() == 0? 1 : 0), GinRummyUtil.cardsToBitstring(unaccounted))),
                                meld.get(meld.size() - 1), 1));
                    }
                }

                /*
                 * The sum of the deadwood of each layoff card * the probability that the opponent has said card
                 * is expectedLayoff. If expectedLayoff < minExpectedLayoff, it is the new minimum, so assign
                 * bestMeldSet to the current meld set. In the end, return the meld set with the lowest expectedLayoff.
                 */
                double expectedLayoff = 0d;
                for(Card card : layoff) {
                    //If the card is in an opponent meld, we don't expect them to try to lay it off.
                    if(GinRummyAndTonic_v2.MyGinRummyUtil.canOpponentMeld(card, state.getPlayerCards(state.getCurrentPlayer() == 0? 1 : 0), buried)) continue;
                    expectedLayoff += GinRummyUtil.getDeadwoodPoints(card) *
                            GinRummyAndTonic_v2.MyGinRummyUtil.getProbabilityThatOpponentHasUnseenCard(MyGinRummyUtil.idsToBitstring(new int[]{card.getId()}), buried, GinRummyUtil.cardsToBitstring(unaccounted), state.getPlayerCards(state.getCurrentPlayer() == 0? 1 : 0));
                }

                if(expectedLayoff < minExpectedLayoff) {
                    minExpectedLayoff = expectedLayoff;
                    bestMeldSet = meldSet;
                }

            }

            for(ArrayList<Card> meld : bestMeldSet) {
                if(meld.size() > 3) {

                    /*
                     * TODO: Find the discard that minimizes layoff opportunity
                     */
                    return new ActionDiscard[]{new ActionDiscard(GinRummyUtil.cardsToBitstring(new ArrayList<>(Collections.singletonList(meld.get(0)))), 1.0, "")};
                }
            }
        }

        //If there's only one card left, always discard it.
        if(GinRummyAndTonic_v2.MyGinRummyUtil.size(candidateCards) == 1) {
            return new ActionDiscard[]{new ActionDiscard(GinRummyUtil.cardsToBitstring(
                    new ArrayList<>(Collections.singletonList(Card.getCard(GinRummyAndTonic_v2.MyGinRummyUtil.bitstringToIDArray(candidateCards)[0])))), 1.0, "")};
        }
        //If there are more than 2 cards remaining, keep pruning
        else if(GinRummyAndTonic_v2.MyGinRummyUtil.size(candidateCards) > 2) {
            //At this point, all cards are unmelded
            //Check if card is useful to opponent

            //Cards we've seen that the opponent doesn't have in their hand
            long toRemove = 0L;
            long preferred = 0L;
            long oppDiscard = MyGinRummyUtil.add(MyGinRummyUtil.addAll(buried, state.getCurrentPlayerCards()), state.getFaceUpCardAsObject().getId());

            for (Card c : MyGinRummyUtil.bitstringToCards(candidateCards)) {

                /*
                 * If a card could be used in an opp meld, or at least bring them closer, avoid
                 * discarding it
                 */
                if (MyGinRummyUtil.canOpponentMeld(c, state.getCurrentPlayerKnownOpponentCards(), buried))
                    toRemove = MyGinRummyUtil.add(toRemove, c.getId()); // If card could help opp meld, avoid tossing
                else if (MyGinRummyUtil.containsRank(state.getCurrentPlayerKnownOpponentCards(), c.getId())
                        || MyGinRummyUtil.containsSuit(state.getCurrentPlayerKnownOpponentCards(), c.getId(), 2))
                    toRemove = MyGinRummyUtil.add(toRemove, c.getId()); // If card brings opp closer to a meld, avoid
                    // tossing

                    /*
                     * If the opp has discarded cards that could be melded with this card, it is
                     * less likely they would find it useful. Prefer to discard any of these cards.
                     */
                else if (MyGinRummyUtil.containsRank(oppDiscard, c.getId())
                        || MyGinRummyUtil.containsSuit(oppDiscard, c.getId(), 2))
                    preferred = MyGinRummyUtil.add(preferred, c.getId()); // If similar cards have been tossed, prefer
            }

            if (toRemove != candidateCards)
                candidateCards = MyGinRummyUtil.removeAll(candidateCards, toRemove); // Remove useful cards to the
            // opponent, unless all cards would
            // be useful
            if (preferred != 0L && preferred != candidateCards)
                candidateCards = MyGinRummyUtil.removeAll(candidateCards, ~preferred); // Only consider cards which we
            // would prefer to discard

            //If there's only one card left, always discard it.
            if(GinRummyAndTonic_v2.MyGinRummyUtil.size(candidateCards) == 1) {
                return new ActionDiscard[]{new ActionDiscard(GinRummyUtil.cardsToBitstring(
                        new ArrayList<>(Collections.singletonList(Card.getCard(GinRummyAndTonic_v2.MyGinRummyUtil.bitstringToIDArray(candidateCards)[0])))), 1.0, "")};
            }
            //If there are more than 2 cards remaining, keep pruning
            else if(GinRummyAndTonic_v2.MyGinRummyUtil.size(candidateCards) > 2) {
                int maxImprove = Integer.MIN_VALUE;
                int maxId = 0;
                int secondMaxImprove = Integer.MIN_VALUE;
                int secondMaxId = 0;

                for(Card c : GinRummyUtil.bitstringToCards(candidateCards)) {
                    int improve = GinRummyAndTonic_v2.MyGinRummyUtil.getImprovement(state.getCurrentPlayerCards(), c.getId());
                    if(improve > maxImprove) {
                        secondMaxImprove = maxImprove;
                        secondMaxId = maxId;
                        maxImprove = improve;
                        maxId = c.getId();
                    }
                    else if(improve > secondMaxImprove) {
                        secondMaxImprove = improve;
                        secondMaxId = c.getId();
                    }
                }

                candidateCards = GinRummyAndTonic_v2.MyGinRummyUtil.add(GinRummyAndTonic_v2.MyGinRummyUtil.add(0L, maxId), secondMaxId);

            }
        }

        //At this point, all remaining action sets should be of size 2.

        int[] candidates = GinRummyAndTonic_v2.MyGinRummyUtil.bitstringToIDArray(candidateCards);

        //Improvement for discarding card 0
        int improvement0 = GinRummyAndTonic_v2.MyGinRummyUtil.getImprovement(state.getCurrentPlayerCards(), candidates[0]);

        //Improvement for discarding card 1
        int improvement1 = GinRummyAndTonic_v2.MyGinRummyUtil.getImprovement(state.getCurrentPlayerCards(), candidates[1]);

        //Number of draws to meld card 0
        int minDrawsToMeld0 = 0;

        /*
         * We couldn't meld this card even if we draw 2 more face-down cards
         */
        if(GinRummyUtil.bitstringToCards(MyGinRummyUtil
                .getIsolatedSingles(state.getCurrentPlayerCards(), 0L, GinRummyUtil.cardsToBitstring(unaccounted))).contains(state.getFaceUpCardAsObject()))
            minDrawsToMeld0 = 2;

            /*
             * We couldn't meld this card even if we draw 1 more face-down card
             */
        else if(GinRummyUtil.bitstringToCards(MyGinRummyUtil
                .getSingles(state.getCurrentPlayerCards(), 0L, GinRummyUtil.cardsToBitstring(unaccounted))).contains(state.getFaceUpCardAsObject()))
            minDrawsToMeld0 = 1;

        //Number of draws to meld card 1
        int minDrawsToMeld1 = 0;

        /*
         * We couldn't meld this card even if we draw 2 more face-down cards
         */
        if(GinRummyUtil.bitstringToCards(MyGinRummyUtil
                .getIsolatedSingles(state.getCurrentPlayerCards(), 0L, GinRummyUtil.cardsToBitstring(unaccounted))).contains(state.getFaceUpCardAsObject()))
            minDrawsToMeld1 = 2;

            /*
             * We couldn't meld this card even if we draw 1 more face-down card
             */
        else if(GinRummyUtil.bitstringToCards(MyGinRummyUtil
                .getSingles(state.getCurrentPlayerCards(), 0L, GinRummyUtil.cardsToBitstring(unaccounted))).contains(state.getFaceUpCardAsObject()))
            minDrawsToMeld1 = 1;

        /*
         * Generalize the infoset by using the differences in improvement and minDrawsToMeld. Will hopefully speed up
         * convergence by removing terms.
         */
        String infoset = (improvement0 - improvement1) + "_" + (minDrawsToMeld0 - minDrawsToMeld1) + "_" + state.getTopCard();

        /*
         * If the infoset hasn't been seen, add it with the two potential cards.
         * The infoset is kind of general, so we need to keep tracks of every case
         * that it applies to.
         */
        if(!infosets.containsKey(infoset))
            infosets.put(infoset, new ConcurrentSkipListSet<int[]>(new Comparator<int[]>() {
                @Override
                public int compare(int[] o1, int[] o2) {
                    int sum1 = 0;
                    int sum2 = 0;
                    for(int i = 0; i < Math.max(o1.length, o2.length); i++) {
                        if(i < o1.length) sum1 += o1[i];
                        if(i < o2.length) sum2 += o2[i];
                    }

                    return sum1 - sum2;
                }
            }) {
                {
                    add(new int[]{candidates[0], candidates[1]});
                }
            });

        else {
            /*
             * If the infoset exists with the cards swapped, swap the cards so that it matches.
             * contains() isn't working properly with the int[]'s, but there hopefully shouldn't
             * be *too* many pairs of cards with the same infoset, so it shouldn't slow things
             * down too much.
             */
            boolean contains = false;
            for(int[] cards : infosets.get(infoset)) {
                if(cards.length != candidates.length) continue;
                else if(cards[0] == candidates[0] && cards[1] == candidates[1]) {
                    contains = true;
                    break;
                }
                else if(cards[0] == candidates[1] && cards[1] == candidates[0]) {
                    int temp = candidates[0];
                    candidates[0] = candidates[1];
                    candidates[1] = temp;
                    contains = true;
                    break;
                }
            }

            /*
             * If the infoset has been seen, but not with these cards, add these cards
             */
            if(!contains)
                infosets.get(infoset).add(new int[]{candidates[0], candidates[1]});
        }

        return new ActionDiscard[]{
                new ActionDiscard(GinRummyUtil.cardsToBitstring(
                        new ArrayList<>(Collections.singletonList(Card.getCard(candidates[0])))), 0.0, infoset + "_" + candidates[1] + "_" + candidates[0]),
                new ActionDiscard(GinRummyUtil.cardsToBitstring(
                        new ArrayList<>(Collections.singletonList(Card.getCard(candidates[1])))), 0.0, infoset + "_" + candidates[0] + "_" + candidates[1])
        };

    }

    /**
     * Deprecated
     */
    public ActionDiscard[] getStrategyOld(GameState state) {
        ActionDiscard[] strategy = new ActionDiscard[1];

        //The card that is in our hand that wasn't last turn is the card we drew. We can't discard it.
        int drawn = MyGinRummyUtil.bitstringToIDArray(state.getPreviousState().getCurrentPlayerCards() ^ state.getCurrentPlayerCards())[0];

        long candidateCards =
                MyGinRummyUtil.findHighestDiscards(state.getCurrentPlayerCards(), drawn, state.getFaceUpCardAsObject().getId(), 0);
        long toRemove = 0L;
        ArrayList<Integer> ids = MyGinRummyUtil.bitstringToIDs(candidateCards);
        ArrayList<Integer> temp = new ArrayList<>(ids);
        long unseen = MyGinRummyUtil.removeAll(GinRummyUtil.cardsToBitstring(new ArrayList<Card>(Arrays.asList(Card.allCards))), state.getCurrentPlayerSeenCards());


        for (int i : ids) {
            temp.remove((Integer) i);
            if (!MyGinRummyUtil
                    .contains((MyGinRummyUtil.getIsolatedSingles(state.getCurrentPlayerCards(), MyGinRummyUtil.idsToBitstring(temp), unseen)), i))
                toRemove = MyGinRummyUtil.add(toRemove, i);
            temp.add((Integer) i);
        }

        if (toRemove != candidateCards)
            candidateCards = MyGinRummyUtil.removeAll(candidateCards, toRemove);

        else {
            toRemove = 0L;

            for (int i : ids) {
                temp.remove((Integer) i);
                if (!MyGinRummyUtil
                        .contains((MyGinRummyUtil.getSingles(state.getCurrentPlayerCards(), MyGinRummyUtil.idsToBitstring(temp), unseen)), i))
                    toRemove = MyGinRummyUtil.add(toRemove, i);
                temp.add((Integer) i);
            }

            if (toRemove != candidateCards)
                candidateCards = MyGinRummyUtil.removeAll(candidateCards, toRemove);
        }

        /*
         * Prefer cards who cannot be melded even after 2 draws. If there are none (or
         * no cards can), prefer those who can't be melded after 1 draw.
         */
        long not_singles = ~MyGinRummyUtil.getIsolatedSingles(state.getCurrentPlayerCards(), 0L, unseen);
        candidateCards = MyGinRummyUtil.removeAll(candidateCards, not_singles) == 0L ? candidateCards
                : MyGinRummyUtil.removeAll(candidateCards, not_singles);

        if (MyGinRummyUtil.removeAll(candidateCards, not_singles) == 0L) {
            not_singles = ~MyGinRummyUtil.getSingles(state.getCurrentPlayerCards(), 0L, unseen);
            candidateCards = MyGinRummyUtil.removeAll(candidateCards, not_singles) == 0L ? candidateCards
                    : MyGinRummyUtil.removeAll(candidateCards, not_singles);
        }

        /*
         * Then, filter out cards which would be helpful to the opponent
         */
        if (MyGinRummyUtil.size(candidateCards) > 1) {
            toRemove = 0L; // Don't remove until after loop
            long preferred = 0L; // Cards we would prefer to remove

            //Cards we've seen that are no longer accessible
            long buried = MyGinRummyUtil
                    .remove(MyGinRummyUtil
                            .removeAll(MyGinRummyUtil
                                    .removeAll(state.getCurrentPlayerSeenCards(), state.getCurrentPlayerCards()), state.getCurrentPlayerKnownOpponentCards()), state.getFaceUpCardAsObject().getId());

            //Cards we've seen that the opponent doesn't have in their hand
            long oppDiscard = MyGinRummyUtil.add(MyGinRummyUtil.addAll(buried, state.getCurrentPlayerCards()), state.getFaceUpCardAsObject().getId());

            for (Card c : MyGinRummyUtil.bitstringToCards(candidateCards)) {

                /*
                 * If a card could be used in an opp meld, or at least bring them closer, avoid
                 * discarding it
                 */
                if (MyGinRummyUtil.canOpponentMeld(c, state.getCurrentPlayerKnownOpponentCards(), buried))
                    toRemove = MyGinRummyUtil.add(toRemove, c.getId()); // If card could help opp meld, avoid tossing
                else if (MyGinRummyUtil.containsRank(state.getCurrentPlayerKnownOpponentCards(), c.getId())
                        || MyGinRummyUtil.containsSuit(state.getCurrentPlayerKnownOpponentCards(), c.getId(), 2))
                    toRemove = MyGinRummyUtil.add(toRemove, c.getId()); // If card brings opp closer to a meld, avoid
                    // tossing

                    /*
                     * If the opp has discarded cards that could be melded with this card, it is
                     * less likely they would find it useful. Prefer to discard any of these cards.
                     */
                else if (MyGinRummyUtil.containsRank(oppDiscard, c.getId())
                        || MyGinRummyUtil.containsSuit(oppDiscard, c.getId(), 2))
                    preferred = MyGinRummyUtil.add(preferred, c.getId()); // If similar cards have been tossed, prefer
            }

            if (toRemove != candidateCards)
                candidateCards = MyGinRummyUtil.removeAll(candidateCards, toRemove); // Remove useful cards to the
            // opponent, unless all cards would
            // be useful
            if (preferred != 0L && preferred != candidateCards)
                candidateCards = MyGinRummyUtil.removeAll(candidateCards, ~preferred); // Only consider cards which we
            // would prefer to discard

        }

        /*
         * If there are more than 2 cards left, if any are dupled, avoid throwing them
         * away
         */
        if (MyGinRummyUtil.size(candidateCards) > 2) {
            ArrayList<Integer> cards = MyGinRummyUtil.bitstringToIDs(candidateCards);
            long duples = 0L;

            for (int i : cards)
                duples += MyGinRummyUtil.getDuples(candidateCards, i);

            if (MyGinRummyUtil.removeAll(candidateCards, duples) != 0L)
                candidateCards = MyGinRummyUtil.removeAll(candidateCards, duples);

        }

        strategy[0] = new ActionDiscard(MyGinRummyUtil.cardsToBitstring(new ArrayList<Card>(Collections.singletonList(MyGinRummyUtil.bitstringToCards(candidateCards).get(0)))), 1.0, null);

        return strategy;
    }

    /**
     * @see Strategy#getName()
     */
    @Override
    public String getName() {
        return "DefaultDiscardStrategy";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(getName()).append("\nDiscards\n");
        for(String s : infosets.descendingKeySet()) {
            for(int[] cards : infosets.get(s)) {
                String key_k = s + "_" + cards[1] + "_" + cards[0];
                String key_n = s + "_" + cards[0] + "_" + cards[1];

                double k = sumStrategy.getOrDefault(key_k,1.0);
                double n = sumStrategy.getOrDefault(key_n,1.0);

                /*
                 * Prevent NaN's from popping up
                 */
                if(k == n && k == 0.0)
                    continue;
                //if(!sumStrategy.containsKey(key_k) || !sumStrategy.containsKey(key_n)) continue;
                //double value = sumStrategy.getOrDefault(key_k,1.0) /
                //(sumStrategy.getOrDefault(key_k,1.0) + sumStrategy.getOrDefault(key_n,1.0));
                sb.append(String.format(Locale.US, "%s\t%.3f\t%.3f\n", key_k, k / (k + n), 1 - (k / (k + n))));
            }
        }

        return sb.toString();
    }

    @Override
    public void toFile(String fname) throws FileNotFoundException {

        PrintWriter pw = new PrintWriter(fname);

        for(String s : infosets.descendingKeySet()) {
            for(int[] cards : infosets.get(s)) {
                String key_k = s + "_" + cards[1] + "_" + cards[0];
                String key_n = s + "_" + cards[0] + "_" + cards[1];


                double k = sumStrategy.getOrDefault(key_k,1.0);
                double n = sumStrategy.getOrDefault(key_n,1.0);

                /*
                 * Prevent NaN's from popping up
                 */
                if(k == n && k == 0.0)
                    continue;
                //if(!sumStrategy.containsKey(key_k) || !sumStrategy.containsKey(key_n)) continue;
                //double value = sumStrategy.getOrDefault(key_k,1.0) /
                        //(sumStrategy.getOrDefault(key_k,1.0) + sumStrategy.getOrDefault(key_n,1.0));
                pw.write(String.format(Locale.US, "put(\"%s\", %.3f);\n", key_k, k / (k + n)));
            }
        }

        pw.close();
    }
}
