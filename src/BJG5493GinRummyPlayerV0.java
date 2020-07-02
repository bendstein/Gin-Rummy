import java.util.*;
import java.util.stream.Collectors;

public class BJG5493GinRummyPlayerV0 implements GinRummyPlayer {

    private int playerNum; //The number assigned to our player
    @SuppressWarnings("unused")
    private int startingPlayerNum;
    private Random random = new Random(); //prng
    private boolean opponentKnocked = false; //Becomes true if opponent knocks
    private Card drawnCard; //The card which was drawn
    private ArrayList<Long> drawDiscardBitstrings = new ArrayList<Long>();
    private GameState state; //State of the game

    @Override
    public void startGame(int playerNum, int startingPlayerNum, Card[] cards) {
        this.playerNum = playerNum;
        this.startingPlayerNum = startingPlayerNum;

        state = new GameState(new ArrayList<>(Arrays.asList(cards)));

        opponentKnocked = false;
        drawDiscardBitstrings.clear();

    }

    @Override
    public boolean willDrawFaceUpCard(Card card) {
        // If first turn, record the face-up card. All other unseen face-up cards should be recorded in reportDiscard()
        if(state.getTurn() == 0) state.addSeen(card);

        //week_5.Card is our face-up
        state.setFaceUp(card);

        return willDrawFaceUpCard(state.getHand(), state.getFaceUp());
    }

    //See if we would draw card if we had the given hand
    public boolean willDrawFaceUpCard(ArrayList<Card> hand, Card card) {

        // Return true if card would be a part of a meld in one of the best meld sets, false otherwise.
        ArrayList<Card> newCards = new ArrayList<>(hand);
        newCards.add(card);
        ArrayList<Card> melded = new ArrayList<>();
        ArrayList<Card> unmelded = new ArrayList<>(newCards);

        for(ArrayList<ArrayList<Card>> meldSet : GinRummyUtil.cardsToBestMeldSets(newCards))
            for(ArrayList<Card> meld : meldSet) {
                ArrayList<Card> toRemove = new ArrayList<>();
                for(Card c : unmelded) {
                    if(meld.contains(c)) {
                        toRemove.add(c);
                    }
                }

                unmelded.removeAll(toRemove);
                melded.addAll(toRemove);
            }

        if(melded.contains(card)) return true;

        Set<Card> toRemove = new HashSet<>();
        for(Card c : unmelded) {
            if(state.getTurn() <= 3 || GinRummyUtil.getDeadwoodPoints(c) < 4) {
                toRemove.addAll(getSameRank(unmelded, c).size() > 1 && getSameRank(state.getLost(), c).size() < 3 && getSameRank(state.getOppHand(), c).size() < 3 ? getSameRank(unmelded, c) : getSameRank(unmelded, null));
                toRemove.addAll(getSameSuit(unmelded, c, 3).size() > 1 && getSameSuit(state.getLost(), c, 3).size() < 3 && getSameSuit(state.getOppHand(), c, 3).size() < 3 ? getSameSuit(unmelded, c, 2) : getSameSuit(unmelded, null, 3));
            }
        }

        unmelded.removeAll(toRemove);

        if(state.getTurn() > 4) {
            for(ArrayList<Card> meld : GinRummyUtil.cardsToAllMelds(state.getOppHand())) if(meld.contains(card)) return true;
        }

        else if(state.getHighestDeadwood(unmelded) != null && GinRummyUtil.getDeadwoodPoints(state.getHighestDeadwood(unmelded)) - GinRummyUtil.getDeadwoodPoints(card) > 4) return true;

        return false;
    }

