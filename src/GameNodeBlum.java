import java.util.ArrayList;

public class GameNodeBlum {
	public static boolean DEBUG = false;
	
	public static double playFrom(GameState state, Player[] players, double probP0, double probP1) {
		
		if (DEBUG) {
			System.out.println(state.toString());
		}
		
		if (state.getTopCard() >= 50) {
			if (DEBUG)
				System.out.println("The draw pile was reduced to two cards without knocking, so the hand is cancelled.");
			return 0;
		}

		double util = 0.0;
		if (state.getDecisionPoint()%3 == 0) {
			util = drawCard(state, players, probP0, probP1);
		}
		else if (state.getDecisionPoint()%3 == 1) {
			util = discardCard(state, players, probP0, probP1);
		}
		else {
			util = knock(state, players, probP0, probP1);
		}
		if (DEBUG) System.out.println("Utility " + util);

		return util;
	}

	/**
	 * Handle the decision point where player is drawing a card
	 *
     * @param state the current game state
     * @param players an array with the two players
     * @param probP0 the probability that player 0 would play to this state
     * @param probP1 the probability that player 1 would play to this state
	 *
	 * @return the expected utility of following the player's action profile at this decision point
	 */
	private static double drawCard(GameState state, Player[] players, double probP0, double probP1) {
		int player = state.getCurrentPlayer();

		ActionDraw[] actions = players[player].getDrawStrategy(state);
		double[] util = new double[actions.length];

		for (int i = 0; i < actions.length; i++) {
			if (actions[i].isDraw()) {
				if (DEBUG) System.out.println("Drawing face up card");

				GameState nextState = new GameState(state);
				nextState.addFaceUpCardToHand();

				// Note - don't flip utility values!
				util[i] = player == 0
		                ? playFrom(nextState, players, probP0 * actions[i].p, probP1)
		                : playFrom(nextState, players, probP0, probP1 * actions[i].p);
			}
			else { // No draw
				if (state.getDecisionPoint() == 0 || state.getDecisionPoint() == 3) {
					if (DEBUG) System.out.println("Round 1 skipping draw, so skipping turn");
					// For first face up card, we don't automatically pickup a card if we decline it
					GameState nextState = new GameState(state, state.getDecisionPoint() + 3);
		            util[i] = player == 0
		                    ? - playFrom(nextState, players, probP0 * actions[i].p, probP1)
		                    : - playFrom(nextState, players, probP0, probP1 * actions[i].p);
				}
				else {
					GameState nextState = new GameState(state);
					nextState.addFaceDownCardToHand();

					util[i] = player == 0
		                    ? playFrom(nextState, players, probP0 * actions[i].p, probP1)
		                    : playFrom(nextState, players, probP0, probP1 * actions[i].p);
				}
			}
		}

		return players[player].getDrawStrategy().postProcess(actions, util, player==0?probP0:probP1, player==0?probP1:probP0);
	}

    /**
     * Handle the decision point where a player is choosing a card to discard
     *
     * @param state the current game state
     * @param probP0 the probability that player 0 would play to this state
     * @param probP1 the probability that player 1 would play to this state
     *
     * @return the expected utility of following the player's action profile at this decision point
     */
	private static double discardCard(GameState state, Player[] players, double probP0, double probP1) {
		int player = state.getCurrentPlayer();

		ActionDiscard[] discards = players[player].getDiscardStrategy(state);
		double[] util = new double[discards.length];

		for (int i = 0; i < discards.length; i++) {
			ActionDiscard discard = discards[i];
			if (DEBUG) System.out.println("Discarding " + discards[i].getCardAsObject().toString());
			GameState nextState = new GameState(state);
			nextState.discardCard(discards[i].getCard());

	        util[i] = player == 0
	                ? playFrom(nextState, players, probP0 * discard.p, probP1)
	                : playFrom(nextState, players, probP0, probP1 * discard.p);
		}

		return players[player].getDiscardStrategy().postProcess(discards, util, player==0?probP0:probP1, player==0?probP1:probP0);
	}

    /**
     * Handle the decision point where a player is choosing whether to knock
     * 
     * @param state the current game state
     * @param probP0 the probability that player 0 would play to this state
     * @param probP1 the probability that player 1 would play to this state
     * 
     * @return the expected utility of following the player's action profile at this decision point
     */
	private static double knock(GameState state, Player[] players, double probP0, double probP1) {
		int player = state.getCurrentPlayer();
		
		ActionKnock[] actions = players[player].getKnockStrategy(state);
		double[] util = new double[actions.length];
	
		for (int i = 0; i < actions.length; i++) {
			if (actions[i].isKnock()) {
				if (DEBUG) System.out.println("Knock - game over");
				util[i] = gameOver(state);
			}
			else {
				if (state.getDecisionPoint() == 2) {
					// For first face up card, we picked up, so advance to first play for player 2 where they can pick up
					if (DEBUG) System.out.println("Round 1, and player 0 picked up so advance to first regular play for other player");
					GameState nextState = new GameState(state, state.getDecisionPoint() + 7);
		            util[i] = player == 0 
		                    ? - playFrom(nextState, players, probP0 * actions[i].p, probP1)
		                    : - playFrom(nextState, players, probP0, probP1 * actions[i].p);
				}
				else {
					if (DEBUG) System.out.println("No knock");
					GameState nextState = new GameState(state);
		
					util[i] = player == 0 
		                    ? - playFrom(nextState, players, probP0 * actions[i].p, probP1)
		                    : - playFrom(nextState, players, probP0, probP1 * actions[i].p);
				}
			}
		}

		return players[player].getKnockStrategy().postProcess(actions, util, player==0?probP0:probP1, player==0?probP1:probP0);
	}
	

