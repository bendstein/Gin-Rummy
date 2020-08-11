import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * The superclass for a CFR strategy

 * @author jjb24
 */
public abstract class Strategy {
	protected boolean training; // Are we training or not

	protected Map<String, Double> sumRegret = new HashMap<>();     // The sum of the counterfactual regret for a string equal to the infoset plus the action
	protected Map<String, Double> sumStrategy = new TreeMap<>();   // From sigma_bar^t for a string equal to the infoset and the action
	protected Map<String, Long> frequencies = new TreeMap<>();
	protected Long total_visits = 0L;
	// This is just the numerator of the equation given

	/**
	 * Constructor
	 *
	 * @param train Are we accumulating regrets
	 */
	public Strategy(boolean train) {
		this.training = train;
	}

	/**
	 * @return the name of this strategy, used in the toString
	 */
	public abstract String getName();

	/**
	 * Get the strategy as an array of Actions for a player at a point in the game
	 *
	 * @param state the state of the game
	 * @return strategy
	 */
	public abstract Action[] getStrategy(GameState state);


	/**
	 * @return whether we are accumulating regrets at infosets
	 */
	public boolean isTrain() {
		return training;
	}

	/**
	 * @param train whether we should accumulate regrest at infosets
	 */
	public void setTrain(boolean train) {
		this.training = train;
	}

	/**
	 * Fill in the probabilities in action, based on values stored at InfoSet
	 *
	 * @param actions the actions to be taken
	 */
	public final void getProbabilities(Action[] actions) {
		if (training)
			getRegretMatchingStrategy(actions);
		else
			getLearnedStrategy(actions);
	}

	/**
	 * Fill in the probabilities in action, based on the regret shown
	 *
	 * @param actions the actions to be taken
	 */
	private final void getRegretMatchingStrategy(Action[] actions) {
		synchronized (sumRegret) {
			for (Action action: actions) {
				if (action.infosetAndAction == null) {
					throw new IllegalArgumentException("The infoset for the actions must be set");
				}
				action.p = sumRegret.getOrDefault(action.infosetAndAction, 0.0);
				action.p = Math.max(action.p, 0.0);
			}
		}
		normalize(actions);
	}

	/**
	 * Fill in the probabilities in action, based on the average profiles over training runs
	 *
	 * @param actions the actions to be taken
	 */
	private final void getLearnedStrategy(Action[] actions) {

		/*
		 * Add a count variable so that everytime an infoset is seen here, the count is incremented
		 */

		synchronized (sumStrategy) {
			for (Action action: actions) {
				if (action.infosetAndAction == null) {
					throw new IllegalArgumentException("The infoset for the actions must be set");
				}
				action.p = sumStrategy.getOrDefault(action.infosetAndAction, 0.0);
				total_visits++;
				if(frequencies.containsKey(action.infosetAndAction))
					frequencies.replace(action.infosetAndAction, frequencies.get(action.infosetAndAction) + 1L);
				else
					frequencies.put(action.infosetAndAction, 1L);
			}
		}
		normalize(actions);
	}


	/**
	 * Update sampled counterfactual regret
	 *
	 * @param actions          strategy chosen by this player
	 * @param sampledAction    index of action in actions that was chosen
	 * @param utilProb        a pair: <[the utility of sampled terminal node]/[probability that we reach that terminal node],
	 *                               [the probability that we reach the terminal node from the current node]
	 */
	public final void updateSampledRegret(Action[] actions, int sampledAction, GameNode.UtilityProbability utilProb) {
		// TODO: complete this code

		double w = utilProb.scaledUtility;
		synchronized (sumRegret) {
			for (int a = 0; a < actions.length; a++) {
				if (actions[a].infosetAndAction != null) {
					// TODO: Update the sum of the sampled regret in sumRegret, at the key
					// actions[a].infosetAndAction. Add an appropriate value to it, based on our
					// lecture notes.  Note it will be different depending on whether a == sampledAction
					double delta = a == sampledAction ?
							w * (1.0 - actions[sampledAction].p) * utilProb.pTail :
							-w * utilProb.pTail * actions[sampledAction].p;
					sumRegret.put(actions[a].infosetAndAction, sumRegret.getOrDefault(actions[a].infosetAndAction, 0.0) + delta);
					// End TODO
				}
			}
		}
	}

	/**
	 * Update sigma_bar, the average strategy for the player who's actions are not being sampled
	 *
	 * @param actions          strategy chosen by this player
	 * @param pi            the probability that the player whose actions are being sampled would
	 *                         play to this node
	 * //@param utilProb        a pair: <[the utility of sampled terminal node]/[probability that we reach that terminal node],
	 *                               [the probability that we reach the terminal node from the current node]
	 */
	public final void updateAverageStrategy(Action[] actions, double pi) {
		// TODO: complete this code
		synchronized (sumStrategy) {
			for (Action action: actions) {
				if (action.infosetAndAction != null)
					// TODO: For the key action.infosetAndAction, increment the value stored in sumStrategy by action.p scaled
					// appropriately (as we discussed in the lecture
					sumStrategy.put(action.infosetAndAction, sumStrategy.getOrDefault(action.infosetAndAction, 0.0) + action.p / pi);
				// End TODO
			}
		}
	}

	/**
	 * Output learned strategy
	 *
	 * @return a string representation of the strategy
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append(getName());
		sb.append("Key\tProbability\n");
		synchronized (sumStrategy) {
			for (String key : sumStrategy.keySet()) {
				sb.append(key);
				sb.append("\t");
				sb.append(String.format("%.6f", sumStrategy.get(key)));
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	public void toFile(String fname) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(fname);
		synchronized (sumRegret) {
			pw.println(sumRegret.size());
			for (String key: sumRegret.keySet()) {
				pw.println(key + "\t" + sumRegret.get(key));
			}
		}
		synchronized (sumStrategy) {
			pw.println(sumStrategy.size());
			for (String key: sumStrategy.keySet()) {
				pw.println(key + "\t" + sumStrategy.get(key));
			}
		}
		pw.close();
	}

	public void fromFile(String fname) throws FileNotFoundException {
		Scanner sc = new Scanner(new File(fname));
		int n = Integer.valueOf(sc.nextLine());
		synchronized (sumRegret) {
			for (int i = 0; i < n; i++) {
				String[] tokens = sc.nextLine().split("\t");
				sumRegret.put(tokens[0], Double.valueOf(tokens[1]));
			}
		}
		synchronized (sumStrategy) {
			n = Integer.valueOf(sc.nextLine());
			for (int i = 0; i < n; i++) {
				String[] tokens = sc.nextLine().split("\t");
				sumStrategy.put(tokens[0], Double.valueOf(tokens[1]));
			}
		}
		sc.close();
	}

	/**
	 * Normalize the probabilities in an array of actions so that the sum is equal to 1.0
	 *
	 * @param actions the array of actions to be normalized
	 */
	protected final void normalize(Action[] actions) {
		if (actions.length == 0) throw new RuntimeException("Cannot normalize an empty array");

		double sumP = 0.0;
		for (Action action: actions) {
			if (action.p < 0) throw new RuntimeException("Probabilities should not be negative");
			sumP += action.p;
		}

		if (sumP == 0.0) {
			// Set all probabilities to equal amount if all are 0
			for (Action action: actions) {
				action.p = 1.0 / actions.length;
			}
		}
		else {
			for (Action action: actions) {
				action.p = action.p / sumP;
			}
		}
	}
}