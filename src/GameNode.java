import java.util.ArrayList;
import java.util.Random;

@SuppressWarnings("ALL")
public class GameNode {
	public static boolean DEBUG = false;
	public static Random prng = new Random();
	public static double EPSILON = 0.6; // the proportion of time that we sample uniformly from all actions
										// when sampling regret

	public static double MAX_TURNS = 10; // To speed training, we limit the game to 10 turns each
	
	public static class UtilityProbability {
		double scaledUtility;  // [the utility at the sampled terminal node]/[probability we play to this node]
		double pTail;          // [the probability we play from this node to the sampled terminal node]
		public UtilityProbability() {}
		public UtilityProbability(double scaledUtility, double pTail) {
			this.scaledUtility = scaledUtility;
			this.pTail = pTail;
		}
		
	}
	
	/**
	 * Sample game from the current game state
	 * 
	 * @param state the current game state
	 * @param players the players for the game
	 * @param player_i if useAveStrategy is false, players[player_i] is the player for whom we are sampling regret
	 * 			This player will use a sampling strategy sigma', which differs from the CFR strategy 
	 * 			because we will be sampling all actions with some probability.  The other player will 
	 * 			use its CFR strategy.
	 * 			Note that if useAveStrategy is true, this parameter is ignored.
	 * @param pi this is the probability that player_i would play to this node its current strategy.
	 * 			Note that this strategy will be sigma' if useStrategy is false, otherwise it will be
	 * 			sigma_bar.
	 * @param useAveStrategy if this is true, we will use the average strategy for all players.
	 * @return A pair of <[the utility at the sampled terminal node]/[probability we play to this node], 
	 *                    [the probability we play from this node to the sampled terminal node]>
	 */
	public static UtilityProbability playFrom(GameState state, Player[] players, int player_i, double pi, boolean useAveStrategy) {
		if (DEBUG) {
			System.out.println(state.toString());
		}
		
		if (state.getTopCard() >= 50) {
			if (DEBUG)
				System.out.println("The draw pile was reduced to two cards without knocking, so the hand is cancelled.");
			return new UtilityProbability(0.0, 1.0);
		}
		

        // Limit to 10 turns each
        if (state.getDecisionPoint() / 6 >= 10) {
                if (DEBUG)
                        System.out.println("The draw pile was reduced to two cards without knocking, so the hand is cancelled.");
                int player = state.getCurrentPlayer();
                int opponent = player==0?1:0;
                int myDeadwood = PshUtil.getBestDeadwood(state.getPlayerCardsAsList(player));
                int otherDeadwood = PshUtil.getBestDeadwood(state.getPlayerCardsAsList(opponent));
                return new UtilityProbability(otherDeadwood - myDeadwood, 1.0);
        }
		
		if (state.getDecisionPoint()%3 == 0) {
			return drawCard(state, players, player_i, pi, useAveStrategy);
		}
		else if (state.getDecisionPoint()%3 == 1) {
			return discardCard(state, players, player_i, pi, useAveStrategy);
		}
		else {
			return knock(state, players, player_i, pi, useAveStrategy);
		}
	}
	
