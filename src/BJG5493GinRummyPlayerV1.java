import java.util.*;
import java.util.stream.Collectors;

/*
 * Class containing the player
 */
public class BJG5493GinRummyPlayerV1 implements GinRummyPlayer {

    private int playerNum; //The number assigned to our player
    private Random random = new Random(); //prng
    private boolean opponentKnocked = false; //Becomes true if opponent knocks
    private int drawn; //The id of the card which was drawn
    private Card drawnCard;
    private GameState state; //State of the game
    private GeneralStrategy generalStrategy; //Current strategy the player is using
    private ArrayList<ArrayList<Card>> oppMelds; //If opponent knocks, this is what their meld set is
    private static boolean encoded = true; //If false, we will also use week_5.Card objects. For debugging.

    @Override
    public void startGame(int playerNum, int startingPlayerNum, Card[] cards) {
        this.playerNum = playerNum;

        state = new GameState(new ArrayList<>(Arrays.asList(cards)), encoded);

        /*
         * Choose a random strategy, but only knock if gin.
         *
         * Order of strategy parameters:
         *
         * maxKnockDeadwood
         * maxIsolatedSingleDeadwood
         * minIsolatedSingleDiscardTurn
         * maxSingleDeadwood
         * minSingleDiscardTurn
         * minPickupDifference
         */
        generalStrategy = new GeneralStrategy(MyGinRummyUtil.decoded("034564"));


        oppMelds = null;
        opponentKnocked = false;

    }

    @Override
    public boolean willDrawFaceUpCard(Card card) {
        int card_id = card.getId();
        // If first turn, record the face-up card. All other unseen face-up cards should be recorded in reportDiscard()
        if(state.getTurn() == 0) {
            if(encoded) {
                state.addToSeen(card_id);
            }
            else {
                state.addToSeenCards(card);
            }
            state.decreaseNumRemaining();
        }

        //week_5.Card is our face-up
        if(encoded) {
            state.setFaceUp(card_id);
        }
        else {
            state.setFaceUpCard(card);
        }

        if(encoded) {
            return willDrawFaceUpCard(state.getHand(), state.getFaceUp());
        }

        else {
            return willDrawFaceUpCard(state.getHandCards(), state.getFaceUpCard());
        }
    }

    //See if we would draw card if we had the given hand
    public boolean willDrawFaceUpCard(long hand, int card_id) {

        /*
         * First: If picking up the face-up card will lower our deadwood by an amount that is at
         * least strategy.getMinPickupDifference(), and it lowers it more than we expect the face-down card to
         * draw it.
         */

        int cost = MyGinRummyUtil.makesNewMeld(hand, card_id);
        if(cost >= generalStrategy.getMinPickupDifference() &&
                cost > MyGinRummyUtil.expectedDeadwoodForNextDraw(state)) return true;

        /*
         * If the card can't be melded within 2 draws, don't draw it.
         */
        long newCards = MyGinRummyUtil.add(hand, card_id);

        if(MyGinRummyUtil.getDeadwoodPoints(Card.getCard(card_id)) > generalStrategy.getMaxIsolatedSingleDeadwood()
                && MyGinRummyUtil.contains(MyGinRummyUtil.getIsolatedSingles(newCards, 0L, state), card_id)) return false;
        else if(MyGinRummyUtil.getDeadwoodPoints(Card.getCard(card_id)) > generalStrategy.getMaxSingleDeadwood()
                && MyGinRummyUtil.contains(MyGinRummyUtil.getSingles(newCards, 0L, state), card_id)) return false;

        /*
         * Next: If the card doesn't increase deadwood too much, and the opponent could meld it, draw the face-up.
         */
        if(cost >= generalStrategy.getMaxSingleDeadwood() && MyGinRummyUtil.canOpponentMeld(Card.getCard(card_id), state)) return true;


        /*
         * Then, look at all within 2 of the highest discards. If the list doesn't contain the face-up, pick it up. Otherwise, don't.
         */
        long preferred = MyGinRummyUtil.findHighestDiscards(newCards, -1, -1, 2);

        return !MyGinRummyUtil.contains(preferred, card_id);

    }

    //See if we would draw card if we had the given hand
    public boolean willDrawFaceUpCard(ArrayList<Card> hand, Card card) {

        /*
         * First: If picking up the face-up card will lower our deadwood by an amount that is at
         * least strategy.getMinPickupDifference(), and it lowers it more than we expect the face-down card to
         * draw it.
         */

        //MyGinRummyUtil.printHandWithMelds(hand);
        //System.out.println();

        ArrayList<Card> newCards = new ArrayList<>(hand);
        newCards.add(card);

        //MyGinRummyUtil.printHandWithMelds(newCards);
        //System.out.println();

        int cost = MyGinRummyUtil.makesNewMeld(hand, card);
        if(cost >= generalStrategy.getMinPickupDifference() &&
                cost > MyGinRummyUtil.expectedDeadwoodForNextDraw(state)) return true;

        /*
         * If the card can't be melded within 2 draws, don't draw it.
         */

        if(MyGinRummyUtil.getDeadwoodPoints(card) > generalStrategy.getMaxIsolatedSingleDeadwood()
                && MyGinRummyUtil.getIsolatedSingles(newCards, null, state).contains(card)) return false;
        else if(MyGinRummyUtil.getDeadwoodPoints(card) > generalStrategy.getMaxSingleDeadwood()
                && MyGinRummyUtil.getSingles(newCards, null, state).contains(card)) return false;

        /*
         * Next: If the card doesn't increase deadwood too much, and the opponent could meld it, draw the face-up.
         */
        if(cost >= generalStrategy.getMaxSingleDeadwood() && MyGinRummyUtil.canOpponentMeld(card, state)) return true;


        /*
         * Then, look at all within 2 of the highest discards. If the list doesn't contain the face-up, pick it up. Otherwise, don't.
         */
        ArrayList<Card> preferred = MyGinRummyUtil.findHighestDiscards(newCards, null, null, 2);

        return !preferred.contains(card);

    }

