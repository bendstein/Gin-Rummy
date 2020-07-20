import java.util.*;
import java.util.stream.Collectors;

/**
 * Class containing the player
 */
public class BJG5493GinRummyPlayerV2 implements GinRummyPlayer {

    //<editor-fold desc="Instance Variables">
    /**
     * The number assigned to our player
     */
    private int playerNum;

    /**
     * prng
     */
    private Random random = new Random();

    /**
     * Becomes true if opponent has knocked.
     */
    private boolean opponentKnocked = false;

    /**
     * The id of the card which was drawn
     */
    private int drawn;

    /**
     * The current state of the game
     */
    private State state;

    /**
     * Parameters used for different decision points
     */
    private GeneralStrategy generalStrategy;

    /**
     * If the opponent knocks, this is what their final meld set is
     */
    private ArrayList<ArrayList<Card>> oppMelds;
    //</editor-fold>

    public BJG5493GinRummyPlayerV2() {

        //Information set recorded including index into deck, but only considering deadwood here
        HashMap<String, Double> knockStrat = new HashMap<String, Double>() {
            {
                put("9_39", 0.000);
                put("9_38", 0.000);
                put("9_37", 0.000);
                put("9_36", 0.000);
                put("9_35", 0.000);
                put("9_34", 0.000);
                put("9_33", 0.000);
                put("9_32", 0.000);
                put("9_31", 0.000);
                put("9_30", 0.000);
                put("9_29", 0.000);
                put("9_28", 0.000);
                put("9_27", 0.000);
                put("9_26", 0.000);
                put("9_25", 0.000);
                put("9_24", 0.001);
                put("9_23", 0.009);
                put("9_22", 0.000);
                put("9_21", 0.000);
                put("8_39", 0.004);
                put("8_38", 0.000);
                put("8_37", 0.000);
                put("8_36", 0.000);
                put("8_35", 0.000);
                put("8_34", 0.000);
                put("8_33", 0.000);
                put("8_32", 0.000);
                put("8_31", 0.000);
                put("8_30", 0.000);
                put("8_29", 0.000);
                put("8_28", 0.000);
                put("8_27", 0.000);
                put("8_26", 0.000);
                put("8_25", 0.001);
                put("8_24", 1.000);
                put("8_23", 0.000);
                put("8_22", 0.000);
                put("8_21", 0.000);
                put("7_39", 0.005);
                put("7_38", 0.000);
                put("7_37", 0.000);
                put("7_36", 0.000);
                put("7_35", 0.000);
                put("7_34", 0.000);
                put("7_33", 0.000);
                put("7_32", 0.000);
                put("7_31", 0.000);
                put("7_30", 0.000);
                put("7_29", 0.000);
                put("7_28", 0.000);
                put("7_27", 0.000);
                put("7_26", 0.000);
                put("7_25", 0.000);
                put("7_24", 0.000);
                put("7_23", 1.000);
                put("7_22", 0.000);
                put("7_21", 0.014);
                put("6_39", 0.011);
                put("6_38", 0.005);
                put("6_37", 0.001);
                put("6_36", 0.000);
                put("6_35", 0.000);
                put("6_34", 0.000);
                put("6_33", 0.000);
                put("6_32", 0.000);
                put("6_31", 0.000);
                put("6_30", 0.000);
                put("6_29", 0.000);
                put("6_28", 0.000);
                put("6_27", 0.000);
                put("6_26", 0.000);
                put("6_25", 0.000);
                put("6_24", 0.000);
                put("6_23", 0.000);
                put("6_22", 0.000);
                put("6_21", 0.000);
                put("5_39", 0.010);
                put("5_38", 0.010);
                put("5_37", 0.000);
                put("5_36", 0.000);
                put("5_35", 0.000);
                put("5_34", 0.000);
                put("5_33", 0.000);
                put("5_32", 0.000);
                put("5_31", 0.000);
                put("5_30", 0.000);
                put("5_29", 0.000);
                put("5_28", 0.000);
                put("5_27", 0.000);
                put("5_26", 0.000);
                put("5_25", 0.000);
                put("5_24", 0.000);
                put("5_23", 0.000);
                put("5_22", 0.000);
                put("5_21", 0.138);
                put("4_39", 0.005);
                put("4_38", 1.000);
                put("4_37", 0.000);
                put("4_36", 0.000);
                put("4_35", 0.000);
                put("4_34", 0.000);
                put("4_33", 0.000);
                put("4_32", 0.000);
                put("4_31", 0.000);
                put("4_30", 0.000);
                put("4_29", 0.000);
                put("4_28", 0.000);
                put("4_27", 1.000);
                put("4_26", 0.000);
                put("4_25", 0.000);
                put("4_24", 0.000);
                put("4_23", 0.000);
                put("4_22", 0.000);
                put("4_21", 0.000);
                put("3_39", 1.000);
                put("3_38", 0.006);
                put("3_37", 0.042);
                put("3_36", 0.000);
                put("3_35", 0.001);
                put("3_34", 0.000);
                put("3_33", 0.000);
                put("3_32", 0.000);
                put("3_31", 0.000);
                put("3_30", 0.000);
                put("3_29", 0.000);
                put("3_28", 0.000);
                put("3_27", 0.000);
                put("3_26", 0.000);
                put("3_25", 0.000);
                put("3_24", 0.000);
                put("3_23", 0.000);
                put("3_22", 0.000);
                put("3_21", 0.000);
                put("2_39", 0.013);
                put("2_38", 0.090);
                put("2_37", 0.023);
                put("2_36", 0.000);
                put("2_35", 0.000);
                put("2_34", 0.000);
                put("2_33", 0.000);
                put("2_32", 0.000);
                put("2_31", 0.000);
                put("2_30", 0.000);
                put("2_29", 0.000);
                put("2_28", 0.000);
                put("2_27", 0.000);
                put("2_26", 0.000);
                put("2_25", 0.000);
                put("2_24", 0.000);
                put("2_23", 0.000);
                put("2_22", 0.000);
                put("2_21", 0.000);
                put("1_39", 0.009);
                put("1_38", 0.057);
                put("1_37", 0.000);
                put("1_36", 0.000);
                put("1_35", 0.000);
                put("1_34", 0.002);
                put("1_33", 0.000);
                put("1_32", 0.000);
                put("1_31", 0.000);
                put("1_30", 0.000);
                put("1_29", 0.000);
                put("1_28", 0.000);
                put("1_27", 0.000);
                put("1_26", 0.000);
                put("1_25", 0.000);
                put("1_24", 0.000);
                put("1_23", 0.000);
                put("1_22", 0.000);
                put("1_21", 0.000);
                put("10_39", 0.000);
                put("10_38", 0.000);
                put("10_37", 0.000);
                put("10_36", 0.000);
                put("10_35", 0.000);
                put("10_34", 0.000);
                put("10_33", 0.000);
                put("10_32", 0.000);
                put("10_31", 0.000);
                put("10_30", 0.000);
                put("10_29", 0.000);
                put("10_28", 0.000);
                put("10_27", 0.000);
                put("10_26", 0.000);
                put("10_25", 0.000);
                put("10_24", 0.000);
                put("10_23", 0.001);
                put("10_22", 0.000);
                put("10_21", 1.000);

            }
        };

        HashMap<String, Double> drawStrat = new HashMap<String, Double>() {
            {
                put("9_9_3", 0.023);
                put("9_9_1", 0.114);
                put("9_8_3", 0.567);
                put("9_8_1", 1.000);
                put("9_8_0", 0.000);
                put("9_7_3", 0.044);
                put("9_7_1", 0.438);
                put("9_7_0", 0.500);
                put("9_6_3", 0.828);
                put("9_6_1", 0.938);
                put("9_6_0", 0.500);
                put("9_5_3", 0.625);
                put("9_5_1", 0.512);
                put("9_5_0", 1.000);
                put("9_4_3", 0.953);
                put("9_4_1", 0.697);
                put("9_4_0", 0.500);
                put("9_3_3", 0.592);
                put("9_3_1", 1.000);
                put("9_14_3", 0.073);
                put("9_13_3", 0.705);
                put("9_13_1", 0.500);
                put("9_12_3", 0.118);
                put("9_12_1", 0.000);
                put("9_11_3", 0.336);
                put("9_11_1", 0.000);
                put("9_10_3", 0.120);
                put("9_10_1", 0.852);
                put("9_10_0", 0.500);
                put("8_9_4", 0.827);
                put("8_9_3", 0.526);
                put("8_9_2", 0.327);
                put("8_9_1", 0.418);
                put("8_8_4", 0.988);
                put("8_8_3", 0.043);
                put("8_8_2", 0.438);
                put("8_8_1", 0.327);
                put("8_7_4", 0.916);
                put("8_7_3", 0.458);
                put("8_7_2", 0.813);
                put("8_7_1", 0.611);
                put("8_7_0", 0.500);
                put("8_6_4", 0.267);
                put("8_6_3", 0.132);
                put("8_6_2", 0.951);
                put("8_6_1", 0.310);
                put("8_6_0", 0.500);
                put("8_5_4", 0.918);
                put("8_5_3", 0.450);
                put("8_5_2", 0.919);
                put("8_5_1", 0.285);
                put("8_5_0", 1.000);
                put("8_4_4", 0.409);
                put("8_4_3", 0.951);
                put("8_4_2", 0.210);
                put("8_4_1", 0.101);
                put("8_4_0", 0.500);
                put("8_3_4", 0.186);
                put("8_3_3", 0.290);
                put("8_3_2", 0.000);
                put("8_3_1", 0.273);
                put("8_3_0", 0.439);
                put("8_2_3", 0.338);
                put("8_2_2", 0.500);
                put("8_2_1", 0.500);
                put("8_15_4", 1.000);
                put("8_15_3", 0.500);
                put("8_14_4", 0.033);
                put("8_13_4", 0.881);
                put("8_13_3", 0.500);
                put("8_13_2", 0.000);
                put("8_12_4", 0.166);
                put("8_12_3", 1.000);
                put("8_12_2", 0.094);
                put("8_12_1", 0.500);
                put("8_11_4", 0.202);
                put("8_11_3", 0.934);
                put("8_11_2", 0.000);
                put("8_11_1", 0.500);
                put("8_10_4", 0.310);
                put("8_10_3", 0.295);
                put("8_10_2", 0.466);
                put("8_10_1", 0.411);
                put("7_9_4", 0.083);
                put("7_9_3", 0.473);
                put("7_9_2", 0.499);
                put("7_9_1", 0.828);
                put("7_8_4", 0.549);
                put("7_8_3", 0.400);
                put("7_8_2", 0.999);
                put("7_8_1", 1.000);
                put("7_8_0", 0.500);
                put("7_7_4", 0.457);
                put("7_7_3", 0.707);
                put("7_7_2", 0.832);
                put("7_7_1", 0.018);
                put("7_6_4", 0.977);
                put("7_6_3", 0.922);
                put("7_6_2", 0.259);
                put("7_6_1", 0.506);
                put("7_6_0", 1.000);
                put("7_5_4", 0.690);
                put("7_5_3", 0.790);
                put("7_5_2", 0.827);
                put("7_5_1", 0.031);
                put("7_5_0", 1.000);
                put("7_4_4", 0.973);
                put("7_4_3", 0.065);
                put("7_4_2", 0.064);
                put("7_4_1", 0.057);
                put("7_4_0", 0.000);
                put("7_3_4", 0.647);
                put("7_3_3", 0.920);
                put("7_3_2", 0.004);
                put("7_3_1", 0.832);
                put("7_3_0", 0.500);
                put("7_2_4", 0.201);
                put("7_2_3", 0.249);
                put("7_2_2", 0.500);
                put("7_2_1", 0.260);
                put("7_2_0", 0.500);
                put("7_15_4", 0.993);
                put("7_15_2", 0.500);
                put("7_14_4", 0.884);
                put("7_14_2", 0.500);
                put("7_13_4", 0.711);
                put("7_13_3", 0.500);
                put("7_13_2", 0.000);
                put("7_13_1", 0.500);
                put("7_12_4", 0.145);
                put("7_12_3", 0.000);
                put("7_12_2", 0.257);
                put("7_11_4", 0.607);
                put("7_11_3", 0.060);
                put("7_11_2", 0.945);
                put("7_11_1", 0.500);
                put("7_10_4", 0.591);
                put("7_10_3", 0.048);
                put("7_10_2", 0.716);
                put("7_10_1", 0.022);
                put("7_10_0", 0.500);
                put("6_9_4", 0.063);
                put("6_9_3", 0.968);
                put("6_9_2", 0.820);
                put("6_9_1", 0.061);
                put("6_9_0", 0.500);
                put("6_8_4", 0.815);
                put("6_8_3", 0.860);
                put("6_8_2", 0.399);
                put("6_8_1", 0.050);
                put("6_8_0", 0.500);
                put("6_7_4", 0.219);
                put("6_7_3", 0.758);
                put("6_7_2", 0.881);
                put("6_7_1", 0.386);
                put("6_7_0", 1.000);
                put("6_6_4", 0.897);
                put("6_6_3", 0.336);
                put("6_6_2", 0.704);
                put("6_6_1", 0.998);
                put("6_6_0", 0.939);
                put("6_5_4", 0.781);
                put("6_5_3", 0.838);
                put("6_5_2", 0.046);
                put("6_5_1", 0.031);
                put("6_5_0", 0.700);
                put("6_4_4", 0.419);
                put("6_4_3", 0.517);
                put("6_4_2", 0.022);
                put("6_4_1", 0.401);
                put("6_4_0", 0.166);
                put("6_3_4", 0.260);
                put("6_3_3", 0.266);
                put("6_3_2", 0.846);
                put("6_3_1", 0.339);
                put("6_3_0", 0.553);
                put("6_2_4", 0.607);
                put("6_2_3", 0.929);
                put("6_2_2", 0.353);
                put("6_2_1", 0.088);
                put("6_2_0", 1.000);
                put("6_1_3", 0.793);
                put("6_15_4", 0.000);
                put("6_14_4", 0.788);
                put("6_14_3", 0.500);
                put("6_14_2", 0.500);
                put("6_13_4", 0.896);
                put("6_13_3", 0.000);
                put("6_13_2", 0.399);
                put("6_13_1", 0.500);
                put("6_13_0", 0.500);
                put("6_12_4", 0.247);
                put("6_12_3", 0.437);
                put("6_12_2", 0.759);
                put("6_12_1", 0.500);
                put("6_11_4", 0.543);
                put("6_11_3", 0.012);
                put("6_11_2", 0.746);
                put("6_11_1", 0.754);
                put("6_10_4", 0.636);
                put("6_10_3", 0.736);
                put("6_10_2", 0.030);
                put("6_10_1", 0.482);
                put("5_9_4", 0.525);
                put("5_9_3", 0.169);
                put("5_9_2", 0.789);
                put("5_9_1", 0.203);
                put("5_9_0", 0.353);
                put("5_8_4", 0.340);
                put("5_8_3", 0.024);
                put("5_8_2", 0.485);
                put("5_8_1", 0.265);
                put("5_8_0", 0.112);
                put("5_7_4", 0.380);
                put("5_7_3", 0.962);
                put("5_7_2", 0.839);
                put("5_7_1", 0.100);
                put("5_7_0", 0.055);
                put("5_6_4", 0.719);
                put("5_6_3", 0.555);
                put("5_6_2", 0.346);
                put("5_6_1", 0.258);
                put("5_6_0", 0.829);
                put("5_5_4", 0.949);
                put("5_5_3", 0.009);
                put("5_5_2", 0.357);
                put("5_5_1", 0.243);
                put("5_5_0", 0.138);
                put("5_4_4", 0.803);
                put("5_4_3", 0.011);
                put("5_4_2", 0.577);
                put("5_4_1", 0.054);
                put("5_4_0", 0.002);
                put("5_3_4", 0.860);
                put("5_3_3", 0.622);
                put("5_3_2", 0.583);
                put("5_3_1", 0.011);
                put("5_3_0", 0.819);
                put("5_2_4", 0.975);
                put("5_2_3", 0.879);
                put("5_2_2", 0.163);
                put("5_2_1", 0.208);
                put("5_2_0", 0.608);
                put("5_1_4", 0.321);
                put("5_1_3", 0.980);
                put("5_1_1", 0.027);
                put("5_1_0", 0.500);
                put("5_15_4", 0.500);
                put("5_15_2", 0.500);
                put("5_15_1", 0.500);
                put("5_14_4", 0.426);
                put("5_14_3", 0.500);
                put("5_14_2", 1.000);
                put("5_13_4", 0.518);
                put("5_13_3", 0.295);
                put("5_13_2", 0.478);
                put("5_13_1", 0.500);
                put("5_12_4", 0.637);
                put("5_12_3", 0.266);
                put("5_12_2", 0.545);
                put("5_12_1", 1.000);
                put("5_11_4", 0.462);
                put("5_11_3", 0.389);
                put("5_11_2", 0.337);
                put("5_11_1", 0.438);
                put("5_11_0", 0.974);
                put("5_10_4", 0.274);
                put("5_10_3", 0.358);
                put("5_10_2", 0.582);
                put("5_10_1", 0.895);
                put("5_10_0", 0.000);
                put("4_9_4", 0.074);
                put("4_9_3", 0.172);
                put("4_9_2", 0.934);
                put("4_9_1", 0.316);
                put("4_9_0", 0.081);
                put("4_8_4", 0.105);
                put("4_8_3", 0.210);
                put("4_8_2", 0.899);
                put("4_8_1", 0.828);
                put("4_8_0", 0.381);
                put("4_7_4", 0.168);
                put("4_7_3", 0.007);
                put("4_7_2", 0.991);
                put("4_7_1", 0.342);
                put("4_7_0", 0.962);
                put("4_6_4", 0.377);
                put("4_6_3", 0.056);
                put("4_6_2", 0.477);
                put("4_6_1", 0.687);
                put("4_6_0", 0.739);
                put("4_5_4", 0.929);
                put("4_5_3", 0.124);
                put("4_5_2", 0.460);
                put("4_5_1", 0.007);
                put("4_5_0", 0.969);
                put("4_4_4", 0.627);
                put("4_4_3", 0.095);
                put("4_4_2", 0.388);
                put("4_4_1", 0.047);
                put("4_4_0", 0.410);
                put("4_3_4", 0.761);
                put("4_3_3", 0.000);
                put("4_3_2", 0.943);
                put("4_3_1", 0.064);
                put("4_3_0", 0.622);
                put("4_2_4", 0.028);
                put("4_2_3", 0.201);
                put("4_2_2", 0.815);
                put("4_2_1", 0.015);
                put("4_2_0", 0.000);
                put("4_1_4", 0.996);
                put("4_1_3", 0.956);
                put("4_1_2", 0.154);
                put("4_1_1", 0.381);
                put("4_1_0", 0.584);
                put("4_16_2", 0.500);
                put("4_15_4", 0.500);
                put("4_15_3", 0.500);
                put("4_15_2", 0.500);
                put("4_14_4", 0.388);
                put("4_14_3", 0.074);
                put("4_14_2", 1.000);
                put("4_14_1", 0.500);
                put("4_13_4", 0.388);
                put("4_13_3", 0.895);
                put("4_13_2", 0.789);
                put("4_13_1", 0.230);
                put("4_13_0", 0.500);
                put("4_12_4", 0.028);
                put("4_12_3", 0.666);
                put("4_12_2", 0.865);
                put("4_12_1", 0.454);
                put("4_12_0", 0.000);
                put("4_11_4", 0.298);
                put("4_11_3", 0.095);
                put("4_11_2", 0.145);
                put("4_11_1", 0.976);
                put("4_11_0", 1.000);
                put("4_10_4", 0.019);
                put("4_10_3", 0.013);
                put("4_10_2", 0.004);
                put("4_10_1", 0.955);
                put("4_10_0", 0.598);
                put("3_9_4", 0.023);
                put("3_9_3", 0.271);
                put("3_9_2", 0.653);
                put("3_9_1", 0.077);
                put("3_9_0", 0.878);
                put("3_8_4", 0.590);
                put("3_8_3", 0.143);
                put("3_8_2", 0.896);
                put("3_8_1", 0.500);
                put("3_8_0", 0.992);
                put("3_7_4", 0.502);
                put("3_7_3", 0.341);
                put("3_7_2", 0.455);
                put("3_7_1", 0.797);
                put("3_7_0", 0.165);
                put("3_6_4", 0.021);
                put("3_6_3", 0.942);
                put("3_6_2", 0.386);
                put("3_6_1", 0.294);
                put("3_6_0", 0.458);
                put("3_5_4", 0.104);
                put("3_5_3", 0.000);
                put("3_5_2", 0.172);
                put("3_5_1", 0.004);
                put("3_5_0", 0.051);
                put("3_4_4", 0.542);
                put("3_4_3", 0.000);
                put("3_4_2", 0.076);
                put("3_4_1", 0.022);
                put("3_4_0", 0.137);
                put("3_3_4", 0.030);
                put("3_3_3", 0.641);
                put("3_3_2", 0.579);
                put("3_3_1", 0.005);
                put("3_3_0", 0.448);
                put("3_2_4", 0.379);
                put("3_2_3", 0.033);
                put("3_2_2", 0.020);
                put("3_2_1", 0.048);
                put("3_2_0", 0.735);
                put("3_1_4", 0.853);
                put("3_1_3", 0.205);
                put("3_1_2", 0.754);
                put("3_1_1", 0.534);
                put("3_1_0", 0.017);
                put("3_15_4", 0.969);
                put("3_15_2", 0.500);
                put("3_15_1", 0.500);
                put("3_14_4", 0.076);
                put("3_14_3", 0.909);
                put("3_14_2", 0.634);
                put("3_14_1", 0.793);
                put("3_14_0", 0.500);
                put("3_13_4", 0.696);
                put("3_13_3", 0.958);
                put("3_13_2", 0.369);
                put("3_13_1", 0.437);
                put("3_13_0", 0.962);
                put("3_12_4", 0.863);
                put("3_12_3", 0.874);
                put("3_12_2", 0.080);
                put("3_12_1", 0.709);
                put("3_12_0", 0.037);
                put("3_11_4", 0.010);
                put("3_11_3", 0.021);
                put("3_11_2", 0.104);
                put("3_11_1", 0.999);
                put("3_11_0", 0.929);
                put("3_10_4", 0.850);
                put("3_10_3", 0.264);
                put("3_10_2", 0.043);
                put("3_10_1", 0.229);
                put("3_10_0", 0.650);
                put("3_0_3", 0.094);
                put("3_0_1", 0.959);
                put("3_0_0", 0.500);
                put("2_9_4", 0.000);
                put("2_9_3", 0.622);
                put("2_9_2", 0.251);
                put("2_9_1", 0.056);
                put("2_9_0", 0.263);
                put("2_8_4", 0.042);
                put("2_8_3", 0.292);
                put("2_8_2", 0.004);
                put("2_8_1", 0.017);
                put("2_8_0", 0.008);
                put("2_7_4", 0.229);
                put("2_7_3", 0.229);
                put("2_7_2", 0.260);
                put("2_7_1", 0.066);
                put("2_7_0", 0.838);
                put("2_6_4", 0.003);
                put("2_6_3", 0.383);
                put("2_6_2", 0.227);
                put("2_6_1", 0.004);
                put("2_6_0", 0.050);
                put("2_5_4", 0.001);
                put("2_5_3", 0.076);
                put("2_5_2", 0.097);
                put("2_5_1", 0.000);
                put("2_5_0", 0.011);
                put("2_4_4", 0.001);
                put("2_4_3", 0.002);
                put("2_4_2", 0.015);
                put("2_4_1", 0.023);
                put("2_4_0", 0.306);
                put("2_3_4", 0.029);
                put("2_3_3", 0.067);
                put("2_3_2", 0.553);
                put("2_3_1", 0.097);
                put("2_3_0", 0.406);
                put("2_2_4", 0.001);
                put("2_2_3", 0.489);
                put("2_2_2", 0.041);
                put("2_2_1", 0.047);
                put("2_2_0", 0.024);
                put("2_1_4", 0.284);
                put("2_1_3", 0.628);
                put("2_1_2", 0.604);
                put("2_1_1", 0.234);
                put("2_1_0", 0.000);
                put("2_15_4", 0.000);
                put("2_15_2", 0.500);
                put("2_14_4", 0.780);
                put("2_14_3", 0.442);
                put("2_14_2", 0.769);
                put("2_14_1", 0.647);
                put("2_14_0", 0.500);
                put("2_13_4", 0.164);
                put("2_13_3", 0.057);
                put("2_13_2", 0.019);
                put("2_13_1", 0.908);
                put("2_13_0", 0.265);
                put("2_12_4", 0.036);
                put("2_12_3", 0.257);
                put("2_12_2", 0.603);
                put("2_12_1", 0.442);
                put("2_12_0", 0.456);
                put("2_11_4", 0.021);
                put("2_11_3", 0.004);
                put("2_11_2", 0.060);
                put("2_11_1", 0.212);
                put("2_11_0", 0.159);
                put("2_10_4", 0.031);
                put("2_10_3", 0.175);
                put("2_10_2", 0.463);
                put("2_10_1", 0.260);
                put("2_10_0", 0.245);
                put("2_0_4", 0.188);
                put("2_0_3", 0.695);
                put("2_0_2", 0.996);
                put("2_0_1", 0.517);
                put("2_0_0", 1.000);
                put("1_9_4", 0.003);
                put("1_9_3", 0.394);
                put("1_9_2", 0.071);
                put("1_9_1", 0.374);
                put("1_9_0", 0.458);
                put("1_8_4", 0.155);
                put("1_8_3", 0.024);
                put("1_8_2", 0.042);
                put("1_8_1", 0.310);
                put("1_8_0", 0.155);
                put("1_7_4", 0.000);
                put("1_7_3", 0.118);
                put("1_7_2", 0.124);
                put("1_7_1", 0.083);
                put("1_7_0", 0.833);
                put("1_6_4", 0.002);
                put("1_6_3", 0.000);
                put("1_6_2", 0.000);
                put("1_6_1", 0.005);
                put("1_6_0", 0.163);
                put("1_5_4", 0.003);
                put("1_5_3", 0.016);
                put("1_5_2", 0.004);
                put("1_5_1", 0.002);
                put("1_5_0", 0.024);
                put("1_4_4", 0.008);
                put("1_4_3", 0.001);
                put("1_4_2", 0.001);
                put("1_4_1", 0.004);
                put("1_4_0", 0.012);
                put("1_3_4", 0.018);
                put("1_3_3", 0.000);
                put("1_3_2", 0.009);
                put("1_3_1", 0.001);
                put("1_3_0", 0.687);
                put("1_2_4", 0.003);
                put("1_2_3", 0.029);
                put("1_2_2", 0.000);
                put("1_2_1", 0.003);
                put("1_2_0", 0.010);
                put("1_1_4", 0.023);
                put("1_1_3", 0.092);
                put("1_1_2", 0.073);
                put("1_1_1", 0.218);
                put("1_1_0", 0.940);
                put("1_15_4", 1.000);
                put("1_15_2", 0.500);
                put("1_15_0", 0.500);
                put("1_14_4", 0.000);
                put("1_14_3", 0.874);
                put("1_14_2", 0.669);
                put("1_14_1", 1.000);
                put("1_14_0", 0.500);
                put("1_13_4", 0.311);
                put("1_13_3", 0.267);
                put("1_13_2", 0.000);
                put("1_13_1", 0.970);
                put("1_13_0", 0.516);
                put("1_12_4", 0.129);
                put("1_12_3", 0.976);
                put("1_12_2", 0.770);
                put("1_12_1", 0.400);
                put("1_12_0", 0.208);
                put("1_11_4", 0.081);
                put("1_11_3", 0.559);
                put("1_11_2", 0.870);
                put("1_11_1", 0.216);
                put("1_11_0", 0.242);
                put("1_10_4", 0.128);
                put("1_10_3", 0.319);
                put("1_10_2", 0.425);
                put("1_10_1", 0.045);
                put("1_10_0", 0.252);
                put("1_0_4", 0.081);
                put("1_0_3", 0.574);
                put("1_0_2", 0.964);
                put("1_0_1", 0.485);
                put("1_0_0", 0.209);
                put("0_9_4", 0.002);
                put("0_9_3", 0.018);
                put("0_9_2", 0.008);
                put("0_9_1", 0.044);
                put("0_9_0", 0.607);
                put("0_8_4", 0.000);
                put("0_8_3", 0.003);
                put("0_8_2", 0.128);
                put("0_8_1", 0.000);
                put("0_8_0", 0.181);
                put("0_7_4", 0.000);
                put("0_7_3", 0.000);
                put("0_7_2", 0.002);
                put("0_7_1", 0.007);
                put("0_7_0", 0.013);
                put("0_6_4", 0.000);
                put("0_6_3", 0.002);
                put("0_6_2", 0.001);
                put("0_6_1", 0.000);
                put("0_6_0", 0.000);
                put("0_5_4", 0.001);
                put("0_5_3", 0.002);
                put("0_5_2", 0.000);
                put("0_5_1", 0.000);
                put("0_5_0", 0.000);
                put("0_4_4", 0.000);
                put("0_4_3", 0.000);
                put("0_4_2", 0.002);
                put("0_4_1", 0.000);
                put("0_4_0", 0.000);
                put("0_3_4", 0.001);
                put("0_3_3", 0.001);
                put("0_3_2", 0.000);
                put("0_3_1", 0.000);
                put("0_3_0", 0.000);
                put("0_2_4", 0.000);
                put("0_2_3", 0.000);
                put("0_2_2", 0.000);
                put("0_2_1", 0.000);
                put("0_2_0", 0.000);
                put("0_1_4", 0.000);
                put("0_1_3", 0.000);
                put("0_1_2", 0.000);
                put("0_1_1", 0.000);
                put("0_1_0", 0.000);
                put("0_16_4", 0.500);
                put("0_16_3", 0.500);
                put("0_16_1", 0.500);
                put("0_16_0", 0.500);
                put("0_15_4", 0.798);
                put("0_15_3", 0.185);
                put("0_15_2", 0.000);
                put("0_15_1", 0.903);
                put("0_15_0", 1.000);
                put("0_14_4", 0.122);
                put("0_14_3", 0.206);
                put("0_14_2", 0.944);
                put("0_14_1", 0.722);
                put("0_14_0", 0.326);
                put("0_13_4", 0.097);
                put("0_13_3", 0.244);
                put("0_13_2", 0.017);
                put("0_13_1", 0.260);
                put("0_13_0", 0.369);
                put("0_12_4", 0.175);
                put("0_12_3", 0.207);
                put("0_12_2", 0.349);
                put("0_12_1", 0.509);
                put("0_12_0", 0.140);
                put("0_11_4", 0.039);
                put("0_11_3", 0.001);
                put("0_11_2", 0.004);
                put("0_11_1", 0.128);
                put("0_11_0", 0.021);
                put("0_10_4", 0.049);
                put("0_10_3", 0.043);
                put("0_10_2", 0.025);
                put("0_10_1", 0.445);
                put("0_10_0", 0.946);
                put("0_0_4", 0.003);
                put("0_0_3", 0.000);
                put("0_0_2", 0.001);
                put("0_0_1", 0.000);
                put("0_0_0", 0.000);

            }
        };

        /*
         * Order of strategy parameters:
         *
         * maxIsolatedSingleDeadwood
         * minIsolatedSingleDiscardTurn
         * maxSingleDeadwood
         * minSingleDiscardTurn
         * minPickupDifference
         */
        generalStrategy = new GeneralStrategy(MyGinRummyUtil.decoded("34466"), knockStrat, drawStrat);

    }

