import java.util.ArrayList;
import java.util.Random;

/**
 * The state of a game at a point in time
 * 
 * @author jjb24
 */
public class GameState {
	private static final int HAND_SIZE = 10;
	private static final Random prng = new Random();
	
	private static class Player {
		long cards = 0; // Cards in our hand
		long seenCards = 0; // Cards that have been seen
		long oppCards = 0; // Cards we know opponent holds
		
		public Player() {}
		public Player(Player other) {
			cards = other.cards;
			seenCards = other.seenCards;
			oppCards = other.oppCards;
		}
	}
	
	private Player[] players;
	private long[] deck; 
	private int topCard = 0; // index into deck for top card
	private long faceUpCard; // Top of discard pile
	private int decisionPoint = 0;
	private GameState previous;

	/**
	 * Create a new randomized root for the game tree
	 */
	public GameState() {
		deck = new long[52];
		for (int i = 0; i < 52; i++)
			deck[i] = 1L<<i;
		
		// Shuffle
		for (int i = 0; i < 52; i++) {
			int j = prng.nextInt(52);
			long tmp = deck[i];
			deck[i] = deck[j];
			deck[j] = tmp;
		}
		
		players = new Player[]{new Player(), new Player()};
		for (int i = 0; i < 2 * HAND_SIZE; i++) {
			players[i%2].cards |= deck[topCard];
			players[i%2].seenCards |= deck[topCard++];
		}

		faceUpCard = deck[topCard++];
		players[0].seenCards |= faceUpCard;
		players[1].seenCards |= faceUpCard;		
	}

	/**
	 * Create a child of a game tree node
	 * 
	 * @param lastState the parent node in the game tree
	 */
	public GameState(GameState lastState) {
		topCard = lastState.topCard;
		faceUpCard = lastState.faceUpCard;
		players = new Player[]{new Player(lastState.players[0]), new Player(lastState.players[1])};
		this.decisionPoint = lastState.decisionPoint + 1;
		deck = lastState.deck;
		previous = lastState;
	}

	/**
	 * Create a child of a game tree node with flexibility in setting the decision point.
	 * Note that this will normally only be called on the first turn, where both players
	 * may be given the chance to draw the first face up card.
	 * 
	 * @param lastState the parent node in the game tree
	 * @param decisionPoint the decision point index for this node
	 */
	public GameState(GameState lastState, int decisionPoint) {
		topCard = lastState.topCard;
		faceUpCard = lastState.faceUpCard;
		players = new Player[]{new Player(lastState.players[0]), new Player(lastState.players[1])};
		this.decisionPoint = decisionPoint;
		deck = lastState.deck;
		previous = lastState;
	}
	
	/**
	 * @return the player number for the current player (either 0 or 1)
	 */
	public int getCurrentPlayer() {
		return (decisionPoint / 3) % 2;
	}

	/**
	 * Get the game decision point, defined as follows:
	 *   decisionPoint %6 == 0 player 0 draw decision 
	 *   decisionPoint %6 == 1 player 0 discard decision 
	 *   decisionPoint %6 == 2 player 0 knock decision 
	 *   decisionPoint %6 == 3 player 1 draw decision 
	 *   decisionPoint %6 == 4 player 1 discard decision 
	 *   decisionPoint %6 == 5 player 1 knock decision 
	 *   
	 *   Note that decision points < 6 have a special meaning since
	 *   player 1 gets an opportunity to pick up the first face up card
	 *   if player 0 refuses it.
	 *   
	 * @return the decision point
	 */
	public int getDecisionPoint() {
		return decisionPoint;
	}
	
	/**
	 * 
	 * @return the index of the card into the deck.  When this index reaches 50, the game is done
	 */
	public int getTopCard() {
		return topCard;
	}
	
	/**
	 * 
	 * @return a bit string with the cards in the current player's hand
	 */
	public long getCurrentPlayerCards() {
		return players[getCurrentPlayer()].cards;
	}
	