	/** 
	 * Handle the decision point where player is drawing a card
	 * 
     * @param state the current game state
     * @param players an array with the two players
	 * @param player_i if useAveStrategy is false, players[player_i] is the player for whom we are sampling regret
	 * 			This player will use a sampling strategy sigma', which differs from the CFR strategy 
	 * 			because we will be sampling all actions with some probability.  The other player will 
	 * 			use its CFR strategy.
	 * 			Note that if useAveStrategy is true, this parameter is ignored.
	 * @param pi this is the probability that player_i would play to this node its current strategy.
	 * 			Note that this strategy will be sigma' if useStrategy is false, otherwise it will be
	 * 			sigma_bar.
	 * @param useAveStrategy if this is true, we will use the average strategy for all players.
	 * @return A pair of <[the utility at the sampled terminal node]/[probability we play to this node], 
	 *                    [the probability we play from this node to the sampled terminal node]>
	 */
	private static UtilityProbability drawCard(GameState state, Player[] players, int player_i, double pi, boolean useAveStrategy) {
		int player = state.getCurrentPlayer();
		
		if (useAveStrategy) {
			players[player].getDrawStrategy().setTrain(false);
		}
		ActionDraw[] actions = players[player].getDrawStrategy(state);
		
		double[] probability = new double[actions.length]; // The probability of choosing each action

		// TODO: If the current player is not the player being trained, or useAveStrategy is true,
		// set probability[i] = actions[i].p.
		// Otherwise, set probability[i], the probability of choosing an action i,
		// to at least EPSILON/actions.length. The remaining 1-EPSILON in probability should be 
		// allocated proportional to the strategy (i.e. values in action[i].p).

		for(int i = 0; i < probability.length; i++) {
			if(useAveStrategy || player != player_i)
				probability[i] = actions[i].p;
			else {
				probability[i] = EPSILON/actions.length + (1d - EPSILON) * actions[i].p;
				//actions[i].p = 1 - probability[i];
			}
		}

		int actionIndex = 0;
		// TODO: Set actionIndex to the current action, choosing action i with a probability proportional
		// probability[i]

		double sum = 0d;
		double prob = prng.nextDouble();

		for(int i = 0; i < probability.length; i++) {
			sum += probability[i];

			if(prob < sum) {
				actionIndex = i;
				break;
			}
		}
		
		UtilityProbability uP;
	
		GameState nextState;
		
		if (actions[actionIndex].isDraw()) {
			if (DEBUG) System.out.println("Drawing face up card");
			
			nextState = new GameState(state);
			nextState.addFaceUpCardToHand();
		}
		else if (state.getDecisionPoint() == 0 || state.getDecisionPoint() == 3) {
			// For first face up card, we don't automatically pickup a card if we decline it
			nextState = new GameState(state, state.getDecisionPoint() + 3);
		}
		else {
			nextState = new GameState(state);
			nextState.addFaceDownCardToHand();
		}
		
		// TODO: Set uP equal to the value returned by the call to playFrom from the nextState game state
		// Note that the call will vary based on whether player == player_i
		uP = playFrom(nextState, players, player_i, player == player_i ? pi * probability[actionIndex] : pi, useAveStrategy);
		
		// If we don't draw first face up card, we jumped directly to other player's turn, so we
		// need to negate utility
		if (!actions[actionIndex].isDraw() && (state.getDecisionPoint() == 0 || state.getDecisionPoint() == 3)) {
			uP.scaledUtility = -uP.scaledUtility;
		}
		
		if (useAveStrategy == false) {
			if (player == player_i) {
				players[player].getDrawStrategy().updateSampledRegret(actions, actionIndex, uP);
			}
			else {
				players[player].getDrawStrategy().updateAverageStrategy(actions, pi);				
			}
		}

		
		// TODO: Update uP.pTail coming out of the recusion, so that, in the calling function,
		// it will be equal to the probability we played from the node to the sampled terminal node
		if(player == player_i) uP.pTail *= probability[actionIndex];

		return uP;
	}

