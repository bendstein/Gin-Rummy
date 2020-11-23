/**
 * An action taken at a knock decision point
 * 
 * @author jjb24
 */
public class ActionKnock extends Action {
	private boolean knock;
	
    /**
     * 	Constructor
     * 
     * @param knock do we knock
     * @param p the probability that this action will be taken in a strategy
     * @param infoset the infoset at the parent node of this action in the Game Tree
     */
	public ActionKnock(boolean knock, double p, String infoset) {
		super(p,infoset);
		this.knock = knock;
	}

	/**
	 * @return whether we knock
	 */
	public boolean isKnock() {
		return knock;
	}

	/**
	 * @param knock do we knock
	 */
	public void setKnock(boolean knock) {
		this.knock = knock;
	}}
