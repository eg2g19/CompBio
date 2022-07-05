import java.io.*;
import java.util.Scanner;

public class SequenceLoader {

    private String sequence;

    public SequenceLoader(String s) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(s));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            sequence = sb.toString();
        } finally {
            br.close();
        }
    }

    public String getSequence(){
        return sequence;
    }
}

