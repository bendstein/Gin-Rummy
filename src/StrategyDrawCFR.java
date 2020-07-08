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
		int improvement = PshUtil.getDeadwoodImprovementIfDrawFaceUpCard(state);

		//If the face-up card makes a new meld and improves deadwood, always draw it.
		if (improvement > 0 && PshUtil.doesFaceUpCardMakeNewMeld(state)) {
			ActionDraw[] strategy = {new ActionDraw(true, 1.0, null)};
			return strategy;
		}


		//Get the expected value of the top card in the face-down pile

		//Get all cards which have not yet been seen by the player
		long unaccounted = MyGinRummyUtil.cardsToBitstring(new ArrayList<>(Arrays.asList(Card.allCards)));
		unaccounted = MyGinRummyUtil.removeAll(unaccounted, state.getCurrentPlayerSeenCards());
		ArrayList<Card> unaccountedList = GinRummyUtil.bitstringToCards(unaccounted);
		double sum = 0;

		//Card probabilities are uniformly distributed, so probability is 1d/unaccountedList.size().
		for(Card card : unaccountedList)
			sum += 1d/unaccountedList.size() * MyGinRummyUtil.getImprovement(GinRummyUtil.bitstringToCards(state.getCurrentPlayerCards()), card);


		//Get the number of melds you can make with the face-up

		//All cards which are/could be available to you
		long available = unaccounted + state.getPlayerCards(state.getCurrentPlayer());
		int face_up = MyGinRummyUtil.bitstringToIDArray(state.getFaceUpCard())[0];
		ArrayList<Long> melds = new ArrayList<>();

		//All available cards of the same rank as id
		long sameRank = MyGinRummyUtil.getSameRank(available, face_up);
		int[] sameRankIds = MyGinRummyUtil.bitstringToIDArray(sameRank);

		//Add all potential same-rank melds to the list
		for(int i : sameRankIds) {
			for(int j : sameRankIds) {
				if(i != j) {
					long meld = MyGinRummyUtil.idsToBitstring(new int[]{i, j, face_up});
					if(!melds.contains(meld)) melds.add(meld);
				}
			}
		}

		//All available adjacent cards to id of the same suit
		long sameSuit = MyGinRummyUtil.getSameSuit(available, face_up, 1);
		int[] sameSuitIds = MyGinRummyUtil.bitstringToIDArray(sameSuit);

		//Add all potential same-suit melds to the list
		if(sameSuitIds.length == 2) melds.add(MyGinRummyUtil.add(MyGinRummyUtil.idsToBitstring(sameSuitIds), face_up));

		for(int i : sameSuitIds) {
			long adj = MyGinRummyUtil.getSameSuit(available, i, 1);
			int[] adjIds = MyGinRummyUtil.bitstringToIDArray(adj);
			if(adjIds.length == 2) melds.add(MyGinRummyUtil.add(MyGinRummyUtil.idsToBitstring(sameSuitIds), i));
		}

		int numberOfMelds = melds.size();

		String infosetDraw = improvement + "_" + (int) Math.round(sum) + "_" + numberOfMelds;
		String infosetDont = improvement + "_" + (int) Math.round(sum) + "_" + numberOfMelds;

		infosets.add(infosetDraw);
		infosets.add(infosetDont);

		ActionDraw[] strategy = new ActionDraw[] {new ActionDraw(true, 0.0, infosetDraw + "_y"), new ActionDraw(false, 0.0, infosetDont + "_n")};
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
