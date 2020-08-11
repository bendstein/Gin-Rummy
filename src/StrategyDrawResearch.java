
public class StrategyDrawResearch extends StrategyDraw {

    /**
     * In the default strategy, we are not training
     */
    public StrategyDrawResearch(boolean training) {
        super(training);
    }

    @Override
    public ActionDraw[] getStrategy(GameState state) {
        ActionDraw[] strategy = new ActionDraw[1];
        GinRummyAndTonic_v10.State state2 = state.getCurrentPlayerObject().state2;

        /*
         * Let the improvement from drawing card c be defined as the change in deadwood
         * from drawing the card c and discarding the card c_1 that would give the hand
         * the lowest possible deadwood.
         *
         * A positive improvement indicates that the deadwood from performing the described
         * set of actions is better than previously. 0 indicates it is the same, and -1
         * indicates that it is worse.
         *
         * If the improvement from drawing the face_up card is positive, and it makes a new meld,
         * draw it. Otherwise, draw face-down.
         */

        if(GinRummyPlayerImpl.MyGinRummyUtil.getImprovement(state2.getHand(), state2.getFaceUp()) > 0 &&
                GinRummyPlayerImpl.MyGinRummyUtil.makesNewMeld(state2.getHand(), state2.getFaceUp())) {
            strategy[0] = new ActionDraw(true, 1.0, null);
        }
        else {
            strategy[0] = new ActionDraw(false, 1.0, null);
        }
        return strategy;
    }

    @Override
    public String getName() {
        return "Research Draw Strategy";
    }

}
