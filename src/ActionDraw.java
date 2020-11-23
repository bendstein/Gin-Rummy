/**
 * An action taken at a draw decision point
 * 
 * @author jjb24
 */
public class ActionDraw extends Action {
	private boolean draw;
	
    /**
     * 	Constructor
     * 
     * @param draw do we pick up the currently face up card
     * @param p the probability that this action will be taken in a strategy
     * @param infoset the infoset at the parent node of this action in the Game Tree
     */
	public ActionDraw(boolean draw, double p, String infoset) {
		super(p,infoset);
		this.draw = draw;
	}

	/**
	 * @return whether we pick up the currently face up card
	 */
	public boolean isDraw() {
		return draw;
	}

	/**
	 * @param draw do we pick up the currently face up card
	 */
	public void setDraw(boolean draw) {
		this.draw = draw;
	}
}