    @Override
    public void reportDraw(int playerNum, Card drawnCard) {

        //If drawn card is null, player drew face-down. Decrease numRemaining
        if(drawnCard == null) state.decreaseNumRemaining();

        // Ignore other player draws.  Add to cards if playerNum is this player.
        if (playerNum == this.playerNum) {

            if(encoded) {
                //If we drew face-down, decrease numRemaining.
                if(drawnCard.getId() != state.getFaceUp()) state.decreaseNumRemaining();
                state.addToHand(drawnCard.getId());
                this.drawn = drawnCard.getId();
            }
            else {
                //If we drew face-down, decrease numRemaining.
                if(!drawnCard.equals(state.getFaceUpCard())) state.decreaseNumRemaining();
                state.addToHandCards(drawnCard);
                this.drawnCard = drawnCard;
            }
        }
        //If the other player drew, and drawnCard isn't null, other player drew face-up.
        else {
            if(drawnCard != null) {
                if(encoded) {
                    state.addToOppHand(drawnCard.getId());
                }
                else {
                    state.addToOppHandCards(drawnCard);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Card getDiscard() {
        if(encoded) {
            long potentialDiscards = findDiscard(state.getHand(), state.getFaceUp());
            return MyGinRummyUtil.bitstringToCards(potentialDiscards).get(random.nextInt(MyGinRummyUtil.size(potentialDiscards)));
        }

        else {
            ArrayList<Card> potentialDiscards = findDiscard(state.getHandCards(), state.getFaceUpCard());
            return potentialDiscards.get(random.nextInt(potentialDiscards.size()));
        }
    }

    //Find the card to discard in a hand, given the card that you can't discard.
    public long findDiscard(long hand, int face_up) {

        /*
         * First, get all cards who's removal would lower our deadwood the most
         */
        long candidateCards = MyGinRummyUtil.findHighestDiscards(hand, drawn, face_up, 0);
        long toRemove = 0L;
        ArrayList<Integer> ids = MyGinRummyUtil.bitstringToIDs(candidateCards);
        ArrayList<Integer> temp = new ArrayList<>(ids);

        for(int i : ids) {
            temp.remove((Integer) i);
            if(!MyGinRummyUtil.contains((MyGinRummyUtil.getIsolatedSingles(hand, MyGinRummyUtil.idsToBitstring(temp), state)), i))
                toRemove = MyGinRummyUtil.add(toRemove, i);
            temp.add((Integer) i);
        }

        if(toRemove != candidateCards) candidateCards = MyGinRummyUtil.removeAll(candidateCards, toRemove);

        else {
            toRemove = 0L;

            for(int i : ids) {
                temp.remove((Integer) i);
                if(!MyGinRummyUtil.contains((MyGinRummyUtil.getSingles(hand, MyGinRummyUtil.idsToBitstring(temp), state)), i))
                    toRemove = MyGinRummyUtil.add(toRemove, i);
                temp.add((Integer) i);
            }

            if(toRemove != candidateCards) candidateCards = MyGinRummyUtil.removeAll(candidateCards, toRemove);
        }

        /*
         * Prefer cards who cannot be melded even after 2 draws.
         * If there are none (or no cards can), prefer those who can't be melded after 1 draw.
         */
        long not_singles = ~MyGinRummyUtil.getIsolatedSingles(hand, 0L, state);
        candidateCards = MyGinRummyUtil.removeAll(candidateCards, not_singles) == 0L ?
                candidateCards : MyGinRummyUtil.removeAll(candidateCards, not_singles);

        if(MyGinRummyUtil.removeAll(candidateCards, not_singles) == 0L) {
            not_singles = ~MyGinRummyUtil.getSingles(hand, 0L, state);
            candidateCards = MyGinRummyUtil.removeAll(candidateCards, not_singles) == 0L ?
                    candidateCards : MyGinRummyUtil.removeAll(candidateCards, not_singles);
        }

        /*
         * Then, filter out cards which would be helpful to the opponent
         */
        if(MyGinRummyUtil.size(candidateCards) > 1) {
            toRemove = 0L; //Don't remove until after loop
            long preferred = 0L; //Cards we would prefer to remove

            for(Card c : MyGinRummyUtil.bitstringToCards(candidateCards)) {

                /*
                 * If a card could be used in an opp meld, or at least bring them closer, avoid discarding it
                 */
                if(MyGinRummyUtil.canOpponentMeld(c, state)) toRemove = MyGinRummyUtil.add(toRemove, c.getId()); //If card could help opp meld, avoid tossing
                else if(MyGinRummyUtil.containsRank(state.getOppHand(), c.getId()) || MyGinRummyUtil.containsSuit(state.getOppHand(), c.getId(), 2)) toRemove = MyGinRummyUtil.add(toRemove, c.getId()); //If card brings opp closer to a meld, avoid tossing

                /*
                 * If the opp has discarded cards that could be melded with this card, it is less likely they would find it useful. Prefer to discard any of these cards.
                 */
                else if(MyGinRummyUtil.containsRank(state.getOppDiscard(), c.getId()) || MyGinRummyUtil.containsSuit(state.getOppDiscard(), c.getId(), 2)) preferred = MyGinRummyUtil.add(preferred, c.getId()); //If similar cards have been tossed, prefer
            }

            if(toRemove != candidateCards) candidateCards = MyGinRummyUtil.removeAll(candidateCards, toRemove); //Remove useful cards to the opponent, unless all cards would be useful
            if(preferred != 0L && preferred != candidateCards) candidateCards = MyGinRummyUtil.removeAll(candidateCards, ~preferred); //Only consider cards which we would prefer to discard

        }

        /*
         * If there are more than 2 cards left, if any are dupled, avoid throwing them away
         */
        if(MyGinRummyUtil.size(candidateCards) > 2) {
            ArrayList<Integer> cards = MyGinRummyUtil.bitstringToIDs(candidateCards);
            long duples = 0L;

            for(int i : cards)
                duples += MyGinRummyUtil.getDuples(candidateCards, i);

            if(MyGinRummyUtil.removeAll(candidateCards, duples) != 0L)
                candidateCards = MyGinRummyUtil.removeAll(candidateCards, duples);

        }

        return candidateCards;

    }

    //Find the card to discard in a hand, given the card that you can't discard.
    public ArrayList<Card> findDiscard(ArrayList<Card> hand, Card face_up) {

        /*
         * First, get all cards who's removal would lower our deadwood the most
         */

        //MyGinRummyUtil.printHandWithMelds(hand);

        ArrayList<Card> candidateCards = MyGinRummyUtil.findHighestDiscards(hand, drawnCard, state.getFaceUpCard(), 0);

        /*
         * Prefer cards who cannot be melded even after 2 draws, not considering other potential discards.
         * If there are none (or no cards can), prefer those who can't be melded after 1 draw.
         */
        ArrayList<Card> temp = new ArrayList<>(candidateCards);
        ArrayList<Card> toRemove = new ArrayList<>();

        for(Card c : candidateCards) {
            temp.remove(c);
            if(!(MyGinRummyUtil.getIsolatedSingles(hand, temp, state)).contains(c))
                toRemove.add(c);
            temp.add(c);
        }

        if(toRemove.size() != candidateCards.size()) candidateCards.removeAll(toRemove);

        else {
            toRemove = new ArrayList<>();

            for(Card c : candidateCards) {
                temp.remove(c);
                if(!(MyGinRummyUtil.getSingles(hand, temp, state)).contains(c))
                    toRemove.add(c);
                temp.add(c);
            }

            if(toRemove.size() != candidateCards.size()) candidateCards.removeAll(toRemove);
        }


        /*
         * Then, filter out cards which would be helpful to the opponent
         */
        if(candidateCards.size() > 1) {
            toRemove = new ArrayList<>(); //Don't remove until after loop
            ArrayList<Card> preferred = new ArrayList<>(); //Cards we would prefer to remove

            for(Card c : candidateCards) {

                /*
                 * If a card could be used in an opp meld, or at least bring them closer, avoid discarding it
                 */
                if(MyGinRummyUtil.canOpponentMeld(c, state)) toRemove.add(c); //If card could help opp meld, avoid tossing
                else if(MyGinRummyUtil.containsRank(state.getOppHand(), c.getId()) || MyGinRummyUtil.containsSuit(state.getOppHand(), c.getId(), 2)) toRemove.add(c); //If card brings opp closer to a meld, avoid tossing

                    /*
                     * If the opp has discarded cards that could be melded with this card, it is less likely they would find it useful. Prefer to discard any of these cards.
                     */
                else if(MyGinRummyUtil.containsRank(state.getOppDiscard(), c.getId()) || MyGinRummyUtil.containsSuit(state.getOppDiscard(), c.getId(), 2)) preferred.add(c); //If similar cards have been tossed, prefer
            }

            if(toRemove.size() != candidateCards.size()) candidateCards.removeAll(toRemove); //Remove useful cards to the opponent, unless all cards would be useful
            if(!preferred.isEmpty() && preferred.size() != candidateCards.size()) candidateCards.removeIf(c -> (!preferred.contains(c))); //Only consider cards which we would prefer to discard

        }

        /*
         * If there are more than 2 cards left, if any are dupled, avoid throwing them away
         */
        if(candidateCards.size() > 2) {
            Set<Set<Card>> duples = new HashSet<>();
            toRemove = new ArrayList<>();

            for(Card c : candidateCards)
                duples.addAll(MyGinRummyUtil.getDuples(candidateCards, c));

            for(Set<Card> duple : duples) {
                for(Card c : duple) {
                    if(!toRemove.contains(c)) toRemove.add(c);
                }
            }

            if(toRemove.size() != candidateCards.size()) candidateCards.removeAll(toRemove);
        }

        return candidateCards;

    }

    @Override
    public void reportDiscard(int playerNum, Card discardedCard) {
        // Ignore other player discards.  Remove from cards if playerNum is this player.
        if (playerNum == this.playerNum) {
            if(encoded) {
                state.removeFromHand(discardedCard.getId());
            }
            else  {
                state.removeFromHandCards(discardedCard);
            }

            state.nextTurn();
        }

        //If we knew the discarded card was in the opponent's hand, remove. If we didn't, add it to seen.
        else {
            if(encoded) {
                state.addToSeen(discardedCard.getId());
                state.removeFromOppHand(discardedCard.getId());
            }
            else {
                state.addToSeenCards(discardedCard);
                state.removeFromOppHandCards(discardedCard);
            }

        }
    }

    @Override
    public ArrayList<ArrayList<Card>> getFinalMelds() {

        ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets;
        int deadwood;

        if(encoded) {
            bestMeldSets = MyGinRummyUtil.cardsToBestMeldSets(MyGinRummyUtil.bitstringToCards(state.getHand()));
            deadwood = bestMeldSets.isEmpty() ?
                    MyGinRummyUtil.getDeadwoodPoints(MyGinRummyUtil.bitstringToCards(state.getHand())) :
                    MyGinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), MyGinRummyUtil.bitstringToCards(state.getHand()));
        }
        else {
            //MyGinRummyUtil.printHandWithMelds(state.getHandCards());
            bestMeldSets = MyGinRummyUtil.cardsToBestMeldSets(state.getHandCards());
            deadwood = bestMeldSets.isEmpty() ?
                    MyGinRummyUtil.getDeadwoodPoints(state.getHandCards()) :
                    MyGinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), state.getHandCards());
        }

        // Check if deadwood of maximal meld is low enough to go out.


        //Only knock if deadwood is both less than or equal to 10 and strategy.getMaxKnockDeadwood() [0].

        if (!opponentKnocked &&
                (bestMeldSets.isEmpty() || deadwood > MyGinRummyUtil.MAX_DEADWOOD || deadwood > generalStrategy.getMaxKnockDeadwood()))
            return null;
        else if (!opponentKnocked)
            return bestMeldSets.isEmpty() ? new ArrayList<>() : bestMeldSets.get(random.nextInt(bestMeldSets.size()));


        else {
            ArrayList<Card> layoff = new ArrayList<>();

            if(bestMeldSets.isEmpty()) return new ArrayList<>();

            //Add all cards to layoff who could be inserted into opponent hand
            for(ArrayList<Card> meld : oppMelds) {
                //Meld of cards of same rank
                if(meld.get(0).getRank() == meld.get(1).getRank()) {
                    if(encoded) {
                        layoff.addAll(MyGinRummyUtil.getSameRank(MyGinRummyUtil.bitstringToCards(state.getHand()), meld.get(0)));
                    }
                    else layoff.addAll(MyGinRummyUtil.getSameRank(state.getHandCards(), meld.get(0)));
                }

                //Cards of same suit
                else {
                    if(encoded) {
                        layoff.addAll(MyGinRummyUtil.getSameSuit(MyGinRummyUtil.bitstringToCards(state.getHand()), meld.get(0), 1));
                        layoff.addAll(MyGinRummyUtil.getSameSuit(MyGinRummyUtil.bitstringToCards(state.getHand()), meld.get(meld.size() - 1), 1));
                    }
                    else {
                        layoff.addAll(MyGinRummyUtil.getSameSuit(state.getHandCards(), meld.get(0), 1));
                        layoff.addAll(MyGinRummyUtil.getSameSuit(state.getHandCards(), meld.get(meld.size() - 1), 1));
                    }
                }
            }

            /*
             * Deadwood cards will be laid off no matter what, so check potential layoffs in melds to see if
             * a better config is available.
             */

            ArrayList<Card> temp = null;
            ArrayList<ArrayList<Card>> bestMeldSet = bestMeldSets.get(0);
            int minDeadwood = deadwood;

            if(layoff.isEmpty()) return bestMeldSet;

            //Go through EVERY permutation of potential layoffs to find the one that leaves the best deadwood
            for(int i = 0; i < Math.pow(2, layoff.size()); i++) {
                String bString = Integer.toBinaryString(i);
                if(encoded) temp = MyGinRummyUtil.bitstringToCards(state.getHand());
                else temp = new ArrayList<>(state.getHandCards());

                for(int j = 0; j < bString.length(); j++) {
                    if(bString.charAt(bString.length() - 1 - j) == '1') {
                        temp.remove(layoff.get(j));
                    }
                }

                ArrayList<ArrayList<ArrayList<Card>>> meldSets = MyGinRummyUtil.cardsToBestMeldSets(temp);

                if(meldSets.isEmpty()) {
                    if(MyGinRummyUtil.getDeadwoodPoints(temp) < minDeadwood) {
                        minDeadwood = MyGinRummyUtil.getDeadwoodPoints(temp);
                        bestMeldSet = new ArrayList<>();
                    }
                }

                else {
                    if(MyGinRummyUtil.getDeadwoodPoints(meldSets.get(0), temp) < minDeadwood) {
                        minDeadwood = MyGinRummyUtil.getDeadwoodPoints(meldSets.get(0), temp);
                        bestMeldSet = meldSets.get(0);
                    }
                }
            }

            return bestMeldSet;

        }

    }

