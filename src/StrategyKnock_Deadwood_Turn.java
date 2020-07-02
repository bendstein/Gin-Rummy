import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class StrategyKnock_Deadwood_Turn extends StrategyKnock {
	private boolean verbose;

	public StrategyKnock_Deadwood_Turn(boolean training) {
		this(training, false);
	}

	public StrategyKnock_Deadwood_Turn(boolean training, boolean verbose) {
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
			strategy = new ActionKnock[]
					{new ActionKnock(true, 0.0, deadwood + "_" + state.getTopCard() + "_k"),
							new ActionKnock(false, 0.0, deadwood + "_" + state.getTopCard() + "_n")};
			getProbabilities(strategy);			
		}
		return strategy;
	}

	@Override
	public String getName() {
		return "CFRKnockStrategyOnDeadwood";
	}

	@Override
	public void toFile(String fname) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(fname);

		for (int d = 1; d <= 10; d++) {
			for(int i = 0; i <= 50; i++) {
				String key_k = d + "_" + i + "_k";
				String key_n = d + "_" + i + "_n";
				pw.append("put(\"" + key_k + "\", ");
				pw.append(String.format("%.3f", Double.isNaN((sumStrategy.getOrDefault(key_k, 1.0) /
						(sumStrategy.getOrDefault(key_k, 1.0) +
								sumStrategy.getOrDefault(key_n, 1.0))))? 0.5 :
						(sumStrategy.getOrDefault(key_k, 1.0) /
								(sumStrategy.getOrDefault(key_k, 1.0) +
										sumStrategy.getOrDefault(key_n, 1.0)))));
				pw.append(");\n");
			}

		}

		pw.close();
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append(getName() + "\nKnocking Percent as a function of DeadWood and Turn #\n");
		for (int d = 1; d <= 10; d++) {
			for(int i = 0; i <= 50; i++) {
				String key_k = d + "_" + i + "_k";
				String key_n = d + "_" + i + "_n";
				sb.append(d + " " + i + "\t");
				sb.append(String.format("%.3f", sumStrategy.getOrDefault(key_k,1.0) /(sumStrategy.getOrDefault(key_k,1.0)+sumStrategy.getOrDefault(key_n,1.0))));
				sb.append("\t");
				sb.append(String.format("%.3f", sumStrategy.getOrDefault(key_n,1.0) /(sumStrategy.getOrDefault(key_k,1.0)+sumStrategy.getOrDefault(key_n,1.0))));
				sb.append("\n");
			}

		}
	    return sb.toString();
	}

}
