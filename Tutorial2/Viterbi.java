import javax.swing.text.Document;
import javax.xml.parsers.DocumentBuilder;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Viterbi {

    static enum State {
        AT,
        CG,
        NaN
    }

//    static String sequence = "ATTAGTCCACTGTGATAAAACTGCCAGTCTCTTTACGGTCTCCGTGAAGTG" +
//            "GACTTATGTTAATAATGGATCTAAGGTAGCAAGTCGTATTATTGCCCATGTAGGAGCGTGTAATTTCG" +
//            "CTTCTACCAACTTACTAGCCTTTAATACGTTGTGCTTGATTGCTAACTGTAAGGTGCTGCGGTTTTGT" +
//            "TGCTAACTATCTTTAGGAAAATTATTAGTTCGGGTGCTCTTATATACTCGATCAAATAAGCGCTGTCA" +
//            "CGATAAATTACTTTTTTAGCTACTACCGTTTGACGGTTCTGTATAAAACGCCCGTACCCTACCGCCGA" +
//            "AACCCTTCGAAGTCCCTATATATCGGGTCACTCTTTTTTATTGTATTCACGTTAGAATTTCATCTCGT" +
//            "ATCAAGGC";

    static String sequence;

    static String filePath = "C:\\Users\\Ed\\OneDrive - University of Southampton\\3rd Yeard\\CompBio\\LambdaPhageSequence.txt";


    private static HashMap<String, HashMap<String, Double>> stateToBaseToProbability;
    private static ArrayList<Double> ATRowProbabilities;
    private static ArrayList<Double> CGRowProbabilities;
    private static ArrayList<String> ATRowRoutes;
    private static ArrayList<String> CGRowRoutes;

    private static String[] finalRoute;

    //AT is red, CG is green
    private static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";



    // log(x) + log(x) = log(x * x)
    // more negative = higher probability

    public static void main(String[] args) throws IOException {
        loadProbabilityMap();
        loadSequence();

        ATRowProbabilities = new ArrayList<>();
        CGRowProbabilities = new ArrayList<>();
        ATRowRoutes = new ArrayList<>();
        CGRowRoutes = new ArrayList<>();


        for(int i = 0; i < sequence.length(); i++) {
            calculateP(i);
        }
        backTrack();
        colourSequence();
        writeResults();
    }

    private static void loadSequence() throws IOException {
        SequenceLoader sl = new SequenceLoader(filePath);
        sequence = sl.getSequence();
//        sequence = "ATTAGTCCACTGTGATAAAACTGCCAGTCTCTTTACGGTCTCCGTGAAGTG" +
//                "GACTTATGTTAATAATGGATCTAAGGTAGCAAGTCGTATTATTGCCCATGTAGGAGCGTGTAATTTCG" +
//                "CTTCTACCAACTTACTAGCCTTTAATACGTTGTGCTTGATTGCTAACTGTAAGGTGCTGCGGTTTTGT" +
//                "TGCTAACTATCTTTAGGAAAATTATTAGTTCGGGTGCTCTTATATACTCGATCAAATAAGCGCTGTCA" +
//                "CGATAAATTACTTTTTTAGCTACTACCGTTTGACGGTTCTGTATAAAACGCCCGTACCCTACCGCCGA" +
//                "AACCCTTCGAAGTCCCTATATATCGGGTCACTCTTTTTTATTGTATTCACGTTAGAATTTCATCTCGT" +
//                "ATCAAGGC";
    }

    //outputs final sequence with colours corresponding to states
    private static void colourSequence() {
        int index = 0;
        boolean isAT = true;
        for(char c : sequence.toCharArray()) {
            if(index % 350 == 0) System.out.println();
            if(finalRoute[index] == "AT") {
                if(isAT == false) {
//                    System.out.println();
                }
                System.out.print(ANSI_RED + c);
                isAT = true;
            }
            else {
                if(isAT == true) {
//                    System.out.println();
                }
                System.out.print(ANSI_GREEN + c);
                isAT = false;
            }
            System.out.print(ANSI_RESET);
            index++;
        }
    }

    // responsible for backwards pass of algorithm to determine most probable path taken
    private static void backTrack() {

        finalRoute = new String[ATRowProbabilities.size()];
        boolean isAT;
        if(ATRowProbabilities.get(ATRowProbabilities.size()-1) > CGRowProbabilities.get(CGRowProbabilities.size()-1)) isAT = true;
        else isAT = false;
        //start in AT row
        if(isAT) {
            for(int index = finalRoute.length-1; index >= 0; index--) {
                if(index == finalRoute.length-1) {
                    finalRoute[index] = "AT";
                } else {
                    if(finalRoute[index+1] == "AT" && ATRowRoutes.get(index+1) == "Same") finalRoute[index] = "AT";
                    else if(finalRoute[index+1] == "AT" && ATRowRoutes.get(index+1) == "Switch") finalRoute[index] = "CG";
                    else if(finalRoute[index+1] == "CG" && ATRowRoutes.get(index+1) == "Same") finalRoute[index] = "CG";
                    else finalRoute[index] = "AT";
                }
            }
        }
        //start in CG row
        else {
            for(int index = finalRoute.length-1; index >= 0; index--) {
                if(index == finalRoute.length-1) {
                    finalRoute[index] = "CG";
                } else {
                    if(finalRoute[index+1] == "AT" && ATRowRoutes.get(index+1) == "Same") finalRoute[index] = "AT";
                    else if(finalRoute[index+1] == "AT" && ATRowRoutes.get(index+1) == "Switch") finalRoute[index] = "CG";
                    else if(finalRoute[index+1] == "CG" && ATRowRoutes.get(index+1) == "Same") finalRoute[index] = "CG";
                    else finalRoute[index] = "AT";
                }
            }
        }
    }


    //Pa(G,t) = Pa(G) + max(Pa(one before) + P(aaTransition) || Pc(one before) + P(caTransition))
    //Pc(G,t) = Pc(G) + max(Pa(one before) + P(aaTransition) || Pc(one before) + P(acTransition))
    //calculates respective probabilites of being in AT rich or CG rich state at a given index
    private static void calculateP(int index) {

        if(index == 0) {
            ATRowProbabilities.add(Math.log(0.5) + stateToBaseToProbability.get("AT").get(sequence.substring(0,1)));
            ATRowRoutes.add("Start");
            CGRowProbabilities.add(Math.log(0.5) + stateToBaseToProbability.get("CG").get(sequence.substring(0,1)));
            CGRowRoutes.add("Start");
        } else {
            Double pATStateSame = 0d;
            Double pATStateSwitch = 0d;
            pATStateSame = stateToBaseToProbability.get("AT").get(sequence.substring(index, index + 1))
                    + ATRowProbabilities.get(ATRowProbabilities.size() - 1)
                    + stateToBaseToProbability.get("AT").get("Same");
            pATStateSwitch = stateToBaseToProbability.get("AT").get(sequence.substring(index, index + 1))
                    + CGRowProbabilities.get(CGRowProbabilities.size() - 1)
                    + stateToBaseToProbability.get("CG").get("Switch");

            if (pATStateSame > pATStateSwitch) {
                ATRowProbabilities.add(pATStateSame);
                ATRowRoutes.add("Same");
            } else {
                ATRowProbabilities.add(pATStateSwitch);
                ATRowRoutes.add("Switch");
            }

            Double pCGStateSame = 0d;
            Double pCGStateSwitch = 0d;
            pCGStateSame = stateToBaseToProbability.get("CG").get(sequence.substring(index, index + 1))
                    + CGRowProbabilities.get(CGRowProbabilities.size() - 1)
                    + stateToBaseToProbability.get("CG").get("Same");
            pCGStateSwitch = stateToBaseToProbability.get("AT").get(sequence.substring(index, index + 1))
                    + ATRowProbabilities.get(ATRowProbabilities.size() - 1)
                    + stateToBaseToProbability.get("AT").get("Switch");

            if (pCGStateSame > pCGStateSwitch) {
                CGRowProbabilities.add(pCGStateSame);
                CGRowRoutes.add("Same");
            } else {
                CGRowProbabilities.add(pCGStateSwitch);
                CGRowRoutes.add("Switch");
            }
        }

    }

    private static void loadProbabilityMap() {
        stateToBaseToProbability = new HashMap<>();

        HashMap<String, Double> ATBaseToProbabilities = new HashMap<>();
        ATBaseToProbabilities.putIfAbsent("A", Math.log(0.2698));
        ATBaseToProbabilities.putIfAbsent("T", Math.log(0.3237));
        ATBaseToProbabilities.putIfAbsent("C", Math.log(0.2080));
        ATBaseToProbabilities.putIfAbsent("G", Math.log(0.1985));
        ATBaseToProbabilities.putIfAbsent("Same", Math.log(0.9997));
        ATBaseToProbabilities.putIfAbsent("Switch", Math.log(0.0003));
        stateToBaseToProbability.putIfAbsent("AT", ATBaseToProbabilities);

        HashMap<String, Double> CGBaseToProbabilities = new HashMap<>();
        CGBaseToProbabilities.putIfAbsent("A", Math.log(0.2459));
        CGBaseToProbabilities.putIfAbsent("T", Math.log(0.2079));
        CGBaseToProbabilities.putIfAbsent("C", Math.log(0.2478));
        CGBaseToProbabilities.putIfAbsent("G", Math.log(0.2984));
        CGBaseToProbabilities.putIfAbsent("Same", Math.log(0.9998));
        CGBaseToProbabilities.putIfAbsent("Switch", Math.log(0.0002));
        stateToBaseToProbability.putIfAbsent("CG", CGBaseToProbabilities);
    }

    private static void testPs() {
        System.out.println("AT - A P = " + stateToBaseToProbability.get("AT").get("A"));
        System.out.println("Should be = " + Math.log(0.2698));
        System.out.println("AT - T P = " + stateToBaseToProbability.get("AT").get("T"));
        System.out.println("Should be = " + Math.log(0.3237));
        System.out.println("AT - C P = " + stateToBaseToProbability.get("AT").get("C"));
        System.out.println("Should be = " + Math.log(0.2080));
        System.out.println("AT - G P = " + stateToBaseToProbability.get("AT").get("G"));
        System.out.println("Should be = " + Math.log(0.1985));
        System.out.println("AT - Same P = " + stateToBaseToProbability.get("AT").get("Same"));
        System.out.println("Should be = " + Math.log(0.9997));
        System.out.println("AT - Switch P = " + stateToBaseToProbability.get("AT").get("Switch"));
        System.out.println("Should be = " + Math.log(0.0003));
        System.out.println("CG - A P = " + stateToBaseToProbability.get("CG").get("A"));
        System.out.println("Should be = " + Math.log(0.2459));
        System.out.println("CG - T P = " + stateToBaseToProbability.get("CG").get("T"));
        System.out.println("Should be = " + Math.log(0.2079));
        System.out.println("CG - C P = " + stateToBaseToProbability.get("CG").get("C"));
        System.out.println("Should be = " + Math.log(0.2478));
        System.out.println("CG - G P = " + stateToBaseToProbability.get("CG").get("G"));
        System.out.println("Should be = " + Math.log(0.2984));
        System.out.println("CG - Same P = " + stateToBaseToProbability.get("CG").get("Same"));
        System.out.println("Should be = " + Math.log(0.9998));
        System.out.println("CG - Switch P = " + stateToBaseToProbability.get("CG").get("Switch"));
        System.out.println("Should be = " + Math.log(0.0002));
    }
}