    @Override
    public void reportFinalMelds(int playerNum, ArrayList<ArrayList<Card>> melds) {
        // Melds ignored by simple player, but could affect which melds to make for complex player.
        if(playerNum != this.playerNum) {
            opponentKnocked = true;
            oppMelds = melds;
        }
    }

    @Override
    public void reportScores(int[] scores) {
        // Ignored by simple player, but could affect strategy of more complex player.
    }

    @Override
    public void reportLayoff(int playerNum, Card layoffCard, ArrayList<Card> opponentMeld) {
        // Ignored by simple player, but could affect strategy of more complex player.

    }

    @Override
    public void reportFinalHand(int playerNum, ArrayList<Card> hand) {
        // Ignored by simple player, but could affect strategy of more complex player.
    }

    /*
     * Class to record details about the state of the game
     */
    private class GameState {

        private long hand; //Our hand, as a bitstring
        private long seen; //Cards which have been seen, as a bitstring
        private long oppHand; //Cards which we know the opponent has, as a bitstring
        private long oppDiscard; //Cards which the opponent discarded, as a bitstring
        private int faceUp; //Face-up card id

        private ArrayList<Card> handCards;
        private ArrayList<Card> seenCards;
        private ArrayList<Card> oppHandCards;
        private ArrayList<Card> oppDiscardCards;
        private Card faceUpCard;

        private boolean encoded; //If cards are encoded as longs, true. For debugging.

        private int turn; //Current turn of the game
        private int num_remaining; //Number of remaining face-down cards

        GameState(ArrayList<Card> hand, boolean encoded) {
            this.encoded = encoded;

            if(this.encoded) {
                this.hand = MyGinRummyUtil.cardsToBitstring(hand);
                seen = this.hand;
                oppHand = 0L;
                oppDiscard = 0L;
            }

            else {
                this.handCards = new ArrayList<>(hand);
                this.seenCards = new ArrayList<>(handCards);
                this.oppHandCards = new ArrayList<>();
                this.oppDiscardCards = new ArrayList<>();
            }

            turn = 0;
            faceUp = -1;
            num_remaining = 32;
        }

        void clear() {
            if(encoded) hand = seen = oppHand = oppDiscard = 0L;
            else handCards = seenCards = oppHandCards = oppDiscardCards = null;
            faceUp = turn = num_remaining = 0;
        }

        int getTurn() {
            return turn;
        }

        void nextTurn() {
            turn++;
        }

        public long getHand() {
            return hand;
        }

        public ArrayList<Card> getHandCards() {
            return handCards;
        }

        public void setHand(long hand) {
            this.hand = hand;
        }

        public void setHand(ArrayList<Card> hand) {
            this.hand = MyGinRummyUtil.cardsToBitstring(hand);
        }

        public void setHandCards(ArrayList<Card> handCards) {
            this.handCards = new ArrayList<>(handCards);
        }

        public void addToHand(int card_id) {
            this.hand = MyGinRummyUtil.add(this.hand, card_id);
        }

        public void addToHandCards(Card c) {
            this.handCards.add(c);
        }

        public void addAllToHand(long cards) {
            this.hand = MyGinRummyUtil.addAll(this.hand, cards);
        }

        public void addAllToHandCards(ArrayList<Card> cards) {
            this.handCards.addAll(cards);
        }

        public void removeFromHand(int card_id) {
            this.hand = MyGinRummyUtil.remove(this.hand, card_id);
        }

        public void removeFromHandCards(Card c) {
            this.handCards.remove(c);
        }

        public void removeAllFromHand(long cards) {
            this.hand = MyGinRummyUtil.removeAll(this.hand, cards);
        }

        public void removeAllFromHandCards(ArrayList<Card> cards) {
            this.handCards.removeAll(cards);
        }

        public long getSeen() {
            return seen;
        }

        public ArrayList<Card> getSeenCards() {
            return seenCards;
        }

        public void addToSeen(int card_id) {
            this.seen = MyGinRummyUtil.add(this.seen, card_id);
        }

        public void addToSeenCards(Card c) {
            seenCards.add(c);
        }

        public void addAllToSeen(long cards) {
            this.seen = MyGinRummyUtil.addAll(this.seen, cards);
        }

        public void addAllToSeenCards(ArrayList<Card> cards) {
            seenCards.addAll(cards);
        }

        public void removeFromSeen(int card_id) {
            this.seen = MyGinRummyUtil.remove(this.seen, card_id);
        }

        public void removeFromSeenCards(Card card) {
            this.seenCards.remove(card);
        }

        public void removeAllFromSeen(long cards) {
            this.seen = MyGinRummyUtil.removeAll(this.seen, cards);
        }

        public void removeAllFromSeenCards(ArrayList<Card> cards) {
            this.seenCards.removeAll(cards);
        }

        public long getUnseen() {
            return ~seen;
        }

        public ArrayList<Card> getUnseenCards() {
            return MyGinRummyUtil.bitstringToCards(~MyGinRummyUtil.cardsToBitstring(seenCards));
        }

        public long getOppHand() {
            return oppHand;
        }

        public ArrayList<Card> getOppHandCards() {
            return oppHandCards;
        }

        public void setOppHand(long oppHand) {
            this.oppHand = oppHand;
        }

        public void setOppHand(ArrayList<Card> oppHand) {
            this.oppHand = MyGinRummyUtil.cardsToBitstring(oppHand);
        }

        public void setOppHandCards(ArrayList<Card> oppHandCards) {
            this.oppHandCards = new ArrayList<>(oppHandCards);
        }

        public void addToOppHand(int card_id) {
            this.oppHand = MyGinRummyUtil.add(this.oppHand, card_id);
        }

        public void addToOppHandCards(Card c) {
            this.oppHandCards.add(c);
        }

        public void addAllToOppHand(long cards) {
            this.oppHand = MyGinRummyUtil.addAll(this.oppHand, cards);
        }

        public void addAllToOppHandCards(ArrayList<Card> cards) {
            this.oppHandCards.addAll(cards);
        }

        public void removeFromOppHand(int card_id) {
            this.oppHand = MyGinRummyUtil.remove(this.oppHand, card_id);
        }

        public void removeFromOppHandCards(Card c) {
            this.oppHandCards.remove(c);
        }

        public void removeAllFromOppHand(long cards) {
            this.oppHand = MyGinRummyUtil.removeAll(this.oppHand, cards);
        }

        public void removeAllFromOppHandCards(ArrayList<Card> cards) {
            this.oppHandCards.removeAll(cards);
        }

        public long getOppDiscard() {
            return oppDiscard;
        }

        public ArrayList<Card> getOppDiscardCards() {
            return oppDiscardCards;
        }

        public void setOppDiscard(long oppDiscard) {
            this.oppDiscard = oppDiscard;
        }

        public void setOppDiscard(ArrayList<Card> oppDiscard) {
            this.oppDiscard = MyGinRummyUtil.cardsToBitstring(oppDiscard);
        }

        public void setOppDiscardCards(ArrayList<Card> oppDiscardCards) {
            this.oppHandCards = new ArrayList<>(oppDiscardCards);
        }

        public void addToOppDiscard(int card_id) {
            this.oppDiscard = MyGinRummyUtil.add(this.oppDiscard, card_id);
        }

        public void addToOppDiscardsCards(Card c) {
            this.oppDiscardCards.add(c);
        }

        public void addAllToOppDiscard(long cards) {
            this.oppDiscard = MyGinRummyUtil.addAll(this.oppDiscard, cards);
        }

        public void addAllToOppDiscardCards(ArrayList<Card> cards) {
            this.oppDiscardCards.addAll(cards);
        }

        public void removeFromOppDiscard(int card_id) {
            this.oppDiscard = MyGinRummyUtil.remove(this.oppDiscard, card_id);
        }

        public void removeFromOppDiscardCards(Card c) {
            this.oppDiscardCards.remove(c);
        }