	@SuppressWarnings("unchecked")
	private static double gameOver(GameState state) {
		int player = state.getCurrentPlayer();
		int opponent = player==0?1:0;

		ArrayList<ArrayList<Card>> knockMelds = PshUtil.getFinalMelds(state, player);

		// check legality of knocking meld
		long unmelded = state.getCurrentPlayerCards();
		
		for (ArrayList<Card> meld : knockMelds) {
			long meldBitstring = GinRummyUtil.cardsToBitstring(meld);
			if (!GinRummyUtil.getAllMeldBitstrings().contains(meldBitstring) // non-meld ...
					|| (meldBitstring & unmelded) != meldBitstring) { // ... or meld not in hand
				throw new RuntimeException("week_5.Player "  + player + " melds " + knockMelds + " illegally and forfeits.\n");
			}
			unmelded &= ~meldBitstring; // remove successfully melded cards from 
		}
		
		// compute knocking deadwood
		int knockingDeadwood = GinRummyUtil.getDeadwoodPoints(knockMelds, state.getCurrentPlayerCardsAsList()); 
		if (knockingDeadwood > GinRummyUtil.MAX_DEADWOOD) {
			throw new RuntimeException("week_5.Player " + player + " melds " + knockMelds + " with " + knockingDeadwood + " deadwood and forfeits.\n");
		}

		ArrayList<ArrayList<Card>> meldsCopy = new ArrayList<ArrayList<Card>>();
		for (ArrayList<Card> meld : knockMelds)
			meldsCopy.add((ArrayList<Card>) meld.clone());
	
		// get opponent meld
		ArrayList<ArrayList<Card>> opponentMelds = PshUtil.getFinalMelds(state, opponent);
		for (ArrayList<Card> meld : opponentMelds)
			meldsCopy.add((ArrayList<Card>) meld.clone());
		meldsCopy = new ArrayList<ArrayList<Card>>();

		// check legality of opponent meld
		long opponentUnmelded = state.getPlayerCards(opponent);
		for (ArrayList<Card> meld : opponentMelds) {
			long meldBitstring = GinRummyUtil.cardsToBitstring(meld);
			if (!GinRummyUtil.getAllMeldBitstrings().contains(meldBitstring) // non-meld ...
					|| (meldBitstring & opponentUnmelded) != meldBitstring) { // ... or meld not in hand
				throw new RuntimeException("week_5.Player " + opponent + " melds " + opponentMelds + " illegally and forfeits.\n");
			}
			opponentUnmelded &= ~meldBitstring; // remove successfully melded cards from 
		}
		if (DEBUG)
			System.out.printf("week_5.Player %d melds %s.\n", opponent, opponentMelds);

		// lay off on knocking meld (if not gin)
		ArrayList<Card> unmeldedCards = GinRummyUtil.bitstringToCards(opponentUnmelded);
		if (knockingDeadwood > 0) { // knocking player didn't go gin
			boolean cardWasLaidOff;
			do { // attempt to lay each card off
				cardWasLaidOff = false;
				Card layOffCard = null;
				ArrayList<Card> layOffMeld = null;
				for (Card card : unmeldedCards) {
					for (ArrayList<Card> meld : knockMelds) {
						ArrayList<Card> newMeld = (ArrayList<Card>) meld.clone();
						newMeld.add(card);
						long newMeldBitstring = GinRummyUtil.cardsToBitstring(newMeld);
						if (GinRummyUtil.getAllMeldBitstrings().contains(newMeldBitstring)) {
							layOffCard = card;
							layOffMeld = meld;
							break;
						}
					}
					if (layOffCard != null) {
						if (DEBUG)
							System.out.printf("week_5.Player %d lays off %s on %s.\n", opponent, layOffCard, layOffMeld);
						unmeldedCards.remove(layOffCard);
						layOffMeld.add(layOffCard);
						cardWasLaidOff = true;
						break;
					}
	
				}
			} while (cardWasLaidOff);
		}
		int opponentDeadwood = 0;
		for (Card card : unmeldedCards)
			opponentDeadwood += GinRummyUtil.getDeadwoodPoints(card);
		if (DEBUG)
			System.out.printf("week_5.Player %d has %d deadwood with %s\n", opponent, opponentDeadwood, unmeldedCards);
		
		// compare deadwood and compute new scores
		if (knockingDeadwood == 0) { // gin round win
			if (DEBUG)
				System.out.printf("week_5.Player %d scores the gin bonus of %d plus opponent deadwood %d for %d total points.\n", player, GinRummyUtil.GIN_BONUS, opponentDeadwood, GinRummyUtil.GIN_BONUS + opponentDeadwood);
			return GinRummyUtil.GIN_BONUS + opponentDeadwood;
		}
		else if (knockingDeadwood < opponentDeadwood) { // non-gin round win
			if (DEBUG)
				System.out.printf("week_5.Player %d scores the deadwood difference of %d.\n", player, opponentDeadwood - knockingDeadwood);
			return opponentDeadwood - knockingDeadwood;
		}
		else { // undercut win for opponent
			if (DEBUG)
				System.out.printf("week_5.Player %d undercuts and scores the undercut bonus of %d plus deadwood difference of %d for %d total points.\n", opponent, GinRummyUtil.UNDERCUT_BONUS, knockingDeadwood - opponentDeadwood, GinRummyUtil.UNDERCUT_BONUS + knockingDeadwood - opponentDeadwood);
			return -(GinRummyUtil.UNDERCUT_BONUS + knockingDeadwood - opponentDeadwood);
		}

	}
}
