import java.util.*;

public class InputPlayer implements GinRummyPlayer {


    Scanner scan;
    private int playerNum; //The number assigned to our player
    @SuppressWarnings("unused")
    private int startingPlayerNum;
    private Random random = new Random(); //prng
    private boolean opponentKnocked = false; //Becomes true if opponent knocks
    private Card drawnCard; //The card which was drawn
    private GameState state; //State of the game

    @Override
    public void startGame(int playerNum, int startingPlayerNum, Card[] cards) {
        scan = new Scanner(System.in);
        this.playerNum = playerNum;
        this.startingPlayerNum = startingPlayerNum;

        state = new GameState(new ArrayList<>(Arrays.asList(cards)));

        opponentKnocked = false;

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

        String pickup;

        System.out.println("Face up card is: " + card.toString());
        System.out.println();

        System.out.println("Opponent hand: ");
        BJG5493GinRummyPlayerV1.MyGinRummyUtil.printHandWithMelds(state.getOppHand());
        System.out.println();

        System.out.println("Opponent Discard: ");
        BJG5493GinRummyPlayerV1.MyGinRummyUtil.printHand(state.getOppDiscard());

        System.out.println("Seen: ");
        BJG5493GinRummyPlayerV1.MyGinRummyUtil.printHand(new ArrayList<>(state.getSeen()));
        System.out.println();

        System.out.println("Buried: ");
        BJG5493GinRummyPlayerV1.MyGinRummyUtil.printHand(state.getBuried());

        System.out.println("Your hand: ");
        BJG5493GinRummyPlayerV1.MyGinRummyUtil.printHandWithMelds(hand);
        System.out.println();

        while(true) {
            System.out.println("Pick up face-up card?");
            pickup = scan.nextLine();
            if(pickup.toLowerCase().equals("yes")) return true;
            if(pickup.toLowerCase().equals("no")) return false;
        }
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

        ArrayList<Card> potentialDiscards = findDiscard(state.getHand(), state.getFaceUp());
        return potentialDiscards.get(random.nextInt(potentialDiscards.size()));

    }

    //Find the card to discard in a hand, given the card that you can't discard.
    public ArrayList<Card> findDiscard(ArrayList<Card> hand, Card face_up) {

        BJG5493GinRummyPlayerV1.MyGinRummyUtil.printHandWithMelds(hand);

        String card;
        while(true) {
            System.out.println("Which card would you like to discard?");
            card = scan.nextLine().toUpperCase();
            if(hand.contains(Card.strCardMap.get(card))) {
                if(face_up.equals(drawnCard) && drawnCard.equals(Card.strCardMap.get(card))) System.out.println("Cannot draw and discard face-up.");
                else return new ArrayList<>(Collections.singletonList(Card.strCardMap.get(card)));
            }
        }

    }

    @Override
    public void reportDiscard(int playerNum, Card discardedCard) {
        // Ignore other player discards.  Remove from cards if playerNum is this player.
        if (playerNum == this.playerNum) {
            state.remHand(discardedCard);
            state.nextTurn();
        }

        //If we knew the discarded card was in the opponent's hand, remove. If we didn't, add it to seen.
        else {
            state.addSeen(discardedCard);
            state.remOppHand(discardedCard);
        }
    }