    @Override
    public void startGame(int playerNum, int startingPlayerNum, Card[] cards) {
        this.playerNum = playerNum;

        state = new State(new ArrayList<>(Arrays.asList(cards)));

        oppMelds = null;
        opponentKnocked = false;

    }

    @Override
    public boolean willDrawFaceUpCard(Card card) {
        int card_id = card.getId();
        // If first turn, record the face-up card. All other unseen face-up cards should be recorded in reportDiscard()
        if(state.getTurn() == 0) {
            state.addToSeen(card_id);
            state.increaseTopCard();
            state.decreaseNumRemaining();
        }

        //Card is our face-up
        state.setFaceUp(card_id);

        return willDrawFaceUpCard(state.getHand(), state.getFaceUp());

    }

    /**
     * @param hand A hand of cards
     * @param card_id The id of the face-up card
     * @return true if we would pick up card_id with the given hand
     */
    public boolean willDrawFaceUpCard(long hand, int card_id) {

        int cost = MyGinRummyUtil.getImprovement(hand, card_id);
        if(cost >= generalStrategy.getMinPickupDifference() &&
                cost > MyGinRummyUtil.expectedDeadwoodForNextDraw(state)) return true;


        //If the card can't be melded within 2 draws, don't draw it.

        long newCards = MyGinRummyUtil.add(hand, card_id);


        if(MyGinRummyUtil.getDeadwoodPoints(Card.getCard(card_id)) > generalStrategy.getMaxIsolatedSingleDeadwood()
                && MyGinRummyUtil.contains(MyGinRummyUtil.getIsolatedSingles(newCards, 0L, state), card_id)) return false;
        else if(MyGinRummyUtil.getDeadwoodPoints(Card.getCard(card_id)) > generalStrategy.getMaxSingleDeadwood()
                && MyGinRummyUtil.contains(MyGinRummyUtil.getSingles(newCards, 0L, state), card_id)) return false;

        //Next: If the card doesn't increase deadwood too much, and the opponent could meld it, draw the face-up.
        if(cost >= generalStrategy.getMaxSingleDeadwood() && MyGinRummyUtil.canOpponentMeld(Card.getCard(card_id), state)) return true;

        //Then, look at all within 2 of the highest discards. If the list doesn't contain the face-up, pick it up. Otherwise, don't.
        long preferred = MyGinRummyUtil.findHighestDiscards(newCards, -1, -1, 1);

        return !MyGinRummyUtil.contains(preferred, card_id);

    }