    /**
     * Handle the decision point where a player is choosing a card to discard
     * 
     * @param state the current game state
     * @param players an array with the two players
	 * @param player_i if useAveStrategy is false, players[player_i] is the player for whom we are sampling regret
	 * 			This player will use a sampling strategy sigma', which differs from the CFR strategy 
	 * 			because we will be sampling all actions with some probability.  The other player will 
	 * 			use its CFR strategy.
	 * 			Note that if useAveStrategy is true, this parameter is ignored.
	 * @param pi this is the probability that player_i would play to this node its current strategy.
	 * 			Note that this strategy will be sigma' if useStrategy is false, otherwise it will be
	 * 			sigma_bar.
	 * @param useAveStrategy if this is true, we will use the average strategy for all players.
	 * @return A pair of <[the utility at the sampled terminal node]/[probability we play to this node], 
	 *                    [the probability we play from this node to the sampled terminal node]>
     */
	private static UtilityProbability discardCard(GameState state, Player[] players, int player_i, double pi, boolean useAveStrategy) {
		int player = state.getCurrentPlayer();
		
		if (useAveStrategy) {
			players[player].getDiscardStrategy().setTrain(false);
		}
		ActionDiscard[] discards = players[player].getDiscardStrategy(state);
		
		double[] probability = new double[discards.length];

		// TODO: If the current player is not the player being trained, or useAveStrategy is true,
		// set probability[i] = actions[i].p.
		// Otherwise, set probability[i], the probability of choosing an action i,
		// to at least EPSILON/actions.length. The remaining 1-EPSILON in probability should be 
		// allocated proportional to the strategy (i.e. values in action[i].p).
		for(int i = 0; i < probability.length; i++) {
			if(useAveStrategy || player != player_i)
				probability[i] = discards[i].p;
			else {
				probability[i] = EPSILON/discards.length + (1d - EPSILON) * discards[i].p;
				//discards[i].p = 1 - probability[i];
			}
		}

		
		int actionIndex = 0;
		// TODO: Set actionIndex to the current action, choosing action i with a probability proportional
		// probability[i]

		double sum = 0d;
		double prob = Math.random();

		for(int i = 0; i < probability.length; i++) {
			sum += probability[i];

			if(prob < sum) {
				actionIndex = i;
				break;
			}
		}

		ActionDiscard discard = discards[actionIndex];
		if (DEBUG) System.out.println("Discarding " + discard.getCardAsObject().toString());	
		GameState nextState = new GameState(state);
		nextState.discardCard(discard.getCard());

		UtilityProbability uP;

		// TODO: Set uP equal to the value returned by the call to playFrom from the nextState game state
		// Note that the call will vary based on whether player == player_i

		uP = playFrom(nextState, players, player_i, player == player_i ? pi * probability[actionIndex] : pi, useAveStrategy);
		
		if (useAveStrategy == false) {
			if (player == player_i) {
				players[player].getDiscardStrategy().updateSampledRegret(discards, actionIndex, uP);
			}
			else {
				players[player].getDiscardStrategy().updateAverageStrategy(discards, pi);				
			}
		}
		
		// TODO: Update uP.pTail coming out of the recusion, so that, in the calling function,
		// it will be equal to the probability we played from the node to the sampled terminal node 

		if(player == player_i) uP.pTail *= probability[actionIndex];
		
		return uP;
	}

