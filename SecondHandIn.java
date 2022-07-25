import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SecondHandIn {

    private static ArrayList<ArrayList<Integer>> BLOSUM50;
    private static HashMap<Character, Integer> aminoToIndex;
    private static ArrayList<ArrayList<Integer>> matrix;
    private static ArrayList<ArrayList<Character>> directions;
    private static String blosumPath = "C:\\Users\\Ed\\OneDrive - University of Southampton\\3rd Yeard\\CompBio\\BLOSUM50.txt";
    private static char[] sequenceA = " HEAGAWGHEE".toCharArray();
    private static char[] sequenceB = " PAWHEAE".toCharArray();
    private static char[] sequenceC = " SALPQPTTPVSSFTSGSMLGRTDTALTNTYSAL".toCharArray();
    private static char[] sequenceD = " PSPTMEAVTSVEASTASHPHSTSSYFATTYYHLY".toCharArray();
    private static char[] sequenceE = " MQNSHSGVNQLGGVFVNGRPLPDSTRQKIVELAHSGARPCDISRILQVSNGCVSKILGRY".toCharArray();
    private static char[] sequenceF = " TDDECHSGVNQLGGVFVGGRPLPDSTRQKIVELAHSGARPCDISRI".toCharArray();
    private static String finalA;
    private static String finalB;


    public static void main(String args[]) throws IOException {
        mapAminoToIndex();
        loadBLOSUM();
        needlemanWulch(sequenceA, sequenceB);
        System.out.println("Needleman-Wulch:");
        System.out.println("Part A: " + finalA + ", " + finalB);
        needlemanWulch(sequenceC, sequenceD);
        System.out.println("Part B: " + finalA + ", " + finalB);
        System.out.println("");
        System.out.println("Smith-Waterman:");
        smithWaterman(sequenceA, sequenceB);
        System.out.println("Part A: " + finalA + ", " + finalB);
        smithWaterman(sequenceE, sequenceF);
        System.out.println("Part B: " + finalA + ", " + finalB);
    }

    //Sets variables final A and B to results of supplied char[]
    private static void smithWaterman(char[] seq1, char[] seq2) {
        matrix = new ArrayList<>();
        directions = new ArrayList<>();
        int rowInd = 0;
        int gap = 0;
        for(char chaRow : seq2) {
            int colInd = 0;
            ArrayList<Integer> row = new ArrayList<>();
            ArrayList<Character> rowDir = new ArrayList<>();
            for (char chaCol : seq1) {
                int left;
                int up;
                int diag;

                if(colInd == 0 || rowInd == 0) {
                    row.add(0);
                    rowDir.add('g');
                }
                else {
                    left = row.get(colInd - 1) - 8;
                    up = matrix.get(rowInd - 1).get(colInd) - 8;
                    diag = matrix.get(rowInd - 1).get(colInd - 1) + BLOSUM50.get(aminoToIndex.get(chaRow)).get(aminoToIndex.get(chaCol));
                    int[] numbers = {gap, left, up, diag};
                    if(diag == getMaxValue(numbers)){
                        rowDir.add('d');
                        row.add(diag);
                    }
                    else if(gap == getMaxValue(numbers)) {
                        rowDir.add('g');
                        row.add(gap);
                    }
                    else if(left == getMaxValue(numbers)) {
                        rowDir.add('l');
                        row.add(left);
                    }
                    else {
                        rowDir.add('u');
                        row.add(up);
                    }
                }
                colInd++;
            }
            matrix.add(row);
            directions.add(rowDir);
            rowInd++;
        }
        ArrayList<Character> codedSeq = workBack(true);

        String a = "";
        String b = "";

        HashMap<Integer, Integer> startPlace = findMaxStart();
        int seq1Index = 0;
        int seq2Index = 0;
        for(Map.Entry<Integer, Integer> entry : startPlace.entrySet()) {
            seq2Index = entry.getKey();
            seq1Index = entry.getValue();
        }
        for(char cha : codedSeq) {
            if(cha == 'l') {
                a = a + seq1[seq1Index];
                seq1Index--;
                b = b + "-";
            }
            else if(cha == 'u') {
                b = b + seq2[seq2Index];
                seq2Index--;
                a = a + "-";
            }
            else if(cha == 'd') {
                a = a + seq1[seq1Index];
                b = b + seq2[seq2Index];
                seq1Index--;
                seq2Index--;
            } else {
                break;
            }
        }
        finalA = new StringBuilder(a).reverse().toString();
        finalB = new StringBuilder(b).reverse().toString();
    }

    // backtracking method - distinguishes between smith-waterman and needleman-wulch backtracking
    private static ArrayList<Character> workBack(Boolean isSmith) {

        ArrayList<Character> codedSeq = new ArrayList<>();
        if(!isSmith) {
            int rowIndex = directions.size()-1;
            int colIndex = directions.get(directions.size()-1).size()-1;

            while(!(rowIndex == 0) || !(colIndex == 0)) {
                char a = directions.get(rowIndex).get(colIndex);
                if(a == 'l') {
                    codedSeq.add(a);
                    colIndex--;
                }
                else if(a == 'u') {
                    codedSeq.add(a);
                    rowIndex--;
                }
                else if(a == 'd') {
                    codedSeq.add(a);
                    rowIndex--;
                    colIndex--;
                }
            }
        } else {
            int rowIndex = 0;
            int colIndex = 0;
            HashMap<Integer, Integer> startPlace = findMaxStart();
            for(Map.Entry<Integer, Integer> entry : startPlace.entrySet()) {
                rowIndex = entry.getKey();
                colIndex = entry.getValue();
            }
            while(!(matrix.get(rowIndex).get(colIndex) == 0)) {
                char a = directions.get(rowIndex).get(colIndex);
                if(a == 'l') {
                    codedSeq.add(a);
                    colIndex--;
                }
                else if(a == 'u') {
                    codedSeq.add(a);
                    rowIndex--;
                }
                else if(a == 'd') {
                    codedSeq.add(a);
                    rowIndex--;
                    colIndex--;
                } else {
                    break;
                }
            }
        }
        return codedSeq;
    }

    //finds start point for backtracking
    private static HashMap<Integer, Integer> findMaxStart() {
        int max = Integer.MIN_VALUE;
        HashMap<Integer, Integer> maxIndex = new HashMap<>();
        for(int i = 0; i < matrix.size(); i++) {
            for(int y = 0; y < matrix.get(i).size(); y++) {
                if (matrix.get(i).get(y) > max) {
                    max = matrix.get(i).get(y);
                    maxIndex.clear();
                    maxIndex.put(i , y);
                }
            }
        }
        return maxIndex;
    }

    //Sets variables final A and B to results of supplied char[]
    private static void needlemanWulch(char[] seq1, char[] seq2) {
        matrix = new ArrayList<>();
        directions = new ArrayList<>();
        int rowInd = 0;
        for(char chaRow : seq2) {
            int colInd = 0;
            ArrayList<Integer> row = new ArrayList<>();
            ArrayList<Character> rowDir = new ArrayList<>();
            for(char chaCol : seq1) {
               int left;
               int up;
               int diag;

               if(colInd == 0 && rowInd == 0) {
                   row.add(0);
               }
               else if(colInd == 0 || rowInd == 0) {
                   if(colInd == 0) {
                       rowDir.add('u');
                       row.add(matrix.get(rowInd-1).get(colInd) - 8);
                   }
                   else {
                       rowDir.add('l');
                       row.add(row.get(colInd-1) - 8);
                   }
               }
               else {
                   left = row.get(colInd - 1) - 8;
                   up = matrix.get(rowInd - 1).get(colInd) - 8;
                   diag = matrix.get(rowInd - 1).get(colInd - 1) + BLOSUM50.get(aminoToIndex.get(chaRow)).get(aminoToIndex.get(chaCol));

                   if(left > up && left > diag) {
                       row.add(left);
                       rowDir.add('l');
                   }
                   else if(up > diag) {
                       row.add(up);
                       rowDir.add('u');
                   }
                   else {
                       row.add(diag);
                       rowDir.add('d');
                   }
               }
               colInd++;
            }
            matrix.add(row);
            directions.add(rowDir);
            rowInd++;
        }

        ArrayList<Character> codedSeq = workBack(false);
        String a = "";
        String b = "";

        int seq1Index = seq1.length-1;
        int seq2Index = seq2.length-1;
        for(char cha : codedSeq) {
            if(cha == 'l') {
                a = a + seq1[seq1Index];
                seq1Index--;
                b = b + "-";
            }
            else if(cha == 'u') {
                b = b + seq2[seq2Index];
                seq2Index--;
                a = a + "-";
            }
            else {
                a = a + seq1[seq1Index];
                b = b + seq2[seq2Index];
                seq1Index--;
                seq2Index--;
            }
        }
        finalA = new StringBuilder(a).reverse().toString();
        finalB = new StringBuilder(b).reverse().toString();
    }

    private static void loadBLOSUM() throws IOException {
        BLOSUM50 = new ArrayList<>();
        BufferedReader csvReader = new BufferedReader(new FileReader(blosumPath));
        String trainRow;
        while((trainRow = csvReader.readLine()) != null) {
            String[] data = trainRow.split(" ");
            ArrayList<Integer> row = new ArrayList<>();
            for(String s : data) {
                if(!s.equals("")) row.add(Integer.parseInt(s));
            }
            BLOSUM50.add(row);
        }
        csvReader.close();
    }

    private static void mapAminoToIndex() {
        aminoToIndex = new HashMap<>();
        char[] chars = {'A','R','N','D','C','Q','E','G','H','I','L','K','M','F','P','S','T','W','Y','V'};
        int ind = 0;
        for(char cha : chars) {
            aminoToIndex.put(cha, ind);
            ind++;
        }
    }

    public static int getMaxValue(int[] numbers){
        int maxValue = numbers[0];
        for(int i=1;i < numbers.length;i++){
            if(numbers[i] > maxValue){
                maxValue = numbers[i];
            }
        }
        return maxValue;
    }
}
