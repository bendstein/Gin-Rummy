import java.util.ArrayList;

/**
 * Implements a fixed knocking strategy: always knock if possible. 
 * 
 * @author jjb24
 */
public class StrategyKnock extends Strategy {

	/** 
	 * In the default strategy, we are not training
	 */
	public StrategyKnock(boolean training) {
		super(training);
	}

	/**
	 * @param state the current game state
	 * @return a strategy that always knocks if possible
	 */
	@Override
	public ActionKnock[] getStrategy(GameState state) {
		ArrayList<Card> myCards = state.getCurrentPlayerCardsAsList();
		ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(myCards);

		int deadwood;
		if (bestMeldSets.isEmpty())
			deadwood = GinRummyUtil.getDeadwoodPoints(myCards);
		else
			deadwood = GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), myCards);

		ActionKnock[] strategy = new ActionKnock[1];
		if (deadwood  > GinRummyUtil.MAX_DEADWOOD) {
			strategy[0] = new ActionKnock(false, 1.0, null);
		}
		else {
			strategy[0] = new ActionKnock(true, 1.0, null);			
		}
		
		return strategy;
	}

	/**
	 * @see Strategy#getName()
	 */
	@Override
	public String getName() {
		return "DefaultKnockStrategy";
	}
	
	/**
	 * @see Strategy#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}

}
