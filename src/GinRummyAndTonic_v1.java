import java.util.*;
import java.util.stream.Collectors;

/**
 * Class containing the player
 */
public class GinRummyAndTonic_v1 implements GinRummyPlayer {

    // <editor-fold desc="Instance Variables">
    /**
     * The number assigned to our player
     */
    private int playerNum;

    /**
     * prng
     */
    private Random random = new Random();

    /**
     * Becomes true if opponent has knocked.
     */
    private boolean opponentKnocked = false;

    /**
     * The id of the card which was drawn
     */
    private int drawn;

    /**
     * The current state of the game
     */
    private State state;

    /**
     * Parameters used for different decision points
     */
    private GeneralStrategy generalStrategy;

    /**
     * If the opponent knocks, this is what their final meld set is
     */
    private ArrayList<ArrayList<Card>> oppMelds;
    // </editor-fold>

    public GinRummyAndTonic_v1() {

        // Information set recorded including index into deck, but only considering
        // deadwood here
        HashMap<String, Double> knockStrat = new HashMap<String, Double>() {
            {
                put("9_39", 0.500);
                put("9_38", 0.000);
                put("9_37", 0.000);
                put("9_36", 0.000);
                put("9_35", 0.000);
                put("9_34", 0.000);
                put("9_33", 0.000);
                put("9_32", 0.000);
                put("9_31", 0.000);
                put("9_30", 0.000);
                put("9_29", 0.000);
                put("9_28", 0.000);
                put("9_27", 0.000);
                put("9_26", 0.000);
                put("9_25", 0.021);
                put("9_24", 0.003);
                put("9_23", 0.500);
                put("9_22", 0.000);
                put("9_21", 0.583);
                put("8_39", 0.500);
                put("8_38", 1.000);
                put("8_37", 0.000);
                put("8_36", 0.000);
                put("8_35", 0.000);
                put("8_34", 0.000);
                put("8_33", 0.000);
                put("8_32", 0.000);
                put("8_31", 0.000);
                put("8_30", 0.000);
                put("8_29", 0.000);
                put("8_28", 0.000);
                put("8_27", 0.001);
                put("8_26", 0.035);
                put("8_25", 0.001);
                put("8_24", 0.049);
                put("8_23", 0.029);
                put("8_22", 0.000);
                put("8_21", 1.000);
                put("7_39", 0.500);
                put("7_38", 0.001);
                put("7_37", 0.001);
                put("7_36", 0.000);
                put("7_35", 0.006);
                put("7_34", 0.000);
                put("7_33", 0.000);
                put("7_32", 0.000);
                put("7_31", 0.000);
                put("7_30", 0.000);
                put("7_29", 0.066);
                put("7_28", 0.000);
                put("7_27", 0.001);
                put("7_26", 1.000);
                put("7_25", 0.008);
                put("7_24", 0.028);
                put("7_23", 0.112);
                put("7_22", 0.000);
                put("7_21", 0.000);
                put("6_39", 0.500);
                put("6_38", 0.000);
                put("6_37", 1.000);
                put("6_36", 0.003);
                put("6_35", 1.000);
                put("6_34", 0.003);
                put("6_33", 1.000);
                put("6_32", 1.000);
                put("6_31", 0.000);
                put("6_30", 0.000);
                put("6_29", 0.011);
                put("6_28", 0.000);
                put("6_27", 0.000);
                put("6_26", 0.000);
                put("6_25", 0.001);
                put("6_24", 0.000);
                put("6_23", 0.020);
                put("6_22", 0.002);
                put("6_21", 1.000);
                put("5_39", 0.500);
                put("5_38", 0.783);
                put("5_37", 1.000);
                put("5_36", 0.002);
                put("5_35", 0.000);
                put("5_34", 0.000);
                put("5_33", 0.000);
                put("5_32", 0.000);
                put("5_31", 0.000);
                put("5_30", 0.000);
                put("5_29", 0.000);
                put("5_28", 0.000);
                put("5_27", 0.000);
                put("5_26", 1.000);
                put("5_25", 0.001);
                put("5_24", 1.000);
                put("5_23", 0.015);
                put("5_22", 0.002);
                put("5_21", 0.000);
                put("4_39", 0.500);
                put("4_38", 1.000);
                put("4_37", 0.001);
                put("4_36", 0.000);
                put("4_35", 0.000);
                put("4_34", 0.000);
                put("4_33", 0.000);
                put("4_32", 0.000);
                put("4_31", 0.000);
                put("4_30", 0.000);
                put("4_29", 0.000);
                put("4_28", 0.000);
                put("4_27", 0.000);
                put("4_26", 0.000);
                put("4_25", 0.000);
                put("4_24", 0.076);
                put("4_23", 1.000);
                put("4_22", 0.000);
                put("4_21", 0.202);
                put("3_39", 0.500);
                put("3_38", 0.001);
                put("3_37", 0.000);
                put("3_36", 0.000);
                put("3_35", 0.000);
                put("3_34", 1.000);
                put("3_33", 0.000);
                put("3_32", 0.000);
                put("3_31", 0.000);
                put("3_30", 0.000);
                put("3_29", 0.000);
                put("3_28", 0.000);
                put("3_27", 0.000);
                put("3_26", 0.000);
                put("3_25", 0.000);
                put("3_24", 0.005);
                put("3_23", 1.000);
                put("3_22", 0.000);
                put("3_21", 0.000);
                put("2_39", 0.500);
                put("2_38", 1.000);
                put("2_37", 1.000);
                put("2_36", 0.007);
                put("2_35", 0.000);
                put("2_34", 0.000);
                put("2_33", 0.000);
                put("2_32", 0.000);
                put("2_31", 0.001);
                put("2_30", 0.000);
                put("2_29", 1.000);
                put("2_28", 0.000);
                put("2_27", 0.000);
                put("2_26", 0.000);
                put("2_25", 0.000);
                put("2_24", 0.001);
                put("2_23", 0.000);
                put("2_22", 0.009);
                put("2_21", 0.000);
                put("1_39", 0.500);
                put("1_38", 0.001);
                put("1_37", 0.000);
                put("1_36", 1.000);
                put("1_35", 0.000);
                put("1_34", 0.001);
                put("1_33", 0.000);
                put("1_32", 0.000);
                put("1_31", 0.000);
                put("1_30", 0.000);
                put("1_29", 0.000);
                put("1_28", 0.000);
                put("1_27", 0.000);
                put("1_26", 0.000);
                put("1_25", 0.000);
                put("1_24", 0.002);
                put("1_23", 0.000);
                put("1_22", 0.000);
                put("1_21", 0.000);
                put("10_39", 0.500);
                put("10_38", 0.001);
                put("10_37", 0.000);
                put("10_36", 0.000);
                put("10_35", 0.000);
                put("10_34", 0.000);
                put("10_33", 0.000);
                put("10_32", 0.000);
                put("10_31", 0.000);
                put("10_30", 0.000);
                put("10_29", 0.000);
                put("10_28", 0.000);
                put("10_27", 1.000);
                put("10_26", 0.000);
                put("10_25", 0.005);
                put("10_24", 0.000);
                put("10_23", 0.003);
                put("10_22", 0.044);
                put("10_21", 0.273);

            }
        };

        HashMap<String, Double> drawStrat = new HashMap<String, Double>() {
            {
                put("9_36_false", 0.500);
                put("9_35_false", 0.000);
                put("9_34_false", 1.000);
                put("9_33_false", 0.500);
                put("9_32_false", 0.500);
                put("9_31_false", 0.925);
                put("9_30_false", 0.593);
                put("9_29_false", 0.841);
                put("9_28_false", 0.236);
                put("9_27_false", 0.745);
                put("9_26_false", 0.014);
                put("9_25_false", 0.500);
                put("9_24_false", 0.998);
                put("9_23_false", 0.000);
                put("9_22_false", 0.406);
                put("9_21_false", 0.988);
                put("8_35_false", 0.000);
                put("8_34_false", 0.168);
                put("8_33_false", 0.699);
                put("8_32_false", 0.346);
                put("8_31_false", 0.462);
                put("8_30_false", 0.941);
                put("8_29_false", 0.842);
                put("8_28_false", 0.673);
                put("8_27_false", 0.506);
                put("8_26_false", 0.624);
                put("8_25_false", 0.009);
                put("8_24_false", 0.957);
                put("8_23_false", 0.103);
                put("8_22_false", 0.919);
                put("8_21_false", 0.000);
                put("7_36_false", 0.500);
                put("7_35_false", 0.315);
                put("7_34_false", 0.344);
                put("7_33_false", 0.526);
                put("7_32_false", 0.519);
                put("7_31_false", 0.988);
                put("7_30_false", 0.995);
                put("7_29_false", 0.195);
                put("7_28_false", 0.095);
                put("7_27_false", 0.029);
                put("7_26_false", 0.893);
                put("7_25_false", 0.004);
                put("7_24_false", 1.000);
                put("7_23_false", 0.075);
                put("7_22_false", 0.979);
                put("7_21_false", 0.002);
                put("6_37_false", 0.000);
                put("6_36_false", 0.013);
                put("6_35_false", 0.998);
                put("6_34_false", 0.372);
                put("6_33_false", 0.694);
                put("6_32_false", 0.340);
                put("6_31_false", 0.996);
                put("6_30_false", 0.807);
                put("6_29_false", 0.059);
                put("6_28_false", 0.055);
                put("6_27_false", 0.822);
                put("6_26_false", 0.256);
                put("6_25_false", 0.214);
                put("6_24_false", 1.000);
                put("6_23_false", 0.016);
                put("6_22_false", 0.996);
                put("6_21_false", 0.011);
                put("5_37_false", 0.567);
                put("5_36_false", 0.125);
                put("5_35_false", 0.554);
                put("5_34_false", 0.660);
                put("5_33_false", 0.915);
                put("5_32_false", 0.873);
                put("5_31_false", 0.961);
                put("5_30_false", 0.527);
                put("5_29_false", 0.985);
                put("5_28_false", 0.000);
                put("5_27_false", 0.270);
                put("5_26_false", 0.060);
                put("5_25_false", 0.999);
                put("5_24_false", 1.000);
                put("5_23_false", 0.090);
                put("5_22_false", 0.996);
                put("5_21_false", 0.001);
                put("4_38_false", 0.125);
                put("4_37_false", 0.000);
                put("4_36_false", 0.152);
                put("4_35_false", 0.873);
                put("4_34_false", 0.855);
                put("4_33_false", 0.451);
                put("4_32_false", 0.025);
                put("4_31_false", 0.966);
                put("4_30_false", 0.769);
                put("4_29_false", 0.197);
                put("4_28_false", 0.000);
                put("4_27_false", 0.780);
                put("4_26_false", 0.102);
                put("4_25_false", 0.366);
                put("4_24_false", 1.000);
                put("4_23_false", 0.016);
                put("4_22_false", 0.996);
                put("4_21_false", 0.000);
                put("3_38_false", 0.500);
                put("3_37_false", 0.042);
                put("3_36_false", 0.510);
                put("3_35_false", 0.635);
                put("3_34_false", 0.414);
                put("3_33_false", 0.348);
                put("3_32_false", 0.308);
                put("3_31_false", 0.922);
                put("3_30_false", 0.886);
                put("3_29_false", 0.041);
                put("3_28_false", 0.010);
                put("3_27_false", 0.012);
                put("3_26_false", 0.001);
                put("3_25_false", 0.818);
                put("3_24_false", 1.000);
                put("3_23_false", 0.002);
                put("3_22_false", 0.999);
                put("3_21_false", 0.003);
                put("2_38_false", 0.500);
                put("2_37_false", 0.628);
                put("2_36_false", 0.221);
                put("2_35_false", 0.668);
                put("2_34_false", 0.581);
                put("2_33_false", 0.299);
                put("2_32_false", 0.018);
                put("2_31_false", 0.221);
                put("2_30_false", 0.047);
                put("2_29_false", 0.000);
                put("2_28_false", 0.005);
                put("2_27_false", 0.003);
                put("2_26_false", 0.000);
                put("2_25_false", 0.049);
                put("2_24_false", 1.000);
                put("2_23_false", 0.002);
                put("2_22_false", 0.999);
                put("2_21_false", 0.001);
                put("1_38_false", 0.500);
                put("1_37_false", 0.017);
                put("1_36_false", 0.772);
                put("1_35_false", 0.413);
                put("1_34_false", 0.042);
                put("1_33_false", 0.053);
                put("1_32_false", 0.007);
                put("1_31_false", 0.002);
                put("1_30_false", 0.000);
                put("1_29_false", 0.008);
                put("1_28_false", 0.009);
                put("1_27_false", 0.002);
                put("1_26_false", 0.006);
                put("1_25_false", 0.001);
                put("1_24_false", 1.000);
                put("1_23_false", 0.000);
                put("1_22_false", 1.000);
                put("1_21_false", 0.002);
                put("0_38_true", 0.500);
                put("0_38_false", 0.000);
                put("0_37_true", 0.273);
                put("0_37_false", 0.001);
                put("0_36_true", 0.000);
                put("0_36_false", 0.001);
                put("0_35_true", 0.244);
                put("0_35_false", 0.000);
                put("0_34_true", 0.000);
                put("0_34_false", 0.001);
                put("0_33_true", 0.150);
                put("0_33_false", 0.000);
                put("0_32_true", 0.000);
                put("0_32_false", 0.000);
                put("0_31_true", 0.001);
                put("0_31_false", 0.000);
                put("0_30_true", 0.691);
                put("0_30_false", 0.000);
                put("0_29_true", 0.022);
                put("0_29_false", 0.000);
                put("0_28_true", 0.042);
                put("0_28_false", 0.000);
                put("0_27_true", 0.004);
                put("0_27_false", 0.000);
                put("0_26_true", 0.012);
                put("0_26_false", 0.000);
                put("0_25_true", 0.001);
                put("0_25_false", 0.000);
                put("0_24_true", 0.663);
                put("0_24_false", 1.000);
                put("0_23_true", 0.041);
                put("0_23_false", 0.000);
                put("0_22_true", 0.035);
                put("0_22_false", 1.000);
                put("0_21_true", 0.002);
                put("0_21_false", 0.000);

            }
        };

        /*
         * Order of strategy parameters:
         *
         * maxIsolatedSingleDeadwood minIsolatedSingleDiscardTurn maxSingleDeadwood
         * minSingleDiscardTurn minPickupDifference
         */
        generalStrategy = new GeneralStrategy(MyGinRummyUtil.decoded("34466"), knockStrat, drawStrat);

    }

