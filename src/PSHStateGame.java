import java.util.ArrayList;

/**
 * The current game state
 *
 */
public class PSHStateGame {
    private long unseenCardsAsBitString; // cards that have not been seen
    private int numberOfTurns; // Number of turns this time
    private Card faceUpCard;

    // State of my hand:
    private ArrayList<Card> myCards = new ArrayList<Card>();

    // State of opponents hand:
    private ArrayList<Card> oppCards = new ArrayList<Card>();

    /**
     * Reset the state for a new game
     */
    public void reset() {
        unseenCardsAsBitString = 0xFFFFFFFFFFFFFL;
        numberOfTurns = 0;
        myCards.clear();
        oppCards.clear();
    }

    /**
     * Record the fact that a card has been seen by player
     * @param card
     */
    public void reportFaceUpCard(Card card) {
        faceUpCard = card;
        unseenCardsAsBitString &= ~(1L << card.getId());
    }

    /**
     * The current turn number, which counts the number of cards that have been picked up so far in the game
     * @return the current turn number
     */
    public int getTurnNumber() {
        return numberOfTurns;
    }

    /**
     *
     * @return the last card that was face up
     */
    public Card lastFaceUpCard() {
        return faceUpCard;
    }

    /**
     * Determine if a card has been seen by player
     * @param cardId the id of the card
     * @return true if the card has not been seen, because it was dealt to other player or is in the deck
     */
    public boolean isUnseenCard(int cardId) {
        return (unseenCardsAsBitString & (1L << cardId)) != 0;
    }


    /**
     * Add a card to the hand
     * @param card
     */
    public void reportPickup(Card card, boolean myHand, boolean initialHand) {
        if (myHand) {
            myCards.add(card);
        }
        else {
            if (card != null) {
                oppCards.add(card);
            }
        }
        if (card != null) {
            unseenCardsAsBitString &= ~(1L << card.getId());
        }
        if (!initialHand) numberOfTurns++;
    }

    /**
     * Remove a card to the hand
     * @param card
     */
    public void reportDiscard(Card card, boolean myHand) {
        if (myHand) {
            myCards.remove(card);
        }
        else {
            oppCards.remove(card);
        }
        unseenCardsAsBitString &= ~(1L << card.getId());
    }

    /**
     * Get the current cards in the hand
     * @return cards in hand
     */
    @SuppressWarnings("unchecked")
    public ArrayList<Card> getCards(boolean myHand) {
        if (myHand)
            return (ArrayList<Card>) myCards.clone();
        else
            return null;
    }

    /**
     * Return the cards that cannot be added to a meld no matter what card
     * is drawn next, as long as the card has a point value that is at least
     * the minimum threshold
     *
     * @param minPointValue the minimum threshold for cards to be added
     *
     * @return the unmatchable cards
     */
    public ArrayList<Card> getMyUnmatchableCards(int minPointValue, int taboo) {
        ArrayList<Card> unmatchableCards = new ArrayList<>();
        for (Card card: myCards) {
            if (GinRummyUtil.getDeadwoodPoints(card) >= minPointValue) {
                unmatchableCards.add(card);
            }
        }

        for (int drawCardId = 0; unmatchableCards.size() > 0 && drawCardId < 52; drawCardId++) {
            if (drawCardId != taboo && isUnseenCard(drawCardId)) {
                myCards.add(Card.getCard(drawCardId));
                ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(myCards);
                for (int i = 0; i < bestMeldSets.size(); i++) {
                    for (int j = 0; j < bestMeldSets.get(i).size(); j++) {
                        for (int k = 0; k < bestMeldSets.get(i).get(j).size(); k++) {
                            unmatchableCards.remove(bestMeldSets.get(i).get(j).get(k));
                        }
                    }
                }
                myCards.remove(Card.getCard(drawCardId));
            }
        }

        return unmatchableCards;
    }

    /**
     * Return the cards that are deadwood in at least one meld configurations
     *
     * @return the deadwood cards (which are potential discards)
     */
    public ArrayList<Card> getMyDeadwoodCards() {
        ArrayList<Card> deadwoodCards = new ArrayList<>();

        ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(myCards);
        for (int i = 0; i < bestMeldSets.size(); i++) {
            ArrayList<Card> unmeldedCards = new ArrayList<>();
            for (Card card: myCards) {
                unmeldedCards.add(card);
            }

            for (int j = 0; j < bestMeldSets.get(i).size(); j++) {
                for (int k = 0; k < bestMeldSets.get(i).get(j).size(); k++) {
                    unmeldedCards.remove(bestMeldSets.get(i).get(j).get(k));
                }
            }
            for (Card card: unmeldedCards) {
                deadwoodCards.add(card);
            }
        }
        return deadwoodCards;
    }


