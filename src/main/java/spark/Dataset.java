package spark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import pdb.PdbChainId;
import scala.Tuple2;

/**
 *
 * @author Antonin Pavelka
 */
public class Dataset {

    private Map<PdbChainPair, ReferenceMetrics> data = new HashMap<>();

    public Dataset(File f) {
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line, ", ");
                PdbChainId a = new PdbChainId(st.nextToken());
                PdbChainId b = new PdbChainId(st.nextToken());
                double tmScore = Double.parseDouble(st.nextToken());
                double rmsd = Double.parseDouble(st.nextToken());
                double probability = Double.parseDouble(st.nextToken());
                double alignmentLength = Double.parseDouble(st.nextToken());
                double coverage1 = Double.parseDouble(st.nextToken());
                double coverage2 = Double.parseDouble(st.nextToken());
                ReferenceMetrics rm = new ReferenceMetrics(tmScore, rmsd,
                        probability, alignmentLength, coverage1, coverage2);
                PdbChainPair key = new PdbChainPair(a, b);
                data.put(key, rm);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public ReferenceMetrics getReferenceMetrics(PdbChainId a, PdbChainId b) {
        return data.get(new PdbChainPair(a, b));
    }

    public List<Tuple2<PdbChainId, PdbChainId>> getPairs() {
        List<Tuple2<PdbChainId, PdbChainId>> list = new ArrayList<>();
        for (PdbChainPair sp : data.keySet()) {
            PdbChainId[] a = sp.get();
            list.add(new Tuple2(a[0], a[1]));
        }
        return list;
    }
}
