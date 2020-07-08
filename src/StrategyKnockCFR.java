import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.TreeSet;

public class StrategyKnockCFR extends StrategyKnock {
	private boolean verbose;
	private TreeSet<String> infosets;
	
	public StrategyKnockCFR(boolean training) {
		this(training, false);
		infosets = new TreeSet<>();
	}

	public StrategyKnockCFR(boolean training, boolean verbose) {
		super(training);
		this.verbose = verbose;
	}
	
	/**
	 * If I have gin, I will knock.  If I cannot knock, I won't.  Otherwise I use either
	 * regret matching (if training is true) or the learned strategy if training is false.
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
		
		ActionKnock[] strategy;
		if (deadwood == 0) {
			// I have gin, so always knock
			if (verbose) System.out.println("I have gin, so I will knock.");
			strategy = new ActionKnock[] { new ActionKnock(true, 1.0, null)};
		}
		else if (deadwood  > GinRummyUtil.MAX_DEADWOOD) {
			// I can't knock
			if (verbose) System.out.println("I can't knock, current deadwood: " + deadwood);
			strategy = new ActionKnock[] {new ActionKnock(false, 1.0, null)};
		}
		else {
			// I can knock, let's see if I should

			String infosetKnock = deadwood + "_" + state.getTopCard();
			String infosetDont = deadwood + "_" + state.getTopCard();

			infosets.add(infosetKnock);
			infosets.add(infosetDont);

			strategy = new ActionKnock[] {new ActionKnock(true, 0.0,  infosetKnock + "_k"),
					new ActionKnock(false, 0.0, infosetDont + "_n")};
			getProbabilities(strategy);			
		}
		return strategy;
	}

	@Override
	public String getName() {
		return "KnockStrategyCFR";
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append(getName() + "\nKnocking Percent as a function of DeadWood\n");
		sb.append("\tknock\tdon't\n");
		for(String s : infosets.descendingSet()) {
			String key_k = s + "_y";
			String key_n = s + "_n";
			sb.append(s + "\t");
			sb.append(String.format("%.3f", sumStrategy.getOrDefault(key_k,1.0) /(sumStrategy.getOrDefault(key_k,1.0)+sumStrategy.getOrDefault(key_n,1.0))));
			sb.append("\t");
			sb.append(String.format("%.3f", sumStrategy.getOrDefault(key_n,1.0) /(sumStrategy.getOrDefault(key_k,1.0)+sumStrategy.getOrDefault(key_n,1.0))));
			sb.append("\n");
		}
	    return sb.toString();
	}

	@Override
	public void toFile(String fname) throws FileNotFoundException {
		StringBuffer sb = new StringBuffer();

		for(String s : infosets.descendingSet()) {
			String key_k = s + "_y";
			String key_n = s + "_n";
			sb.append("put(\"" + s + "\", ");
			sb.append(String.format("%.3f", sumStrategy.getOrDefault(key_k,1.0) /(sumStrategy.getOrDefault(key_k,1.0)+sumStrategy.getOrDefault(key_n,1.0))));
			sb.append(");");
			sb.append("\n");
		}

		PrintWriter pw = new PrintWriter(fname);
		pw.print(sb.toString());
		pw.close();
	}

}