    /**
     * Determine if taking this card would lower number of deadwood points because it
     * would become part of a meld
     *
     * @param card
     * @return
     */
    public boolean doesCardMakeNewMeld(Card card) {
        boolean promising = false;

        int currentDeadwood = getMyDeadwood();

        myCards.add(card);

        for (ArrayList<Card> meld : GinRummyUtil.cardsToAllMelds(myCards)) {
            if (meld.contains(card)) {
                promising = true;
                break;
            }
        }

        if (promising) {
            ArrayList<Card> potentialDiscards = new ArrayList<>();
            int newDeadwood = getMyBestDeadwood(-1, potentialDiscards);

            if (potentialDiscards.contains(card)) {
                // Do not take this card, if I would be indifferent to discarding it
                promising = false;
            }
            else if (newDeadwood >= currentDeadwood) {
                promising = false;

            }
        }
        if (!myCards.get(10).equals(card)) throw new RuntimeException("Error removing test card.");
        myCards.remove(10);
        return promising;
    }

    /**
     * Find best deadwood value after a discard
     * @param bestDiscards a collection of cards that can be discarded to yield the best deadwood value
     * @return the best deadwood value possible
     */
    public int getMyBestDeadwood(int tabooCard, ArrayList<Card> bestDiscards) {
        if (myCards.size() != 11) throw new IllegalArgumentException("Must pass a hand with a card needing to be discarded.");

        int bestDeadwood = Integer.MAX_VALUE;
        for (int i = 0; i < myCards.size(); i++) {
            if (i == tabooCard) continue;
            Card potentialDiscard = myCards.get(i);

            // Remove discard from hand and evaluate
            myCards.set(i, myCards.get(10));
            myCards.remove(10);

            int deadwood = getMyDeadwood();

            if (deadwood < bestDeadwood) {
                if (bestDiscards != null) {
                    bestDiscards.clear();
                    bestDiscards.add(potentialDiscard);
                }
                bestDeadwood = deadwood;
            }
            else if (deadwood == bestDeadwood) {
                if (bestDiscards != null) bestDiscards.add(potentialDiscard);
            }

            // Put discard back in hand
            if (i == 10) {
                myCards.add(potentialDiscard);
            }
            else {
                myCards.add(myCards.get(i));
                myCards.set(i, potentialDiscard);
            }
        }
        return bestDeadwood;
    }

    /**
     * Return deadwood points after making best melds
     * @return
     */
    public int getMyDeadwood() {
        ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(myCards);
        return bestMeldSets.isEmpty() ? GinRummyUtil.getDeadwoodPoints(myCards) : GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), myCards);
    }

    /**
     * Estimate probability that opponent will not find this card useful
     *
     * Currently the algorithm looks for cards that cannot help the opponent create
     * a meld, given what is in my hand and what has been discarded
     *
     * Ideas for improvement: Make this estimate a function of
     * cards that opponent picked up and did not pick up
     *
     * @param card
     * @return an estimate of usefulness of card
     */
    public double probabilityThatCardIsNotUseful(Card card) {
        int rankCount = 0;
        for (int suit = 0; suit < Card.NUM_SUITS; suit++) {
            if (suit != card.getSuit()) {
                Card other = Card.getCard(card.getRank(), suit);
                if (isUnseenCard(other.getId()) || oppCards.contains(other)) {
                    rankCount++;
                }
            }
        }
        if (rankCount >= 3) return 1.0;

        int upperGap = 0;
        for (int rank = card.getRank() + 1; rank < card.getRank() + 3 && rank < Card.NUM_RANKS; rank++) {
            Card other = Card.getCard(rank, card.getSuit());
            if (isUnseenCard(other.getId()) || oppCards.contains(other)) {
                upperGap++;
            }
        }
        int lowerGap = 0;
        for (int rank = card.getRank() - 1; rank > card.getRank() - 3 && rank >= 0; rank--) {
            Card other = Card.getCard(rank, card.getSuit());
            if (isUnseenCard(other.getId()) || oppCards.contains(other)) {
                lowerGap++;
            }
        }
        return upperGap + lowerGap >= 2?1.0:0.0;
    }
}
