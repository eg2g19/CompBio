import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Viterbi1 {


    static String sequence = "ATTAGTCCACTGTGATAAAACTGCCAGTCTCTTTACGGTCTCCGTGAAGTG" +
            "GACTTATGTTAATAATGGATCTAAGGTAGCAAGTCGTATTATTGCCCATGTAGGAGCGTGTAATTTCG" +
            "CTTCTACCAACTTACTAGCCTTTAATACGTTGTGCTTGATTGCTAACTGTAAGGTGCTGCGGTTTTGT" +
            "TGCTAACTATCTTTAGGAAAATTATTAGTTCGGGTGCTCTTATATACTCGATCAAATAAGCGCTGTCA" +
            "CGATAAATTACTTTTTTAGCTACTACCGTTTGACGGTTCTGTATAAAACGCCCGTACCCTACCGCCGA" +
            "AACCCTTCGAAGTCCCTATATATCGGGTCACTCTTTTTTATTGTATTCACGTTAGAATTTCATCTCGT" +
            "ATCAAGGC";

    static int pAATRich = 2698;
    static int pTATRich = 3237;
    static int pCATRich = 2080;
    static int pGATRich = 1985;

    static int pACGRich = 2459;
    static int pTCGRich = 2079;
    static int pCCGRich = 2478;
    static int pGCGRich = 2983;

    static int stayAT = 9998;
    static int moveToGC = 2;
    static int stayCG = 9997;
    static int moveToAT = 3;

    static HashMap<String, Integer> probabilitiesAT;
    static HashMap<String, Integer> probabilitiesCG;

    static enum State {
        AT,
        CG,
        NaN
    }

    public static void main(String[] args) {

        loadProbabilities();
        State state = State.NaN;
        Random rand = new Random();
    }

    //state is the state at current
    private static HashMap<ArrayList<Viterbi1.State>, Double> pathProbablity(int seqIndex, Double logProbabilty, State state, Random rand, ArrayList<Viterbi1.State> path, boolean hasSwitched){
        Double sameState = 0d;
        Double switchState = 0d;
        ArrayList<Viterbi1.State> newPath = path;
        HashMap<ArrayList<Viterbi1.State>, Double> sameStateResult;
        int probabilityOfBaseAtIndex;
        int transitionCost;

        if(path.size() == sequence.length()) {
            HashMap<ArrayList<Viterbi1.State>, Double> pathProbability = new HashMap<>();
            pathProbability.put(path, logProbabilty);
            return pathProbability;
        }

        if(seqIndex == 0) {
            if(rand.nextInt(2) == 0) state = State.AT;
            else state = State.CG;
        } else {
            Double newProbability;
            if (state.equals(State.AT)){
                probabilityOfBaseAtIndex = probabilitiesAT.get(sequence.substring(seqIndex,seqIndex+1));
            } else {
                probabilityOfBaseAtIndex = probabilitiesCG.get(sequence.substring(seqIndex,seqIndex+1));
            }
            if(hasSwitched) {
                if(state.equals(State.AT)) {
                    transitionCost = probabilitiesCG.get("Switch");
                } else {
                    transitionCost = probabilitiesAT.get("Switch");
                }
            } else {
                if(state.equals(State.AT)) {
                    transitionCost = probabilitiesAT.get("Same");
                } else {
                    transitionCost = probabilitiesCG.get("Same");
                }
            }
            newProbability = Math.log((double) transitionCost * probabilityOfBaseAtIndex / 1000);
            ArrayList<Viterbi1.State> sameStatePath = path;
            sameStatePath.add(state);
            sameStateResult = pathProbablity(seqIndex+1, newProbability, state, rand, sameStatePath, false);
            for(Map.Entry<ArrayList<Viterbi1.State>, Double> entry : sameStateResult.entrySet()) {
                sameState = entry.getValue();
            }
        }


        if(sameState > switchState) {
            HashMap<ArrayList<Viterbi1.State>, Double> r = new HashMap<>();
            r.put(newPath, sameState);
            return r;
        }
        else {
            HashMap<ArrayList<Viterbi1.State>, Double> r = new HashMap<>();
            r.put(newPath, switchState);
            return r;
        }
    }

    private static void loadProbabilities() {
        probabilitiesAT.putIfAbsent("A", 2698);
        probabilitiesAT.putIfAbsent("T", 3237);
        probabilitiesAT.putIfAbsent("C", 2080);
        probabilitiesAT.putIfAbsent("G", 1985);
        probabilitiesAT.putIfAbsent("Same", 9998);
        probabilitiesAT.putIfAbsent("Switch", 2);

        probabilitiesCG.putIfAbsent("A", 2459);
        probabilitiesCG.putIfAbsent("T", 2079);
        probabilitiesCG.putIfAbsent("C", 2478);
        probabilitiesCG.putIfAbsent("G", 2984);
        probabilitiesCG.putIfAbsent("Same", 9997);
        probabilitiesCG.putIfAbsent("Switch", 3);
    }
}