    @Override
    public ArrayList<ArrayList<Card>> getFinalMelds() {

        String knock;

        // Check if deadwood of maximal meld is low enough to go out.
        ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(state.getHand());
        if (!opponentKnocked && (bestMeldSets.isEmpty() || GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), state.getHand()) > GinRummyUtil.MAX_DEADWOOD))
            return null;

        else if (!opponentKnocked && (bestMeldSets.isEmpty() || GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), state.getHand()) <= 10)) {
            System.out.println("Knock?");
            knock = scan.nextLine().toLowerCase();
            if(knock.equals("yes"))
                return bestMeldSets.isEmpty() ? new ArrayList<ArrayList<Card>>() : bestMeldSets.get(random.nextInt(bestMeldSets.size()));
            else return null;
        }

        else if (!opponentKnocked) return null;

        else {
            return bestMeldSets.isEmpty() ? new ArrayList<ArrayList<Card>>() : bestMeldSets.get(random.nextInt(bestMeldSets.size()));
        }


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

    private class GameState {

        private ArrayList<Card> hand = new ArrayList<Card>(); //Our hand
        private Set<Card> seen; //Cards which have been seen
        private ArrayList<Card> opp_hand; //Cards which we know the opponent has
        private ArrayList<Card> oppDiscard; //Cards which the opponent discarded
        private Card face_up; //Face-up card
        private int turn; //Current turn of the game
        private int num_remaining; //Number of remaining face-down cards

        GameState(ArrayList<Card> hand) {
            this.hand = new ArrayList<>(hand);

            seen = new HashSet<>();
            seen.addAll(hand);

            opp_hand = new ArrayList<>();
            oppDiscard = new ArrayList<>();
            turn = 0;
            face_up = null;
            num_remaining = 2 * hand.size();
        }

        ArrayList<Card> getHand() {
            return hand;
        }

        void setHand(ArrayList<Card> hand) {
            this.hand = new ArrayList<>(hand);
        }

        void addHand(Card card) {
            hand.add(card);
        }

        void remHand(Card card) {
            hand.remove(card);
        }

        Set<Card> getSeen() {
            return seen;
        }

        void addSeen(Card card) {
            seen.add(card);
        }

        ArrayList<Card> getOppHand() {
            return opp_hand;
        }

        void addOppHand(Card card) {
            if(!opp_hand.contains(card)) opp_hand.add(card);

            //If for some reason they discarded a card and then picked it back up
            oppDiscard.remove(card);
        }

        void remOppHand(Card card) {
            opp_hand.remove(card);
            addOppDiscard(card);
        }

        Card getFaceUp() {
            return face_up;
        }

        void setFaceUp(Card face_up) {
            this.face_up = face_up;
        }

        int getTurn() {
            return turn;
        }

        void nextTurn() {
            turn++;
        }

        ArrayList<Card> getOppDiscard() {
            return oppDiscard;
        }

        void addOppDiscard(Card card) {
            oppDiscard.add(card);
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
        double getSecondCardProbability(Card card, int numTurns) {
            return 0.0;
        }

        /**
         * @param cards A duple of cards
         * @param numTurns Number of turns
         * @return the probability that the duple will become a meld within numTurns turns
         *  For now, return 0 if all cards are buried or in the opponent's hand, and 1 otherwise
         */
        double getThirdCardProbability(ArrayList<Card> cards, int numTurns) {

            //Cards which could complete the meld
            ArrayList<Card> adjCards = new ArrayList<>();

            //Third card must make set
            if(cards.get(0).rank == cards.get(1).rank) {
                adjCards.addAll(BJG5493GinRummyPlayerV1.MyGinRummyUtil.getSameRank(new ArrayList<>(Arrays.asList(Card.allCards)), cards.get(0)));
            }

            //Third card must make run
            else {
                adjCards.addAll(BJG5493GinRummyPlayerV1.MyGinRummyUtil.getSameSuit(new ArrayList<>(Arrays.asList(Card.allCards)), cards.get(0), 1));
                adjCards.addAll(BJG5493GinRummyPlayerV1.MyGinRummyUtil.getSameSuit(new ArrayList<>(Arrays.asList(Card.allCards)), cards.get(1), 1));
            }

            //Don't consider cards already in duple
            adjCards.remove(cards.get(0));
            adjCards.remove(cards.get(1));

            ArrayList<Card> toRemove = new ArrayList<>();

            for(Card card : adjCards) {
                if(getBuried().contains(card) || opp_hand.contains(card)) return 0.0;
            }

            return 0.0;
        }

        /**
         * @return a list of all cards which are under the face-up card
         */
        ArrayList<Card> getBuried() {
            ArrayList<Card> seen = new ArrayList<>(this.seen);
            seen.removeAll(hand);
            seen.removeAll(opp_hand);
            seen.remove(face_up);
            return seen;
        }

    }

}