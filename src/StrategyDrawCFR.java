import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

public class StrategyDrawCFR extends StrategyDraw {

	private TreeSet<String> infosets;

	public StrategyDrawCFR(boolean training) {
		super(training);
		infosets = new TreeSet<>();
	}

	@Override
	public ActionDraw[] getStrategy(GameState state) {

		/*
		 * If change in deadwood is positive, and card makes a new meld, draw face-up.
		 */
		int improvement = PshUtil.getDeadwoodImprovementIfDrawFaceUpCard(state);
		if (improvement > 0 && PshUtil.doesFaceUpCardMakeNewMeld(state))
			return new ActionDraw[]{new ActionDraw(true, 1.0, null)};


		/*
		 * List of all unseen cards
		 */
		ArrayList<Card> unaccounted =
				GinRummyUtil.bitstringToCards(MyGinRummyUtil
						.removeAll(MyGinRummyUtil
								.cardsToBitstring(new ArrayList<>(Arrays.asList(Card.allCards))), state
								.getCurrentPlayerSeenCards()));

		/*
		 * Calculate the expected change in deadwood in our hand for drawing face-down.
		 */
		double evFaceDown = 0;

		for (Card card : unaccounted)
			evFaceDown += (1d / unaccounted.size()) *
					MyGinRummyUtil.getImprovement(GinRummyUtil.bitstringToCards(state.getCurrentPlayerCards()), card);

		/*
		 * If improvement in deadwood from drawing face-up is at least 6, and is higher than the
		 * expected improvement in deadwood for drawing face-down, draw the face-up.
		 */
		if(improvement >= 6 && improvement > evFaceDown)
			return new ActionDraw[]{new ActionDraw(true, 1.0, null)};

		/*
		 * The player's hand plus the face-up card
		 */
		long newCards = MyGinRummyUtil.add(state.getCurrentPlayerCards(), state.getFaceUpCardAsObject().getId());

		/*
		 * If we would consider discarding the face-up card, we probably shouldn't pick it up.
		 */
		long preferred = MyGinRummyUtil.findHighestDiscards(newCards, -1, -1, 1);
		if(MyGinRummyUtil.contains(preferred, state.getFaceUpCardAsObject().getId()))
			return new ActionDraw[]{new ActionDraw(false, 1.0, null)};

		/*
		 * If the card's deadwood is greater than 3, and it can't be melded even after 2 draws, don't draw it
		 */
		/*
		if(GinRummyUtil.bitstringToCards(MyGinRummyUtil
				.getIsolatedSingles(newCards, 0L, GinRummyUtil.cardsToBitstring(unaccounted))).contains(state.getFaceUpCardAsObject()))
			return new ActionDraw[]{new ActionDraw(false, 1.0, null)};

		 */

		/*
		 * If the card's deadwood is greater than 4, and it can't be melded even after 1 draws, don't draw it
		 */

		/*
		if(GinRummyUtil.bitstringToCards(MyGinRummyUtil
				.getSingles(newCards, 0L, GinRummyUtil.cardsToBitstring(unaccounted))).contains(state.getFaceUpCardAsObject()))
			return new ActionDraw[]{new ActionDraw(false, 1.0, null)};

		 */

		int minDrawsToMeld = 0;

		/*
		 * We couldn't meld this card even if we draw 2 more face-down cards
		 */
		if(GinRummyUtil.bitstringToCards(MyGinRummyUtil
				.getIsolatedSingles(newCards, 0L, GinRummyUtil.cardsToBitstring(unaccounted))).contains(state.getFaceUpCardAsObject()))
			minDrawsToMeld = 2;

		/*
		 * We couldn't meld this card even if we draw 1 more face-down card
		 */
		else if(GinRummyUtil.bitstringToCards(MyGinRummyUtil
				.getSingles(newCards, 0L, GinRummyUtil.cardsToBitstring(unaccounted))).contains(state.getFaceUpCardAsObject()))
			minDrawsToMeld = 1;

		/*
		 * Move inBestMeld out of CFR, make it into a heuristic
		 */

		/*
		boolean inBestMeld = false;
		@SuppressWarnings("unchecked")
		ArrayList<Card> cardsCopy = (ArrayList<Card>)state.getCurrentPlayerCardsAsList().clone();
		cardsCopy.add(state.getFaceUpCardAsObject());
		for(ArrayList<ArrayList<Card>> melds : GinRummyUtil.cardsToBestMeldSets(cardsCopy)) {
			for(ArrayList<Card> m : melds){
				if(m.contains(state.getFaceUpCardAsObject()))
					inBestMeld = true;
			}
		}

		 */

		/*
		 * Top Card isn't reliable. Make it so driver doesn't end early?????????
		 */
		/*
		 * Make quickknock into a heuristic and do cfr on the turn number????????
		 * Generalize improvement and put in ranges to decrease state space?
		 */
		/*
		if(PshUtil.getBestDeadwoodAfterDiscard(cardsCopy) < 10) {
			if(52 - state.getTopCard() > 26) {
				ActionDraw[] strategy = {new ActionDraw(true, 1.0, null)};
				return strategy;
			}
			else if(52 - state.getTopCard() <= 4) {
				ActionDraw[] strategy = {new ActionDraw(true, 1.0, null)};
				return strategy;
			}
		}

		 */

		//String infoset = improvement + "_" + state.getTopCard() + "_" + inBestMeld;

		/*
		 * After heuristics, consider: Improvement from drawing face-up.
		 * 							   The index of the top card in the deck.
		 * 							   The number of draws it would take to meld the card (0, 1, 2+).
		 */

		String infoset = improvement + "_" + state.getTopCard() + "_" + minDrawsToMeld;

		infosets.add(infoset);

		ActionDraw[] strategy = new ActionDraw[] {
				new ActionDraw(true, 0.0, infoset + "_y"),
				new ActionDraw(false, 0.0, infoset + "_n")
		};
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
		sb.append("\tdraw\tdon't\n");
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
