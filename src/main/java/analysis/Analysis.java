package analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 *
 * @author Antonin Pavelka
 */
public class Analysis {

    public static void main(String[] args) {
        File dir = new File("c:/tonik/data/fragments/analysis/plots");
        Table table = new Table();
        for (File f : dir.listFiles()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line;
                while ((line = br.readLine()) != null) {
                    table.add(line);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        table.sort(1);
        for (int i = 0; i < 10; i++) {
            for (double d : table.get().get(i)) {
                System.out.print(d + " ");
            }
            System.out.println();
        }
    }
}
