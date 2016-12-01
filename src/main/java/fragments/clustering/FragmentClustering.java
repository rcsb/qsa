package fragments.clustering;

import fragments.Fragment;
import fragments.Fragments;
import fragments.FragmentsFactory;
import fragments.Parameters;
import geometry.Transformer;
import grid.BufferProcessor;
import grid.Grid;
import io.Directories;
import java.util.ArrayList;
import java.util.List;
import pdb.MmtfStructureProvider;
import pdb.SimpleStructure;
import util.pymol.PymolVisualizer;

public class FragmentClustering {

    private Parameters pars = Parameters.create();
    private Directories dir = Directories.createDefault();
    private List<Cluster> clusters = new ArrayList<>();

    public String[] getPdbCodes() {
        String[] codes = {"1fxi", "1ubq", "1ten", "3hhr", "3hla", "2rhe", "2aza", "1paz", "1cew", "1mol", "1cid", "2rhe", "1crl", "1ede", "2sim", "1nsb", "1bge", "2gmf", "1tie", "4fgf"};
        return codes;
    }

    public Grid<Cluster> createGrid() {
        double[] min = {0, 0, 0};
        double[] max = {100, 100, 100};
        double[] range = {4, 4, 1};
        Grid<Cluster> s = new Grid<>(min, max, range);
        return s;
    }

    public void create() {
        FragmentsFactory ff = new FragmentsFactory();
        MmtfStructureProvider provider = new MmtfStructureProvider(dir.getMmtf().toPath());
        Transformer tr = new Transformer();
        int count = 0;

        Grid<Cluster> g = createGrid();

        for (String code : getPdbCodes()) {

            SimpleStructure ss = provider.getStructure(code);
            Fragments fs = ff.create(ss, 1);
            count += fs.size();
            int missing = 0;
            int shared = 0;
            int total = 0;
            int box = 0;

            for (Fragment x : fs.getFragments()) {
                boolean added = false;
                BufferProcessor processor = new BufferProcessor();
                g.search(x, processor);
                //Set result = processor.get();
                total += g.size();
                box += processor.size();
                //for (Object o : result) {
                //    Fragment f = (Fragment) o;
                //    System.out.print(f + " ");
                //}
                //System.out.println();
                for (int i = 0; i < processor.size(); i++) {
                //for (Object o : result) {
                    Cluster c = (Cluster) processor.get(i);
                //for (Cluster c : clusters) {
                    Fragment y = c.getRepresentant();
                    tr.set(x.getPoints3d(), y.getPoints3d());
                    double rmsd = tr.getRmsd();
                    if (rmsd < 6) {
                        c.add(x);
                        added = true;
                        /*if (!result.contains(c)) {
                            missing++;
                            //System.out.println(y + "");
                        } else {
                            shared++;
                        }*/
                    }
                }
                if (!added) {
                    Cluster c = new Cluster();
                    c.add(x);
                    clusters.add(c);
                    g.add(c);
                }
            }
            double efficency = (double) box / total;
            
            //System.out.println("missing " + missing + " / " + shared + " " + box + " " + total + " " + efficency);
            System.out.println("clusters: " + clusters.size() + " / " + count);

        }
        int index = 0;
        for (Cluster c : clusters) {
            System.out.println(c.size());
            PymolVisualizer.save(c.getFragments(100), dir.getCluster(index++));
        }
    }

    public static void main(String[] args) {
        FragmentClustering fc = new FragmentClustering();
        fc.create();
    }

}
