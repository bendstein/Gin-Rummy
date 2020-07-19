import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Implements a fixed discard strategy: 
 * 1)  We start by looking for “unmatchable” cards.  A card is unmatchable if it is not currently in a meld 
 *       AND it cannot be put in a meld even after drawing one more card AND it is worth at least seven points.
 * 2)  Among these unmatchable cards, we check for cards that are “dead”.  A card is dead if it cannot be put 
 *       into a meld by the opponent.
 * 3)  If there are unmatchable, dead cards, we pick the highest value of unmatchable, dead card to discard.
 * 4)  Else if there are unmatchable cards, we discard the highest value unmatchable card
 * 5)  Else we discard the card that leaves us with the least amount of deadwood.
 * 
 * @author jjb24
 */
public class StrategyDiscard extends Strategy {
	static final int DISCARD_THRESHOLD = 7; // Do not discard unmatchable cards, unless they are at least a seven

	/** 
	 * In the default strategy, we are not training
	 */
	public StrategyDiscard(boolean training) {
		super(training);
	}
	
	/**
	 * Return a pure strategy that looks first for dead cards (cards that cannot be used for a meld, then 
	 */
	@Override
	public ActionDiscard[] getStrategy(GameState state) {		
		ActionDiscard[] strategy = new ActionDiscard[1];

		//The card that is in our hand that wasn't last turn is the card we drew. We can't discard it.
		int drawn = MyGinRummyUtil.bitstringToIDArray(state.getPreviousState().getCurrentPlayerCards() ^ state.getCurrentPlayerCards())[0];

		long candidateCards =
				MyGinRummyUtil.findHighestDiscards(state.getCurrentPlayerCards(), drawn, state.getFaceUpCardAsObject().getId(), 0);
		long toRemove = 0L;
		ArrayList<Integer> ids = MyGinRummyUtil.bitstringToIDs(candidateCards);
		ArrayList<Integer> temp = new ArrayList<>(ids);
		long unseen = MyGinRummyUtil.removeAll(GinRummyUtil.cardsToBitstring(new ArrayList<Card>(Arrays.asList(Card.allCards))), state.getCurrentPlayerSeenCards());

		for (int i : ids) {
			temp.remove((Integer) i);
			if (!MyGinRummyUtil
					.contains((MyGinRummyUtil.getIsolatedSingles(state.getCurrentPlayerCards(), MyGinRummyUtil.idsToBitstring(temp), unseen)), i))
				toRemove = MyGinRummyUtil.add(toRemove, i);
			temp.add((Integer) i);
		}

		if (toRemove != candidateCards)
			candidateCards = MyGinRummyUtil.removeAll(candidateCards, toRemove);

		else {
			toRemove = 0L;

			for (int i : ids) {
				temp.remove((Integer) i);
				if (!MyGinRummyUtil
						.contains((MyGinRummyUtil.getSingles(state.getCurrentPlayerCards(), MyGinRummyUtil.idsToBitstring(temp), unseen)), i))
					toRemove = MyGinRummyUtil.add(toRemove, i);
				temp.add((Integer) i);
			}

			if (toRemove != candidateCards)
				candidateCards = MyGinRummyUtil.removeAll(candidateCards, toRemove);
		}

		/*
		 * Prefer cards who cannot be melded even after 2 draws. If there are none (or
		 * no cards can), prefer those who can't be melded after 1 draw.
		 */
		long not_singles = ~MyGinRummyUtil.getIsolatedSingles(state.getCurrentPlayerCards(), 0L, unseen);
		candidateCards = MyGinRummyUtil.removeAll(candidateCards, not_singles) == 0L ? candidateCards
				: MyGinRummyUtil.removeAll(candidateCards, not_singles);

		if (MyGinRummyUtil.removeAll(candidateCards, not_singles) == 0L) {
			not_singles = ~MyGinRummyUtil.getSingles(state.getCurrentPlayerCards(), 0L, unseen);
			candidateCards = MyGinRummyUtil.removeAll(candidateCards, not_singles) == 0L ? candidateCards
					: MyGinRummyUtil.removeAll(candidateCards, not_singles);
		}

		/*
		 * Then, filter out cards which would be helpful to the opponent
		 */
		if (MyGinRummyUtil.size(candidateCards) > 1) {
			toRemove = 0L; // Don't remove until after loop
			long preferred = 0L; // Cards we would prefer to remove

			//Cards we've seen that are no longer accessible
			long buried = MyGinRummyUtil
					.remove(MyGinRummyUtil
							.removeAll(MyGinRummyUtil
									.removeAll(state.getCurrentPlayerSeenCards(), state.getCurrentPlayerCards()), state.getCurrentPlayerKnownOpponentCards()), state.getFaceUpCardAsObject().getId());

			//Cards we've seen that the opponent doesn't have in their hand
			long oppDiscard = MyGinRummyUtil.add(MyGinRummyUtil.addAll(buried, state.getCurrentPlayerCards()), state.getFaceUpCardAsObject().getId());

			for (Card c : MyGinRummyUtil.bitstringToCards(candidateCards)) {

				/*
				 * If a card could be used in an opp meld, or at least bring them closer, avoid
				 * discarding it
				 */
				if (MyGinRummyUtil.canOpponentMeld(c, state.getCurrentPlayerKnownOpponentCards(), buried))
					toRemove = MyGinRummyUtil.add(toRemove, c.getId()); // If card could help opp meld, avoid tossing
				else if (MyGinRummyUtil.containsRank(state.getCurrentPlayerKnownOpponentCards(), c.getId())
						|| MyGinRummyUtil.containsSuit(state.getCurrentPlayerKnownOpponentCards(), c.getId(), 2))
					toRemove = MyGinRummyUtil.add(toRemove, c.getId()); // If card brings opp closer to a meld, avoid
					// tossing

					/*
					 * If the opp has discarded cards that could be melded with this card, it is
					 * less likely they would find it useful. Prefer to discard any of these cards.
					 */
				else if (MyGinRummyUtil.containsRank(oppDiscard, c.getId())
						|| MyGinRummyUtil.containsSuit(oppDiscard, c.getId(), 2))
					preferred = MyGinRummyUtil.add(preferred, c.getId()); // If similar cards have been tossed, prefer
			}

			if (toRemove != candidateCards)
				candidateCards = MyGinRummyUtil.removeAll(candidateCards, toRemove); // Remove useful cards to the
			// opponent, unless all cards would
			// be useful
			if (preferred != 0L && preferred != candidateCards)
				candidateCards = MyGinRummyUtil.removeAll(candidateCards, ~preferred); // Only consider cards which we
			// would prefer to discard

		}

		/*
		 * If there are more than 2 cards left, if any are dupled, avoid throwing them
		 * away
		 */
		if (MyGinRummyUtil.size(candidateCards) > 2) {
			ArrayList<Integer> cards = MyGinRummyUtil.bitstringToIDs(candidateCards);
			long duples = 0L;

			for (int i : cards)
				duples += MyGinRummyUtil.getDuples(candidateCards, i);

			if (MyGinRummyUtil.removeAll(candidateCards, duples) != 0L)
				candidateCards = MyGinRummyUtil.removeAll(candidateCards, duples);

		}

		strategy[0] = new ActionDiscard(MyGinRummyUtil.cardsToBitstring(new ArrayList<Card>(Collections.singletonList(MyGinRummyUtil.bitstringToCards(candidateCards).get(0)))), 1.0, null);

		return strategy;
	}


	/**
	 * @see Strategy#getName()
	 */
	@Override
	public String getName() {
		return "DefaultDiscardStrategy";
	}

}
