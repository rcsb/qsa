package fragments.range;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class WordVectorReader {

    public VectorData read(File f, double rmsd) {
        List<double[]> p = new ArrayList<>();
        List<double[]> n = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line, ", \t");
                List<Double> ds = new ArrayList<>();
                while (st.hasMoreTokens()) {
                    ds.add(Double.parseDouble(st.nextToken()));
                }
                List<Double> values = ds.subList(1, ds.size());
                double[] a = new double[values.size()];
                for (int i = 0; i < a.length; i++) {
                    a[i] = values.get(i);
                }
                if (ds.get(0) <= rmsd) {
                    p.add(a);
                } else {
                    n.add(a);
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        double[][] positives = new double[p.size()][p.get(0).length];
        p.toArray(positives);
        double[][] negatives = new double[n.size()][n.get(0).length];
        n.toArray(negatives);
        VectorData data = new VectorData(negatives, positives);
        return data;
    }
}
