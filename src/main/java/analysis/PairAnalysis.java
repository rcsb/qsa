package analysis;

import io.FileOperations;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonin Pavelka
 */
public class PairAnalysis {

    private void run() {
        File f = new File("c:/tonik/data/fragments/result2.csv");
        String out = "c:/tonik/data/fragments/quarters/";
        Table t = new Table(f);
        double my = 0.05;
        double their = 0.7;
        List<List<String>> quarters = new ArrayList<>();
        for (int x = 0; x <= 1; x++) {
            for (int y = 0; y <= 1; y++) {
                t.setFilter(x, 4, my);
                t.setFilter(y, 5, their);
                List<String> q = t.getFiltered();
                quarters.add(q);
                FileOperations.save(q, new File(out + x + y));
                System.out.println(q.size());
                t.resetFilters();
            }
        }
    }

    public static void main(String[] args) {
        PairAnalysis pa = new PairAnalysis();
        pa.run();
    }
}
