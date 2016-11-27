package fragments.clustering;

import fragments.Fragment;
import fragments.Fragments;
import fragments.FragmentsFactory;
import fragments.Parameters;
import geometry.Transformer;
import io.Directories;
import java.util.ArrayList;
import java.util.List;
import pdb.MmtfStructureProvider;
import pdb.SimpleStructure;

public class FragmentClustering {

    private Parameters pars = Parameters.create();
    private Directories dir = Directories.createDefault();
    private List<Cluster> clusters = new ArrayList<>();

    public String[] getPdbCodes() {
        String[] codes = {"1fxi", "1ubq", "1ten", "3hhr", "3hla", "2rhe", "2aza", "1paz", "1cew", "1mol", "1cid", "2rhe", "1crl", "1ede", "2sim", "1nsb", "1bge", "2gmf", "1tie", "4fgf"};
        return codes;
    }

    public void create() {
        FragmentsFactory ff = new FragmentsFactory();
        MmtfStructureProvider provider = new MmtfStructureProvider(dir.getMmtf().toPath());
        Transformer tr = new Transformer();
        int count = 0;
        for (String code : getPdbCodes()) {
            SimpleStructure ss = provider.getStructure(code);
            Fragments fs = ff.create(ss, 1);
            count+=fs.size();
            for (Fragment x : fs.getFragments()) {
                boolean added = false;
                for (Cluster c : clusters) {
                    Fragment y = c.getRepresentant();
                    tr.set(x.getPoints3d(), y.getPoints3d());
                    double rmsd = tr.getRmsd();
                    if (rmsd < 4) {
                        c.add(x);
                        added = true;
                    }

                }
                if (!added) {
                    Cluster c = new Cluster();
                    c.add(x);
                    clusters.add(c);
                }
            }
            System.out.println("clusters: " + clusters.size() + " / " + count);
            
        }
        for (Cluster c : clusters) {
            System.out.println(c.size());
        }
    }

    public static void main(String[] args) {
        FragmentClustering fc = new FragmentClustering();
        fc.create();
    }

}