        public void removeAllFromOppDiscard(long cards) {
            this.oppDiscard = MyGinRummyUtil.removeAll(this.oppDiscard, cards);
        }

        public void removeAllFromOppDiscardCards(ArrayList<Card> cards) {
            this.oppDiscardCards.removeAll(cards);
        }

        public int getFaceUp() {
            return faceUp;
        }

        public Card getFaceUpCard() {
            return faceUpCard;
        }

        public void setFaceUp(int faceUp) {
            this.faceUp = faceUp;
        }

        public void setFaceUp(Card c) {
            this.faceUp = c.getId();
        }

        public void setFaceUpCard(Card c) {
            this.faceUpCard = c;
        }

        public int getNum_remaining() {
            return num_remaining;
        }

        public void decreaseNumRemaining() {
            num_remaining--;
        }

        /**
         * @param card A card
         * @param numTurns Number of turns
         * @return the probability that the single will become a duple within numTurns turns
         */
        public double getSecondCardProbability(Card card, int numTurns) {
            return 0.0;
        }

        /**
         * @param cards A duple of cards
         * @param numTurns Number of turns
         * @return the probability that the duple will become a meld within numTurns turns
         *  For now, return 0 if all cards are buried or in the opponent's hand, and 1 otherwise
         */
        public double getThirdCardProbability(ArrayList<Card> cards, int numTurns) {

            //Cards which could complete the meld
            ArrayList<Card> adjCards = new ArrayList<>();

            //Third card must make set
            if(cards.get(0).rank == cards.get(1).rank) {
                adjCards.addAll(MyGinRummyUtil.getSameRank(new ArrayList<>(Arrays.asList(Card.allCards)), cards.get(0)));
            }

            //Third card must make run
            else {
                adjCards.addAll(MyGinRummyUtil.getSameSuit(new ArrayList<>(Arrays.asList(Card.allCards)), cards.get(0), 1));
                adjCards.addAll(MyGinRummyUtil.getSameSuit(new ArrayList<>(Arrays.asList(Card.allCards)), cards.get(1), 1));
            }

            //Don't consider cards already in duple
            adjCards.remove(cards.get(0));
            adjCards.remove(cards.get(1));

            ArrayList<Card> toRemove = new ArrayList<>();

            for(Card card : adjCards) {
                if(MyGinRummyUtil.contains(getBuried(), card.getId()) || MyGinRummyUtil.contains(oppHand, card.getId())) return 0.0;
            }

            return 0.0;
        }

        /**
         * @return a bitstring of all cards which are under the face-up card
         */
        public long getBuried() {
            long seen = this.seen;
            seen = MyGinRummyUtil.removeAll(seen, hand);
            seen = MyGinRummyUtil.removeAll(seen, oppHand);
            seen = MyGinRummyUtil.removeAll(seen, faceUp);

            return seen;
        }

        public ArrayList<Card> getBuriedCards() {
            return MyGinRummyUtil.bitstringToCards(getBuried());
        }

        /**
         * @return a list of all cards which have not yet been seen
         */
        public long getUnaccounted() {
            long unaccounted = MyGinRummyUtil.cardsToBitstring(new ArrayList<>(Arrays.asList(Card.allCards)));
            unaccounted = MyGinRummyUtil.removeAll(unaccounted, seen);
            return unaccounted;
        }

        public ArrayList<Card> getUnaccountedCards() {
            return MyGinRummyUtil.bitstringToCards(getUnaccounted());
        }

    }

    /*
     * Class containing helper methods
     */
    static class MyGinRummyUtil extends GinRummyUtil {

        /**
         * @param cards a hand of cards
         * @param exclude cards to exclude from the check
         * @return all cards not in melds
         */
        public static ArrayList<Card> getUnmelded(ArrayList<Card> cards, ArrayList<Card> exclude) {

            ArrayList<Card> temp = new ArrayList<>(cards);
            if(exclude != null) temp.removeAll(exclude);

            ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = cardsToBestMeldSets(temp);
            if(bestMeldSets.size() == 0) return temp;

            ArrayList<ArrayList<Card>> bestMeldSet = bestMeldSets.get(0);

            ArrayList<Card> unmelded = new ArrayList<>(temp);

            unmelded.removeIf(card -> {
                for(ArrayList<Card> meld : bestMeldSet)
                    if(meld.contains(card)) return true;
                return false;
            });

            return unmelded;

        }

        /**
         * @param cards a hand of cards
         * @param exclude cards to exclude from the check
         * @return all cards not in melds
         */
        public static long getUnmelded(long cards, long exclude) {
            return cardsToBitstring(getUnmelded(bitstringToCards(cards), bitstringToCards(exclude)));
        }

        /**
         * @param hand A hand of cards
         * @param card_id the id of a card
         * @return The hand with the card added
         */
        public static long add(long hand, int card_id) {
            return hand | 1L << card_id;
        }

        /**
         * @param hand A hand of cards
         * @param toAdd The cards to add
         * @return The hand with the cards added
         */
        public static long addAll(long hand, long toAdd) {
            return hand | toAdd;
        }

        /**
         * @param hand A hand of cards
         * @param card_id the id of a card
         * @return The hand with the card removed. Nothing is changed if hand doesn't contain card.
         */
        public static long remove(long hand, int card_id) {
            return hand & (hand ^ 1L << card_id);
        }

        /**
         * @param hand A hand of cards
         * @param toRemove The set of cards to remove from hand
         * @return The hand with the cards removed. Nothing is changed if hand doesn't contain card.
         */
        public static long removeAll(long hand, long toRemove) {
            return hand & (hand ^ toRemove);
        }

        /**
         * @param hand a hand of cards
         * @param card_id the reference card
         * @return true if hand contains card_id
         */
        public static boolean contains(long hand, int card_id) {
            return (hand & 1L << card_id) != 0;
        }

        /**
         * @param cards a hand of cards
         * @param exclude cards to exclude from the check
         * @return all cards who are in one of the melds in the best meld set for cards
         */
        public static ArrayList<Card> getMelded(ArrayList<Card> cards, ArrayList<Card> exclude) {
            ArrayList<Card> melded = new ArrayList<>(cards);
            melded.removeAll(getUnmelded(melded, exclude));
            return melded;
        }

        /**
         * @param cards a hand of cards
         * @param exclude cards to exclude from the check
         * @return all cards who are in one of the melds in the best meld set for cards
         */
        public static long getMelded(long cards, long exclude) {
            cards = removeAll(cards, exclude);
            return cards & (cards ^ getUnmelded(cards, exclude));
        }

        /**
         * @param cards a hand of cards
         * @param exclude cards to exclude from the check
         * @param state the current state of the game
         * @return all cards in hand that cannot be made into any melds even after drawing one card.
         */
        public static ArrayList<Card> getSingles(ArrayList<Card> cards, ArrayList<Card> exclude, GameState state) {

            ArrayList<Card> temp = new ArrayList<>(cards);
            if(exclude != null) temp.removeAll(exclude);
            if(temp.isEmpty()) return temp;

            ArrayList<Card> singles = getUnmelded(temp, exclude);
            ArrayList<Card> toRemove = new ArrayList<>();

            for(Card card : singles) {

                //If there exists another card of the same rank in the hand and unseen, it can be melded within 1 draw
                if(containsRank(temp, card) && containsRank(state.getUnaccounted(), card.getId())) {
                    toRemove.add(card);
                    continue;
                }

                /*
                 * For each adjacent card of the same suit to c, if there is an adjacent card
                 * of the same suit to that card which is unseen, it can be melded within 1 draw
                 */

                for(Card c : getSameSuit(temp, card, 1)) {
                    if(containsSuit(state.getUnaccounted(), card.getId(), 1)) {
                        toRemove.add(card);
                        break;
                    }
                }
            }

            singles.removeAll(toRemove);
            return singles;

        }

        /**
         * @param cards a hand of cards
         * @param state the current state of the game
         * @param exclude cards to exclude from the check
         * @return all cards in hand that cannot be made into any melds even after drawing one card.
         */
        public static long getSingles(long cards, long exclude, GameState state) {
            return cardsToBitstring(getSingles(bitstringToCards(cards), bitstringToCards(exclude), state));
        }

        /**
         * @param cards a hand of cards
         * @param exclude cards to exclude from the check
         * @param state the current state of the game
         * @return all cards in hand that cannot be made into any melds even after drawing two cards.
         */
        public static ArrayList<Card> getIsolatedSingles(ArrayList<Card> cards, ArrayList<Card> exclude, GameState state) {
            ArrayList<Card> singles = getSingles(cards, exclude, state); //All cards which cannot be made into a meld after drawing one card
            ArrayList<Card> unaccounted = bitstringToCards(state.getUnaccounted()); //All cards which have not yet been seen
            unaccounted.removeAll(bitstringToCards(state.getSeen()));

            ArrayList<Card> toRemove = new ArrayList<>();

            for(Card card : singles) {

                //Get all cards of the same rank as the card
                ArrayList<Card> adjacent = new ArrayList<>(getSameRank(unaccounted, card));

                //If at least 2 unseen cards of the same rank as card, it can be melded after drawing 2 cards
                if(adjacent.size() > 1) {
                    toRemove.add(card);
                    break;
                }

                //All cards of the same suit as the card whose rank is within 1 (should only be 2 cards max)
                adjacent = new ArrayList<>(getSameSuit(unaccounted, card, 1));

                //If no adjacent cards, then it cannot be melded even after drawing 2 cards. Check next card.
                if(adjacent.isEmpty()) continue;

                //For each adjacent card, see if the next card also exists. If any do, then the card can be melded after drawing 2 cards.
                for(Card c : adjacent) {
                    ArrayList<Card> c_adjacent = new ArrayList<>(getSameSuit(unaccounted, c, 1)); //Unaccounted cards adjacent to c. Should never contain card, so should contain 1 card max.
                    if(!c_adjacent.isEmpty()) {
                        toRemove.add(card);
                        break;
                    }

                }

            }

            singles.removeAll(toRemove);

            return singles;

        }

