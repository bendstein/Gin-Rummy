import java.io.FileNotFoundException;

public class Driver {

	private static Double util = 0.0;
	
	public static void main(String[] args) throws FileNotFoundException, InterruptedException {
		final int TOTAL_ROUNDS = 50;
		final int TRAINING_GAMES_PER_ROUND = 1_000_000;
		final int EVALUATION_GAMES_PER_ROUND = 100_000;
        final int CONCURRENT_THREADS = 16;
        
		Player basePlayer = new Player(new StrategyDraw(false), new StrategyDiscard(false), new StrategyKnock(false));
		Player cfrPlayer = new Player(new StrategyDrawCFR(false), new StrategyDiscardNew(false), new StrategyKnockCFR(false));
		
		for (int round = 0; round < TOTAL_ROUNDS; round++) {
			// Train for a while
			cfrPlayer.getKnockStrategy().setTrain(true);
			cfrPlayer.getDrawStrategy().setTrain(true);
			Player[] players = new Player[]{cfrPlayer, cfrPlayer};
			Thread[] threads = new Thread[CONCURRENT_THREADS];
			util = 0.0;
			for (int i = 0; i < TRAINING_GAMES_PER_ROUND; i+=CONCURRENT_THREADS) {
				for (int j = 0; j < CONCURRENT_THREADS; j++) {
						final int player = j % 2;
						threads[j] = new Thread(new Runnable() {
							@Override
							public void run() {
								GameNode.UtilityProbability utilProb = GameNode.playFrom(new GameState(), players, player, 1.0, false);
								double tmp = utilProb.scaledUtility * utilProb.pTail;
								synchronized (util) {util += tmp;}
							}
						});
						threads[j].start();
				}
				for (int j = 0; j < threads.length; j++) {
					if (threads[j] != null) threads[j].join();
				}
			}
			System.out.println("Utility in training round " + (round+1) + " is " + util/TRAINING_GAMES_PER_ROUND);
			
			cfrPlayer.getKnockStrategy().setTrain(false);
			cfrPlayer.getDrawStrategy().setTrain(false);
			util = 0.0;
			final Player[] players0 = new Player[]{cfrPlayer, basePlayer};
			final Player[] players1 = new Player[]{basePlayer, cfrPlayer};
			for (int i = 0; i < EVALUATION_GAMES_PER_ROUND; i+=2*CONCURRENT_THREADS) {
				for (int j = 0; j < CONCURRENT_THREADS; j++) {
					threads[j] = new Thread(new Runnable() {
						@Override
						public void run() {
							GameNode.UtilityProbability utilProb = GameNode.playFrom(new GameState(), players0, -1, 1.0, true);
							double tmp1 = utilProb.scaledUtility * utilProb.pTail;
							utilProb = GameNode.playFrom(new GameState(), players1, -1, 1.0, true);
							double tmp2 = -utilProb.scaledUtility * utilProb.pTail;
							synchronized (util) {util += tmp1 + tmp2;}
						}
					});
					threads[j].start();
				}
				for (int j = 0; j < threads.length; j++) {
					if (threads[j] != null) threads[j].join();
				}
			}
			System.out.println("EV of CFR player vs. base player in " + (round+1) + " is " + util/EVALUATION_GAMES_PER_ROUND);
			
			cfrPlayer.getDrawStrategy().toFile("CFR_Draw_Deadwood.txt");
			cfrPlayer.getKnockStrategy().toFile("CFR_Knock_Deadwood.txt");
			cfrPlayer.getDiscardStrategy().toFile("CFR_Discard_Deadwood.txt");
		}

		System.out.println("\nEquilibrium Drawing Strategy");
		System.out.println(cfrPlayer.getDrawStrategy().toString());		
		
		System.out.println("\nEquilibrium Knocking Strategy");
		System.out.println(cfrPlayer.getKnockStrategy().toString());

		System.out.println("\nEquilibrium Discarding Strategy");
		System.out.println(cfrPlayer.getDiscardStrategy().toString());
	}
}