    @Override
    public void reportDraw(int playerNum, Card drawnCard) {

        //If drawn card is null or its id != the face up, player drew face-down. Decrease numRemaining, and add to oppForwent
        if(drawnCard == null || drawnCard.getId() != state.getFaceUp()) {
            state.decreaseNumRemaining();
            state.increaseTopCard();
            if(playerNum != this.playerNum) state.addToOppForwent(state.getFaceUp());
        }

        // Ignore other player draws.  Add to cards if playerNum is this player.
        if (playerNum == this.playerNum) {

            state.addToHand(drawnCard.getId());
            this.drawn = drawnCard.getId();

        }
        //If the other player drew, and drawnCard isn't null, other player drew face-up.
        else {
            if(drawnCard != null)
                state.addToOppHand(drawnCard.getId());
        }
    }

    @Override
    public Card getDiscard() {
        long potentialDiscards = findDiscard(state.getHand(), state.getFaceUp());
        return MyGinRummyUtil.bitstringToCards(potentialDiscards).get(random.nextInt(MyGinRummyUtil.size(potentialDiscards)));
    }

    /**
     * @param hand A hand of cards
     * @param face_up The id of the face-up card
     * @return A group of all cards which we would most prefer to discard
     */
    public long findDiscard(long hand, int face_up) {

        /*
         * First, get all cards who's removal would lower our deadwood the most
         */
        long candidateCards = MyGinRummyUtil.findHighestDiscards(hand, drawn, face_up, 0);
        long toRemove = 0L;
        ArrayList<Integer> ids = MyGinRummyUtil.bitstringToIDs(candidateCards);
        ArrayList<Integer> temp = new ArrayList<>(ids);

        for(int i : ids) {
            temp.remove((Integer) i);
            if(!MyGinRummyUtil.contains((MyGinRummyUtil.getIsolatedSingles(hand, MyGinRummyUtil.idsToBitstring(temp), state)), i))
                toRemove = MyGinRummyUtil.add(toRemove, i);
            temp.add((Integer) i);
        }

        if(toRemove != candidateCards) candidateCards = MyGinRummyUtil.removeAll(candidateCards, toRemove);

        else {
            toRemove = 0L;

            for(int i : ids) {
                temp.remove((Integer) i);
                if(!MyGinRummyUtil.contains((MyGinRummyUtil.getSingles(hand, MyGinRummyUtil.idsToBitstring(temp), state)), i))
                    toRemove = MyGinRummyUtil.add(toRemove, i);
                temp.add((Integer) i);
            }

            if(toRemove != candidateCards) candidateCards = MyGinRummyUtil.removeAll(candidateCards, toRemove);
        }

        /*
         * Prefer cards who cannot be melded even after 2 draws.
         * If there are none (or no cards can), prefer those who can't be melded after 1 draw.
         */
        long not_singles = ~MyGinRummyUtil.getIsolatedSingles(hand, 0L, state);
        candidateCards = MyGinRummyUtil.removeAll(candidateCards, not_singles) == 0L ?
                candidateCards : MyGinRummyUtil.removeAll(candidateCards, not_singles);

        if(MyGinRummyUtil.removeAll(candidateCards, not_singles) == 0L) {
            not_singles = ~MyGinRummyUtil.getSingles(hand, 0L, state);
            candidateCards = MyGinRummyUtil.removeAll(candidateCards, not_singles) == 0L ?
                    candidateCards : MyGinRummyUtil.removeAll(candidateCards, not_singles);
        }

        /*
         * Then, filter out cards which would be helpful to the opponent
         */
        if(MyGinRummyUtil.size(candidateCards) > 1) {
            toRemove = 0L; //Don't remove until after loop
            long preferred = 0L; //Cards we would prefer to remove

            for(Card c : MyGinRummyUtil.bitstringToCards(candidateCards)) {

                /*
                 * If a card could be used in an opp meld, or at least bring them closer, avoid discarding it
                 */
                if(MyGinRummyUtil.canOpponentMeld(c, state)) toRemove = MyGinRummyUtil.add(toRemove, c.getId()); //If card could help opp meld, avoid tossing
                else if(MyGinRummyUtil.containsRank(state.getOppHand(), c.getId()) || MyGinRummyUtil.containsSuit(state.getOppHand(), c.getId(), 2)) toRemove = MyGinRummyUtil.add(toRemove, c.getId()); //If card brings opp closer to a meld, avoid tossing

                /*
                 * If the opp has discarded cards that could be melded with this card, it is less likely they would find it useful. Prefer to discard any of these cards.
                 */
                else if(MyGinRummyUtil.containsRank(state.getOppDiscard(), c.getId()) || MyGinRummyUtil.containsSuit(state.getOppDiscard(), c.getId(), 2)) preferred = MyGinRummyUtil.add(preferred, c.getId()); //If similar cards have been tossed, prefer
                else if(MyGinRummyUtil.containsRank(state.getOppForwent(), c.getId()) || MyGinRummyUtil.containsSuit(state.getOppForwent(), c.getId(), 1)) preferred = MyGinRummyUtil.add(preferred, c.getId());;
            }

            if(toRemove != candidateCards) candidateCards = MyGinRummyUtil.removeAll(candidateCards, toRemove); //Remove useful cards to the opponent, unless all cards would be useful
            if(preferred != 0L && preferred != candidateCards) candidateCards = MyGinRummyUtil.removeAll(candidateCards, ~preferred); //Only consider cards which we would prefer to discard

        }

        /*
         * If there are more than 2 cards left, if any are dupled, avoid throwing them away
         */
        if(MyGinRummyUtil.size(candidateCards) > 2) {
            ArrayList<Integer> cards = MyGinRummyUtil.bitstringToIDs(candidateCards);
            long duples = 0L;

            for(int i : cards)
                duples += MyGinRummyUtil.getDuples(candidateCards, i);

            if(MyGinRummyUtil.removeAll(candidateCards, duples) != 0L)
                candidateCards = MyGinRummyUtil.removeAll(candidateCards, duples);

        }

        return candidateCards;

    }