        /**
         * @param cards a hand of cards
         * @param exclude cards to exclude from the check
         * @param state the current state of the game
         * @return all cards in hand that cannot be made into any melds even after drawing two cards.
         */
        public static long getIsolatedSingles(long cards, long exclude, GameState state) {
            return cardsToBitstring(getIsolatedSingles(bitstringToCards(cards), bitstringToCards(exclude), state));
        }

        /**
         * @param cards a hand of cards
         * @param card the reference card
         * @return A list of all duples containing card
         */
        public static ArrayList<Set<Card>> getDuples(ArrayList<Card> cards, Card card) {

            ArrayList<Set<Card>> duples = new ArrayList<>();
            ArrayList<Card> sameRank = getSameRank(cards, card);
            ArrayList<Card> sameSuit = getSameSuit(cards, card, 2);

            for(Card c : sameRank) duples.add(new HashSet<>(Arrays.asList(card, c)));
            for(Card c : sameSuit) duples.add(new HashSet<>(Arrays.asList(card, c)));

            return duples;
        }

        /**
         * @param cards a hand of cards
         * @param card the reference card
         * @return A list of all cards in duples with the reference card
         */
        public static long getDuples(long cards, int card) {

            long duples = cards;
            long sameRank = getSameRank(cards, card);
            long sameSuit = getSameSuit(cards, card, 2);

            duples = addAll(duples, addAll(sameRank, sameSuit));
            return duples;
        }

        /**
         * @param hand A hand of cards
         * @return A list of all the card ids in the hand
         */
        public static ArrayList<Integer> bitstringToIDs(long hand) {
            ArrayList<Integer> cards = new ArrayList<>();
            for (int i = 0; i < Card.NUM_CARDS; i++) {
                if (hand % 2 == 1)
                    cards.add(Card.allCards[i].getId());
                hand /= 2;
            }
            return cards;
        }

        /**
         * @param ids A list of ids in a hand
         * @return The list of ids as a long
         */
        public static long idsToBitstring(ArrayList<Integer> ids) {
            long bitstring = 0L;
            for (int i : ids)
                bitstring = add(bitstring, i);
            return bitstring;
        }

        /**
         * @param c The reference card
         * @param s The current state of the game
         * @return true if the opponent can make a meld from c
         */
        public static boolean canOpponentMeld(Card c, GameState s) {

            //Cards of same rank as c
            ArrayList<Card> sameRank = getSameRank(new ArrayList<>(Arrays.asList(Card.allCards)), c);

            //Cards of same suit as c which are adjacent
            ArrayList<Card> sameSuitAdj = getSameSuit(new ArrayList<>(Arrays.asList(Card.allCards)), c, 1);

            //Cards of same suit as c who's rank is 2 away
            ArrayList<Card> sameSuit = getSameSuit(new ArrayList<>(Arrays.asList(Card.allCards)), c, 2);
            sameSuit.removeAll(sameSuitAdj);

            //Filter cards from collections so they only contain cards in the deck or in the opponent's hand
            sameRank.removeIf(card -> !contains(s.getHand(), card.getId()) && !contains(s.getBuried(), card.getId()));
            sameSuitAdj.removeIf(card -> !contains(s.getHand(), card.getId()) && !contains(s.getBuried(), card.getId()));
            sameSuit.removeIf(card -> !contains(s.getHand(), card.getId()) && !contains(s.getBuried(), card.getId()));

            if(sameRank.size() >= 2) return true;

            else if(sameSuitAdj.isEmpty() || sameSuit.isEmpty()) return false;

            //Looking at all the cards which are 2 away, if there is no card between it and c, remove it
            sameSuit.removeIf(card -> {
                for(Card card1 : sameSuitAdj) {
                    if(card.getRank() > c.getRank()) return card.getRank() - 1 != card1.getRank();
                    else return card.getRank() + 1 != card1.getRank();
                }
                return false;
            });

            return !sameSuit.isEmpty();
        }

        /**
         * @param hand A hand of cards
         * @param c The card we're considering
         * @return The change in deadwood from inserting c and discarding the worst card. If return value is negative,
         *  drawing c increases the overall deadwood of our hand.
         */
        public static int makesNewMeld(ArrayList<Card> hand, Card c) {
            int minDeadwood = Integer.MAX_VALUE;
            ArrayList<Card> newCards = new ArrayList<>(hand);
            newCards.add(c);

            //Find all cards whose removal would reduce the hand's deadwood by the max amount
            for (Card card : newCards) {
                ArrayList<Card> remainingCards = new ArrayList<>(newCards);
                remainingCards.remove(card);

                ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = cardsToBestMeldSets(remainingCards);

                int deadwood = bestMeldSets.isEmpty() ? getDeadwoodPoints(remainingCards) : getDeadwoodPoints(bestMeldSets.get(0), remainingCards);
                if (deadwood < minDeadwood) minDeadwood = deadwood;
            }

            return cardsToBestMeldSets(hand).isEmpty() ? getDeadwoodPoints(hand) - minDeadwood : getDeadwoodPoints(cardsToBestMeldSets(hand).get(0), getUnmelded(hand, null)) - minDeadwood;
        }

        /**
         * @param hand A hand of cards
         * @param c_id The id of the card we're considering
         * @return The change in deadwood from inserting c and discarding the worst card. If return value is negative,
         *  drawing c increases the overall deadwood of our hand.
         */
        public static int makesNewMeld(long hand, int c_id) {
            return makesNewMeld(bitstringToCards(hand), Card.getCard(c_id));
        }

        /**
         * @param hand A hand of cards
         * @param c The reference card
         * @return true if there exists at least one card of the same rank as c
         */
        public static boolean containsRank(ArrayList<Card> hand, Card c) {
            for(Card card : hand) {
                if(card.equals(c)) continue; //Don't count card c
                if(card.getRank() == c.getRank()) return true;
            }

            return false;
        }

        /**
         * @param hand A hand of cards
         * @param c The reference card's id
         * @return true if there exists at least one card of the same rank as c
         */
        public static boolean containsRank(long hand, int c) {
            return containsRank(bitstringToCards(hand), Card.getCard(c));
        }

        /**
         * @param hand A hand of cards
         * @param c The reference card
         * @return a list of all cards of the same rank as c
         */
        public static ArrayList<Card> getSameRank(ArrayList<Card> hand, Card c) {
            if(c == null) return new ArrayList<>();
            return (ArrayList<Card>) hand
                    .stream()
                    .filter(card -> (card.getRank() == c.getRank() && !card.equals(c)))
                    .collect(Collectors.toList());
        }

        /**
         * @param hand A hand of cards
         * @param c The reference card's id
         * @return a list of all cards of the same rank as c
         */
        public static long getSameRank(long hand, int c) {
            return cardsToBitstring(getSameRank(bitstringToCards(hand), Card.getCard(c)));
        }

        /**
         * @param hand A hand of cards
         * @param c The reference card
         * @param diff the difference between the cards' ranks
         * @return true if there exists at least one card of the same suit as c, given that its rank is within diff of c's rank
         */
        public static boolean containsSuit(ArrayList<Card> hand, Card c, int diff) {
            for(Card card : hand) {
                if(card.equals(c)) continue; //Don't count card c
                if(card.getSuit() == c.getSuit() && Math.abs(c.getRank() - card.getRank()) <= diff) return true;
            }

            return false;
        }

        /**
         * @param hand A hand of cards
         * @param c The reference card's id
         * @param diff the difference between the cards' ranks
         * @return true if there exists at least one card of the same suit as c, given that its rank is within diff of c's rank
         */
        public static boolean containsSuit(long hand, int c, int diff) {
            return containsSuit(bitstringToCards(hand), Card.getCard(c), diff);
        }

        /**
         * @param hand A hand of cards
         * @param c The reference card
         * @param diff the difference between the cards' ranks
         * @return a list of all cards of same suit as c, given that its rank is within diff of c's rank
         */
        public static ArrayList<Card> getSameSuit(ArrayList<Card> hand, Card c, int diff) {
            if(c == null) return new ArrayList<>();
            return (ArrayList<Card>) hand
                    .stream()
                    .filter(card -> (card.getSuit() == c.getSuit() && Math.abs(c.getRank() - card.getRank()) <= diff && !card.equals(c)))
                    .collect(Collectors.toList());
        }

        /**
         * @param hand A hand of cards
         * @param c The reference card's id
         * @param diff the difference between the cards' ranks
         * @return a list of all cards of same suit as c, given that its rank is within diff of c's rank
         */
        public static long getSameSuit(long hand, int c, int diff) {
            return cardsToBitstring(getSameSuit(bitstringToCards(hand), Card.getCard(c), diff));
        }

        /**
         * @param cards A hand of cards
         * @return the card with the highest deadwood
         */
        public static Card getHighestDeadwood(ArrayList<Card> cards) {
            if(cards.size() == 0) return null;
            return cards
                    .stream()
                    .reduce(cards.get(0), (max, c) -> getDeadwoodPoints(c) > getDeadwoodPoints(max) ? c : max);
        }

        /**
         * @param cards A hand of cards
         * @return the id of the card with the highest deadwood
         */
        public static int getHighestDeadwood(long cards) {
            try {
                return getHighestDeadwood(bitstringToCards(cards)).getId();
            } catch (NullPointerException e) {
                return -1;
            }

        }

