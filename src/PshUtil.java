import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * A set of utility functions to help analyze game state and make decisions
 * 
 * @author jjb24
 */
public class PshUtil {
	final static long DECK = 0xF_FFFF_FFFF_FFFFL;
	
	/**
	 * List of lists of meld bitstrings.  Melds appearing after melds in lists are supersets, so the 
	 * first meld not made in a list makes further checking in that list unnecessary.
	 */
	private static ArrayList<Long> meldBitstrings = new ArrayList<>();
	private static HashMap<Long, Card> bitStringToCards = new HashMap<>();
	private static HashMap<Long, Integer> cardsToDeadwoodPoints = new HashMap<>();
	private static Random prng = new Random();
	
	static {
		// initialize meldBitStrings
		for (int suit = 0; suit < 4; suit++) {
			long run = 7L;
			run <<= (suit * 13);
			for (int rank = 0; rank <= 10; rank++) {
				meldBitstrings.add(run);
				run <<= 1;
			}
		}
		for (int suit = 0; suit < 4; suit++) {
			long set = 1L | (1L << 13) | (1L << 13*2) | (1L << 13*3);
			set = set ^ (1L << 13 * suit);
			for (int rank = 0; rank < 13; rank++) {
				meldBitstrings.add(set);
				set <<= 1;
			}
		}
		
		// Initialize cardsToDeadwoodPoints
		for (int i = 0; i < 52; i++) {
			Card c = Card.getCard(i);
			cardsToDeadwoodPoints.put(cardAsBitString(c), GinRummyUtil.getDeadwoodPoints(c));
		}

		// Initialize cardsToDeadwoodPoints
		for (int i = 0; i < 52; i++) {
			bitStringToCards.put(cardAsBitString(i), Card.getCard(i));
		}
	}

	
	/**
	 * Determine if the face up card would become part of a meld
	 *  
	 * @param state the current state
	 * 
	 * @return true if the face up card would be a part of a meld
	 */
	public static boolean doesFaceUpCardMakeNewMeld(GameState state) {
		ArrayList<Card> myCards = state.getCurrentPlayerCardsAsList();
		
		Card card = state.getFaceUpCardAsObject();
		myCards.add(card);

		for (ArrayList<Card> meld : GinRummyUtil.cardsToAllMelds(myCards)) {
			if (meld.contains(card)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Determine best deadwood if take face up card
	 *  
	 * @param state the current state
	 * 
	 * @return the minimum deadwood possible after drawing this card and discarding one
	 */
	public static int getDeadwoodImprovementIfDrawFaceUpCard(GameState state) {
		ArrayList<Card> myCards = state.getCurrentPlayerCardsAsList();
		
		int currentDeadwood = PshUtil.getBestDeadwood(myCards);

		myCards.add(state.getFaceUpCardAsObject());
		
		int newDeadwood = getBestDeadwoodAfterDiscard(myCards);

		return currentDeadwood - newDeadwood;
	}

	/**
	 * Find a card in my hand that cannot make a meld for the opponent.  If there is
	 * more than one card, return the one that has the highest point value.
	 *
	 * @param cards a subset of cards that you are considering discarding
	 * @param state the current game state
	 * 
	 * @return a card within cards that cannot be made into a meld by the opponent
	 */
	public static long getDeadCard(long cards, GameState state) {
		long seenCards = state.getCurrentPlayerSeenCards();
		long oppHand = state.getCurrentPlayerKnownOpponentCards();
		
		if ((cards & seenCards) != cards) throw new RuntimeException("All cards in my hand should have been marked as seen.");
		if ((oppHand & seenCards) != oppHand) throw new RuntimeException("All cards in opponents hand should have been marked as seen.");

		long deadCard = 0;
		int points = 0;
		
		long availableCards = DECK ^ (seenCards ^ oppHand) ;
		
		long card = 1;
		for (int i = 0; i < 52; i++) {
			if ((card & cards) != 0) {
				boolean dead = true;
				for (Long meld : meldBitstrings) {
					if (((availableCards | card) & meld) == meld) {
						dead = false;
						break;
					}
				}
				if (dead) {
					Card c = Card.getCard(i);
					if (deadCard == 0 || GinRummyUtil.getDeadwoodPoints(c) > points) {
						deadCard = card;
						points = GinRummyUtil.getDeadwoodPoints(c);
						if (GameNode.DEBUG) System.out.println("Found dead card " + c);
					}
				}
			}
			card <<= 1;
		}
		return deadCard;
	}

	/** 
	 * Return cards that cannot be a part of any meld even after drawing another card,
	 * with the constraint that cards must have at least minPointValue deadwood points
	 * AND card is not the Face Up Card
	 * 
	 * @param state the current game state
	 * @param minPointValue the minimum point value that a card must have to be considered
	 * 
	 * @return the bitstring of the unmatchable cards in player's hand
	 */
	public static long getUnmatchableCards(GameState state, int minPointValue) {				
		// Find cards in hand with the minimum number of deadwood points
		long unmatchableCards = 0L;
		
		for (int i = 0; i < 52; i++) {
			long card = cardAsBitString(i);
			if ((card & state.getCurrentPlayerCards()) != 0 && card != state.getFaceUpCard()) {
				Card c = Card.getCard(i);
			    if (GinRummyUtil.getDeadwoodPoints(c) >= minPointValue) {
			    	unmatchableCards |= (1L << i);
			    }
			}
		}	
		if (unmatchableCards == 0L) {
			if (GameNode.DEBUG) System.out.println("No cards with sufficient points selected for matchability.");
			return 0L;
		}
		
		// See which of these cards is unmatchable
		for (long meld: meldBitstrings) {
			if ((meld & unmatchableCards) != 0) {
				long partial = meld & unmatchableCards;
				long neededCards = meld & ~partial & ~state.getCurrentPlayerCards();

				if (neededCards == 0) {
					unmatchableCards = unmatchableCards & ~meld;					
				}
				else if (getSetBits(neededCards) == 1 && (neededCards & ~state.getCurrentPlayerSeenCards()) == neededCards) {
					// There is an unseen card that makes a meld with this
					unmatchableCards = unmatchableCards & ~meld;
				}
			}
			if (unmatchableCards == 0L) {
				return 0L;
			}
		}

		return unmatchableCards;
	}


	/**
	 * Return the card with the biggest penalty from a collection
	 * 
	 * @param discards the collection of cards as a bitstring
	 * 
	 * @return the card (as a bitstring) with the highest deadwood points
	 */
	public static long getCardWithBiggestPenalty(long discards) {
		if (discards == 0) throw new IllegalArgumentException("discards cannot be empty");
		long cardToReturn = 0;
		int points = 0;
		
		long card = 1;
		for (int i = 0; i < 52; i++) {
			if ((card & discards) != 0) {
				Card c = Card.getCard(i);
				if (cardToReturn == 0 || GinRummyUtil.getDeadwoodPoints(c) > points) {
					cardToReturn = card;
					points = GinRummyUtil.getDeadwoodPoints(c);
				}
			}
			card <<= 1;
		}
		return cardToReturn;
	}


	/**
	 * Find a discard by looking for a card that would minimize the deadwood
	 * 
	 * @param state the current game state
	 * 
	 * @return the card to discard as a bitstring
	 */
	public static long getCardThatMinimizesDeadwood(GameState state) {
		ArrayList<Card> bestDiscards = new ArrayList<>();
		ArrayList<Card> myCards = state.getCurrentPlayerCardsAsList();

		if (myCards.size() != 11) throw new IllegalArgumentException("Must pass a hand with a card needing to be discarded.");

		int bestDeadwood = Integer.MAX_VALUE;
		for (int i = 0; i < myCards.size(); i++) {
			if (cardAsBitString(myCards.get(i)) != state.getFaceUpCard()) {
				Card potentialDiscard = myCards.get(i);

				// Remove discard from hand and evaluate
				myCards.set(i, myCards.get(10));
				myCards.remove(10);

				int deadwood = getBestDeadwood(myCards);

				if (deadwood < bestDeadwood) {
					if (bestDiscards.size() != 0) {
						bestDiscards.clear();
					}
					bestDiscards.add(potentialDiscard);
					bestDeadwood = deadwood;
				}
				else if (deadwood == bestDeadwood) {
					bestDiscards.add(potentialDiscard);					
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
		}

		return cardAsBitString(bestDiscards.get(prng.nextInt(bestDiscards.size())));
	}

	/**
	 * Find the minimum deadwood after a discard
	 * 
	 * @param hand a list of cards in the hand
	 * 
	 * @return the minimum possible deadwood after a discard
	 */
	public static int getBestDeadwoodAfterDiscard(ArrayList<Card> hand) {
		if (hand.size() != 11) throw new IllegalArgumentException("Must pass a hand with a card needing to be discarded.");

		int bestDeadwood = Integer.MAX_VALUE;
		for (int i = 0; i < hand.size(); i++) {
			Card potentialDiscard = hand.get(i);

			// Remove discard from hand and evaluate
			hand.set(i, hand.get(10));
			hand.remove(10);

			int deadwood = getBestDeadwood(hand);

			if (deadwood < bestDeadwood) {
				bestDeadwood = deadwood;
			}

			// Put discard back in hand
			if (i == 10) {
				hand.add(potentialDiscard);
			}
			else {
				hand.add(hand.get(i));
				hand.set(i, potentialDiscard);
			}
		}

		return bestDeadwood;
	}

	
	/**
	 * Return deadwood points after making best melds
	 *
	 * @param myCards the hand from which to make melds
	 * 
	 * @return the minimum deadwood points
	 */
	public static int getBestDeadwood(ArrayList<Card> myCards) {
		if (myCards.size() != 10) throw new IllegalArgumentException("Expected a hand with 10 cards, received one with " + myCards.size() +" instead");
		ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(myCards);
		return bestMeldSets.isEmpty() ? GinRummyUtil.getDeadwoodPoints(myCards) : GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), myCards);
	}
	
	/**
	 * Return a bitstring representation of this card

	 * @param card the card
	 * @return a bitstring with the 1 set in bit position equal the card's id
	 */
	public static long cardAsBitString(Card card) {
		return 1L << card.getId();
	}

	/**
	 * Return a bitstring representation of this card

	 * @param id the card's id
	 * @return a bitstring with the 1 set in bit position equal the card's id
	 */
	public static long cardAsBitString(int id) {
		return 1L << id;
	}

	/**
	 * Convert card bitstring to Card object
	 * 
	 * @param card bitstring for card
	 * @return corresponding Card object
	 */
	public static Card bitStringToCard(long card) {
		if (!bitStringToCards.containsKey(card)) throw new IllegalArgumentException("Expected a card as a bitstring, received Ox" + Long.toString(card,16) +" instead.");
		return bitStringToCards.get(card);
	}


	/**
	 * Return best melds for a player
	 * 
	 * @param state the current game state
	 * @param player the player
	 * @return the melds
	 */
	public static ArrayList<ArrayList<Card>> getFinalMelds(GameState state, int player) {
		ArrayList<Card> myCards = state.getPlayerCardsAsList(player);
		ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(myCards);

		// Opponent has knocked, but I cannot go out
		if (bestMeldSets.isEmpty()) {
			return new ArrayList<ArrayList<Card>>(); 
		}
		else {
			// Give best melds because opponent has gone out, or I have decided to
			return bestMeldSets.get(prng.nextInt(bestMeldSets.size()));
		}
	}
	
    /**
     * Efficiently Count # of 1's in a bitstring
     * 
     * @param bitString the bitstring
     * @return the number of set bits (i.e. bits set to 1) in the bitstring
     */
	public static int getSetBits(long bitString) {
		// Using Kernighan�s Algorithm for counting set bits 
		int count = 0;
		while (bitString != 0) {
			bitString = bitString & (bitString - 1); // Unset the least significant bit with a 1
			count++;
		}
		return count;		
	}
}
	