    @Override
    public void startGame(int playerNum, int startingPlayerNum, Card[] cards) {
        this.playerNum = playerNum;

        state = new State(new ArrayList<>(Arrays.asList(cards)));

        oppMelds = null;
        opponentKnocked = false;

    }

    @Override
    public boolean willDrawFaceUpCard(Card card) {
        int card_id = card.getId();
        // If first turn, record the face-up card. All other unseen face-up cards should
        // be recorded in reportDiscard()
        if (state.getTurn() == 0) {
            state.addToSeen(card_id);
            state.increaseTopCard();
            state.decreaseNumRemaining();
        }

        // Card is our face-up
        state.setFaceUp(card_id);

        return willDrawFaceUpCard(state.getHand(), state.getFaceUp());

    }

    /**
     * @param hand    A hand of cards
     * @param card_id The id of the face-up card
     * @return true if we would pick up card_id with the given hand
     */
    public boolean willDrawFaceUpCard(long hand, int card_id) {
        int improvement = MyGinRummyUtil.getImprovement(hand, card_id);
        boolean makesNewMeld = MyGinRummyUtil.makesNewMeld(hand, card_id);

        if (improvement > 0 && makesNewMeld)
            return true;

        ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil
                .cardsToBestMeldSets(GinRummyUtil.bitstringToCards(hand));
        boolean inBestMeld = false;
        if (!bestMeldSets.isEmpty()) {
            ArrayList<ArrayList<Card>> set = bestMeldSets.get(0);
            for (ArrayList<Card> meld : set) {
                if (meld.contains(Card.getCard(card_id))) {
                    inBestMeld = true;
                    break;
                }
            }
        }

        String infoset = improvement + "_" + state.getTopCard() + "_" + inBestMeld;

        if (random.nextDouble() < generalStrategy.getDrawAt(infoset))
            return true;
        else
            return false;

    }