        /**
         * @param hand A hand of cards
         * @param drawnCard The card drawn - null if a card wasn't just picked up
         * @param face_up The card that was face-up before the draw
         * @param range The range of deadwoods still recorded. Range of 0 only gives those who would lower deadwood the most.
         * @return A list of all cards whose removal would lower the deadwood within range of the most
         */
        public static ArrayList<Card> findHighestDiscards (ArrayList<Card> hand, Card drawnCard, Card face_up, int range) {

            ArrayList<Card> candidateCards = new ArrayList<>(hand);
            HashMap<Integer, ArrayList<Card>> candidateLog = new HashMap<>();

            int minDeadwood = Integer.MAX_VALUE;

            //Find all cards whose removal would reduce the hand's deadwood by the max amount
            for (Card card : hand) {
                // Cannot draw and discard face up card.
                if (drawnCard != null && (card == drawnCard && drawnCard == face_up))
                    continue;

                ArrayList<Card> remainingCards = new ArrayList<>(hand);
                remainingCards.remove(card);
                ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = cardsToBestMeldSets(remainingCards);
                int deadwood = bestMeldSets.isEmpty() ? getDeadwoodPoints(remainingCards) : getDeadwoodPoints(bestMeldSets.get(0), remainingCards);
                if (deadwood <= minDeadwood) {
                    if (deadwood < minDeadwood) {

                        candidateLog.put(minDeadwood, new ArrayList<>(candidateCards));

                        candidateCards.clear();
                        minDeadwood = deadwood;

                    }
                    candidateCards.add(card);
                }
            }

            for(HashMap.Entry<Integer, ArrayList<Card>> entry : candidateLog.entrySet())
                if(entry.getKey() - minDeadwood <= range) candidateCards.addAll(entry.getValue());

            return candidateCards;
        }

        /**
         * @param hand A hand of cards
         * @param drawnCard The card drawn's id - -1 if a card wasn't just picked up
         * @param face_up The if of the card that was face-up before the draw
         * @param range The range of deadwoods still recorded
         * @return A list of all cards whose removal would lower the deadwood within range of the most
         */
        public static long findHighestDiscards (long hand, int drawnCard, int face_up, int range) {
            return cardsToBitstring(findHighestDiscards(bitstringToCards(hand),
                    drawnCard == -1 ? null : Card.getCard(drawnCard),
                    face_up == -1 ? null : Card.getCard(face_up), range));
        }

        /**
         * @param state The current state of the game
         * @return The expected deadwood for drawing one more card
         */
        public static double expectedDeadwoodForNextDraw(GameState state) {
            ArrayList<Card> unaccounted = bitstringToCards(state.getUnaccounted()); //Cards which have not been seen
            double sum = 0;

            for(Card card : unaccounted) sum += 1d/unaccounted.size() * makesNewMeld(bitstringToCards(state.getHand()), card);

            return sum;
        }

        /**
         * @param hand A hand of cards
         * @return The number of cards in the hand
         */
        public static int size(long hand) {
            String s = Long.toBinaryString(hand);
            int count = 0;
            for(int i = 0; i < s.length(); i++) {
                if(s.charAt(i) == '1') count++;
            }
            return count;
        }

        /**
         * Print out a hand of cards, with melds separated from the rest
         * @param hand A hand of cards
         */
        public static void printHandWithMelds(ArrayList<Card> hand) {
            ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(hand);
            ArrayList<ArrayList<Card>> bestMeldSet;
            ArrayList<Card> unmelded = MyGinRummyUtil.getUnmelded(hand, null);
            unmelded.sort((c1, c2) -> {
                String c1str;
                String c2str;
                char first;

                switch (c1.toString().charAt(0)) {
                    case '2': {
                        first = 'B';
                        break;
                    }
                    case '3': {
                        first = 'C';
                        break;
                    }
                    case '4': {
                        first = 'D';
                        break;
                    }
                    case '5': {
                        first = 'E';
                        break;
                    }
                    case '6': {
                        first = 'F';
                        break;
                    }
                    case '7': {
                        first = 'G';
                        break;
                    }
                    case '8': {
                        first = 'H';
                        break;
                    }
                    case '9': {
                        first = 'I';
                        break;
                    }
                    case 'T': {
                        first = 'J';
                        break;
                    }
                    case 'J': {
                        first = 'K';
                        break;
                    }
                    case 'Q': {
                        first = 'L';
                        break;
                    }
                    case 'K': {
                        first = 'M';
                        break;
                    }
                    default: {
                        first = 'A';
                    }

                }
                c1str = first + c1.toString().substring(1);

                switch (c2.toString().charAt(0)) {
                    case '2': {
                        first = 'B';
                        break;
                    }
                    case '3': {
                        first = 'C';
                        break;
                    }
                    case '4': {
                        first = 'D';
                        break;
                    }
                    case '5': {
                        first = 'E';
                        break;
                    }
                    case '6': {
                        first = 'F';
                        break;
                    }
                    case '7': {
                        first = 'G';
                        break;
                    }
                    case '8': {
                        first = 'H';
                        break;
                    }
                    case '9': {
                        first = 'I';
                        break;
                    }
                    case 'T': {
                        first = 'J';
                        break;
                    }
                    case 'J': {
                        first = 'K';
                        break;
                    }
                    case 'Q': {
                        first = 'L';
                        break;
                    }
                    case 'K': {
                        first = 'M';
                        break;
                    }
                    default: {
                        first = 'A';
                    }

                }
                c2str = first + c2.toString().substring(1);

                return c1str.compareTo(c2str);
            });
            unmelded.forEach(c -> System.out.print(c.toString() + " "));
            System.out.println();

            if(!bestMeldSets.isEmpty()) {
                bestMeldSet = bestMeldSets.get(0);

                for(ArrayList<Card> meld : bestMeldSet) {
                    meld.sort((c1, c2) -> {
                        String c1str;
                        String c2str;
                        char first;

                        switch (c1.toString().charAt(0)) {
                            case '2': {
                                first = 'B';
                                break;
                            }
                            case '3': {
                                first = 'C';
                                break;
                            }
                            case '4': {
                                first = 'D';
                                break;
                            }
                            case '5': {
                                first = 'E';
                                break;
                            }
                            case '6': {
                                first = 'F';
                                break;
                            }
                            case '7': {
                                first = 'G';
                                break;
                            }
                            case '8': {
                                first = 'H';
                                break;
                            }
                            case '9': {
                                first = 'I';
                                break;
                            }
                            case 'T': {
                                first = 'J';
                                break;
                            }
                            case 'J': {
                                first = 'K';
                                break;
                            }
                            case 'Q': {
                                first = 'L';
                                break;
                            }
                            case 'K': {
                                first = 'M';
                                break;
                            }
                            default: {
                                first = 'A';
                            }

                        }
                        c1str = first + c1.toString().substring(1);

                        switch (c2.toString().charAt(0)) {
                            case '2': {
                                first = 'B';
                                break;
                            }
                            case '3': {
                                first = 'C';
                                break;
                            }
                            case '4': {
                                first = 'D';
                                break;
                            }
                            case '5': {
                                first = 'E';
                                break;
                            }
                            case '6': {
                                first = 'F';
                                break;
                            }
                            case '7': {
                                first = 'G';
                                break;
                            }
                            case '8': {
                                first = 'H';
                                break;
                            }
                            case '9': {
                                first = 'I';
                                break;
                            }
                            case 'T': {
                                first = 'J';
                                break;
                            }
                            case 'J': {
                                first = 'K';
                                break;
                            }
                            case 'Q': {
                                first = 'L';
                                break;
                            }
                            case 'K': {
                                first = 'M';
                                break;
                            }
                            default: {
                                first = 'A';
                            }

                        }
                        c2str = first + c2.toString().substring(1);

                        return c1str.compareTo(c2str);
                    });
                    meld.forEach(c -> System.out.print(c.toString() + " "));
                    System.out.println();
                }

                System.out.println("Deadwood: " + MyGinRummyUtil.getDeadwoodPoints(bestMeldSet, hand));
            }

            else
                System.out.println("Deadwood: " + MyGinRummyUtil.getDeadwoodPoints(hand));
        }

        /**
         * Print out a hand of cards, with melds separated from the rest
         * @param hand A hand of cards
         */
        public static void printHandWithMelds(long hand) {
            printHandWithMelds(bitstringToCards(hand));
        }

        /**
         * Print out a hand of cards
         * @param hand A hand of cards
         */
        public static void printHand(ArrayList<Card> hand) {
            hand.sort((c1, c2) -> {
                String c1str;
                String c2str;
                char first;

                switch (c1.toString().charAt(0)) {
                    case '2': {
                        first = 'B';
                        break;
                    }
                    case '3': {
                        first = 'C';
                        break;
                    }
                    case '4': {
                        first = 'D';
                        break;
                    }
                    case '5': {
                        first = 'E';
                        break;
                    }
                    case '6': {
                        first = 'F';
                        break;
                    }
                    case '7': {
                        first = 'G';
                        break;
                    }
                    case '8': {
                        first = 'H';
                        break;
                    }
                    case '9': {
                        first = 'I';
                        break;
                    }
                    case 'T': {
                        first = 'J';
                        break;
                    }
                    case 'J': {
                        first = 'K';
                        break;
                    }
                    case 'Q': {
                        first = 'L';
                        break;
                    }
                    case 'K': {
                        first = 'M';
                        break;
                    }
                    default: {
                        first = 'A';
                    }

                }
                c1str = first + c1.toString().substring(1);

                switch (c2.toString().charAt(0)) {
                    case '2': {
                        first = 'B';
                        break;
                    }
                    case '3': {
                        first = 'C';
                        break;
                    }
                    case '4': {
                        first = 'D';
                        break;
                    }
                    case '5': {
                        first = 'E';
                        break;
                    }
                    case '6': {
                        first = 'F';
                        break;
                    }
                    case '7': {
                        first = 'G';
                        break;
                    }
                    case '8': {
                        first = 'H';
                        break;
                    }
                    case '9': {
                        first = 'I';
                        break;
                    }
                    case 'T': {
                        first = 'J';
                        break;
                    }
                    case 'J': {
                        first = 'K';
                        break;
                    }
                    case 'Q': {
                        first = 'L';
                        break;
                    }
                    case 'K': {
                        first = 'M';
                        break;
                    }
                    default: {
                        first = 'A';
                    }

                }
                c2str = first + c2.toString().substring(1);

                return c1str.compareTo(c2str);
            });

            hand.forEach(c -> System.out.print(c.toString() + " "));
            System.out.println();
        }

