/**
 * An action taken at a discard decision point
 * 
 * @author jjb24
 */
public class ActionDiscard extends Action {
	private long cardAsBitString;
	
    /**
     * 	Constructor
     * 
     * @param cardAsBitstring a bitstring representation of the card to be discarded in this action
     * @param p the probability that this action will be taken in a strategy
     * @param infoset the infoset at the parent node of this action in the Game Tree
     */
	public ActionDiscard(long cardAsBitstring, double p, String infoset) {
		super(p, infoset);
		this.cardAsBitString = cardAsBitstring;
	}

	/**
	 * @return a bitstring representation of the card to be discarded in this action
	 */
	public long getCard(){
		return cardAsBitString;
	}
	
	/**
	 * @return the card to be discarded in this action
	 */
	public Card getCardAsObject() { 
		return PshUtil.bitStringToCard(cardAsBitString);
	}
	
	/**
	 * @param cardAsBitString a bitstring representation of the card to be discarded in this action
	 */
	public void setCard(long cardAsBitString) {
		this.cardAsBitString = cardAsBitString;
	}
}
