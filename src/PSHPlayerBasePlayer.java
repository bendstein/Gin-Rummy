
import java.util.ArrayList;
import java.util.Random;

/**
 * A simple player using the following strategies:
 * - If I do not have gin, but have strategy.minNoKnock points or less, I will wait to try
 *   to get gin, or undercut opponent
 * - If there are singleton cards (i.e. cards that I need two draw cards to put in a meld),
 *   I will discard them unless their value is less than or equal to strategy.maximumSingletonPointValue.
 * - If there are no singleton cards, I discard an unmelded card.
 * - If there are multiple potential discards, I discard a card that I know won't help opponent first, if 
 *   none exists, I discard the highest point value card.
 * - When deciding to take a face up card I take it if it would make a new meld OR if it has a point value that
 *   is at least strategy.minDropToTakeFaceUp points less than my highest singleton card
 */
public class PSHPlayerBasePlayer implements GinRummyPlayer {

    private int playerNum;

    private Random random = new Random();

    private boolean opponentKnocked = false;

    // Tunable parameters for this player
    private Strategy strategy;

    // The game state objects
    private PSHStateGame gameState = new PSHStateGame();

    // Do I report verbose information
    private boolean verbose;


    public PSHPlayerBasePlayer(boolean verbose, Strategy strategy) {
        this.verbose = verbose;
        this.strategy = strategy;
    }

    public PSHPlayerBasePlayer(boolean verbose) {
        this(verbose, new Strategy(6,6,3));
    }

    public PSHPlayerBasePlayer() {
        this(false, new Strategy(6,6,3));
    }


    @Override
    public void startGame(int playerNum, int startingPlayerNum, Card[] cards) {
        this.playerNum = playerNum;

        // Reset game state
        gameState.reset();

        opponentKnocked = false;

        for (Card card : cards) {
            gameState.reportPickup(card, true, true);
        }
    }

    @Override
    public boolean willDrawFaceUpCard(Card card) {
        if (verbose) System.out.println("willDrawFaceUpCard (card = " + card  + ")");

        gameState.reportFaceUpCard(card);

        if (gameState.doesCardMakeNewMeld(card)) {
            return true;
        }
        else {
            ArrayList<Card> potentialDiscards = gameState.getMyUnmatchableCards(strategy.maximumSingletonPointValue + 1, -1);
            int minDrop = 0;
            for (Card discard: potentialDiscards) {
                minDrop = Math.max(minDrop, GinRummyUtil.getDeadwoodPoints(discard) - GinRummyUtil.getDeadwoodPoints(card));
            }
            return minDrop >= strategy.minDropToTakeFaceUp;
        }
    }

    @Override
    public void reportDraw(int playerNum, Card drawnCard) {
        if (verbose) System.out.println("reportDraw ( playerNum = " + playerNum + ", card = " + drawnCard + ")");

        gameState.reportPickup(drawnCard, playerNum == this.playerNum, false);
    }

    @Override
    public Card getDiscard() {
        if (verbose) {
            System.out.println("getDiscard()");
        }
        return getPreferredDiscard(gameState.lastFaceUpCard().getId());
    }

    @Override
    public void reportDiscard(int playerNum, Card discardedCard) {
        if (verbose) System.out.println("reportDiscard( playerNum = " + playerNum + ", discardedCard = " + discardedCard + ")");
        gameState.reportDiscard(discardedCard, playerNum == this.playerNum);
    }

    @Override
    public ArrayList<ArrayList<Card>> getFinalMelds() {
        ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(gameState.getCards(true));

        // I cannot go out, and opponent has not knocked
        if (!opponentKnocked && (bestMeldSets.isEmpty() || GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), gameState.getCards(true)) > GinRummyUtil.MAX_DEADWOOD))
            return null;

        // I can go out, but I will try to undercut opponent or get gin
        if (!opponentKnocked && (bestMeldSets.isEmpty() ||
                (GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), gameState.getCards(true)) != 0 &&
                        GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), gameState.getCards(true)) != 7 &&
                        GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), gameState.getCards(true)) != 9)))  {
            return null;
        }

        // Opponent has knocked, but I cannot go out
        if (bestMeldSets.isEmpty()) {
            return new ArrayList<ArrayList<Card>>();
        }
        else {
            // Give best melds because opponent has gone out, or I have decided to
            return bestMeldSets.get(random.nextInt(bestMeldSets.size()));
        }
    }

    @Override
    public void reportFinalMelds(int playerNum, ArrayList<ArrayList<Card>> melds) {
        // Melds ignored by simple player, but could affect which melds to make for complex player.
        if (playerNum != this.playerNum) {
            opponentKnocked = true;
        }
    }

    @Override
    public void reportScores(int[] scores) { }

    @Override
    public void reportLayoff(int playerNum, Card layoffCard, ArrayList<Card> opponentMeld) {}

    @Override
    public void reportFinalHand(int playerNum, ArrayList<Card> hand) {}


    /**
     * Identify collection of potential discards
     *
     * @param tabooId a card ID for a card that cannot be discarded because it was
     *                just drawn face up
     * @return an arraylist of potential discards
     */
    private ArrayList<Card> getPreferredDiscards(int tabooId) {
        ArrayList<Card> potentialDiscards;  // Best cards to discard

        // If there are cards that cannot be made into a meld regardless of the
        // next pickup, and they have sufficient point value get rid of them
        potentialDiscards = gameState.getMyUnmatchableCards(strategy.maximumSingletonPointValue + 1, tabooId);
        if (potentialDiscards.size() > 0) return potentialDiscards;

        // Look for best discard for this turn
        gameState.getMyBestDeadwood(tabooId,potentialDiscards);
        return potentialDiscards;
    }

    /**
     * Get best discard from a collection of discards
     * @param tabooId the Id of a face up card that was drawn, and cannot be discarded
     * @return the best card to discard
     */
    private Card getPreferredDiscard(int tabooId) {
        ArrayList<Card> potentialDiscards = getPreferredDiscards(tabooId);

        if (potentialDiscards.size() == 0) {
            throw new RuntimeException("Error finding card to discard");
        }

        // Discard cards that can't help opponent first
        Card discard = null;
        for (Card card: potentialDiscards) {
            if (gameState.probabilityThatCardIsNotUseful(card) == 1.0 &&
                    (discard == null || GinRummyUtil.getDeadwoodPoints(discard) < GinRummyUtil.getDeadwoodPoints(card)))
                discard = card;
        }
        if (discard != null) return discard;

        // Choose highest value card
        for (Card card: potentialDiscards) {
            if ((discard == null || GinRummyUtil.getDeadwoodPoints(discard) < GinRummyUtil.getDeadwoodPoints(card)))
                discard = card;
        }
        return discard;
    }


    /**
     * Tunable parameters for this player
     *
     */
    public static class Strategy {
        private int maximumSingletonPointValue; // Maximum value for which a card that cannot be matched should be kept

        // If I have a singleton card, and picking up the face up card would result in a drop of points
        // of at least minDropToTakeFaceUp, I will pick up the face up card
        private int minDropToTakeFaceUp;

        // If I have more than 0 but less than minNoKnock points, do not knock
        // in an attempt to undercut opponent or get Gin
        private int minNoKnock;

        Strategy(int maximumSingletonPointValue, int minDropToTakeFaceUp, int minNoKnock) {
            this.maximumSingletonPointValue = maximumSingletonPointValue;
            this.minDropToTakeFaceUp = minDropToTakeFaceUp;
            this.minNoKnock = minNoKnock;
        }
    }


}