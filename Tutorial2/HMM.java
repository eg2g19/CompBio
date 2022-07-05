import java.util.Random;

public class HMM {

    static int pAATRich = 2698;
    static int pTATRich = 3237;
    static int pCATRich = 2080;
    static int pGATRich = 1985;

    static int pACGRich = 2459;
    static int pTCGRich = 2079;
    static int pCCGRich = 2478;
    static int pGCGRich = 2984;

    static int stayAT = 9998;
    static int moveToGC = 2;
    static int stayCG = 9997;
    static int moveToAT = 3;

    //True if in state AT rich; false if in state CG Rich
    static boolean isAT;

    public static void main(String args[]){


        Random rand = new Random();
        isAT = rand.nextBoolean();
        int AorTCount = 0;
        int CorGCount = 0;

        // generates sequence using the HMM of 500 bases
        for(int i = 0; i < 500; i++) {
            if(i % 100 == 0) System.out.print("\n");
            //outputs base based on probability distribution of HMM
            if (isAT) {
                int nextInt = rand.nextInt(9999);
                if (nextInt < pAATRich - 1) {
                    System.out.print("A");
                    AorTCount++;
                } else if (nextInt < pAATRich + pTATRich - 1) {
                    System.out.print("T");
                    AorTCount++;
                } else if (nextInt < pCATRich + pAATRich + pTATRich - 1) {
                    System.out.print("C");
                    CorGCount++;
                } else if (nextInt < pGATRich + pCATRich + pAATRich + pTATRich - 1) {
                    System.out.print("G");
                    CorGCount++;
                }
            } else {
                int nextInt = rand.nextInt(9999);
                if (nextInt < pACGRich - 1) {
                    System.out.print("A");
                    AorTCount++;
                } else if (nextInt < pACGRich + pTCGRich - 1) {
                    System.out.print("T");
                    AorTCount++;
                } else if (nextInt < pCCGRich + pACGRich + pTCGRich - 1) {
                    System.out.print("C");
                    CorGCount++;
                } else if (nextInt < pGCGRich + pCCGRich + pACGRich + pTCGRich - 1) {
                    System.out.print("G");
                    CorGCount++;
                }
            }

            // decides whether to change state of HMM based on probability distribution of HMM
            if (isAT) {
                int nextInt = rand.nextInt(9999);
                if (nextInt < stayAT - 1) {

                } else {
                    isAT = false;
                }
            } else {
                int nextInt = rand.nextInt(9999);
                if (nextInt < stayCG - 1) {

                } else {
                    isAT = true;
                }
            }
        }
        System.out.println("\nA and T count = " + AorTCount + "\nC and G count = " + CorGCount);
    }
}