    @Override
    public void reportDraw(int playerNum, Card drawnCard) {
        // Ignore other player draws.  Add to cards if playerNum is this player.
        if (playerNum == this.playerNum) {
            state.addHand(drawnCard);
            this.drawnCard = drawnCard;
        }
        //If the other player drew, and drawnCard isn't null, other player drew face-up.
        else {
            if(drawnCard != null)
                state.addOppHand(drawnCard);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Card getDiscard() {

        return findDiscard(state.getHand(), state.getFaceUp());

    }

    //Find the card to discard in a hand, given the card that you can't discard.
    public Card findDiscard(ArrayList<Card> hand, Card face_up) {

        // Discard a random card (not just drawn face up) leaving minimal deadwood points.
        int minDeadwood = Integer.MAX_VALUE;
        ArrayList<Card> candidateCards = new ArrayList<Card>();
        for (Card card : hand) {
            // Cannot draw and discard face up card.
            if (card == drawnCard && drawnCard == face_up)
                continue;
            // Disallow repeat of draw and discard.
            ArrayList<Card> drawDiscard = new ArrayList<Card>();
            drawDiscard.add(drawnCard);
            drawDiscard.add(card);
            if (drawDiscardBitstrings.contains(GinRummyUtil.cardsToBitstring(drawDiscard)))
                continue;

            ArrayList<Card> remainingCards = new ArrayList<>(hand);
            remainingCards.remove(card);
            ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(remainingCards);
            int deadwood = bestMeldSets.isEmpty() ? GinRummyUtil.getDeadwoodPoints(remainingCards) : GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), remainingCards);
            if (deadwood <= minDeadwood) {
                if (deadwood < minDeadwood) {
                    minDeadwood = deadwood;
                    candidateCards.clear();
                }
                candidateCards.add(card);
            }
        }

        //If more than one card, avoid discarding cards that would be good for opponent
        if(candidateCards.size() > 1) {
            ArrayList<Card> toRemove = new ArrayList<>(); //Don't remove until after loop
            ArrayList<Card> preferred = new ArrayList<>(); //Cards we would prefer to remove

            for(Card card : candidateCards) {

                if(willDrawFaceUpCard(state.getOppHand(), card)) toRemove.add(card);
                else if(containsRank(state.getOppHand(), card) || containsSuit(state.getOppHand(), card, 3)) toRemove.add(card); //If card could help opp meld, avoid tossing
                else if(containsRank(state.getOppDiscard(), card) || containsSuit(state.getOppDiscard(), card, 3)) preferred.add(card); //If similar cards have been tossed, prefer
            }

            if(toRemove.size() < candidateCards.size()) candidateCards.removeAll(toRemove); //Remove useful cards to the opponent, unless all cards would be useful
            if(preferred.size() != 0 && preferred.size() < candidateCards.size()) candidateCards.removeIf(card -> (!preferred.contains(card)));

        }

        Card discard = candidateCards.get(random.nextInt(candidateCards.size()));
        // Prevent future repeat of draw, discard pair.
        ArrayList<Card> drawDiscard = new ArrayList<Card>();
        drawDiscard.add(drawnCard);
        drawDiscard.add(discard);
        drawDiscardBitstrings.add(GinRummyUtil.cardsToBitstring(drawDiscard));

        //Increment turn at the end of our discard
        state.nextTurn();

        return discard;

    }

    //Check if hand contains any card of same rank as c
    public boolean containsRank(ArrayList<Card> hand, Card c) {
        for(Card card : hand) {
            if(card.equals(c)) continue; //Don't count card c
            if(card.getRank() == c.getRank()) return true;
        }

        return false;
    }

    //Get all cards of same ranks as C
    public ArrayList<Card> getSameRank(ArrayList<Card> hand, Card c) {
        if(c == null) return new ArrayList<>();
        return (ArrayList<Card>) hand
                .stream()
                .filter(card -> (card.getRank() == c.getRank()))
                .collect(Collectors.toList());
    }

    //Check if hand contains any card of same suit as c if its rank is within diff of c's rank
    public boolean containsSuit(ArrayList<Card> hand, Card c, int diff) {
        for(Card card : hand) {
            if(card.equals(c)) continue; //Don't count card c
            if(card.getSuit() == c.getSuit() && Math.abs(c.getRank() - card.getRank()) < 3) return true;
        }

        return false;
    }

    //Get all cards of same suit as c, given that its rank is within diff of c's rank
    public ArrayList<Card> getSameSuit(ArrayList<Card> hand, Card c, int diff) {
        if(c == null) return new ArrayList<>();
        return (ArrayList<Card>) hand
                .stream()
                .filter(card -> (card.getSuit() == c.getSuit() && Math.abs(c.getRank() - card.getRank()) < diff))
                .collect(Collectors.toList());
    }

    @Override
    public void reportDiscard(int playerNum, Card discardedCard) {
        // Ignore other player discards.  Remove from cards if playerNum is this player.
        if (playerNum == this.playerNum)
            state.remHand(discardedCard);
            //If we knew the discarded card was in the opponent's hand, remove. If we didn't, add it to seen.
        else {
            state.addSeen(discardedCard);
            state.remOppHand(discardedCard);
        }
    }

