
public class StrategyDrawCFR extends StrategyDraw {

	public StrategyDrawCFR(boolean training) {
		super(training);
	}

	@Override
	public ActionDraw[] getStrategy(GameState state) {		
		int improvement = PshUtil.getDeadwoodImprovementIfDrawFaceUpCard(state);
		if (improvement > 0 && PshUtil.doesFaceUpCardMakeNewMeld(state)) {
			ActionDraw[] strategy = {new ActionDraw(true, 1.0, null)};
			return strategy;
		}
		
		ActionDraw[] strategy = new ActionDraw[] {new ActionDraw(true, 0.0, improvement + "_y"), new ActionDraw(false, 0.0, improvement + "_n")};
		getProbabilities(strategy);			
		return strategy;
	}

	/**
	 * @see Strategy#getName()
	 */
	@Override
	public String getName() {
		return "DrawStrategy_CFR";
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append(getName() + "\nDrawing Percent as a function of DeadWood Improvement\n");
		sb.append("draw\tdon't\n");
		for (int d = 0; d <= 9; d++) {
			String key_k = d + "_y";
			String key_n = d + "_n";
			sb.append(d + "\t");
			sb.append(String.format("%.3f", sumStrategy.getOrDefault(key_k,1.0) /(sumStrategy.getOrDefault(key_k,1.0)+sumStrategy.getOrDefault(key_n,1.0))));
			sb.append("\t");
			sb.append(String.format("%.3f", sumStrategy.getOrDefault(key_n,1.0) /(sumStrategy.getOrDefault(key_k,1.0)+sumStrategy.getOrDefault(key_n,1.0))));
			sb.append("\n");
		}
	    return sb.toString();
	}

}