    @Override
    public void reportDraw(int playerNum, Card drawnCard) {

        // If drawn card is null or its id != the face up, player drew face-down.
        // Decrease numRemaining, and add to oppForwent
        if (drawnCard == null || drawnCard.getId() != state.getFaceUp()) {
            state.decreaseNumRemaining();
            state.increaseTopCard();
            if (playerNum != this.playerNum)
                state.addToOppForwent(state.getFaceUp());
        }

        // Ignore other player draws. Add to cards if playerNum is this player.
        if (playerNum == this.playerNum) {

            state.addToHand(drawnCard.getId());
            this.drawn = drawnCard.getId();

        }
        // If the other player drew, and drawnCard isn't null, other player drew
        // face-up.
        else {
            if (drawnCard != null)
                state.addToOppHand(drawnCard.getId());
        }
    }

    @Override
    public Card getDiscard() {
        long potentialDiscards = findDiscard(state.getHand(), state.getFaceUp());
        return MyGinRummyUtil.bitstringToCards(potentialDiscards)
                .get(random.nextInt(MyGinRummyUtil.size(potentialDiscards)));
    }

    /**
     * @param hand    A hand of cards
     * @param face_up The id of the face-up card
     * @return A group of all cards which we would most prefer to discard
     */
    public long findDiscard(long hand, int face_up) {

        /*
         * First, get all cards who's removal would lower our deadwood the most
         */
        long candidateCards = MyGinRummyUtil.findHighestDiscards(hand, drawn, face_up, 0);
        long toRemove = 0L;
        ArrayList<Integer> ids = MyGinRummyUtil.bitstringToIDs(candidateCards);
        ArrayList<Integer> temp = new ArrayList<>(ids);

        for (int i : ids) {
            temp.remove((Integer) i);
            if (!MyGinRummyUtil
                    .contains((MyGinRummyUtil.getIsolatedSingles(hand, MyGinRummyUtil.idsToBitstring(temp), state)), i))
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
                        .contains((MyGinRummyUtil.getSingles(hand, MyGinRummyUtil.idsToBitstring(temp), state)), i))
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
        long not_singles = ~MyGinRummyUtil.getIsolatedSingles(hand, 0L, state);
        candidateCards = MyGinRummyUtil.removeAll(candidateCards, not_singles) == 0L ? candidateCards
                : MyGinRummyUtil.removeAll(candidateCards, not_singles);

        if (MyGinRummyUtil.removeAll(candidateCards, not_singles) == 0L) {
            not_singles = ~MyGinRummyUtil.getSingles(hand, 0L, state);
            candidateCards = MyGinRummyUtil.removeAll(candidateCards, not_singles) == 0L ? candidateCards
                    : MyGinRummyUtil.removeAll(candidateCards, not_singles);
        }

        /*
         * Then, filter out cards which would be helpful to the opponent
         */
        if (MyGinRummyUtil.size(candidateCards) > 1) {
            toRemove = 0L; // Don't remove until after loop
            long preferred = 0L; // Cards we would prefer to remove

            for (Card c : MyGinRummyUtil.bitstringToCards(candidateCards)) {

                /*
                 * If a card could be used in an opp meld, or at least bring them closer, avoid
                 * discarding it
                 */
                if (MyGinRummyUtil.canOpponentMeld(c, state))
                    toRemove = MyGinRummyUtil.add(toRemove, c.getId()); // If card could help opp meld, avoid tossing
                else if (MyGinRummyUtil.containsRank(state.getOppHand(), c.getId())
                        || MyGinRummyUtil.containsSuit(state.getOppHand(), c.getId(), 2))
                    toRemove = MyGinRummyUtil.add(toRemove, c.getId()); // If card brings opp closer to a meld, avoid
                                                                        // tossing

                /*
                 * If the opp has discarded cards that could be melded with this card, it is
                 * less likely they would find it useful. Prefer to discard any of these cards.
                 */
                else if (MyGinRummyUtil.containsRank(state.getOppDiscard(), c.getId())
                        || MyGinRummyUtil.containsSuit(state.getOppDiscard(), c.getId(), 2))
                    preferred = MyGinRummyUtil.add(preferred, c.getId()); // If similar cards have been tossed, prefer
                else if (MyGinRummyUtil.containsRank(state.getOppForwent(), c.getId())
                        || MyGinRummyUtil.containsSuit(state.getOppForwent(), c.getId(), 1))
                    preferred = MyGinRummyUtil.add(preferred, c.getId());
                ;
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

        return candidateCards;

    }

    @Override
    public void reportDiscard(int playerNum, Card discardedCard) {
        // Ignore other player discards. Remove from cards if playerNum is this player.
        if (playerNum == this.playerNum) {
            state.removeFromHand(discardedCard.getId());
            state.nextTurn();
        }

        // If we knew the discarded card was in the opponent's hand, remove. If we
        // didn't, add it to seen.
        else {
            state.addToSeen(discardedCard.getId());
            state.removeFromOppHand(discardedCard.getId());
        }
    }

    @Override
    public ArrayList<ArrayList<Card>> getFinalMelds() {

        /*
         * TODO: Consider the deadwood of opponent discards in figuring out whether they're preparing to knock or not.
         *  If they are consistently tossing low deadwood cards, they could be preparing to knock. Maybe.
         */

        ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets;
        int deadwood;

        bestMeldSets = MyGinRummyUtil.cardsToBestMeldSets(MyGinRummyUtil.bitstringToCards(state.getHand()));
        deadwood = bestMeldSets.isEmpty()
                ? MyGinRummyUtil.getDeadwoodPoints(MyGinRummyUtil.bitstringToCards(state.getHand()))
                : MyGinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0),
                        MyGinRummyUtil.bitstringToCards(state.getHand()));

        // Check if deadwood of maximal meld is low enough to go out.

        if (!opponentKnocked && (bestMeldSets.isEmpty() || deadwood > MyGinRummyUtil.MAX_DEADWOOD))
            return null;
        else if (!opponentKnocked) {

            String k = deadwood + "_" + state.getTopCard();

            //If we have gin, or if we get a random value less than our probability to knock at infoset k, knock.
            if (deadwood == 0 || random.nextDouble() < generalStrategy.getKnockAt(k)) {

                //Select the meld configuration to submit.
                ArrayList<ArrayList<Card>> bestMeldSet = null;
                 double minExpectedLayoff = Double.MAX_VALUE;
                 for(ArrayList<ArrayList<Card>> meldSet : bestMeldSets) {
                     ArrayList<Card> layoff = new ArrayList<>();

                     // Add all cards to layoff who could be inserted into our hand
                     for (ArrayList<Card> meld : meldSet) {
                         // Meld of cards of same rank
                         if (meld.get(0).getRank() == meld.get(1).getRank()) {
                             layoff.addAll(
                                     MyGinRummyUtil.getSameRank(MyGinRummyUtil.bitstringToCards(
                                             MyGinRummyUtil.addAll(state.getOppHand(), state.getUnaccounted())), meld.get(0)));
                         }

                         // Cards of same suit
                         else {
                             layoff.addAll(MyGinRummyUtil.getSameSuit(MyGinRummyUtil.bitstringToCards(
                                     MyGinRummyUtil.addAll(state.getOppHand(), state.getUnaccounted())),
                                     meld.get(0), 1));
                             layoff.addAll(MyGinRummyUtil.getSameSuit(MyGinRummyUtil.bitstringToCards(
                                     MyGinRummyUtil.addAll(state.getOppHand(), state.getUnaccounted())),
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
                         if(MyGinRummyUtil.canOpponentMeld(card, state)) continue;
                         expectedLayoff += GinRummyUtil.getDeadwoodPoints(card) *
                                 MyGinRummyUtil.getProbabilityThatOpponentHasUnseenCard(card, state);
                     }

                     if(expectedLayoff < minExpectedLayoff) {
                         minExpectedLayoff = expectedLayoff;
                         bestMeldSet = meldSet;
                     }

                 }

                 return bestMeldSet;

            }
            else
                return null;
        }

        else {
            ArrayList<Card> layoff = new ArrayList<>();

            if (bestMeldSets.isEmpty())
                return new ArrayList<>();

            // Add all cards to layoff who could be inserted into opponent hand
            for (ArrayList<Card> meld : oppMelds) {
                // Meld of cards of same rank
                if (meld.get(0).getRank() == meld.get(1).getRank()) {
                    layoff.addAll(
                            MyGinRummyUtil.getSameRank(MyGinRummyUtil.bitstringToCards(state.getHand()), meld.get(0)));
                }

                // Cards of same suit
                else {
                    layoff.addAll(MyGinRummyUtil.getSameSuit(MyGinRummyUtil.bitstringToCards(state.getHand()),
                            meld.get(0), 1));
                    layoff.addAll(MyGinRummyUtil.getSameSuit(MyGinRummyUtil.bitstringToCards(state.getHand()),
                            meld.get(meld.size() - 1), 1));
                }
            }

            /*
             * Deadwood cards will be laid off no matter what, so check potential layoffs in
             * melds to see if a better config is available.
             */

            ArrayList<Card> temp;
            ArrayList<ArrayList<Card>> bestMeldSet = bestMeldSets.get(0);
            int minDeadwood = deadwood;

            if (layoff.isEmpty())
                return bestMeldSet;

            // Go through EVERY permutation of potential layoffs to find the one that leaves
            // the best deadwood
            for (int i = 0; i < Math.pow(2, layoff.size()); i++) {
                String bString = Integer.toBinaryString(i);
                temp = MyGinRummyUtil.bitstringToCards(state.getHand());

                for (int j = 0; j < bString.length(); j++) {
                    if (bString.charAt(bString.length() - 1 - j) == '1') {
                        temp.remove(layoff.get(j));
                    }
                }

                ArrayList<ArrayList<ArrayList<Card>>> meldSets = MyGinRummyUtil.cardsToBestMeldSets(temp);

                if (meldSets.isEmpty()) {
                    if (MyGinRummyUtil.getDeadwoodPoints(temp) < minDeadwood) {
                        minDeadwood = MyGinRummyUtil.getDeadwoodPoints(temp);
                        bestMeldSet = new ArrayList<>();
                    }
                }

                else {
                    if (MyGinRummyUtil.getDeadwoodPoints(meldSets.get(0), temp) < minDeadwood) {
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
        // Melds ignored by simple player, but could affect which melds to make for
        // complex player.
        if (playerNum != this.playerNum) {
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
}
