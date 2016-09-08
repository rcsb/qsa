package fragments;

import java.io.IOException;
import java.util.Properties;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureIO;
import geometry.Point;
import io.Directories;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.io.PDBFileReader;
import pdb.PdbLine;

/**
 * Main class of the utility for coloring of viral capsids.
 */
public class StructureLoader {

    Directories dirs_;
    Parameters params_ = new Parameters();

    public StructureLoader(Directories dir) {
        dirs_ = dir;
    }

    public Structure getStructure(String pdbCode) {
        dirs_.setPdbCode(pdbCode);
        Properties props = System.getProperties();
        props.setProperty("PDB_CACHE_DIR", dirs_.getPdb().getPath());
        props.setProperty("PDB_DIR", dirs_.getPdb().getPath());
        props.setProperty("DPDB_DIR", dirs_.getTemp().getPath());
        System.setProperty("java.io.tmpdir", dirs_.getTemp().getPath());
        try {
            Structure structure = StructureIO.getStructure(pdbCode);//.getBiologicalAssembly(pdbCode);
            return structure;
        } catch (StructureException ex) {
            FlexibleLogger.error("cif structure creation failure", ex);
        } catch (IOException ex) {
            FlexibleLogger.error("cif parsing IO failure", ex);
        }
        return null;
    }

    public Structure getStructure(File pdbFile) {
        PDBFileReader pdbreader = new PDBFileReader();
        try {
            Structure struc = pdbreader.getStructure(pdbFile);
            return struc;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param structure
     * @return all chains from all models (because symetries are stored in
     * models). Also creates a unique internal id for each chain and sets chain
     * ID to model:chain_id.
     */
    public List<Chain> getAllChains(Structure structure) {
        List<Chain> allChains = new ArrayList<>();
        int max = 1000000;
        int id = 0;
        try { // is there a better way, st. like structure.getModelCount()?
            for (int model = 0; model < max; model++) {
                List<Chain> chains = structure.getModel(model);
                for (Chain c : chains) {
                    String sId = Integer.toString(id, 36).toUpperCase();
                    c.setInternalChainID(sId);
                    c.setChainID(model + ":" + c.getChainID());
                    id++;
                }
                allChains.addAll(chains);
            }
        } catch (Exception ex) {
        }
        return allChains;
    }

    /**
     * Creates PDB file showing shape of capsid for fast visualization in PyMOL.
     */
    private void generateCompactPdb(List<Chain> chains, int compactness,
            File file) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            int serial = 1;
            int resi = 1;
            for (Chain c : chains) {
                int index = 1;
                for (Group g : c.getAtomGroups()) {
                    if (index++ % compactness != 0) {
                        continue;
                    }
                    Point p = null;
                    for (Atom a : g.getAtoms()) {
                        if (p == null
                                || a.getName().toUpperCase().equals("CA")) {
                            p = new Point(a);
                        }
                    }
                    if (p != null) {
                        Point center = p;
                        PdbLine pl = new PdbLine(serial, "X", "U",
                                c.getInternalChainID() + "", "" + resi, 'A',
                                center.getX(), center.getY(), center.getZ());
                        serial++;
                        bw.write(pl + "\n");
                    }
                }
                resi++;
            }
        } catch (IOException ex) {
            FlexibleLogger.error("generateCompactPdb() failure", ex);
        }
    }

}
