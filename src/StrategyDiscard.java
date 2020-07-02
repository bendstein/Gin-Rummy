/**
 * Implements a fixed discard strategy: 
 * 1)  We start by looking for �unmatchable� cards.  A card is unmatchable if it is not currently in a meld 
 *       AND it cannot be put in a meld even after drawing one more card AND it is worth at least seven points.
 * 2)  Among these unmatchable cards, we check for cards that are �dead�.  A card is dead if it cannot be put 
 *       into a meld by the opponent.
 * 3)  If there are unmatchable, dead cards, we pick the highest value of unmatchable, dead card to discard.
 * 4)  Else if there are unmatchable cards, we discard the highest value unmatchable card
 * 5)  Else we discard the card that leaves us with the least amount of deadwood.
 * 
 * @author jjb24
 */
public class StrategyDiscard extends Strategy {
	static final int DISCARD_THRESHOLD = 7; // Do not discard unmatchable cards, unless they are at least a seven

	/** 
	 * In the default strategy, we are not training
	 */
	public StrategyDiscard(boolean training) {
		super(training);
	}
	
	/**
	 * Return a pure strategy that looks first for dead cards (cards that cannot be used for a meld, then 
	 */
	@Override
	public ActionDiscard[] getStrategy(GameState state) {		
		ActionDiscard[] strategy = new ActionDiscard[1];
		
		long discards = PshUtil.getUnmatchableCards(state, DISCARD_THRESHOLD); 
		if (discards != 0) {
			long dead = PshUtil.getDeadCard(discards, state);
			if (dead != 0) {
				strategy[0] = new ActionDiscard(dead, 1.0, null);
			}
			else {
				strategy[0] = new ActionDiscard(PshUtil.getCardWithBiggestPenalty(discards), 1.0, null);  
			}
		}
		else {
			strategy[0] = new ActionDiscard(PshUtil.getCardThatMinimizesDeadwood(state), 1.0, null);  
		}
		return strategy;
	}


	/**
	 * @see Strategy#getName()
	 */
	@Override
	public String getName() {
		return "DefaultDiscardStrategy";
	}

}
