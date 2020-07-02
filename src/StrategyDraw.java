public class StrategyDraw extends Strategy {
	public static final int MIN_DROP_TO_TAKE_FACE_UP = 6;

	/** 
	 * In the default strategy, we are not training
	 */
	public StrategyDraw(boolean training) {
		super(training);
	}

	@Override
	public ActionDraw[] getStrategy(GameState state) {
		ActionDraw[] strategy = new ActionDraw[1];
		
		int improvement = PshUtil.getDeadwoodImprovementIfDrawFaceUpCard(state);
		if (improvement >= MIN_DROP_TO_TAKE_FACE_UP) {
			strategy[0] = new ActionDraw(true, 1.0, null);
		}
		else if (improvement > 0 && PshUtil.doesFaceUpCardMakeNewMeld(state)) {
			strategy[0] = new ActionDraw(true, 1.0, null);
		}
		else {
			strategy[0] = new ActionDraw(false, 1.0, null);			
		}
		return strategy;
	}

	/**
	 * @see Strategy#getName()
	 */
	@Override
	public String getName() {
		return "DefaultDrawStrategy";
	}

}
