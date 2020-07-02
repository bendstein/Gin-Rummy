import java.io.FileNotFoundException;

public class DriverBlum {

	public static void main(String[] args) throws FileNotFoundException {
		final int TOTAL_ROUNDS = 5;
		final int TRAINING_GAMES_PER_ROUND = 500;
		final int EVALUATION_GAMES_PER_ROUND = 1;
        
		Player basePlayer = new Player(new StrategyDraw(false), new StrategyDiscard(false), new StrategyKnock(false));
		Player cfrKnockPlayer = new Player(new StrategyDraw(false), new StrategyDiscard(false), new StrategyKnock_Deadwood(false));

		for (int round = 0; round < TOTAL_ROUNDS; round++) {
			// Train for a while
			cfrKnockPlayer.getKnockStrategy().setTrain(true);
			double util = 0.0;
			Player[] players = new Player[]{cfrKnockPlayer, cfrKnockPlayer};
			for (int i = 0; i < TRAINING_GAMES_PER_ROUND; i++) {
				util += GameNodeBlum.playFrom(new GameState(), players, 1.0, 1.0);
			}
			System.out.println("Utility in training round " + (round+1) + " is " + util/TRAINING_GAMES_PER_ROUND);

			cfrKnockPlayer.getKnockStrategy().setTrain(false);
			util = 0.0;
			Player[] players0 = new Player[]{cfrKnockPlayer, basePlayer};
			Player[] players1 = new Player[]{basePlayer, cfrKnockPlayer};
			for (int i = 0; i < EVALUATION_GAMES_PER_ROUND; i+=2) {
				util += GameNodeBlum.playFrom(new GameState(), players0, 1.0, 1.0);
				util -= GameNodeBlum.playFrom(new GameState(), players1, 1.0, 1.0);
			}
			System.out.println("EV of CFR player vs. base player in " + (round+1) + " is " + util/EVALUATION_GAMES_PER_ROUND);
			
			cfrKnockPlayer.getKnockStrategy().toFile("CFR_Knock_Deadwood.txt");
		}

		System.out.println("\nEquilibrium Knocking week_5.Strategy");
		System.out.println(cfrKnockPlayer.getKnockStrategy().toString());
	}
}