    @Override
    public void reportDiscard(int playerNum, Card discardedCard) {
        // Ignore other player discards.  Remove from cards if playerNum is this player.
        if (playerNum == this.playerNum) {
            state.removeFromHand(discardedCard.getId());
            state.nextTurn();
        }

        //If we knew the discarded card was in the opponent's hand, remove. If we didn't, add it to seen.
        else {
            state.addToSeen(discardedCard.getId());
            state.removeFromOppHand(discardedCard.getId());
        }
    }

    @Override
    public ArrayList<ArrayList<Card>> getFinalMelds() {

        ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets;
        int deadwood;

        /*
         * TODO:
         *  -Try to find the best melds to minimize opponent layoff. Make sure to balance with deadwood to prevent undercut.
         *          -Based off of what we know is in their hand, and what cards we haven't seen. Account for # of remaining cards.
         *              What's the probability that the opp has a given unseen card?
         *          -Runs have more potential locations for layoff than 3 or 4 of a kind.
         *          -Laying off a low-value card is less important than a high-value one. May not be significant.
         */
        bestMeldSets = MyGinRummyUtil.cardsToBestMeldSets(MyGinRummyUtil.bitstringToCards(state.getHand()));
        deadwood = bestMeldSets.isEmpty() ?
                MyGinRummyUtil.getDeadwoodPoints(MyGinRummyUtil.bitstringToCards(state.getHand())) :
                MyGinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), MyGinRummyUtil.bitstringToCards(state.getHand()));

        // Check if deadwood of maximal meld is low enough to go out.


        //Only knock if deadwood is both less than or equal to 10 and strategy.getMaxKnockDeadwood() [0].

        if (!opponentKnocked &&
                (bestMeldSets.isEmpty() || deadwood > MyGinRummyUtil.MAX_DEADWOOD))
            return null;
        else if (!opponentKnocked) {
            if(deadwood == 0) return bestMeldSets.get(random.nextInt(bestMeldSets.size()));

            String k = deadwood + "_" + state.getTopCard();
            double prob = generalStrategy.getKnockAt(k);
            if(random.nextDouble() < prob)
                return bestMeldSets.get(random.nextInt(bestMeldSets.size()));
            else return null;
        }


        else {
            ArrayList<Card> layoff = new ArrayList<>();

            if(bestMeldSets.isEmpty()) return new ArrayList<>();

            //Add all cards to layoff who could be inserted into opponent hand
            for(ArrayList<Card> meld : oppMelds) {
                //Meld of cards of same rank
                if(meld.get(0).getRank() == meld.get(1).getRank()) {
                    layoff.addAll(MyGinRummyUtil.getSameRank(MyGinRummyUtil.bitstringToCards(state.getHand()), meld.get(0)));
                }

                //Cards of same suit
                else {
                    layoff.addAll(MyGinRummyUtil.getSameSuit(MyGinRummyUtil.bitstringToCards(state.getHand()), meld.get(0), 1));
                    layoff.addAll(MyGinRummyUtil.getSameSuit(MyGinRummyUtil.bitstringToCards(state.getHand()), meld.get(meld.size() - 1), 1));
                }
            }

            /*
             * Deadwood cards will be laid off no matter what, so check potential layoffs in melds to see if
             * a better config is available.
             */

            ArrayList<Card> temp;
            ArrayList<ArrayList<Card>> bestMeldSet = bestMeldSets.get(0);
            int minDeadwood = deadwood;

            if(layoff.isEmpty()) return bestMeldSet;

            //Go through EVERY permutation of potential layoffs to find the one that leaves the best deadwood
            for(int i = 0; i < Math.pow(2, layoff.size()); i++) {
                String bString = Integer.toBinaryString(i);
                temp = MyGinRummyUtil.bitstringToCards(state.getHand());

                for(int j = 0; j < bString.length(); j++) {
                    if(bString.charAt(bString.length() - 1 - j) == '1') {
                        temp.remove(layoff.get(j));
                    }
                }

                ArrayList<ArrayList<ArrayList<Card>>> meldSets = MyGinRummyUtil.cardsToBestMeldSets(temp);

                if(meldSets.isEmpty()) {
                    if(MyGinRummyUtil.getDeadwoodPoints(temp) < minDeadwood) {
                        minDeadwood = MyGinRummyUtil.getDeadwoodPoints(temp);
                        bestMeldSet = new ArrayList<>();
                    }
                }

                else {
                    if(MyGinRummyUtil.getDeadwoodPoints(meldSets.get(0), temp) < minDeadwood) {
                        minDeadwood = MyGinRummyUtil.getDeadwoodPoints(meldSets.get(0), temp);
                        bestMeldSet = meldSets.get(0);
                    }
                }
            }

            return bestMeldSet;

        }

    }

    @Override
    public void reportFinalMelds(int playerNum, ArrayList<ArrayList<Card>> melds) {
        // Melds ignored by simple player, but could affect which melds to make for complex player.
        if(playerNum != this.playerNum) {
            opponentKnocked = true;
            oppMelds = melds;
        }
    }

    @Override
    public void reportScores(int[] scores) {
        // Ignored by simple player, but could affect strategy of more complex player.
    }

    @Override
    public void reportLayoff(int playerNum, Card layoffCard, ArrayList<Card> opponentMeld) {
        // Ignored by simple player, but could affect strategy of more complex player.

    }

    @Override
    public void reportFinalHand(int playerNum, ArrayList<Card> hand) {
        // Ignored by simple player, but could affect strategy of more complex player.
    }
}
