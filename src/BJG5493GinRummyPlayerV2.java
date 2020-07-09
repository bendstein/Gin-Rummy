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
                put("9_39", 0.013);
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
                put("9_24", 0.002);
                put("9_23", 0.000);
                put("9_22", 0.000);
                put("9_21", 0.410);
                put("8_39", 0.000);
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
                put("8_25", 0.000);
                put("8_24", 0.000);
                put("8_23", 0.000);
                put("8_22", 0.004);
                put("8_21", 0.500);
                put("7_39", 0.001);
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
                put("7_24", 0.136);
                put("7_23", 0.000);
                put("7_22", 0.000);
                put("7_21", 0.091);
                put("6_39", 0.001);
                put("6_38", 0.003);
                put("6_37", 0.000);
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
                put("6_24", 0.001);
                put("6_23", 0.000);
                put("6_22", 0.006);
                put("6_21", 0.048);
                put("5_39", 0.002);
                put("5_38", 0.000);
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
                put("5_24", 1.000);
                put("5_23", 0.000);
                put("5_22", 0.016);
                put("5_21", 0.320);
                put("4_39", 1.000);
                put("4_38", 0.001);
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
                put("4_27", 0.000);
                put("4_26", 0.000);
                put("4_25", 0.000);
                put("4_24", 0.000);
                put("4_23", 0.000);
                put("4_22", 1.000);
                put("4_21", 0.296);
                put("3_39", 0.010);
                put("3_38", 1.000);
                put("3_37", 1.000);
                put("3_36", 0.001);
                put("3_35", 0.000);
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
                put("2_39", 1.000);
                put("2_38", 0.002);
                put("2_37", 0.000);
                put("2_36", 0.000);
                put("2_35", 0.002);
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
                put("1_39", 0.008);
                put("1_38", 0.002);
                put("1_37", 0.000);
                put("1_36", 0.003);
                put("1_35", 0.006);
                put("1_34", 0.000);
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
                put("1_21", 0.500);
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
                put("10_23", 1.000);
                put("10_22", 0.013);
                put("10_21", 0.000);
            }
        };

        HashMap<String, Double> drawStrat = new HashMap<String, Double>() {
            {
                put("9_9_3", 0.002);
                put("9_9_1", 0.594);
                put("9_8_3", 0.325);
                put("9_8_1", 0.949);
                put("9_7_3", 0.583);
                put("9_7_1", 0.244);
                put("9_7_0", 0.500);
                put("9_6_3", 1.000);
                put("9_6_1", 1.000);
                put("9_6_0", 0.500);
                put("9_5_3", 0.669);
                put("9_5_1", 0.997);
                put("9_5_0", 0.500);
                put("9_4_3", 0.332);
                put("9_4_1", 0.982);
                put("9_4_0", 0.500);
                put("9_3_3", 0.032);
                put("9_3_1", 0.000);
                put("9_3_0", 0.500);
                put("9_14_3", 0.500);
                put("9_13_3", 1.000);
                put("9_12_3", 0.003);
                put("9_11_3", 0.389);
                put("9_11_1", 0.500);
                put("9_10_3", 0.802);
                put("9_10_1", 0.000);
                put("8_9_4", 0.986);
                put("8_9_3", 0.076);
                put("8_9_2", 0.794);
                put("8_9_1", 0.500);
                put("8_8_4", 0.968);
                put("8_8_3", 0.809);
                put("8_8_2", 0.042);
                put("8_8_1", 0.000);
                put("8_7_4", 0.994);
                put("8_7_3", 0.301);
                put("8_7_2", 0.967);
                put("8_7_1", 1.000);
                put("8_6_4", 0.982);
                put("8_6_3", 0.355);
                put("8_6_2", 1.000);
                put("8_6_1", 0.244);
                put("8_6_0", 0.500);
                put("8_5_4", 0.532);
                put("8_5_3", 0.916);
                put("8_5_2", 0.096);
                put("8_5_1", 0.228);
                put("8_5_0", 0.500);
                put("8_4_4", 0.587);
                put("8_4_3", 0.141);
                put("8_4_2", 0.750);
                put("8_4_1", 0.000);
                put("8_4_0", 0.500);
                put("8_3_4", 0.604);
                put("8_3_3", 0.510);
                put("8_3_2", 0.000);
                put("8_3_1", 0.673);
                put("8_3_0", 0.500);
                put("8_2_3", 0.000);
                put("8_14_4", 0.634);
                put("8_13_4", 0.952);
                put("8_13_3", 0.500);
                put("8_12_4", 0.350);
                put("8_12_3", 0.500);
                put("8_12_2", 0.048);
                put("8_11_4", 0.336);
                put("8_11_3", 0.500);
                put("8_11_2", 0.013);
                put("8_10_4", 0.424);
                put("8_10_3", 0.480);
                put("8_10_2", 0.637);
                put("8_10_1", 0.500);
                put("7_9_4", 0.491);
                put("7_9_3", 0.468);
                put("7_9_2", 0.482);
                put("7_9_1", 0.000);
                put("7_8_4", 0.880);
                put("7_8_3", 0.667);
                put("7_8_2", 0.890);
                put("7_8_1", 0.017);
                put("7_7_4", 0.916);
                put("7_7_3", 0.173);
                put("7_7_2", 0.291);
                put("7_7_1", 0.911);
                put("7_6_4", 0.261);
                put("7_6_3", 0.079);
                put("7_6_2", 0.356);
                put("7_6_1", 0.873);
                put("7_6_0", 1.000);
                put("7_5_4", 0.527);
                put("7_5_3", 0.431);
                put("7_5_2", 0.398);
                put("7_5_1", 0.343);
                put("7_5_0", 0.500);
                put("7_4_4", 0.867);
                put("7_4_3", 0.984);
                put("7_4_2", 0.341);
                put("7_4_1", 0.000);
                put("7_4_0", 0.500);
                put("7_3_4", 0.291);
                put("7_3_3", 0.080);
                put("7_3_2", 0.012);
                put("7_3_1", 0.635);
                put("7_3_0", 0.500);
                put("7_2_4", 0.969);
                put("7_2_3", 0.596);
                put("7_2_2", 0.500);
                put("7_2_1", 0.986);
                put("7_14_4", 1.000);
                put("7_13_4", 0.957);
                put("7_13_3", 0.500);
                put("7_13_2", 1.000);
                put("7_12_4", 0.401);
                put("7_12_3", 0.500);
                put("7_12_2", 0.018);
                put("7_11_4", 0.004);
                put("7_11_3", 0.039);
                put("7_11_2", 0.478);
                put("7_10_4", 0.710);
                put("7_10_3", 0.972);
                put("7_10_2", 0.721);
                put("7_10_1", 0.602);
                put("6_9_4", 0.299);
                put("6_9_3", 0.308);
                put("6_9_2", 0.999);
                put("6_9_1", 0.957);
                put("6_8_4", 0.170);
                put("6_8_3", 0.018);
                put("6_8_2", 0.040);
                put("6_8_1", 0.913);
                put("6_8_0", 0.000);
                put("6_7_4", 0.072);
                put("6_7_3", 0.000);
                put("6_7_2", 0.603);
                put("6_7_1", 0.418);
                put("6_7_0", 0.500);
                put("6_6_4", 0.917);
                put("6_6_3", 0.949);
                put("6_6_2", 0.715);
                put("6_6_1", 0.531);
                put("6_6_0", 0.000);
                put("6_5_4", 0.510);
                put("6_5_3", 0.848);
                put("6_5_2", 0.590);
                put("6_5_1", 0.188);
                put("6_5_0", 0.973);
                put("6_4_4", 0.045);
                put("6_4_3", 0.162);
                put("6_4_2", 0.841);
                put("6_4_1", 0.092);
                put("6_4_0", 0.500);
                put("6_3_4", 0.583);
                put("6_3_3", 0.442);
                put("6_3_2", 0.111);
                put("6_3_1", 0.129);
                put("6_3_0", 0.500);
                put("6_2_4", 1.000);
                put("6_2_3", 0.514);
                put("6_2_2", 0.208);
                put("6_2_1", 0.564);
                put("6_2_0", 0.500);
                put("6_1_3", 0.175);
                put("6_1_1", 0.500);
                put("6_16_2", 0.500);
                put("6_15_4", 0.500);
                put("6_14_4", 0.306);
                put("6_14_3", 0.500);
                put("6_14_2", 0.500);
                put("6_13_4", 0.866);
                put("6_13_3", 0.651);
                put("6_13_2", 0.500);
                put("6_12_4", 0.715);
                put("6_12_3", 0.856);
                put("6_12_2", 0.998);
                put("6_12_1", 0.440);
                put("6_11_4", 0.911);
                put("6_11_3", 0.929);
                put("6_11_2", 0.030);
                put("6_11_1", 1.000);
                put("6_10_4", 0.469);
                put("6_10_3", 0.740);
                put("6_10_2", 0.843);
                put("6_10_1", 0.027);
                put("5_9_4", 0.109);
                put("5_9_3", 0.010);
                put("5_9_2", 0.035);
                put("5_9_1", 0.010);
                put("5_9_0", 0.500);
                put("5_8_4", 0.326);
                put("5_8_3", 0.792);
                put("5_8_2", 0.633);
                put("5_8_1", 0.935);
                put("5_8_0", 0.500);
                put("5_7_4", 0.653);
                put("5_7_3", 0.201);
                put("5_7_2", 0.974);
                put("5_7_1", 0.594);
                put("5_7_0", 0.742);
                put("5_6_4", 0.892);
                put("5_6_3", 0.855);
                put("5_6_2", 0.353);
                put("5_6_1", 0.300);
                put("5_6_0", 0.500);
                put("5_5_4", 0.992);
                put("5_5_3", 0.197);
                put("5_5_2", 0.080);
                put("5_5_1", 0.146);
                put("5_5_0", 0.190);
                put("5_4_4", 0.849);
                put("5_4_3", 0.573);
                put("5_4_2", 0.127);
                put("5_4_1", 0.478);
                put("5_4_0", 0.528);
                put("5_3_4", 0.070);
                put("5_3_3", 0.298);
                put("5_3_2", 0.923);
                put("5_3_1", 0.183);
                put("5_3_0", 0.000);
                put("5_2_4", 0.787);
                put("5_2_3", 0.004);
                put("5_2_2", 0.600);
                put("5_2_1", 0.888);
                put("5_2_0", 0.920);
                put("5_1_4", 0.866);
                put("5_1_3", 0.017);
                put("5_1_2", 1.000);
                put("5_1_1", 0.224);
                put("5_1_0", 0.500);
                put("5_15_4", 0.500);
                put("5_14_4", 0.451);
                put("5_14_2", 0.500);
                put("5_13_4", 0.851);
                put("5_13_3", 1.000);
                put("5_13_2", 0.440);
                put("5_12_4", 0.144);
                put("5_12_3", 0.596);
                put("5_12_2", 0.622);
                put("5_12_1", 1.000);
                put("5_11_4", 0.553);
                put("5_11_3", 0.736);
                put("5_11_2", 0.000);
                put("5_11_1", 1.000);
                put("5_10_4", 0.246);
                put("5_10_3", 0.315);
                put("5_10_2", 1.000);
                put("5_10_1", 0.988);
                put("4_9_4", 0.725);
                put("4_9_3", 0.448);
                put("4_9_2", 0.949);
                put("4_9_1", 0.966);
                put("4_9_0", 0.820);
                put("4_8_4", 0.059);
                put("4_8_3", 0.722);
                put("4_8_2", 0.795);
                put("4_8_1", 0.624);
                put("4_8_0", 0.438);
                put("4_7_4", 0.465);
                put("4_7_3", 0.993);
                put("4_7_2", 0.998);
                put("4_7_1", 0.316);
                put("4_7_0", 0.736);
                put("4_6_4", 0.006);
                put("4_6_3", 0.979);
                put("4_6_2", 0.008);
                put("4_6_1", 0.155);
                put("4_6_0", 0.930);
                put("4_5_4", 0.032);
                put("4_5_3", 0.205);
                put("4_5_2", 0.176);
                put("4_5_1", 0.576);
                put("4_5_0", 0.226);
                put("4_4_4", 0.031);
                put("4_4_3", 0.176);
                put("4_4_2", 0.008);
                put("4_4_1", 0.210);
                put("4_4_0", 0.013);
                put("4_3_4", 0.900);
                put("4_3_3", 0.625);
                put("4_3_2", 0.806);
                put("4_3_1", 0.988);
                put("4_3_0", 0.500);
                put("4_2_4", 0.043);
                put("4_2_3", 0.442);
                put("4_2_2", 0.865);
                put("4_2_1", 0.519);
                put("4_2_0", 0.620);
                put("4_1_4", 0.944);
                put("4_1_3", 0.101);
                put("4_1_2", 0.620);
                put("4_1_1", 0.361);
                put("4_15_3", 0.500);
                put("4_14_4", 0.000);
                put("4_14_3", 0.500);
                put("4_14_2", 1.000);
                put("4_14_1", 0.500);
                put("4_14_0", 0.500);
                put("4_13_4", 0.449);
                put("4_13_3", 0.164);
                put("4_13_2", 1.000);
                put("4_13_1", 0.000);
                put("4_12_4", 0.338);
                put("4_12_3", 0.243);
                put("4_12_2", 0.122);
                put("4_12_1", 0.029);
                put("4_11_4", 0.006);
                put("4_11_3", 0.164);
                put("4_11_2", 0.525);
                put("4_11_1", 0.435);
                put("4_11_0", 0.000);
                put("4_10_4", 0.294);
                put("4_10_3", 0.950);
                put("4_10_2", 0.303);
                put("4_10_1", 0.905);
                put("4_10_0", 0.996);
                put("3_9_4", 0.018);
                put("3_9_3", 0.431);
                put("3_9_2", 0.067);
                put("3_9_1", 0.007);
                put("3_9_0", 0.876);
                put("3_8_4", 0.008);
                put("3_8_3", 0.528);
                put("3_8_2", 0.322);
                put("3_8_1", 0.265);
                put("3_8_0", 0.425);
                put("3_7_4", 0.106);
                put("3_7_3", 0.661);
                put("3_7_2", 0.346);
                put("3_7_1", 0.493);
                put("3_7_0", 0.003);
                put("3_6_4", 0.201);
                put("3_6_3", 0.225);
                put("3_6_2", 0.042);
                put("3_6_1", 0.057);
                put("3_6_0", 0.906);
                put("3_5_4", 0.720);
                put("3_5_3", 0.047);
                put("3_5_2", 0.690);
                put("3_5_1", 0.021);
                put("3_5_0", 0.038);
                put("3_4_4", 0.624);
                put("3_4_3", 0.105);
                put("3_4_2", 0.037);
                put("3_4_1", 0.032);
                put("3_4_0", 0.931);
                put("3_3_4", 0.951);
                put("3_3_3", 0.156);
                put("3_3_2", 0.564);
                put("3_3_1", 0.194);
                put("3_3_0", 0.000);
                put("3_2_4", 0.004);
                put("3_2_3", 0.014);
                put("3_2_2", 0.821);
                put("3_2_1", 0.588);
                put("3_2_0", 0.401);
                put("3_1_4", 0.755);
                put("3_1_3", 0.236);
                put("3_1_2", 0.014);
                put("3_1_1", 0.568);
                put("3_1_0", 0.000);
                put("3_15_4", 0.500);
                put("3_14_4", 0.471);
                put("3_14_3", 0.500);
                put("3_14_2", 0.826);
                put("3_14_1", 0.500);
                put("3_13_4", 0.761);
                put("3_13_3", 0.745);
                put("3_13_2", 0.006);
                put("3_13_1", 0.995);
                put("3_13_0", 0.500);
                put("3_12_4", 0.207);
                put("3_12_3", 0.088);
                put("3_12_2", 0.008);
                put("3_12_1", 0.016);
                put("3_12_0", 0.980);
                put("3_11_4", 0.464);
                put("3_11_3", 0.739);
                put("3_11_2", 0.213);
                put("3_11_1", 0.858);
                put("3_11_0", 0.331);
                put("3_10_4", 0.098);
                put("3_10_3", 0.547);
                put("3_10_2", 0.386);
                put("3_10_1", 0.160);
                put("3_10_0", 0.834);
                put("3_0_3", 0.000);
                put("3_0_1", 0.500);
                put("2_9_4", 0.102);
                put("2_9_3", 0.587);
                put("2_9_2", 0.441);
                put("2_9_1", 0.737);
                put("2_9_0", 0.397);
                put("2_8_4", 0.001);
                put("2_8_3", 0.005);
                put("2_8_2", 0.151);
                put("2_8_1", 0.848);
                put("2_8_0", 0.229);
                put("2_7_4", 0.006);
                put("2_7_3", 0.553);
                put("2_7_2", 0.178);
                put("2_7_1", 0.962);
                put("2_7_0", 0.367);
                put("2_6_4", 0.076);
                put("2_6_3", 0.002);
                put("2_6_2", 0.096);
                put("2_6_1", 0.155);
                put("2_6_0", 0.865);
                put("2_5_4", 0.031);
                put("2_5_3", 0.012);
                put("2_5_2", 0.129);
                put("2_5_1", 0.118);
                put("2_5_0", 0.006);
                put("2_4_4", 0.020);
                put("2_4_3", 0.039);
                put("2_4_2", 0.054);
                put("2_4_1", 0.200);
                put("2_4_0", 0.001);
                put("2_3_4", 0.237);
                put("2_3_3", 0.221);
                put("2_3_2", 0.020);
                put("2_3_1", 0.003);
                put("2_3_0", 0.499);
                put("2_2_4", 0.118);
                put("2_2_3", 0.054);
                put("2_2_2", 0.040);
                put("2_2_1", 0.666);
                put("2_2_0", 0.120);
                put("2_1_4", 0.159);
                put("2_1_3", 0.098);
                put("2_1_2", 0.491);
                put("2_1_1", 0.314);
                put("2_1_0", 0.945);
                put("2_14_4", 0.523);
                put("2_14_3", 0.989);
                put("2_14_2", 0.914);
                put("2_14_1", 0.500);
                put("2_14_0", 0.500);
                put("2_13_4", 0.778);
                put("2_13_3", 1.000);
                put("2_13_2", 0.673);
                put("2_13_1", 0.755);
                put("2_13_0", 0.200);
                put("2_12_4", 0.097);
                put("2_12_3", 0.658);
                put("2_12_2", 0.566);
                put("2_12_1", 0.902);
                put("2_12_0", 0.461);
                put("2_11_4", 0.086);
                put("2_11_3", 0.768);
                put("2_11_2", 0.982);
                put("2_11_1", 0.915);
                put("2_11_0", 0.626);
                put("2_10_4", 0.199);
                put("2_10_3", 0.362);
                put("2_10_2", 0.402);
                put("2_10_1", 0.080);
                put("2_10_0", 0.999);
                put("2_0_4", 0.796);
                put("2_0_3", 0.033);
                put("2_0_2", 0.500);
                put("2_0_1", 0.274);
                put("2_0_0", 0.000);
                put("1_9_4", 0.082);
                put("1_9_3", 0.215);
                put("1_9_2", 0.637);
                put("1_9_1", 0.154);
                put("1_9_0", 0.007);
                put("1_8_4", 0.020);
                put("1_8_3", 0.327);
                put("1_8_2", 0.029);
                put("1_8_1", 0.197);
                put("1_8_0", 0.117);
                put("1_7_4", 0.033);
                put("1_7_3", 0.077);
                put("1_7_2", 0.047);
                put("1_7_1", 0.245);
                put("1_7_0", 0.849);
                put("1_6_4", 0.539);
                put("1_6_3", 0.112);
                put("1_6_2", 0.174);
                put("1_6_1", 0.994);
                put("1_6_0", 0.238);
                put("1_5_4", 0.011);
                put("1_5_3", 0.046);
                put("1_5_2", 0.021);
                put("1_5_1", 0.007);
                put("1_5_0", 0.061);
                put("1_4_4", 0.105);
                put("1_4_3", 0.019);
                put("1_4_2", 0.092);
                put("1_4_1", 0.107);
                put("1_4_0", 0.138);
                put("1_3_4", 0.001);
                put("1_3_3", 0.149);
                put("1_3_2", 0.002);
                put("1_3_1", 0.003);
                put("1_3_0", 0.007);
                put("1_2_4", 0.077);
                put("1_2_3", 0.276);
                put("1_2_2", 0.000);
                put("1_2_1", 0.001);
                put("1_2_0", 0.650);
                put("1_1_4", 0.098);
                put("1_1_3", 0.069);
                put("1_1_2", 0.529);
                put("1_1_1", 0.344);
                put("1_1_0", 0.431);
                put("1_16_4", 0.500);
                put("1_15_4", 0.000);
                put("1_14_4", 1.000);
                put("1_14_3", 0.500);
                put("1_14_2", 1.000);
                put("1_14_1", 0.182);
                put("1_14_0", 0.500);
                put("1_13_4", 0.128);
                put("1_13_3", 0.113);
                put("1_13_2", 0.442);
                put("1_13_1", 0.160);
                put("1_13_0", 0.047);
                put("1_12_4", 0.145);
                put("1_12_3", 0.091);
                put("1_12_2", 0.140);
                put("1_12_1", 0.591);
                put("1_12_0", 0.164);
                put("1_11_4", 0.066);
                put("1_11_3", 0.168);
                put("1_11_2", 0.517);
                put("1_11_1", 0.955);
                put("1_11_0", 0.965);
                put("1_10_4", 0.004);
                put("1_10_3", 0.777);
                put("1_10_2", 0.044);
                put("1_10_1", 0.385);
                put("1_10_0", 0.801);
                put("1_0_4", 0.003);
                put("1_0_3", 0.001);
                put("1_0_2", 0.580);
                put("1_0_1", 0.597);
                put("1_0_0", 0.500);
                put("0_9_4", 0.025);
                put("0_9_3", 0.134);
                put("0_9_2", 0.006);
                put("0_9_1", 0.037);
                put("0_9_0", 0.414);
                put("0_8_4", 0.024);
                put("0_8_3", 0.026);
                put("0_8_2", 0.000);
                put("0_8_1", 0.019);
                put("0_8_0", 0.070);
                put("0_7_4", 0.001);
                put("0_7_3", 0.298);
                put("0_7_2", 0.138);
                put("0_7_1", 0.002);
                put("0_7_0", 0.003);
                put("0_6_4", 0.000);
                put("0_6_3", 0.000);
                put("0_6_2", 0.021);
                put("0_6_1", 0.001);
                put("0_6_0", 0.087);
                put("0_5_4", 0.007);
                put("0_5_3", 0.000);
                put("0_5_2", 0.006);
                put("0_5_1", 0.000);
                put("0_5_0", 0.002);
                put("0_4_4", 0.000);
                put("0_4_3", 0.000);
                put("0_4_2", 0.009);
                put("0_4_1", 0.000);
                put("0_4_0", 0.000);
                put("0_3_4", 0.000);
                put("0_3_3", 0.002);
                put("0_3_2", 0.004);
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
                put("0_15_4", 0.500);
                put("0_15_3", 0.000);
                put("0_15_2", 0.500);
                put("0_15_1", 0.500);
                put("0_15_0", 0.500);
                put("0_14_4", 0.098);
                put("0_14_3", 0.067);
                put("0_14_2", 0.498);
                put("0_14_1", 0.364);
                put("0_14_0", 0.108);
                put("0_13_4", 0.920);
                put("0_13_3", 0.104);
                put("0_13_2", 0.128);
                put("0_13_1", 0.365);
                put("0_13_0", 0.492);
                put("0_12_4", 0.025);
                put("0_12_3", 0.374);
                put("0_12_2", 0.386);
                put("0_12_1", 0.768);
                put("0_12_0", 0.910);
                put("0_11_4", 0.012);
                put("0_11_3", 0.331);
                put("0_11_2", 0.297);
                put("0_11_1", 0.525);
                put("0_11_0", 0.015);
                put("0_10_4", 0.015);
                put("0_10_3", 0.986);
                put("0_10_2", 0.208);
                put("0_10_1", 0.099);
                put("0_10_0", 0.993);
                put("0_0_4", 0.001);
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
        int improvement = MyGinRummyUtil.getImprovement(hand, card_id);
        boolean makesNewMeld = MyGinRummyUtil.makesNewMeld(hand, card_id);
        double expectedDeadwoodForFaceDown = MyGinRummyUtil.expectedDeadwoodForNextDraw(state);
        int numberOfMelds = state.getNumberOfPossibleMelds(card_id);

        if(improvement > 0 && makesNewMeld) return true;

        String infoset = improvement + "_" + (int) Math.round(expectedDeadwoodForFaceDown) + "_" + numberOfMelds;

        if(random.nextDouble() < generalStrategy.getDrawAt(infoset)) return true;
        else return false;

       /* *//*
         * First: If picking up the face-up card will lower our deadwood by an amount that is at
         * least strategy.getMinPickupDifference(), and it lowers it more than we expect the face-down card to
         * draw it.
         *//*

        int cost = MyGinRummyUtil.getImprovement(hand, card_id);
        if(cost >= generalStrategy.getMinPickupDifference() &&
                cost > MyGinRummyUtil.expectedDeadwoodForNextDraw(state)) return true;

        *//*
         * If the card can't be melded within 2 draws, don't draw it.
         *//*
        long newCards = MyGinRummyUtil.add(hand, card_id);

        if(MyGinRummyUtil.getDeadwoodPoints(Card.getCard(card_id)) > generalStrategy.getMaxIsolatedSingleDeadwood()
                && MyGinRummyUtil.contains(MyGinRummyUtil.getIsolatedSingles(newCards, 0L, state), card_id)) return false;
        else if(MyGinRummyUtil.getDeadwoodPoints(Card.getCard(card_id)) > generalStrategy.getMaxSingleDeadwood()
                && MyGinRummyUtil.contains(MyGinRummyUtil.getSingles(newCards, 0L, state), card_id)) return false;

        *//*
         * Next: If the card doesn't increase deadwood too much, and the opponent could meld it, draw the face-up.
         *//*
        if(cost >= generalStrategy.getMaxSingleDeadwood() && MyGinRummyUtil.canOpponentMeld(Card.getCard(card_id), state)) return true;


        *//*
         * Then, look at all within 2 of the highest discards. If the list doesn't contain the face-up, pick it up. Otherwise, don't.
         *//*
        long preferred = MyGinRummyUtil.findHighestDiscards(newCards, -1, -1, 1);

        return !MyGinRummyUtil.contains(preferred, card_id);*/

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

/**
 * Class to record details about the current state of the game
 */
class State {

    //<editor-fold desc="Instance Variables">
    /**
     * Our hand
     */
    private long hand;

    /**
     * Cards which have been seen by the player
     */
    private long seen;

    /**
     * Cards which we know are currently in the opponent hand
     */
    private long oppHand;

    /**
     * Cards which we have seen the opponent discard
     */
    private long oppDiscard;

    /**
     * Cards which the opponent has ignored when face-up
     */
    private long oppForwent;

    /**
     * The id of the current face-up card
     */
    private int faceUp;

    /**
     * Index into deck for top card
     */
    private int topCard;

    /**
     * Current turn of the game
     */
    private int turn;

    /**
     * Number of remaining cards in the deck
     */
    private int num_remaining;
    //</editor-fold>

    State(ArrayList<Card> hand) {

        this.hand = MyGinRummyUtil.cardsToBitstring(hand);
        seen = this.hand;
        oppHand = 0L;
        oppDiscard = 0L;
        oppForwent = 0L;


        turn = 0;
        topCard = 20;
        faceUp = -1;
        num_remaining = 32;
    }

    /**
     * Clear the game state to be used again
     */
    void clear() {
        hand = seen = oppHand = oppDiscard = 0L;
        faceUp = turn = 0;
        num_remaining = 32;
        topCard = 20;
    }

    //<editor-fold desc="Methods to add and remove cards from the lists recorded in this class">
    /**
     * The following methods are for adding and removing cards/sets of cards from a hand. I don't think they need to
     * documented.
     */
    public void addToHand(int card_id) {
        this.hand = MyGinRummyUtil.add(this.hand, card_id);
    }

    public void addAllToHand(long cards) {
        this.hand = MyGinRummyUtil.addAll(this.hand, cards);
    }

    public void removeFromHand(int card_id) {
        this.hand = MyGinRummyUtil.remove(this.hand, card_id);
    }

    public void removeAllFromHand(long cards) {
        this.hand = MyGinRummyUtil.removeAll(this.hand, cards);
    }

    public void addToSeen(int card_id) {
        this.seen = MyGinRummyUtil.add(this.seen, card_id);
    }

    public void addAllToSeen(long cards) {
        this.seen = MyGinRummyUtil.addAll(this.seen, cards);
    }

    public void removeFromSeen(int card_id) {
        this.seen = MyGinRummyUtil.remove(this.seen, card_id);
    }

    public void removeAllFromSeen(long cards) {
        this.seen = MyGinRummyUtil.removeAll(this.seen, cards);
    }

    public void addToOppHand(int card_id) {
        this.oppHand = MyGinRummyUtil.add(this.oppHand, card_id);
    }

    public void addAllToOppHand(long cards) {
        this.oppHand = MyGinRummyUtil.addAll(this.oppHand, cards);
    }

    public void removeFromOppHand(int card_id) {
        this.oppHand = MyGinRummyUtil.remove(this.oppHand, card_id);
    }

    public void removeAllFromOppHand(long cards) {
        this.oppHand = MyGinRummyUtil.removeAll(this.oppHand, cards);
    }

    public void addToOppDiscard(int card_id) {
        this.oppDiscard = MyGinRummyUtil.add(this.oppDiscard, card_id);
    }

    public void addAllToOppDiscard(long cards) {
        this.oppDiscard = MyGinRummyUtil.addAll(this.oppDiscard, cards);
    }

    public void removeFromOppDiscard(int card_id) {
        this.oppDiscard = MyGinRummyUtil.remove(this.oppDiscard, card_id);
    }

    public void removeAllFromOppDiscard(long cards) {
        this.oppDiscard = MyGinRummyUtil.removeAll(this.oppDiscard, cards);
    }

    public void addToOppForwent(int card_id) {
        this.oppForwent = MyGinRummyUtil.add(this.oppForwent, card_id);
    }

    public void addAllToOppForwent(long cards) {
        this.oppForwent = MyGinRummyUtil.addAll(this.oppForwent, cards);
    }

    public void removeFromOppForwent(int card_id) {
        this.oppForwent = MyGinRummyUtil.remove(this.oppForwent, card_id);
    }

    public void removeAllFromOppForwent(long cards) {
        this.oppForwent = MyGinRummyUtil.removeAll(this.oppForwent, cards);
    }
    //</editor-fold>

    /**
     * Increment the turn
     */
    public void nextTurn() {
        turn++;
    }

    /**
     * Decrement the number of cards remaining
     */
    public void decreaseNumRemaining() {
        num_remaining--;
    }

    /**
     * @param id The id of the card
     * @return All melds of 3 which the opponent could make with id
     */
    public ArrayList<Long> getPotentialOpponentMelds(int id) {

        //All cards which are/could be available to the opponent
        long available = getUnaccounted() + getOppHand();
        ArrayList<Long> melds = new ArrayList<>();

        //All available cards of the same rank as id
        long sameRank = MyGinRummyUtil.getSameRank(available, id);
        int[] sameRankIds = MyGinRummyUtil.bitstringToIDArray(sameRank);

        //Add all potential same-rank melds to the list
        for(int i : sameRankIds) {
            for(int j : sameRankIds) {
                if(i != j) {
                    long meld = MyGinRummyUtil.idsToBitstring(new int[]{i, j, id});
                    if(!melds.contains(meld)) melds.add(meld);
                }
            }
        }

        //All available adjacent cards to id of the same suit
        long sameSuit = MyGinRummyUtil.getSameSuit(available, id, 1);
        int[] sameSuitIds = MyGinRummyUtil.bitstringToIDArray(sameSuit);

        //Add all potential same-suit melds to the list
        if(sameSuitIds.length == 2) melds.add(MyGinRummyUtil.add(MyGinRummyUtil.idsToBitstring(sameSuitIds), id));

        for(int i : sameSuitIds) {
            long adj = MyGinRummyUtil.getSameSuit(available, i, 1);
            int[] adjIds = MyGinRummyUtil.bitstringToIDArray(adj);
            if(adjIds.length == 2) melds.add(MyGinRummyUtil.add(MyGinRummyUtil.idsToBitstring(sameSuitIds), i));
        }

        return melds;
    }

    /**
     * @param id The id of the card
     * @return How many ways the opponent could use a card, ignoring melds of 4+
     */
    public int getUsefulnessToOpponent(int id) {
        return getPotentialOpponentMelds(id).size();
    }

    /**
     * @param id Card to check
     * @return The number of melds we could make with this card
     */
    public int getNumberOfPossibleMelds(int id) {

        //All cards which are/could be available to the opponent
        long available = getUnaccounted() + getHand();
        ArrayList<Long> melds = new ArrayList<>();

        //All available cards of the same rank as id
        long sameRank = MyGinRummyUtil.getSameRank(available, id);
        int[] sameRankIds = MyGinRummyUtil.bitstringToIDArray(sameRank);

        //Add all potential same-rank melds to the list
        for(int i : sameRankIds) {
            for(int j : sameRankIds) {
                if(i != j) {
                    long meld = MyGinRummyUtil.idsToBitstring(new int[]{i, j, id});
                    if(!melds.contains(meld)) melds.add(meld);
                }
            }
        }

        //All available adjacent cards to id of the same suit
        long sameSuit = MyGinRummyUtil.getSameSuit(available, id, 1);
        int[] sameSuitIds = MyGinRummyUtil.bitstringToIDArray(sameSuit);

        //Add all potential same-suit melds to the list
        if(sameSuitIds.length == 2) melds.add(MyGinRummyUtil.add(MyGinRummyUtil.idsToBitstring(sameSuitIds), id));

        for(int i : sameSuitIds) {
            long adj = MyGinRummyUtil.getSameSuit(available, i, 1);
            int[] adjIds = MyGinRummyUtil.bitstringToIDArray(adj);
            if(adjIds.length == 2) melds.add(MyGinRummyUtil.add(MyGinRummyUtil.idsToBitstring(sameSuitIds), i));
        }

        return melds.size();
    }

    /**
     * @return A bitstring of all cards which are under the face-up card. We know for a fact that neither us nor the
     *  opponent can use any of these cards
     */
    public long getBuried() {
        long seen = this.seen;
        seen = MyGinRummyUtil.removeAll(seen, hand);
        seen = MyGinRummyUtil.removeAll(seen, oppHand);
        seen = MyGinRummyUtil.removeAll(seen, faceUp);

        return seen;
    }

    /**
     * @return A list of all cards which have not yet been seen. They are either still in the deck, or in the opponent's
     *  hand.
     */
    public long getUnaccounted() {
        long unaccounted = MyGinRummyUtil.cardsToBitstring(new ArrayList<>(Arrays.asList(Card.allCards)));
        unaccounted = MyGinRummyUtil.removeAll(unaccounted, seen);
        return unaccounted;
    }

    //<editor-fold desc="Getters and Setters">
    public int getTurn() {
        return turn;
    }

    public long getHand() {
        return hand;
    }

    public void setHand(long hand) {
        this.hand = hand;
    }

    public void setHand(ArrayList<Card> hand) {
        this.hand = MyGinRummyUtil.cardsToBitstring(hand);
    }

    public long getSeen() {
        return seen;
    }

    public long getUnseen() {
        return ~seen;
    }

    public long getOppHand() {
        return oppHand;
    }

    public void setOppHand(long oppHand) {
        this.oppHand = oppHand;
    }

    public void setOppHand(ArrayList<Card> oppHand) {
        this.oppHand = MyGinRummyUtil.cardsToBitstring(oppHand);
    }

    public long getOppDiscard() {
        return oppDiscard;
    }

    public void setOppDiscard(long oppDiscard) {
        this.oppDiscard = oppDiscard;
    }

    public void setOppDiscard(ArrayList<Card> oppDiscard) {
        this.oppDiscard = MyGinRummyUtil.cardsToBitstring(oppDiscard);
    }

    public long getOppForwent() {
        return oppForwent;
    }

    public void setOppForwent(long oppForwent) {
        this.oppForwent = oppForwent;
    }

    public void setOppForwent(ArrayList<Card> oppForwent) {
        this.oppForwent = MyGinRummyUtil.cardsToBitstring(oppForwent);
    }

    public int getFaceUp() {
        return faceUp;
    }

    public void setFaceUp(int faceUp) {
        this.faceUp = faceUp;
    }

    public void setFaceUp(Card c) {
        this.faceUp = c.getId();
    }

    public int getNum_remaining() {
        return num_remaining;
    }

    public int getTopCard() {
        return topCard;
    }

    public void setTopCard(int topCard) {
        this.topCard = topCard;
    }

    public void increaseTopCard() {
        topCard++;
    }
    //</editor-fold>

}

/*
 * Class containing helper methods
 */
class MyGinRummyUtil extends GinRummyUtil {

    /**
     * @param cards a hand of cards
     * @param exclude cards to exclude from the check
     * @return all cards not in melds
     */
    public static ArrayList<Card> getUnmelded(ArrayList<Card> cards, ArrayList<Card> exclude) {

        ArrayList<Card> temp = new ArrayList<>(cards);
        if(exclude != null) temp.removeAll(exclude);

        ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = cardsToBestMeldSets(temp);
        if(bestMeldSets.size() == 0) return temp;

        ArrayList<ArrayList<Card>> bestMeldSet = bestMeldSets.get(0);

        ArrayList<Card> unmelded = new ArrayList<>(temp);

        unmelded.removeIf(card -> {
            for(ArrayList<Card> meld : bestMeldSet)
                if(meld.contains(card)) return true;
            return false;
        });

        return unmelded;

    }

    /**
     * @param cards a hand of cards
     * @param exclude cards to exclude from the check
     * @return all cards not in melds
     */
    public static long getUnmelded(long cards, long exclude) {
        return cardsToBitstring(getUnmelded(bitstringToCards(cards), bitstringToCards(exclude)));
    }

    /**
     * @param hand A hand of cards
     * @param card_id the id of a card
     * @return The hand with the card added
     */
    public static long add(long hand, int card_id) {
        return hand | 1L << card_id;
    }

    /**
     * @param hand A hand of cards
     * @param toAdd The cards to add
     * @return The hand with the cards added
     */
    public static long addAll(long hand, long toAdd) {
        return hand | toAdd;
    }

    /**
     * @param hand A hand of cards
     * @param card_id the id of a card
     * @return The hand with the card removed. Nothing is changed if hand doesn't contain card.
     */
    public static long remove(long hand, int card_id) {
        return hand & (hand ^ 1L << card_id);
    }

    /**
     * @param hand A hand of cards
     * @param toRemove The set of cards to remove from hand
     * @return The hand with the cards removed. Nothing is changed if hand doesn't contain card.
     */
    public static long removeAll(long hand, long toRemove) {
        return hand & (hand ^ toRemove);
    }

    /**
     * @param hand a hand of cards
     * @param card_id the reference card
     * @return true if hand contains card_id
     */
    public static boolean contains(long hand, int card_id) {
        return (hand & 1L << card_id) != 0;
    }

    /**
     * @param cards a hand of cards
     * @param exclude cards to exclude from the check
     * @return all cards who are in one of the melds in the best meld set for cards
     */
    public static ArrayList<Card> getMelded(ArrayList<Card> cards, ArrayList<Card> exclude) {
        ArrayList<Card> melded = new ArrayList<>(cards);
        melded.removeAll(getUnmelded(melded, exclude));
        return melded;
    }

    /**
     * @param cards a hand of cards
     * @param exclude cards to exclude from the check
     * @return all cards who are in one of the melds in the best meld set for cards
     */
    public static long getMelded(long cards, long exclude) {
        cards = removeAll(cards, exclude);
        return cards & (cards ^ getUnmelded(cards, exclude));
    }

    /**
     * @param cards a hand of cards
     * @param exclude cards to exclude from the check
     * @param state the current state of the game
     * @return all cards in hand that cannot be made into any melds even after drawing one card.
     */
    public static ArrayList<Card> getSingles(ArrayList<Card> cards, ArrayList<Card> exclude, State state) {

        ArrayList<Card> temp = new ArrayList<>(cards);
        if(exclude != null) temp.removeAll(exclude);
        if(temp.isEmpty()) return temp;

        ArrayList<Card> singles = getUnmelded(temp, exclude);
        ArrayList<Card> toRemove = new ArrayList<>();

        for(Card card : singles) {

            //If there exists another card of the same rank in the hand and unseen, it can be melded within 1 draw
            if(containsRank(temp, card) && containsRank(state.getUnaccounted(), card.getId())) {
                toRemove.add(card);
                continue;
            }

            /*
             * For each adjacent card of the same suit to c, if there is an adjacent card
             * of the same suit to that card which is unseen, it can be melded within 1 draw
             */

            for(Card c : getSameSuit(temp, card, 1)) {
                if(containsSuit(state.getUnaccounted(), card.getId(), 1)) {
                    toRemove.add(card);
                    break;
                }
            }
        }

        singles.removeAll(toRemove);
        return singles;

    }

    /**
     * @param cards a hand of cards
     * @param state the current state of the game
     * @param exclude cards to exclude from the check
     * @return all cards in hand that cannot be made into any melds even after drawing one card.
     */
    public static long getSingles(long cards, long exclude, State state) {
        return cardsToBitstring(getSingles(bitstringToCards(cards), bitstringToCards(exclude), state));
    }

    /**
     * @param cards a hand of cards
     * @param exclude cards to exclude from the check
     * @param state the current state of the game
     * @return all cards in hand that cannot be made into any melds even after drawing two cards.
     */
    public static ArrayList<Card> getIsolatedSingles(ArrayList<Card> cards, ArrayList<Card> exclude, State state) {
        ArrayList<Card> singles = getSingles(cards, exclude, state); //All cards which cannot be made into a meld after drawing one card
        ArrayList<Card> unaccounted = bitstringToCards(state.getUnaccounted()); //All cards which have not yet been seen
        unaccounted.removeAll(bitstringToCards(state.getSeen()));

        ArrayList<Card> toRemove = new ArrayList<>();

        for(Card card : singles) {

            //Get all cards of the same rank as the card
            ArrayList<Card> adjacent = new ArrayList<>(getSameRank(unaccounted, card));

            //If at least 2 unseen cards of the same rank as card, it can be melded after drawing 2 cards
            if(adjacent.size() > 1) {
                toRemove.add(card);
                break;
            }

            //All cards of the same suit as the card whose rank is within 1 (should only be 2 cards max)
            adjacent = new ArrayList<>(getSameSuit(unaccounted, card, 1));

            //If no adjacent cards, then it cannot be melded even after drawing 2 cards. Check next card.
            if(adjacent.isEmpty()) continue;

            //For each adjacent card, see if the next card also exists. If any do, then the card can be melded after drawing 2 cards.
            for(Card c : adjacent) {
                ArrayList<Card> c_adjacent = new ArrayList<>(getSameSuit(unaccounted, c, 1)); //Unaccounted cards adjacent to c. Should never contain card, so should contain 1 card max.
                if(!c_adjacent.isEmpty()) {
                    toRemove.add(card);
                    break;
                }

            }

        }

        singles.removeAll(toRemove);

        return singles;

    }

    /**
     * @param cards a hand of cards
     * @param exclude cards to exclude from the check
     * @param state the current state of the game
     * @return all cards in hand that cannot be made into any melds even after drawing two cards.
     */
    public static long getIsolatedSingles(long cards, long exclude, State state) {
        return cardsToBitstring(getIsolatedSingles(bitstringToCards(cards), bitstringToCards(exclude), state));
    }

    /**
     * @param cards a hand of cards
     * @param card the reference card
     * @return A list of all duples containing card
     */
    public static ArrayList<Set<Card>> getDuples(ArrayList<Card> cards, Card card) {

        ArrayList<Set<Card>> duples = new ArrayList<>();
        ArrayList<Card> sameRank = getSameRank(cards, card);
        ArrayList<Card> sameSuit = getSameSuit(cards, card, 2);

        for(Card c : sameRank) duples.add(new HashSet<>(Arrays.asList(card, c)));
        for(Card c : sameSuit) duples.add(new HashSet<>(Arrays.asList(card, c)));

        return duples;
    }

    /**
     * @param cards a hand of cards
     * @param card the reference card
     * @return A list of all cards in duples with the reference card
     */
    public static long getDuples(long cards, int card) {

        long duples = cards;
        long sameRank = getSameRank(cards, card);
        long sameSuit = getSameSuit(cards, card, 2);

        duples = addAll(duples, addAll(sameRank, sameSuit));
        return duples;
    }

    /**
     * @param hand A hand of cards
     * @return The number of cards in the hand
     */
    public static int getHandSize(long hand) {
        int count = 0;
        for (int i = 0; i < Card.NUM_CARDS; i++) {
            if (hand % 2 == 1)
                count++;
            hand /= 2;
        }

        return count;
    }

    /**
     * @param hand A hand of cards
     * @return A list of all the card ids in the hand
     */
    public static ArrayList<Integer> bitstringToIDs(long hand) {
        ArrayList<Integer> cards = new ArrayList<>();
        for (int i = 0; i < Card.NUM_CARDS; i++) {
            if (hand % 2 == 1)
                cards.add(Card.allCards[i].getId());
            hand /= 2;
        }
        return cards;
    }

    /**
     * @param hand A hand of cards
     * @return An array of all the card ids in the hand
     */
    public static int[] bitstringToIDArray(long hand) {
        int[] ids = new int[getHandSize(hand)];
        int index = 0;
        for (int i = 0; i < Card.NUM_CARDS; i++) {
            if (hand % 2 == 1) {
                ids[index] = Card.allCards[i].getId();
                index++;
            }
            hand /= 2;
        }

        return ids;
    }

    /**
     * @param ids A list of ids in a hand
     * @return The list of ids as a long
     */
    public static long idsToBitstring(ArrayList<Integer> ids) {
        long bitstring = 0L;
        for (int i : ids)
            bitstring = add(bitstring, i);
        return bitstring;
    }

    /**
     * @param ids An array of ids in a hand
     * @return The list of ids as a long
     */
    public static long idsToBitstring(int[] ids) {
        long bitstring = 0L;
        for (int i : ids)
            bitstring = add(bitstring, i);
        return bitstring;
    }

    /**
     * @param c The reference card
     * @param s The current state of the game
     * @return true if the opponent can make a meld from c
     */
    public static boolean canOpponentMeld(Card c, State s) {

        //Cards of same rank as c
        ArrayList<Card> sameRank = getSameRank(new ArrayList<>(Arrays.asList(Card.allCards)), c);

        //Cards of same suit as c which are adjacent
        ArrayList<Card> sameSuitAdj = getSameSuit(new ArrayList<>(Arrays.asList(Card.allCards)), c, 1);

        //Cards of same suit as c who's rank is 2 away
        ArrayList<Card> sameSuit = getSameSuit(new ArrayList<>(Arrays.asList(Card.allCards)), c, 2);
        sameSuit.removeAll(sameSuitAdj);

        //Filter cards from collections so they only contain cards in the deck or in the opponent's hand
        sameRank.removeIf(card -> !contains(s.getHand(), card.getId()) && !contains(s.getBuried(), card.getId()));
        sameSuitAdj.removeIf(card -> !contains(s.getHand(), card.getId()) && !contains(s.getBuried(), card.getId()));
        sameSuit.removeIf(card -> !contains(s.getHand(), card.getId()) && !contains(s.getBuried(), card.getId()));

        if(sameRank.size() >= 2) return true;

        else if(sameSuitAdj.isEmpty() || sameSuit.isEmpty()) return false;

        //Looking at all the cards which are 2 away, if there is no card between it and c, remove it
        sameSuit.removeIf(card -> {
            for(Card card1 : sameSuitAdj) {
                if(card.getRank() > c.getRank()) return card.getRank() - 1 != card1.getRank();
                else return card.getRank() + 1 != card1.getRank();
            }
            return false;
        });

        return !sameSuit.isEmpty();
    }

    /**
     * @param hand A hand of cards
     * @param c The card we're checking
     * @return true if the added card makes a new meld
     */
    public static boolean makesNewMeld(ArrayList<Card> hand, Card c) {
        ArrayList<Card> newHand = new ArrayList<>(hand);
        newHand.add(c);

        ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = MyGinRummyUtil.cardsToBestMeldSets(newHand);
        if(bestMeldSets.size() == 0) return false;

        for(ArrayList<Card> meld : bestMeldSets.get(0)) {
            if(meld.contains(c)) return true;
        }

        return false;
    }

    /**
     * @param hand A hand of cards
     * @param id The card we're checking
     * @return true if the added card makes a new meld
     */
    public static boolean makesNewMeld(long hand, int id) {
        return makesNewMeld(MyGinRummyUtil.bitstringToCards(hand), Card.getCard(id));
    }

    /**
     * @param hand A hand of cards
     * @param c The card we're considering
     * @return The change in deadwood from inserting c and discarding the worst card. If return value is negative,
     *  drawing c increases the overall deadwood of our hand.
     */
    public static int getImprovement(ArrayList<Card> hand, Card c) {
        int minDeadwood = Integer.MAX_VALUE;
        ArrayList<Card> newCards = new ArrayList<>(hand);
        newCards.add(c);

        //Find all cards whose removal would reduce the hand's deadwood by the max amount
        for (Card card : newCards) {
            ArrayList<Card> remainingCards = new ArrayList<>(newCards);
            remainingCards.remove(card);

            ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = cardsToBestMeldSets(remainingCards);

            int deadwood = bestMeldSets.isEmpty() ? getDeadwoodPoints(remainingCards) : getDeadwoodPoints(bestMeldSets.get(0), remainingCards);
            if (deadwood < minDeadwood) minDeadwood = deadwood;
        }

        return cardsToBestMeldSets(hand).isEmpty() ? getDeadwoodPoints(hand) - minDeadwood : getDeadwoodPoints(cardsToBestMeldSets(hand).get(0), getUnmelded(hand, null)) - minDeadwood;
    }

    /**
     * @param hand A hand of cards
     * @param c_id The id of the card we're considering
     * @return The change in deadwood from inserting c and discarding the worst card. If return value is negative,
     *  drawing c increases the overall deadwood of our hand.
     */
    public static int getImprovement(long hand, int c_id) {
        return getImprovement(bitstringToCards(hand), Card.getCard(c_id));
    }

    /**
     * @param hand A hand of cards
     * @param c The reference card
     * @return true if there exists at least one card of the same rank as c
     */
    public static boolean containsRank(ArrayList<Card> hand, Card c) {
        for(Card card : hand) {
            if(card.equals(c)) continue; //Don't count card c
            if(card.getRank() == c.getRank()) return true;
        }

        return false;
    }

    /**
     * @param hand A hand of cards
     * @param c The reference card's id
     * @return true if there exists at least one card of the same rank as c
     */
    public static boolean containsRank(long hand, int c) {
        return containsRank(bitstringToCards(hand), Card.getCard(c));
    }

    /**
     * @param hand A hand of cards
     * @param c The reference card
     * @return a list of all cards of the same rank as c
     */
    public static ArrayList<Card> getSameRank(ArrayList<Card> hand, Card c) {
        if(c == null) return new ArrayList<>();
        return (ArrayList<Card>) hand
                .stream()
                .filter(card -> (card.getRank() == c.getRank() && !card.equals(c)))
                .collect(Collectors.toList());
    }

    /**
     * @param hand A hand of cards
     * @param c The reference card's id
     * @return a list of all cards of the same rank as c
     */
    public static long getSameRank(long hand, int c) {
        return cardsToBitstring(getSameRank(bitstringToCards(hand), Card.getCard(c)));
    }

    /**
     * @param hand A hand of cards
     * @param c The reference card
     * @param diff the difference between the cards' ranks
     * @return true if there exists at least one card of the same suit as c, given that its rank is within diff of c's rank
     */
    public static boolean containsSuit(ArrayList<Card> hand, Card c, int diff) {
        for(Card card : hand) {
            if(card.equals(c)) continue; //Don't count card c
            if(card.getSuit() == c.getSuit() && Math.abs(c.getRank() - card.getRank()) <= diff) return true;
        }

        return false;
    }

    /**
     * @param hand A hand of cards
     * @param c The reference card's id
     * @param diff the difference between the cards' ranks
     * @return true if there exists at least one card of the same suit as c, given that its rank is within diff of c's rank
     */
    public static boolean containsSuit(long hand, int c, int diff) {
        return containsSuit(bitstringToCards(hand), Card.getCard(c), diff);
    }

    /**
     * @param hand A hand of cards
     * @param c The reference card
     * @param diff the difference between the cards' ranks
     * @return a list of all cards of same suit as c, given that its rank is within diff of c's rank
     */
    public static ArrayList<Card> getSameSuit(ArrayList<Card> hand, Card c, int diff) {
        if(c == null) return new ArrayList<>();
        return (ArrayList<Card>) hand
                .stream()
                .filter(card -> (card.getSuit() == c.getSuit() && Math.abs(c.getRank() - card.getRank()) <= diff && !card.equals(c)))
                .collect(Collectors.toList());
    }

    /**
     * @param hand A hand of cards
     * @param c The reference card's id
     * @param diff the difference between the cards' ranks
     * @return a list of all cards of same suit as c, given that its rank is within diff of c's rank
     */
    public static long getSameSuit(long hand, int c, int diff) {
        return cardsToBitstring(getSameSuit(bitstringToCards(hand), Card.getCard(c), diff));
    }

    /**
     * @param cards A hand of cards
     * @return the card with the highest deadwood
     */
    public static Card getHighestDeadwood(ArrayList<Card> cards) {
        if(cards.size() == 0) return null;
        return cards
                .stream()
                .reduce(cards.get(0), (max, c) -> getDeadwoodPoints(c) > getDeadwoodPoints(max) ? c : max);
    }

    /**
     * @param cards A hand of cards
     * @return the id of the card with the highest deadwood
     */
    public static int getHighestDeadwood(long cards) {
        try {
            return getHighestDeadwood(bitstringToCards(cards)).getId();
        } catch (NullPointerException e) {
            return -1;
        }

    }

    /**
     * @param hand A hand of cards
     * @param drawnCard The card drawn - null if a card wasn't just picked up
     * @param face_up The card that was face-up before the draw
     * @param range The range of deadwoods still recorded. Range of 0 only gives those who would lower deadwood the most.
     * @return A list of all cards whose removal would lower the deadwood within range of the most
     */
    public static ArrayList<Card> findHighestDiscards (ArrayList<Card> hand, Card drawnCard, Card face_up, int range) {

        ArrayList<Card> candidateCards = new ArrayList<>(hand);
        HashMap<Integer, ArrayList<Card>> candidateLog = new HashMap<>();

        int minDeadwood = Integer.MAX_VALUE;

        //Find all cards whose removal would reduce the hand's deadwood by the max amount
        for (Card card : hand) {
            // Cannot draw and discard face up card.
            if (drawnCard != null && (card == drawnCard && drawnCard == face_up))
                continue;

            ArrayList<Card> remainingCards = new ArrayList<>(hand);
            remainingCards.remove(card);
            ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = cardsToBestMeldSets(remainingCards);
            int deadwood = bestMeldSets.isEmpty() ? getDeadwoodPoints(remainingCards) : getDeadwoodPoints(bestMeldSets.get(0), remainingCards);
            if (deadwood <= minDeadwood) {
                if (deadwood < minDeadwood) {

                    candidateLog.put(minDeadwood, new ArrayList<>(candidateCards));

                    candidateCards.clear();
                    minDeadwood = deadwood;

                }
                candidateCards.add(card);
            }
        }

        for(HashMap.Entry<Integer, ArrayList<Card>> entry : candidateLog.entrySet())
            if(entry.getKey() - minDeadwood <= range) candidateCards.addAll(entry.getValue());

        return candidateCards;
    }

    /**
     * @param hand A hand of cards
     * @param drawnCard The card drawn's id - -1 if a card wasn't just picked up
     * @param face_up The if of the card that was face-up before the draw
     * @param range The range of deadwoods still recorded
     * @return A list of all cards whose removal would lower the deadwood within range of the most
     */
    public static long findHighestDiscards (long hand, int drawnCard, int face_up, int range) {
        return cardsToBitstring(findHighestDiscards(bitstringToCards(hand),
                drawnCard == -1 ? null : Card.getCard(drawnCard),
                face_up == -1 ? null : Card.getCard(face_up), range));
    }

    /**
     * @param state The current state of the game
     * @return The expected deadwood for drawing one more card
     */
    public static double expectedDeadwoodForNextDraw(State state) {
        ArrayList<Card> unaccounted = bitstringToCards(state.getUnaccounted()); //Cards which have not been seen
        double sum = 0;

        for(Card card : unaccounted) sum += 1d/unaccounted.size() * getImprovement(bitstringToCards(state.getHand()), card);

        return sum;
    }

    /**
     * @param hand A hand of cards
     * @return The number of cards in the hand
     */
    public static int size(long hand) {
        String s = Long.toBinaryString(hand);
        int count = 0;
        for(int i = 0; i < s.length(); i++) {
            if(s.charAt(i) == '1') count++;
        }
        return count;
    }

    /**
     * Print out a hand of cards, with melds separated from the rest
     * @param hand A hand of cards
     */
    public static void printHandWithMelds(ArrayList<Card> hand) {
        ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(hand);
        ArrayList<ArrayList<Card>> bestMeldSet;
        ArrayList<Card> unmelded = MyGinRummyUtil.getUnmelded(hand, null);
        unmelded.sort((c1, c2) -> {
            String c1str;
            String c2str;
            char first;

            switch (c1.toString().charAt(0)) {
                case '2': {
                    first = 'B';
                    break;
                }
                case '3': {
                    first = 'C';
                    break;
                }
                case '4': {
                    first = 'D';
                    break;
                }
                case '5': {
                    first = 'E';
                    break;
                }
                case '6': {
                    first = 'F';
                    break;
                }
                case '7': {
                    first = 'G';
                    break;
                }
                case '8': {
                    first = 'H';
                    break;
                }
                case '9': {
                    first = 'I';
                    break;
                }
                case 'T': {
                    first = 'J';
                    break;
                }
                case 'J': {
                    first = 'K';
                    break;
                }
                case 'Q': {
                    first = 'L';
                    break;
                }
                case 'K': {
                    first = 'M';
                    break;
                }
                default: {
                    first = 'A';
                }

            }
            c1str = first + c1.toString().substring(1);

            switch (c2.toString().charAt(0)) {
                case '2': {
                    first = 'B';
                    break;
                }
                case '3': {
                    first = 'C';
                    break;
                }
                case '4': {
                    first = 'D';
                    break;
                }
                case '5': {
                    first = 'E';
                    break;
                }
                case '6': {
                    first = 'F';
                    break;
                }
                case '7': {
                    first = 'G';
                    break;
                }
                case '8': {
                    first = 'H';
                    break;
                }
                case '9': {
                    first = 'I';
                    break;
                }
                case 'T': {
                    first = 'J';
                    break;
                }
                case 'J': {
                    first = 'K';
                    break;
                }
                case 'Q': {
                    first = 'L';
                    break;
                }
                case 'K': {
                    first = 'M';
                    break;
                }
                default: {
                    first = 'A';
                }

            }
            c2str = first + c2.toString().substring(1);

            return c1str.compareTo(c2str);
        });
        unmelded.forEach(c -> System.out.print(c.toString() + " "));
        System.out.println();

        if(!bestMeldSets.isEmpty()) {
            bestMeldSet = bestMeldSets.get(0);

            for(ArrayList<Card> meld : bestMeldSet) {
                meld.sort((c1, c2) -> {
                    String c1str;
                    String c2str;
                    char first;

                    switch (c1.toString().charAt(0)) {
                        case '2': {
                            first = 'B';
                            break;
                        }
                        case '3': {
                            first = 'C';
                            break;
                        }
                        case '4': {
                            first = 'D';
                            break;
                        }
                        case '5': {
                            first = 'E';
                            break;
                        }
                        case '6': {
                            first = 'F';
                            break;
                        }
                        case '7': {
                            first = 'G';
                            break;
                        }
                        case '8': {
                            first = 'H';
                            break;
                        }
                        case '9': {
                            first = 'I';
                            break;
                        }
                        case 'T': {
                            first = 'J';
                            break;
                        }
                        case 'J': {
                            first = 'K';
                            break;
                        }
                        case 'Q': {
                            first = 'L';
                            break;
                        }
                        case 'K': {
                            first = 'M';
                            break;
                        }
                        default: {
                            first = 'A';
                        }

                    }
                    c1str = first + c1.toString().substring(1);

                    switch (c2.toString().charAt(0)) {
                        case '2': {
                            first = 'B';
                            break;
                        }
                        case '3': {
                            first = 'C';
                            break;
                        }
                        case '4': {
                            first = 'D';
                            break;
                        }
                        case '5': {
                            first = 'E';
                            break;
                        }
                        case '6': {
                            first = 'F';
                            break;
                        }
                        case '7': {
                            first = 'G';
                            break;
                        }
                        case '8': {
                            first = 'H';
                            break;
                        }
                        case '9': {
                            first = 'I';
                            break;
                        }
                        case 'T': {
                            first = 'J';
                            break;
                        }
                        case 'J': {
                            first = 'K';
                            break;
                        }
                        case 'Q': {
                            first = 'L';
                            break;
                        }
                        case 'K': {
                            first = 'M';
                            break;
                        }
                        default: {
                            first = 'A';
                        }

                    }
                    c2str = first + c2.toString().substring(1);

                    return c1str.compareTo(c2str);
                });
                meld.forEach(c -> System.out.print(c.toString() + " "));
                System.out.println();
            }

            System.out.println("Deadwood: " + MyGinRummyUtil.getDeadwoodPoints(bestMeldSet, hand));
        }

        else
            System.out.println("Deadwood: " + MyGinRummyUtil.getDeadwoodPoints(hand));
    }

    /**
     * Print out a hand of cards, with melds separated from the rest
     * @param hand A hand of cards
     */
    public static void printHandWithMelds(long hand) {
        printHandWithMelds(bitstringToCards(hand));
    }

    /**
     * Print out a hand of cards
     * @param hand A hand of cards
     */
    public static void printHand(ArrayList<Card> hand) {
        hand.sort((c1, c2) -> {
            String c1str;
            String c2str;
            char first;

            switch (c1.toString().charAt(0)) {
                case '2': {
                    first = 'B';
                    break;
                }
                case '3': {
                    first = 'C';
                    break;
                }
                case '4': {
                    first = 'D';
                    break;
                }
                case '5': {
                    first = 'E';
                    break;
                }
                case '6': {
                    first = 'F';
                    break;
                }
                case '7': {
                    first = 'G';
                    break;
                }
                case '8': {
                    first = 'H';
                    break;
                }
                case '9': {
                    first = 'I';
                    break;
                }
                case 'T': {
                    first = 'J';
                    break;
                }
                case 'J': {
                    first = 'K';
                    break;
                }
                case 'Q': {
                    first = 'L';
                    break;
                }
                case 'K': {
                    first = 'M';
                    break;
                }
                default: {
                    first = 'A';
                }

            }
            c1str = first + c1.toString().substring(1);

            switch (c2.toString().charAt(0)) {
                case '2': {
                    first = 'B';
                    break;
                }
                case '3': {
                    first = 'C';
                    break;
                }
                case '4': {
                    first = 'D';
                    break;
                }
                case '5': {
                    first = 'E';
                    break;
                }
                case '6': {
                    first = 'F';
                    break;
                }
                case '7': {
                    first = 'G';
                    break;
                }
                case '8': {
                    first = 'H';
                    break;
                }
                case '9': {
                    first = 'I';
                    break;
                }
                case 'T': {
                    first = 'J';
                    break;
                }
                case 'J': {
                    first = 'K';
                    break;
                }
                case 'Q': {
                    first = 'L';
                    break;
                }
                case 'K': {
                    first = 'M';
                    break;
                }
                default: {
                    first = 'A';
                }

            }
            c2str = first + c2.toString().substring(1);

            return c1str.compareTo(c2str);
        });

        hand.forEach(c -> System.out.print(c.toString() + " "));
        System.out.println();
    }

    /**
     * Print out a hand of cards
     * @param hand A hand of cards
     */
    public static void printHand(long hand) {
        printHand(bitstringToCards(hand));
    }

    /**
     * @param strategy A strategy
     * @return The encoded strategy
     */
    static String encoded(int[] strategy) {
        String encoded = "";
        for(int i = 0; i < strategy.length; i++) {
            encoded += Integer.toHexString(strategy[i]);
        }

        return encoded;
    }

    /**
     * @param hex An encoded strategy
     * @return The decoded strategy
     */
    static int[] decoded(String hex) {
        int[] strategy = new int[6];
        for(int i = 0; i < hex.length(); i++) {
            strategy[i] = (int) Long.parseLong(hex.substring(i, i + 1), 16);
        }

        return strategy;
    }

    /**
     * @param hex An encoded strategy
     * @return The decoded strategy
     */
    static int[] decoded(int hex) {
        String hexString = Integer.toHexString(hex);
        int[] strategy = new int[6];
        for(int i = 0; i < hexString.length(); i++) {
            strategy[i] = (int) Long.parseLong(hexString.substring(i, i + 1), 16);
        }

        return strategy;
    }
}

/**
 * Class to hold parameters for player strategy
 * TODO: Implement the regret matching algorithm for sequential games.
 */
 class GeneralStrategy {

    //<editor-fold desc="Instance Variables">
    /**
     * A map from the information set to the probability that we will knock
     */
    private HashMap<String, Double> knockStrat;

    /**
     * A map from the information set to the probability that we will draw
     */
    private HashMap<String, Double> drawStrat;

    /**
     * Max deadwood a card can contribute to a hand while not being able to be melded within 2 turns,
     * in order for us to avoid discarding it.
     */
    private int maxIsolatedSingleDeadwood;

    /**
     * After this turn has passed, we will no longer go out of our way to keep cards "protected" by maxIsolatedSingleDeadwood.
     */
    private int minIsolatedSingleDiscardTurn;

    /**
     * Max deadwood a card can contribute to a hand while not being able to be melded within 1 turn,
     * in order for us to avoid discarding it.
     */
    private int maxSingleDeadwood;

    /**
     * After this turn has passed, we will no longer go out of our way to keep cards "protected" by maxSingleDeadwood.
     */
    private int minSingleDiscardTurn;

    /**
     * If the probability of getting a gin within x turns is >= minWaitForGinProbability, we should wait to try to get a gin.
     * TODO: Figure out if this is even useful, and if it is, implement a way to calculate the probability of getting a gin within x turns.
     *  Maybe generalize to find the probability that total deadwood will become <= some y in x turns.
     */
    private double minWaitForGinProbability;

    /**
     * If our total deadwood is below this value, we should wait and try to undercut.
     * TODO: Determine whether this would affect our strategy differently than minWaitForGinProbability. They might mostly
     *  overlap, in which case I only need to consider one of the 2.
     */
    private int minUndercutDeadwood;

    /**
     * Min turn to try to layoff cards
     */
    private int minLayoffTurn;

    /**
     * Minimum change in deadwood the face-up card can contribute in order for us to consider drawing it.
     */
    private int minPickupDifference;
    //</editor-fold>

    /**
     * Constructor
     */

    GeneralStrategy(int[] strategy, HashMap<String, Double> knockStrat, HashMap<String, Double> drawStrat) {

        this.knockStrat = knockStrat;
        this.drawStrat = drawStrat;

        this.maxIsolatedSingleDeadwood = strategy[1] <= 10 && strategy[1] > 0 ? strategy[1] : 10;
        this.minIsolatedSingleDiscardTurn = Math.max(strategy[2], 0);

        this.maxSingleDeadwood = strategy[3] <= 10 && strategy[3] > 0 ? strategy[3] : 10;
        this.minSingleDiscardTurn = Math.max(strategy[4], 0);

        //this.minWaitForGinProbability = minWaitForGinProbability <= 1 && minWaitForGinProbability >= 0 ? minWaitForGinProbability : 0.0;

        //this.minUndercutDeadwood = minUndercutDeadwood <= 10 && minUndercutDeadwood >= 0 ? minUndercutDeadwood : 10;

        //this.minLayoffTurn = Math.max(minLayoffTurn, 0);

        this.minPickupDifference = strategy[5] <= 10 && strategy[5] >= 0 ? strategy[5] : 0;

    }

    //<editor-fold desc="Getters and Setters">
    public HashMap<String, Double> getKnockStrat() {
        return knockStrat;
    }

    public double getKnockAt(String s) {
        try {
            return knockStrat.get(s);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double getDrawAt(String s) {
        try {
            return drawStrat.get(s);
        } catch (Exception e) {
            return 0.0;
        }
    }

    int getMaxIsolatedSingleDeadwood() {
        return maxIsolatedSingleDeadwood;
    }

    int getMinIsolatedSingleDiscardTurn() {
        return minIsolatedSingleDiscardTurn;
    }

    int getMaxSingleDeadwood() {
        return maxSingleDeadwood;
    }

    int getMinSingleDiscardTurn() {
        return minSingleDiscardTurn;
    }

    double getMinWaitForGinProbability() {
        return minWaitForGinProbability;
    }

    int getMinUndercutDeadwood() {
        return minUndercutDeadwood;
    }

    int getMinLayoffTurn() {
        return minLayoffTurn;
    }

    int getMinPickupDifference() {
        return minPickupDifference;
    }

    public void setMaxIsolatedSingleDeadwood(int maxIsolatedSingleDeadwood) {
        this.maxIsolatedSingleDeadwood = maxIsolatedSingleDeadwood;
    }

    public void setMinIsolatedSingleDiscardTurn(int minIsolatedSingleDiscardTurn) {
        this.minIsolatedSingleDiscardTurn = minIsolatedSingleDiscardTurn;
    }

    public void setMaxSingleDeadwood(int maxSingleDeadwood) {
        this.maxSingleDeadwood = maxSingleDeadwood;
    }

    public void setMinSingleDiscardTurn(int minSingleDiscardTurn) {
        this.minSingleDiscardTurn = minSingleDiscardTurn;
    }

    public void setMinWaitForGinProbability(double minWaitForGinProbability) {
        this.minWaitForGinProbability = minWaitForGinProbability;
    }

    public void setMinUndercutDeadwood(int minUndercutDeadwood) {
        this.minUndercutDeadwood = minUndercutDeadwood;
    }

    public void setMinLayoffTurn(int minLayoffTurn) {
        this.minLayoffTurn = minLayoffTurn;
    }

    public void setMinPickupDifference(int minPickupDifference) {
        this.minPickupDifference = minPickupDifference;
    }
    //</editor-fold>
}