	/**
	 * @param player the player of interest (0 or 1)
	 * 
	 * @return a bit string with the cards in the player's hand
	 */
	public long getPlayerCards(int player) {
		return players[player].cards;		
	}
	
	/**
	 * 
	 * @param player the player number (0 or 1)
	 * @return an array list with the cards in player's hand
	 */
	public ArrayList<Card> getPlayerCardsAsList(int player) {
		return GinRummyUtil.bitstringToCards(players[player].cards);
	}

	/**
	 * 
	 * @return an array list with the cards in the current player's hand
	 */
	public ArrayList<Card> getCurrentPlayerCardsAsList() {
		return GinRummyUtil.bitstringToCards(players[getCurrentPlayer()].cards);
	}
	
	/**
	 * @return the cards that the current player has seen as a bitstring
	 */
	public long getCurrentPlayerSeenCards() {
		return players[getCurrentPlayer()].seenCards;
	}

	/**
	 * @return the cards that the current player knows the opponent is holding
	 */
	public long getCurrentPlayerKnownOpponentCards() {
		return players[getCurrentPlayer()].oppCards;
	}

	/**
	 * Add the currently face up card to the current players hand.  Note that 
	 * the opponent will know that this card is in the current players hand.	
	 */
	public void addFaceUpCardToHand() {
		int player = getCurrentPlayer();
		int opponent = player==0?1:0;
		players[player].cards |= faceUpCard;
		players[player].seenCards |= faceUpCard;
		players[opponent].oppCards |= faceUpCard; 
	}

	/**
	 * Add a face down card to the current players hand.	
	 */
	public void addFaceDownCardToHand() {
		long card = deck[topCard];
		topCard += 1;
		players[getCurrentPlayer()].cards |= card;
		players[getCurrentPlayer()].seenCards |= card;
	}

	/**
	 * Choose a card to discard
	 *
	 * @param card the card to discard as a bitstring
	 */
	public void discardCard(long card) {
		int player = getCurrentPlayer();
		int opponent = player==0?1:0;
				
		players[player].cards ^= card;
		faceUpCard = card;
		players[opponent].seenCards |= card;
		players[opponent].oppCards &= ~card;
	}
	
	/**
	 * 
	 * @return the currently face up card in the discard pile
	 */
	public Card getFaceUpCardAsObject() {
		return PshUtil.bitStringToCard(faceUpCard);
	}

	/**
	 * 
	 * @return the currently face up card in the discard pile as a bitstring
	 */
	public long getFaceUpCard() {
		return faceUpCard;
	}

	/**
	 * Get the previous game state in the game tree
	 * 
	 * @return the parent state
	 */
	public GameState getPreviousState() {
		return previous;
	}
	
	@Override 
	public String toString() {
		StringBuffer sb = new StringBuffer("\nGAME STATE\n");
		for (int i = 0; i < 2; i++) {
			sb.append("Player " + i + "\n");
			sb.append("Hand:");
			ArrayList<Card> cards = GinRummyUtil.bitstringToCards(players[i].cards);
			for (Card card : cards) {
				sb.append(" " + card.toString());
			}

			sb.append("\nSeen Cards:");
			cards = GinRummyUtil.bitstringToCards(players[i].seenCards);
			for (Card card : cards) {
				sb.append(" " + card.toString());
			}
			
			sb.append("\nKnown Opponent Cards:");
			cards = GinRummyUtil.bitstringToCards(players[i].oppCards);
			for (Card card : cards) {
				sb.append(" " + card.toString());
			}
		}
		sb.append("Top card index: " + topCard);

		sb.append("\nFace Up card:");
		if (faceUpCard == 0) {
			sb.append(" None");				
		}
		else {
			sb.append(" " + PshUtil.bitStringToCard(faceUpCard).toString());				
		}

		sb.append("\nDecision Point: " + decisionPoint);
		
		return sb.toString();
	}
}