        /**
         * Print out a hand of cards
         * @param hand A hand of cards
         */
        public static void printHand(long hand) {
            printHand(bitstringToCards(hand));
        }

        /**
         * @param strategy A strategy
         * @return The encoded strategy
         */
        static String encoded(int[] strategy) {
            String encoded = "";
            for(int i = 0; i < strategy.length; i++) {
                encoded += Integer.toHexString(strategy[i]);
            }

            return encoded;
        }

        /**
         * @param hex An encoded strategy
         * @return The decoded strategy
         */
        static int[] decoded(String hex) {
            int[] strategy = new int[6];
            for(int i = 0; i < hex.length(); i++) {
                strategy[i] = (int) Long.parseLong(hex.substring(i, i + 1), 16);
            }

            return strategy;
        }

        /**
         * @param hex An encoded strategy
         * @return The decoded strategy
         */
        static int[] decoded(int hex) {
            String hexString = Integer.toHexString(hex);
            int[] strategy = new int[6];
            for(int i = 0; i < hexString.length(); i++) {
                strategy[i] = (int) Long.parseLong(hexString.substring(i, i + 1), 16);
            }

            return strategy;
        }
    }

    /**
     * Class to hold parameters for player strategy
     * TODO: Implement the regret matching algorithm for sequential games.
     */
    private class GeneralStrategy {

        /**
         * week_5.Player should not knock if their deadwood is above this threshold.
         */
        private int maxKnockDeadwood;

        /**
         * Max deadwood a card can contribute to a hand while not being able to be melded within 2 turns,
         * in order for us to avoid discarding it.
         */
        private int maxIsolatedSingleDeadwood;

        /**
         * After this turn has passed, we will no longer go out of our way to keep cards "protected" by maxIsolatedSingleDeadwood.
         */
        private int minIsolatedSingleDiscardTurn;

        /**
         * Max deadwood a card can contribute to a hand while not being able to be melded within 1 turn,
         * in order for us to avoid discarding it.
         */
        private int maxSingleDeadwood;

        /**
         * After this turn has passed, we will no longer go out of our way to keep cards "protected" by maxSingleDeadwood.
         */
        private int minSingleDiscardTurn;

        /**
         * If the probability of getting a gin within x turns is >= minWaitForGinProbability, we should wait to try to get a gin.
         * TODO: Figure out if this is even useful, and if it is, implement a way to calculate the probability of getting a gin within x turns.
         *  Maybe generalize to find the probability that total deadwood will become <= some y in x turns.
         */
        private double minWaitForGinProbability;

        /**
         * If our total deadwood is below this value, we should wait and try to undercut.
         * TODO: Determine whether this would affect our strategy differently than minWaitForGinProbability. They might mostly
         *  overlap, in which case I only need to consider one of the 2.
         */
        private int minUndercutDeadwood;

        /**
         * Min turn to try to layoff cards
         */
        private int minLayoffTurn;

        /**
         * Minimum change in deadwood the face-up card can contribute in order for us to consider drawing it.
         */
        private int minPickupDifference;


        /**
         * Default constructor
         */
        GeneralStrategy() {
            maxKnockDeadwood = 10;

            maxIsolatedSingleDeadwood = 10;
            minIsolatedSingleDiscardTurn = 0;

            maxSingleDeadwood = 10;
            minSingleDiscardTurn = 0;

            minWaitForGinProbability = 0;

            minUndercutDeadwood = 10;

            minLayoffTurn = 0;
            minPickupDifference = 0;

        }

        /**
         * Constructor
         */
        GeneralStrategy(int maxKnockDeadwood, int maxIsolatedSingleDeadwood, int minIsolatedSingleDiscardTurn, int maxSingleDeadwood, int minSingleDiscardTurn,
                        int minPickupDifference) {

            this.maxKnockDeadwood = maxKnockDeadwood <= 10 && maxKnockDeadwood >= 0 ? maxKnockDeadwood : 10;

            this.maxIsolatedSingleDeadwood = maxIsolatedSingleDeadwood <= 10 && maxIsolatedSingleDeadwood > 0 ? maxIsolatedSingleDeadwood : 10;
            this.minIsolatedSingleDiscardTurn = Math.max(minIsolatedSingleDiscardTurn, 0);

            this.maxSingleDeadwood = maxSingleDeadwood <= 10 && maxSingleDeadwood > 0 ? maxSingleDeadwood : 10;
            this.minSingleDiscardTurn = Math.max(minSingleDiscardTurn, 0);

            //this.minWaitForGinProbability = minWaitForGinProbability <= 1 && minWaitForGinProbability >= 0 ? minWaitForGinProbability : 0.0;

            //this.minUndercutDeadwood = minUndercutDeadwood <= 10 && minUndercutDeadwood >= 0 ? minUndercutDeadwood : 10;

            //this.minLayoffTurn = Math.max(minLayoffTurn, 0);

            this.minPickupDifference = minPickupDifference <= 10 && minPickupDifference >= 0 ? minPickupDifference : 0;

        }

        GeneralStrategy(int[] strategy) {

            this.maxKnockDeadwood = strategy[0] <= 10 && strategy[0] >= 0 ? strategy[0] : 10;

            this.maxIsolatedSingleDeadwood = strategy[1] <= 10 && strategy[1] > 0 ? strategy[1] : 10;
            this.minIsolatedSingleDiscardTurn = Math.max(strategy[2], 0);

            this.maxSingleDeadwood = strategy[3] <= 10 && strategy[3] > 0 ? strategy[3] : 10;
            this.minSingleDiscardTurn = Math.max(strategy[4], 0);

            //this.minWaitForGinProbability = minWaitForGinProbability <= 1 && minWaitForGinProbability >= 0 ? minWaitForGinProbability : 0.0;

            //this.minUndercutDeadwood = minUndercutDeadwood <= 10 && minUndercutDeadwood >= 0 ? minUndercutDeadwood : 10;

            //this.minLayoffTurn = Math.max(minLayoffTurn, 0);

            this.minPickupDifference = strategy[5] <= 10 && strategy[5] >= 0 ? strategy[5] : 0;

        }

        int getMaxKnockDeadwood() {
            return maxKnockDeadwood;
        }

        int getMaxIsolatedSingleDeadwood() {
            return maxIsolatedSingleDeadwood;
        }

        int getMinIsolatedSingleDiscardTurn() {
            return minIsolatedSingleDiscardTurn;
        }

        int getMaxSingleDeadwood() {
            return maxSingleDeadwood;
        }

        int getMinSingleDiscardTurn() {
            return minSingleDiscardTurn;
        }

        double getMinWaitForGinProbability() {
            return minWaitForGinProbability;
        }

        int getMinUndercutDeadwood() {
            return minUndercutDeadwood;
        }

        int getMinLayoffTurn() {
            return minLayoffTurn;
        }

        int getMinPickupDifference() {
            return minPickupDifference;
        }

        public void setMaxKnockDeadwood(int maxKnockDeadwood) {
            this.maxKnockDeadwood = maxKnockDeadwood;
        }

        public void setMaxIsolatedSingleDeadwood(int maxIsolatedSingleDeadwood) {
            this.maxIsolatedSingleDeadwood = maxIsolatedSingleDeadwood;
        }

        public void setMinIsolatedSingleDiscardTurn(int minIsolatedSingleDiscardTurn) {
            this.minIsolatedSingleDiscardTurn = minIsolatedSingleDiscardTurn;
        }

        public void setMaxSingleDeadwood(int maxSingleDeadwood) {
            this.maxSingleDeadwood = maxSingleDeadwood;
        }

        public void setMinSingleDiscardTurn(int minSingleDiscardTurn) {
            this.minSingleDiscardTurn = minSingleDiscardTurn;
        }

        public void setMinWaitForGinProbability(double minWaitForGinProbability) {
            this.minWaitForGinProbability = minWaitForGinProbability;
        }

        public void setMinUndercutDeadwood(int minUndercutDeadwood) {
            this.minUndercutDeadwood = minUndercutDeadwood;
        }

        public void setMinLayoffTurn(int minLayoffTurn) {
            this.minLayoffTurn = minLayoffTurn;
        }

        public void setMinPickupDifference(int minPickupDifference) {
            this.minPickupDifference = minPickupDifference;
        }
    }

}

