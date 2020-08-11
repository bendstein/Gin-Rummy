import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class StrategyDiscardResearch extends StrategyDiscard {

    Random random;

    /**
     * In the default strategy, we are not training
     */
    public StrategyDiscardResearch(boolean training) {
        super(training);
        random = new Random();
    }

    @Override
    public ActionDiscard[] getStrategy(GameState state) {
        ActionDiscard[] strategy = new ActionDiscard[1];
        GinRummyAndTonic_v10.State state2 = state.getCurrentPlayerObject().state2;

        /*
         * First, get all cards who's removal would lower our deadwood the most (with a margin of 2)
         * Ignoring the face_up if it is the card that was drawn this turn.
         */
        long candidateCards = GinRummyPlayerImpl.MyGinRummyUtil.findHighestDiscards(state2.getHand(),
                state.getDrawn(state.getCurrentPlayer()), state2.getFaceUp(), 2);

        long toRemove = 0L;

        /*
         * Remove melded cards from consideration
         */
        for(int c : GinRummyPlayerImpl.MyGinRummyUtil.bitstringToIDArray(candidateCards)) {
            if(GinRummyPlayerImpl.MyGinRummyUtil.contains(GinRummyPlayerImpl.MyGinRummyUtil.getMelded(state2.getHand(), 0L), c))
                toRemove = GinRummyPlayerImpl.MyGinRummyUtil.add(toRemove, c);
        }

        /*
         * If removing the cards from consideration empties our group of candidate cards, don't remove them.
         */
        if(GinRummyPlayerImpl.MyGinRummyUtil.removeAll(candidateCards, toRemove) != 0L) {
            candidateCards = GinRummyPlayerImpl.MyGinRummyUtil.removeAll(candidateCards, toRemove);
        }

        /*
         * If there's only one card left to consider, discard it
         */
        if(GinRummyPlayerImpl.MyGinRummyUtil.size(candidateCards) == 1) {
            strategy[0] = new ActionDiscard(candidateCards, 1.0, null);
            return strategy;
        }

        /*
         * If any of our potential discards could be melded by the opponent, based on what we know is
         * in their hand, don't consider discarding it.
         */
        ArrayList<ArrayList<Card>> oppMelds = GinRummyUtil.cardsToAllMelds(GinRummyUtil.bitstringToCards(state2.getOppHand()));

        for(Card c : GinRummyUtil.bitstringToCards(toRemove)) {
            for(ArrayList<Card> meld : oppMelds) {
                if(meld.contains(c)) {
                    toRemove = GinRummyPlayerImpl.MyGinRummyUtil.add(toRemove, c.getId());
                    break;
                }
            }
        }

        /*
         * If removing the cards from consideration empties our group of candidate cards, don't remove them.
         */
        if(GinRummyPlayerImpl.MyGinRummyUtil.removeAll(candidateCards, toRemove) != 0L) {
            candidateCards = GinRummyPlayerImpl.MyGinRummyUtil.removeAll(candidateCards, toRemove);
        }

        /*
         * If there's only one card left to consider, discard it
         */
        if(GinRummyPlayerImpl.MyGinRummyUtil.size(candidateCards) == 1) {
            strategy[0] = new ActionDiscard(candidateCards, 1.0, null);
            return strategy;
        }


        /*
         * Get the number of melds each card can still make, based on the cards that we have not yet seen, and the
         * cards that are in the opponent's hand.
         */

        TreeMap<Integer, Integer> meldTurns = new TreeMap<>();
        for(int i : GinRummyPlayerImpl.MyGinRummyUtil.bitstringToIDArray(candidateCards))
            meldTurns.put(i, state2.getUsefulnessToOpponent(i));

        /*
         * If there are any cards that are "dead" (can't be melded), only consider them.
         * If there are none, do the same for any cards that only have 1 remaining meld.
         * Do the same for 2, 3.
         */

        long temp = 0L;

        for(int i = 0; i < 4; i++) {
            for(Map.Entry<Integer, Integer> entry : meldTurns.entrySet()) {
                if(entry.getValue() == i) {
                    temp = GinRummyPlayerImpl.MyGinRummyUtil.add(temp, entry.getKey());
                }
            }
            if(temp == 0L)
                break;
        }

        /*
         * If filtering the cards leaves us with nothing, don't filter.
         */
        if(temp != 0L)
            candidateCards = temp;

        /*
         * If there's only one card left to consider, discard it
         */
        if(GinRummyPlayerImpl.MyGinRummyUtil.size(candidateCards) == 1) {
            strategy[0] = new ActionDiscard(candidateCards, 1.0, null);
            return strategy;
        }

        /*
         * If the opponent has cards in his hand that are either
         *  a) Of the same rank as a potential discard, or
         *  b) Of adjacent rank, and of the same suit, as a potential discard
         * then, don't consider it for discard
         */

        toRemove = 0L;

        for(int i : GinRummyPlayerImpl.MyGinRummyUtil.bitstringToIDArray(candidateCards)) {
            if(GinRummyPlayerImpl.MyGinRummyUtil.containsRank(state2.getOppHand(), i) || GinRummyPlayerImpl.MyGinRummyUtil.containsSuit(state2.getOppHand(), i, 1))
                toRemove = GinRummyPlayerImpl.MyGinRummyUtil.add(toRemove, i);
        }

        /*
         * If removing the cards from consideration empties our group of candidate cards, don't remove them.
         */
        if(GinRummyPlayerImpl.MyGinRummyUtil.removeAll(candidateCards, toRemove) != 0L) {
            candidateCards = GinRummyPlayerImpl.MyGinRummyUtil.removeAll(candidateCards, toRemove);
        }

        /*
         * Also check same-suit cards who are 2 ranks away.
         */

        toRemove = 0L;

        for(int i : GinRummyPlayerImpl.MyGinRummyUtil.bitstringToIDArray(candidateCards)) {
            if(GinRummyPlayerImpl.MyGinRummyUtil.containsSuit(state2.getOppHand(), i, 2))
                toRemove = GinRummyPlayerImpl.MyGinRummyUtil.add(toRemove, i);
        }

        /*
         * If removing the cards from consideration empties our group of candidate cards, don't remove them.
         */
        if(GinRummyPlayerImpl.MyGinRummyUtil.removeAll(candidateCards, toRemove) != 0L) {
            candidateCards = GinRummyPlayerImpl.MyGinRummyUtil.removeAll(candidateCards, toRemove);
        }
        /*
         * If the opponent has discarded or ignored cards that are either
         *  a) Of the same rank as a potential discard, or
         *  b) Of adjacent rank, and of the same suit, as a potential discard
         * then, continue to consider it for discard it
         */

        temp = 0L;
        for(int c : GinRummyPlayerImpl.MyGinRummyUtil.bitstringToIDArray(candidateCards)) {
            if(GinRummyPlayerImpl.MyGinRummyUtil.containsRank(state2.getOppDiscard(), c) || GinRummyPlayerImpl.MyGinRummyUtil.containsSuit(state2.getOppDiscard(), c, 1))
                temp = GinRummyPlayerImpl.MyGinRummyUtil.add(temp, c);
            if(GinRummyPlayerImpl.MyGinRummyUtil.containsRank(state2.getOppForwent(), c) || GinRummyPlayerImpl.MyGinRummyUtil.containsSuit(state2.getOppForwent(), c, 1))
                temp = GinRummyPlayerImpl.MyGinRummyUtil.add(temp, c);
        }

        /*
         * If filtering the cards leaves us with nothing, don't filter.
         */
        if(temp != 0L)
            candidateCards = temp;

        /*
         * Also check same-suit cards who are 2 ranks away.
         */

        temp = 0L;
        for(int c : GinRummyPlayerImpl.MyGinRummyUtil.bitstringToIDArray(candidateCards)) {
            if(GinRummyPlayerImpl.MyGinRummyUtil.containsSuit(state2.getOppDiscard(), c, 2))
                temp = GinRummyPlayerImpl.MyGinRummyUtil.add(temp, c);
            if(GinRummyPlayerImpl.MyGinRummyUtil.containsSuit(state2.getOppForwent(), c, 2))
                temp = GinRummyPlayerImpl.MyGinRummyUtil.add(temp, c);
        }

        /*
         * If filtering the cards leaves us with nothing, don't filter.
         */
        if(temp != 0L)
            candidateCards = temp;

        /*
         * If there are any cards that would take more that 2 draws to meld, only consider them
         */
        temp = 0L;
        ArrayList<Card> isolated = GinRummyUtil.bitstringToCards(GinRummyAndTonic_v10.MyGinRummyUtil.getIsolatedSingles(state2.getHand(), 0L, state2));
        for(int c : GinRummyPlayerImpl.MyGinRummyUtil.bitstringToIDArray(candidateCards)) {
            if(isolated.contains(Card.getCard(c)))
                temp = GinRummyPlayerImpl.MyGinRummyUtil.add(temp, c);
        }

        /*
         * If filtering the cards leaves us with nothing, don't filter.
         */
        if(temp != 0L)
            candidateCards = temp;

        int c;

        /*
         * If there is only 1 remaining candidate card, that is our discard
         */
        if(GinRummyAndTonic_v10.MyGinRummyUtil.bitstringToIDArray(candidateCards).length == 1)
            c = GinRummyAndTonic_v10.MyGinRummyUtil.bitstringToIDArray(candidateCards)[0];

        /*
         * Otherwise, discard a random card from our list of candidate cards
         */
        else
            c = GinRummyAndTonic_v10.MyGinRummyUtil.bitstringToIDArray(candidateCards)[random.nextInt(GinRummyAndTonic_v10.MyGinRummyUtil.bitstringToIDArray(candidateCards).length - 1)];

        strategy[0] = new ActionDiscard(MyGinRummyUtil.add(0L, c), 1.0, null);
        return strategy;
    }

    @Override
    public String getName() {
        return "Research Discard Strategy";
    }

}