    /**
     * Handle the decision point where a player is choosing whether to knock
     * 
     * @param state the current game state
     * @param players an array with the two players
	 * @param player_i if useAveStrategy is false, players[player_i] is the player for whom we are sampling regret
	 * 			This player will use a sampling strategy sigma', which differs from the CFR strategy 
	 * 			because we will be sampling all actions with some probability.  The other player will 
	 * 			use its CFR strategy.
	 * 			Note that if useAveStrategy is true, this parameter is ignored.
	 * @param pi this is the probability that player_i would play to this node its current strategy.
	 * 			Note that this strategy will be sigma' if useStrategy is false, otherwise it will be
	 * 			sigma_bar.
	 * //@param p this is probability that this node would be reached given the current strategy used by all players
	 * @param useAveStrategy if this is true, we will use the average strategy for all players.
	 * @return A pair of <[the utility at the sampled terminal node]/[probability we play to this node], 
	 *                    [the probability we play from this node to the sampled terminal node]>
     */
	private static UtilityProbability knock(GameState state, Player[] players, int player_i, double pi, boolean useAveStrategy) {
		int player = state.getCurrentPlayer();
				
		if (useAveStrategy) {
			players[player].getKnockStrategy().setTrain(false);
		}
		ActionKnock[] actions = players[player].getKnockStrategy(state);
		
		double[] probability = new double[actions.length];
		for (int action = 0; action < actions.length; ++action) {
			if (useAveStrategy || player != player_i) {
				probability[action] = actions[action].p;				
			}
			else {
				probability[action] = EPSILON / actions.length + (1.0 - EPSILON) * actions[action].p;
			}
		}
		
		double selector = prng.nextDouble();
		int actionIndex = 0;
		while (selector > 0 && actionIndex < actions.length-1) {
			selector -= probability[actionIndex];
			if (selector > 0) actionIndex++;
		}
		
		UtilityProbability uP;
	
		if (actions[actionIndex].isKnock()) {
			if (DEBUG) System.out.println("Knock - game over");
			double util = gameOver(state);
			// TODO: The game is over.  Set uP to appropriate values for this terminal node.
			uP = new UtilityProbability(util/pi, 1d);
		
		}
		else {
			GameState nextState;
			if (state.getDecisionPoint() == 2) {
				// For first face up card, we picked up, so advance to first play for player 2 where they can pick up
				if (DEBUG) System.out.println("Round 1, and player 0 picked up so advance to first regular play for other player");
				nextState = new GameState(state, state.getDecisionPoint() + 7);
			}
			else {
				if (DEBUG) System.out.println("No knock");
				nextState = new GameState(state);
			}
			// TODO: Set uP equal to the value returned by the call to playFrom from the nextState game state
			// Note that the call will vary based on whether player == player_i

			uP = playFrom(nextState, players, player_i, player == player_i ? pi * probability[actionIndex] : pi, useAveStrategy);
			
			// Other player goes next, so we need to negate utility
			uP.scaledUtility = -uP.scaledUtility;
		}
		
		if (useAveStrategy == false) {
			if (player == player_i) {
				players[player].getKnockStrategy().updateSampledRegret(actions, actionIndex, uP);
			}
			else {
				players[player].getKnockStrategy().updateAverageStrategy(actions, pi);				
			}
		}

		// TODO: Update uP.pTail coming out of the recusion, so that, in the calling function,
		// it will be equal to the probability we played from the node to the sampled terminal node 

		if(player == player_i) uP.pTail *= probability[actionIndex];

		return uP;
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
				throw new RuntimeException("Player "  + player + " melds " + knockMelds + " illegally and forfeits.\n");
			}
			unmelded &= ~meldBitstring; // remove successfully melded cards from 
		}
		
		// compute knocking deadwood
		int knockingDeadwood = GinRummyUtil.getDeadwoodPoints(knockMelds, state.getCurrentPlayerCardsAsList()); 
		if (knockingDeadwood > GinRummyUtil.MAX_DEADWOOD) {
			throw new RuntimeException("Player " + player + " melds " + knockMelds + " with " + knockingDeadwood + " deadwood and forfeits.\n");
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
				throw new RuntimeException("Player " + opponent + " melds " + opponentMelds + " illegally and forfeits.\n");
			}
			opponentUnmelded &= ~meldBitstring; // remove successfully melded cards from 
		}
		if (DEBUG)
			System.out.printf("Player %d melds %s.\n", opponent, opponentMelds);

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
							System.out.printf("Player %d lays off %s on %s.\n", opponent, layOffCard, layOffMeld);
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
			System.out.printf("Player %d has %d deadwood with %s\n", opponent, opponentDeadwood, unmeldedCards); 
		
		// compare deadwood and compute new scores
		if (knockingDeadwood == 0) { // gin round win
			if (DEBUG)
				System.out.printf("Player %d scores the gin bonus of %d plus opponent deadwood %d for %d total points.\n", player, GinRummyUtil.GIN_BONUS, opponentDeadwood, GinRummyUtil.GIN_BONUS + opponentDeadwood); 
			return GinRummyUtil.GIN_BONUS + opponentDeadwood;
		}
		else if (knockingDeadwood < opponentDeadwood) { // non-gin round win
			if (DEBUG)
				System.out.printf("Player %d scores the deadwood difference of %d.\n", player, opponentDeadwood - knockingDeadwood); 
			return opponentDeadwood - knockingDeadwood;
		}
		else { // undercut win for opponent
			if (DEBUG)
				System.out.printf("Player %d undercuts and scores the undercut bonus of %d plus deadwood difference of %d for %d total points.\n", opponent, GinRummyUtil.UNDERCUT_BONUS, knockingDeadwood - opponentDeadwood, GinRummyUtil.UNDERCUT_BONUS + knockingDeadwood - opponentDeadwood); 
			return -(GinRummyUtil.UNDERCUT_BONUS + knockingDeadwood - opponentDeadwood);
		}

	}
}