/*
 * WEEK 1 QUESTIONS:
 * 1) There are two decision points during a turn. The first is at the beginning of the turn, when it is time to draw a card. The second is at the end of the turn, when it is time to discard.
 *
 * 2) During the first decision point, the options are to either draw the face up card, or to decline the face up card, and (excluding the first turn), draw the face down card.
 *    During the second decision point, the player must choose which card to discard, and (if they have less than 10 deadwood points) whether to knock.
 *
 * 3) When choosing which card to draw (face up or face down), it's important to consider the face up card's utility, as we want to draw cards with higher utility. Some factors which could affect utility are:
 *      -The value of the card. Lower cards (like A, 2) increase deadwood by less, and so are better to have than higher cards which may already be in the hand (like 10, J).
 *      -Whether the card fits into a meld. If it does, then it doesn't contribute any deadwood points, and by forming the meld and/or discarding a card, can only reduce deadwood.
 *       This makes the card more useful than cards in the hand which don't fit into a meld.
 *          -We also need to consider whether the melds we are making are the best possible. (A run of {AH, AS, AD} and loose cards {2H, 3H, 2S, 3S, ...} is not as efficient as {AH, 2H, 3H}, {AS, 2S, 3S}, {AD, ...}.)
 *      -Whether the card has the potential to form a meld soon. It's less useful than the above, but still has the potential to lower deadwood.
 *          -Whether this is worth doing depends on the point in the game. Earlier in the game, it may be beneficial to do this with higher cards (say you have a K,
 *           and have the option to pick up another K) because the risk of getting knocked is low, but later on, we don't want to make "long" term investments, because
 *           we don't want to risk getting knocked while our deadwood is high. That said, if we are losing by a large margin, we may want to take more risks, because there's not much to lose. If we
 *           we are close in scores, we should play it safe. If we are winning by a large margin, we may want to take more risks, because messing up a little won't hurt too much.
 *          -Here, it is important to consider not only how close we are to a meld (like if we have 2 K's and need a 3rd), but how likely it is to pull another card which fits into the meld.
 *           If we saw that many of the cards which would coordinate well with the face up have been discarded (and possibly picked up by the other player), it isn't worth trying to use it.
 *           That said, if we haven't seen many cards which would coordinate well with the face up, we know that it is possible that there are useful cards soon to come. We can't predict the latter
 *           with too much certainty, because we don't know which face down cards the opponent has taken.
 *      -If we think the opponent is getting close to knocking, we might also consider whether we think the card could be inserted into one of their melds, as (assuming they don't get a gin)
 *       we could eliminate that from our final deadwood, and either lower the amount of points they win, or even undercut them.
 *
 *    It is also important to consider each card's utility when choosing what to discard (and the fact that you can't discard the face-up card, if you just drew it).
 *    Intuitively, the factors which would determine a card's utility should be the same as above. The main difference is that we want to draw high utility cards and discard low utility cards.
 *
 *    When deciding whether to knock, there's a few things to consider:
 *      -We can only knock if we have less than or equal to 10 deadwood points
 *      -If we have gin, we should knock immediately to prevent the opponent from further reducing their deadwood.
 *      -If our deadwood is really low (1, 2), we might want to hold out to try to undercut the opponent, as it is unlikely that their deadwood is lower than ours.
 *      -If our deadwood is higher (9, 10), we might want to hold out to try to reduce our deadwood further. We should only do this if not many turns have passed, though, as if we wait too
 *       long, we give the opponent time to get their deadwood lower than ours, causing them to knock, or for us to knock and get undercut.
 *      -If we also consider the card's we've seen enter and exit the opponent's hand, we can try to approximate their deadwood and decide when to knock. If we think they have very low deadwood,
 *       we should wait for them to knock so they don't undercut us. If we think they have higher deadwood (maybe even greater than 10), we can take our time and try to reduce our's a little more
 *       and then knock them. If we think they have close to us in deadwood, we should knock sooner rather than later to prevent them from lowering it further.
 *
 * 4) For drawing a card: willDrawFaceUpCard(week_5.Card card)
 *    For discarding a card: getDiscard()
 *    For knocking: getFinalMelds()
 * 5) I made the following changes:
 *                                   - Made class week_5.GameState to keep track of what is in my hand, what I know is in the opponent's hand, what cards I've seen, what cards the opponent has discarded, the face-up card, and the turn number.
 *                                   Got rid of the instance variables cards, and faceUpCard, and added week_5.GameState state.
 *                                   - Updated methods reportDraw and reportDiscard to record changes in what I've seen, and what is in the opponent's hand.
 *                                   - willDrawFaceUpCard now checks only the best meld sets, instead of all melds. This has a *very* small positive impact.
 *                                   - Made it so we avoid discarding cards if we think the opponent will pick it up (prefer discarding if they've discarded similar, and avoid discarding if they have similar)
 *                                   - Made is so that we pick up a card if we would only need one more card to make a meld, on turn 3 or earlier, or if its deadwood is 3 or less.
 *                                   - Made it so that on turns 4 and later, we pick up unmelded cards if they could fit into one of the opponent's known melds
 *                                   - Made it so that if the deadwood for the face-up card is 4 or more less than that of our highest unmelded card, we pick it up
 *                                   - Made it so that we knock if our deadwood is 1 or less. Otherwise, wait it out and try to undercut.
 * 6) MyGinRummyPlayer won against SimpleGinRummyPlayer in ~67% of the games.
 */

/*
 * WEEK 2 QUESTIONS:
 * Notes:
 *      - My background in economics is slim, but I wonder if it would be worthwhile to consider the (expected) marginal benefit/cost of waiting one more turn to knock rather than knocking.
 *          Do I expect that waiting one more turn will lower my deadwood enough that it exceeds the amount that I expect my opponent's deadwood will lower in the same period of time?
 *          This would be difficult to do effectively without sufficient knowledge of what's in my opponent's hand, I think.
 *      - When considering whether or not to discard a single, I think it would be worthwhile to consider the probability that I will pick up a card to make it a duple within x turns.
 *      - Similarly, when considering whether or not to discard a card which is part of a tuple, I think it would be worthwhile to consider the probability that I will pick up a card to make it a meld within y turns.
 *      - I made another player, InputPlayer, which makes decisions based off of user input. I made it so that it prints out the state of the game at the beginning of willDrawFaceUpCard.
 *          - This allowed me to get a better "feel" on the opponent's strategies. For one, I noted that it picks up the face-down far more often than the face-up, and so it's not very effective
 *              to base decisions on what I know is in their hand. It is useful more often to look at what they've discarded and neglected to pick up from the face-up pile.
 *              - That said, looking at its algorithm for willDrawFaceUp, if they do pick up a face-up, it must be in a meld, which is useful to know.
 * Changelist:
 *      - Created week_5.Strategy class, containing parameters for different conditions. It is current a fixed strategy, nothing crazy like the HMC one yet.
 *          - It considers a lot of different factors, but right now the only ones that I am using are maxKnockDeadwood, maxDupleDeadwood, minThirdCardProb, minDupleDiscardTurn, minLayoffTurn and minPickupDifference
 *              - maxKnockDeadwood is the maximum our deadwood can be before knocking. It is currently 1.
 *              - maxDupleDeadwood is the maximum deadwood a card in a duple can have before we consider discarding it. It is currently a 5.
 *              - minThirdCardProb is the minimum probability that we can make a duple into a meld quickly in order to consider keeping the cards. It is currently 0.01 (keep duple unless it's impossible to complete the meld).
 *              - minDupleDiscardTurn is the first turn where we will stop trying to hold onto duples containing a card with deadwood higher than maxDupleDeadwood. It is currently an 8.
 *              - minPickupDifference is the minimum difference there has to be between the face-up and the card in our hand with the worst deadwood in order to pick it up, if it would be a single. It is currently a 9.
 *              - minLayoffTurn is the first turn where we will start trying to pick up cards that are in the opponent's melds to try to lay them off.
 *      - Incorporated values from strategy into willDrawFaceUpCard, findDiscard, getFinalMelds
 *      - Added condition to findDiscard, to avoid discarding cards in a duple
 *      - Made it so that we don't try to keep duples if there are no more face-down cards which could complete it
 *      - Tinkered with values for strategy to find the best combination
 *      -Out of the remaining cards, find any cards who's removal would drop the overall deadwood by at least strategy.getMinPickupDifference(). If face-up is one of these, dont pick it up.
 *      - Made method getMinThirdCardProb. Currently, returns 1.0 if the face-down pile still contains cards which could complete the meld, given a duple. Returns 0.0 otherwise. Room to expand on.
 *
 * Last week, my win rate was ~67%. Now, my win rate is ~68%. I feel like I made some significant changes, but the amount of change in my win rate is very low, so they must mostly
 *  just be helpful in rare cases. I'll need to put more thought to it
 */

/*
 * WEEK 3 QUESTIONS:
 * Decision point strategies:
 *
 * Draw face-up:
 *  -If picking up the face-up card will lower our deadwood by an amount that is at least strategy.getMinPickupDifference() [3],
 *      and it lowers it more than we expect the face-down card to draw it.
 *  -If the card can't be melded within one turn, and its deadwood is greater than maxDupleDeadwood [6], or if it can't be melded
 *      within 2 turns and its deadwood is greater than maxSingleDeadwood [3], don't draw it.
 *  -If the card improves our deadwood by at least maxDupleDeadwood [6], and it would help an opponent meld, draw it.
 *  -If the change in deadwood from picking up the face-up is within 2 of the worst possible change in deadwood from a discard,
 *      don't pick it up. Otherwise, pick it up.
 * Discard:
 *  -Potential discards should be cards who's discard would lower deadwood the most.
 *  -Prefer to discard cards who cannot be melded even after 2 draws. If there are none (or no cards can), prefer those
 *      who can't be melded after 1 draw.
 *  -Avoid discarding cards which we think the opponent can make a meld from (or at least get closer to one).
 *  -Prefer to discard cards if the opponent has discarded compatible cards to.
 * Knock:
 *  -If deadwood is less than or equal to maxKnockDeadwood [1], knock.
 *
 */