    @Override
    public ArrayList<ArrayList<Card>> getFinalMelds() {
        // Check if deadwood of maximal meld is low enough to go out.
        ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(state.getHand());
        if (!opponentKnocked && (bestMeldSets.isEmpty() || GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), state.getHand()) > GinRummyUtil.MAX_DEADWOOD))
            return null;

        else if (!opponentKnocked && (bestMeldSets.isEmpty() || GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), state.getHand()) <= 1))
            return bestMeldSets.isEmpty() ? new ArrayList<ArrayList<Card>>() : bestMeldSets.get(random.nextInt(bestMeldSets.size()));

        else if (!opponentKnocked && state.getTurn() <= Integer.MAX_VALUE) return null;

        else return bestMeldSets.isEmpty() ? new ArrayList<ArrayList<Card>>() : bestMeldSets.get(random.nextInt(bestMeldSets.size()));
    }

    @Override
    public void reportFinalMelds(int playerNum, ArrayList<ArrayList<Card>> melds) {
        // Melds ignored by simple player, but could affect which melds to make for complex player.
        if (playerNum != this.playerNum)
            opponentKnocked = true;
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

    class GameState {

        private ArrayList<Card> hand = new ArrayList<Card>(); //Our hand
        private Set<Card> seen; //Cards which have been seen
        private ArrayList<Card> opp_hand; //Cards which we know the opponent has
        private ArrayList<Card> oppDiscard; //Cards which the opponent discarded
        private Card face_up; //Face-up card
        private int turn; //Current turn of the game

        public GameState(ArrayList<Card> hand) {
            this.hand = new ArrayList<>(hand);

            seen = new HashSet<>();
            seen.addAll(hand);

            opp_hand = new ArrayList<>();
            oppDiscard = new ArrayList<>();
            turn = 0;
            face_up = null;
        }

        public ArrayList<Card> getHand() {
            return hand;
        }

        public void setHand(ArrayList<Card> hand) {
            this.hand = new ArrayList<>(hand);
        }

        public void addHand(Card card) {
            hand.add(card);
        }

        public void remHand(Card card) {
            hand.remove(card);
        }

        public Set<Card> getSeen() {
            return seen;
        }

        public void addSeen(Card card) {
            seen.add(card);
        }

        public ArrayList<Card> getOppHand() {
            return opp_hand;
        }

        public void addOppHand(Card card) {
            if(!opp_hand.contains(card)) opp_hand.add(card);

            //If for some reason they discarded a card and then picked it back up
            oppDiscard.remove(card);
        }

        public void remOppHand(Card card) {
            opp_hand.remove(card);
            addOppDiscard(card);
        }

        public Card getFaceUp() {
            return face_up;
        }

        public void setFaceUp(Card face_up) {
            this.face_up = face_up;
        }

        public int getTurn() {
            return turn;
        }

        public void nextTurn() {
            turn++;
        }

        public ArrayList<Card> getOppDiscard() {
            return oppDiscard;
        }

        public void addOppDiscard(Card card) {
            oppDiscard.add(card);
        }

        //Given a hand, find all cards which belong to none of the best melds
        public ArrayList<Card> getUnmelded(ArrayList<Card> cards) {

            ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(cards);

            ArrayList<Card> unmelded = new ArrayList<>(cards);

            unmelded.removeIf(card -> {
                for(ArrayList<ArrayList<Card>> meldSet : bestMeldSets)
                    for(ArrayList<Card> meld : meldSet)
                        if(meld.contains(card)) return true;
                return false;
            });

            return unmelded;

        }

        //Get cards which could become melds
        public ArrayList<Card> getAlmostMelded(ArrayList<Card> cards) {

            ArrayList<Card> unmelded = getUnmelded(cards);
            ArrayList<Card> almostMelded = new ArrayList<>();

            for(Card card : unmelded) {
                for(Card card1 : cards) {
                    if(card.equals(card1) || almostMelded.contains(card1)) continue;

                    if(card.getRank() == card1.getRank() || (card.getSuit() == card1.getSuit() && Math.abs(card.getRank() - card1.getRank()) <= 2)){
                        almostMelded.add(card);
                        almostMelded.add(card1);
                    }
                }
            }

            return almostMelded;

        }

        public Card getHighestDeadwood(ArrayList<Card> cards) {
            if(cards.size() == 0) return null;
            return cards
                    .stream()
                    .reduce(cards.get(0), (max, c) -> GinRummyUtil.getDeadwoodPoints(c) > GinRummyUtil.getDeadwoodPoints(max) ? c : max);
        }

        public ArrayList<Card> getLost() {
            ArrayList<Card> seen = new ArrayList<>(this.seen);
            seen.removeAll(hand);
            seen.removeAll(opp_hand);
            return seen;
        }

    }
}
