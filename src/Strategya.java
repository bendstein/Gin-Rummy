import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * The superclass for a CFR strategy

 * @author jjb24
 */
public abstract class Strategya {
	protected boolean training; // Are we training or not

	protected Map<String, Double> sumRegret = new HashMap<>();     // The sum of the counterfactual regret for a string equal to the infoset plus the action
	protected Map<String, Double> sumStrategy = new TreeMap<>();   // From sigma_bar^t for a string equal to the infoset and the action
																	// This is just the numerator of the equation given

	/**
	 * Constructor
	 *
	 * @param train Are we accumulating regrets
	 */
	public Strategya(boolean train) {
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

		//First, get the sum of all the regrets for the information set
		double sum = 0.0;
		for (Action action: actions) {
			if (action.infosetAndAction == null) {
				throw new IllegalArgumentException("The infoset for the actions must be set");
			}

			sum += sumRegret.getOrDefault(action.infosetAndAction, 0.0);
		}

		//Then, set each p as the action's regret/sum
		for(Action action : actions) {
			if(sumRegret.getOrDefault(action.infosetAndAction, 0.0) < 0)
				action.p = 0;
			else action.p = sum <= 0.0? 1.0/actions.length : sumRegret.get(action.infosetAndAction)/sum;
		}
		normalize(actions);
	}

	/**
	 * Fill in the probabilities in action, based on the average profiles over training runs
	 *
	 * @param actions the actions to be taken
	 */
	private final void getLearnedStrategy(Action[] actions) {

		//First, get the sum of all the strategies for the information set
		double sum = 0.0;
		for (Action action: actions) {
			if (action.infosetAndAction == null) {
				throw new IllegalArgumentException("The infoset for the actions must be set");
			}

			sum += sumStrategy.getOrDefault(action.infosetAndAction, 0.0);
		}

		//Then, set each p as the action's strategy/sum
		for(Action action : actions) {
			action.p = sum == 0.0? 1.0/actions.length : sumStrategy.get(action.infosetAndAction)/sum;
		}
		normalize(actions);
	}
	
	/**
	 * Having processed a subtree in the game tree, update the regret if we are training and return the utility 
	 * 
	 * @param actions  strategy chosen by this player
	 * @param utils    utilities for each of the chosen actions
	 * @param myProb   the probability that current player would play to this node
	 * @param otherProb  the probability that the other player would play to this node
	 * 
	 * @return the utility of this strategy (assuming that we played to this game state with probability of 1)
	 */
	public final double postProcess(Action[] actions, double[] utils, double myProb, double otherProb) {
		if (training) {
			updateRegret(actions, utils, myProb, otherProb);
		}
		
		return getUtil(actions,utils);
	}

	/**
	 * Update counterfactual regret at the infoset and cumulative strategy
	 *
	 * @param actions  strategy chosen by this player
	 * @param utils    utilities for each of the chosen actions
	 * @param myProb   the probability that current player would play to this node
	 * @param otherProb  the probability that the other player would play to this node
	 */
	private final void updateRegret(Action[] actions, double[] utils, double myProb, double otherProb) {
		if (utils.length != actions.length) throw new IllegalArgumentException("The actions and utils array must be parallel arrays");

		// Keep track of cumulative strategy
		for (Action action: actions) {
			if (action.infosetAndAction != null) {
				//If we've seen this information set before, update it
				if(sumStrategy.containsKey(action.infosetAndAction))
					sumStrategy.replace(action.infosetAndAction,
						sumStrategy.get(action.infosetAndAction) + (myProb * action.p));
				//Otherwise, visit each action equally as often
				else
					sumStrategy.put(action.infosetAndAction, myProb * action.p);
			}
		}

		double expectedUtilityAtThisNode = getUtil(actions, utils);

		// Update counterfactual regret
		for (int i = 0; i < utils.length; i++) {
			double cfr = sumRegret.getOrDefault(actions[i].infosetAndAction, 0.0);

			if (actions[i].infosetAndAction != null) {
				cfr += (utils[i] - expectedUtilityAtThisNode) * otherProb;
			} else if (actions.length > 1) {
				throw new RuntimeException("The infoset property of the action must be set if there is more than one action taken");
			}

			if(sumRegret.containsKey(actions[i].infosetAndAction)) sumRegret.replace(actions[i].infosetAndAction, cfr);
			else sumRegret.put(actions[i].infosetAndAction, cfr);
		}

	}

	/**
	 * Get the utility of the current strategy at this node
	 * 
	 * @param actions  strategy chosen by this player
	 * @param utils    utilities for each of the chosen actions
	 * 
	 * @return the utility of this strategy (assuming that we played to this game state with probability of 1)
	 */
	private final double getUtil(Action[] actions, double[] utils) {
		if (utils.length != actions.length) throw new IllegalArgumentException("The actions and utils array must be parallel arrays");

		// Calculated the expected utility at this node
		double nodeUtil = 0.0;
		for (int i = 0; i < utils.length; i++) {
			nodeUtil += utils[i] * actions[i].p;
		}
		
        return nodeUtil;
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
	    for (String key : sumStrategy.keySet()) {
	        sb.append(key);
	        sb.append("\t");
	        sb.append(String.format("%.6f", sumStrategy.get(key)));
	        sb.append("\n");
	    }
	    
	    return sb.toString();
	}

	public void toFile(String fname) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(fname);
		pw.println(sumRegret.size());
		for (String key: sumRegret.keySet()) {
			pw.println(key + "\t" + sumRegret.get(key));
		}
		pw.println(sumStrategy.size());
		for (String key: sumStrategy.keySet()) {
			pw.println(key + "\t" + sumStrategy.get(key));
		}
		pw.close();
	}
	
	public void fromFile(String fname) throws FileNotFoundException {
		Scanner sc = new Scanner(new File(fname));
		int n = Integer.valueOf(sc.nextLine());
		for (int i = 0; i < n; i++) {
			String[] tokens = sc.nextLine().split("\t");
			sumRegret.put(tokens[0], Double.valueOf(tokens[1]));
		}
		n = Integer.valueOf(sc.nextLine());
		for (int i = 0; i < n; i++) {
			String[] tokens = sc.nextLine().split("\t");
			sumStrategy.put(tokens[0], Double.valueOf(tokens[1]));
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

