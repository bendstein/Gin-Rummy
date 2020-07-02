/**
 * An action in the game tree.  Includes description of action, and a String for the parent Infoset.
 *
 * @author jjb24
 */
public class Action {
	protected double p;         
	protected String infosetAndAction;   
	
    /**
     * 	Constructor
     * 
     * @param p the probability that this action will be taken in a strategy
     * @param infoset the infoset at the parent node of this action in the Game Tree
     */
	public Action(double p, String infoset) {
		this.p = p;
		this.infosetAndAction = infoset;
	}

	/**
	 * @return the probability that this action will be taken in a strategy
	 */
	public double getP() {
		return p;
	}
	/**
	 * 
	 * @param p the probability that this action will be taken in a strategy
	 */
	public void setP(double p) {
		this.p = p;
	}
	
	/**
	 * 
	 * @return the infoset at the parent node of this action in the Game Tree
	 */
	public String getInfoset() {
		return infosetAndAction;
	}
	
	/**
	 * 
	 * @param infoset the infoset at the parent node of this action in the Game Tree
	 */
	public void setInfoset(String infoset) {
		this.infosetAndAction = infoset;
	}
	
}
