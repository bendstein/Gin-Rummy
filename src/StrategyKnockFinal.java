import java.io.*;
import java.util.ArrayList;
import java.util.TreeSet;


public class StrategyKnockFinal extends StrategyKnock {
    private boolean verbose;
    private TreeSet<String> infosets;

    public StrategyKnockFinal(boolean training) {
        this(training, false);
        infosets = new TreeSet<>();
    }

    public StrategyKnockFinal(boolean training, boolean verbose) {
        super(training);
        this.verbose = verbose;
    }

    @Override
    public ActionKnock[] getStrategy(GameState state) {
        ArrayList<Card> myCards = state.getCurrentPlayerCardsAsList();
        ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(myCards);

        /*
         * Calculate our hand's deadwood.
         */
        int deadwood;
        if (bestMeldSets.isEmpty())
            deadwood = GinRummyUtil.getDeadwoodPoints(myCards);
        else
            deadwood = GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), myCards);

        ActionKnock[] strategy;

        /*
         * If deadwood > 10, we can't knock. Never knock.
         */
        if (deadwood  > GinRummyUtil.MAX_DEADWOOD)
            strategy = new ActionKnock[] {new ActionKnock(false, 1.0, null)};

        else {
            /*
             * Let CFR Minimization converge to a strategy using the following abstraction:
             * Our hand's current deadwood,
             * The number of cards remaining face-down, and
             * The deadwood of the previous state's face-up card, i.e. The opponent's last discard
             */
            long face_up = state.getPreviousState().getFaceUpCard();
            int faceUpDeadwood = GinRummyUtil.getDeadwoodPoints(GinRummyUtil.bitstringToCards(face_up));

            String infoset= deadwood + "_" + state.getTopCard() + "_" + faceUpDeadwood;

            infosets.add(infoset);

            strategy = new ActionKnock[] {new ActionKnock(true, 0.0,  infoset + "_k"),
                    new ActionKnock(false, 0.0, infoset + "_n")};
            getProbabilities(strategy);
        }
        return strategy;
    }

    @Override
    public String getName() {
        return "Research Knock Strategy";
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append(getName() + "\nKnocking Percent as a function of DeadWood\n");
        sb.append("\tknock\tdon't\n");
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

        sb.append(total_visits/2).append("\n");
        for(String s : infosets.descendingSet()) {
            String key_k = s + "_y";
            String key_n = s + "_n";

            if(frequencies.getOrDefault(key_k, 0L) + frequencies.getOrDefault(key_n, 0L) == 0) continue;

            String st = String.format("%s %.3f %d", s,
                    sumStrategy.getOrDefault(key_k,1.0) / (sumStrategy.getOrDefault(key_k,1.0) + sumStrategy.getOrDefault(key_n,1.0)),
                    frequencies.getOrDefault(key_k, 0L) + frequencies.getOrDefault(key_n, 0L));
            sb.append(st).append("\n");
        }

        PrintWriter pw = new PrintWriter(fname);
        pw.print(sb.toString());
        pw.close();
    }

}
