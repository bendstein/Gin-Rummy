/**
 * A combination of a draw, discard, and knock strategy
 * 
 * @author jjb24
 */
public class Player {
	private StrategyDraw drawStrategy;
	private StrategyDiscard discardStrategy;
	private StrategyKnock knockStrategy;
	
	/**
	 * @param drawStrategy 
	 * @param discardStrategy
	 * @param knockStrategy
	 */
	public Player(StrategyDraw drawStrategy, StrategyDiscard discardStrategy, StrategyKnock knockStrategy) {
		this.drawStrategy = drawStrategy;
		this.discardStrategy = discardStrategy;
		this.knockStrategy = knockStrategy;
	}

	/** 
	 * Return the draw strategy for this player at this state
	 * 
	 * @param state the current game state
	 * 
	 * @return a strategy for drawing the face up card
	 */
	ActionDraw[] getDrawStrategy(GameState state) {
		return drawStrategy.getStrategy(state);
	}

	/** 
	 * Return the discard strategy for this player at this state
	 * 
	 * @param state the current game state
	 * 
	 * @return a strategy for determining a discard
	 */
	ActionDiscard[] getDiscardStrategy(GameState state) {
		return discardStrategy.getStrategy(state);
	}

	/** 
	 * Return the knock strategy for this player at this state
	 * 
	 * @param state the current game state
	 * 
	 * @return a strategy for determining whether to knock
	 */
	ActionKnock[] getKnockStrategy(GameState state) {
		return knockStrategy.getStrategy(state);
	}

	/**
	 * @return the drawStrategy
	 */
	public StrategyDraw getDrawStrategy() {
		return drawStrategy;
	}

	/**
	 * @param drawStrategy the drawStrategy to set
	 */
	public void setDrawStrategy(StrategyDraw drawStrategy) {
		this.drawStrategy = drawStrategy;
	}

	/**
	 * @return the discardStrategy
	 */
	public StrategyDiscard getDiscardStrategy() {
		return discardStrategy;
	}

	/**
	 * @param discardStrategy the discardStrategy to set
	 */
	public void setDiscardStrategy(StrategyDiscard discardStrategy) {
		this.discardStrategy = discardStrategy;
	}

	/**
	 * @return the knockStrategy
	 */
	public StrategyKnock getKnockStrategy() {
		return knockStrategy;
	}

	/**
	 * @param knockStrategy the knockStrategy to set
	 */
	public void setKnockStrategy(StrategyKnock knockStrategy) {
		this.knockStrategy = knockStrategy;
	}



}
