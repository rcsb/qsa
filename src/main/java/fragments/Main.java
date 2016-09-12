package fragments;

import alignment.FragmentsAlignment;
import io.Directories;
import io.FileOperations;
import io.Printer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.biojava.nbio.structure.AminoAcid;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.StandardAminoAcid;
import org.biojava.nbio.structure.Structure;
import pdb.ChainId;
import pdb.PdbChain;
import pdb.Residue;
import pdb.SimpleStructure;
import spark.Dataset;
import spark.ReferenceMetrics;
import util.Randomness;

/**
 *
 * @author Antonin Pavelka
 */
public class Main {

    private File home_;
    private Parameters params_;
    private Directories dirs_;
    private FragmentsFactory ff_;
    private Dataset benchmark_;

    public Main(File home, Parameters params) {
        home_ = home;
        dirs_ = new Directories(home_);
        params_ = params;
        ff_ = new FragmentsFactory(params_);
        benchmark_ = dirs_.getEasyBenchmark();
    }
    
     private String[] readPdbCodes() {
        List<String> codes = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(dirs_.getPdbCodes()));
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    codes.add(line.substring(0, 4));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        String[] a = new String[codes.size()];
        codes.toArray(a);
        return a;
    }

    private List<String> readPairs() {
        List<String> result = new ArrayList<>();
        Randomness<String> r = new Randomness<>();
        File dir = dirs_.getPairDirs();
        for (File f : dir.listFiles()) {
            List<String> list = FileOperations.load(f);
            List<String> sample = r.subsample(list, 50);
            result.addAll(sample);
        }
        return result;

    }

    private SimpleStructure loadStructure(String pdbCode) {
        return loadStructure(pdbCode, null);
    }

    private SimpleStructure loadStructure(String pdbCode, String cidFilterS) {
        ChainId cidFilter = new ChainId(cidFilterS);
        StructureLoader sl = new StructureLoader(dirs_);
        Structure structure = sl.getStructure(pdbCode);
        List<Chain> chains = sl.getAllChains(structure);
        PdbChain pc;
        if (cidFilterS == null) {
            pc = new PdbChain(pdbCode);
        } else {
            pc = new PdbChain(pdbCode + "." + cidFilterS);
        }
        SimpleStructure ss = new SimpleStructure(pc);
        int index = 1;
        for (Chain c : chains) {
            /*System.out.println(" " + c.getInternalChainID());
            System.out.println(" " + c.getId());
            System.out.println(" " + c.getChainID());*/
            ChainId cid = new ChainId(c.getChainID());
            if (!cid.equals(cidFilter)) {
                continue;
            }
            for (Group g : c.getAtomGroups()) {
                AminoAcid aa = StandardAminoAcid.getAminoAcid(g.getPDBName());
                if (aa != null) {
                    Residue r = new Residue(index++, g);
                    ss.add(cid, r);
                }
            }
        }
        return ss;
    }

    public void run(int max) {
        List<String> pairs = readPairs();
        List<FragmentsAlignment> results = new ArrayList<>();
        for (String pair : pairs) {
            try {
                StringTokenizer st = new StringTokenizer(pair, ",");
                PdbChain a = new PdbChain(st.nextToken());
                PdbChain b = new PdbChain(st.nextToken());
                System.out.println("Processing " + a + " " + b);
                FragmentsAlignment result = align(a, b);
                results.add(result);
                Printer.println("RESULT " + result.getLine());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        for (FragmentsAlignment r : results) {
            ReferenceMetrics rm = benchmark_.getReferenceMetrics(r.getA(), r.getB());
            assert rm != null;
            String s = r.getLine() + "," + rm.getLine();
            Printer.println(s);
        }
    }

    public FragmentsAlignment align(PdbChain a, PdbChain b) {
        SimpleStructure ssa = loadStructure(a.getPdb(), a.getChain().toString());
        SimpleStructure ssb = loadStructure(b.getPdb(), b.getChain().toString());
        Fragments fa = ff_.create(ssa, 1);
        Fragments fb = ff_.create(ssb, 1); // !!! SPARTSITY
        FragmentsAligner al = new FragmentsAligner(params_, dirs_);
        FragmentsAlignment result = al.align(fa, fb);
        return result;
    }

    public static void main(String[] args) {
        File home = new File(args[0]);
        Parameters params = new Parameters();
        Main f = new Main(home, params);
        f.run(1);
    }
}
