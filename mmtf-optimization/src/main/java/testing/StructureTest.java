package testing;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Structure;
import structure.AtomLite;
import structure.ChainId;
import structure.ChainLite;
import structure.ModelLite;
import structure.ResidueLite;
import structure.StructureLite;

/**
 *
 * @author Antonin Pavelka
 *
 */
public class StructureTest {

    private final String pdbCode;

    public StructureTest(String pdbCode) {
        this.pdbCode = pdbCode;
    }

    private void check(boolean b) {
        if (!b) {
            throw new RuntimeException(pdbCode);
        }
    }

    private void check(boolean b, String s) {
        if (!b) {
            System.err.println(s);
            throw new RuntimeException(pdbCode + ": " + s);
        }
    }

    private void ae(Object a, Object b) {
        assert a == b : pdbCode + ": " + a + " != " + b;
    }

    private void compareNumbers(double a, double b) {
        assert Double.compare(a, b) == 0 : pdbCode + ": " + a + " != " + b;
    }

    public void compareStructures(Structure sb, StructureLite sl) {        
        //System.out.println(sl.getId());
        check(sb.nrModels() == sl.numberOfModels());
        for (int i = 0; i < sb.nrModels(); i++) {
            List<Chain> chains = sb.getModel(i);
            ModelLite ml = sl.getModels().get(i);
            //for (ChainLite cl : ml.getChains()) {
            //    System.out.print(cl.getId() + " ");
            //}
            //System.out.println();
            check(ml.numberOfChains() == chains.size(),
                    ml.numberOfChains() + " " + chains.size());
            for (Chain c : chains) {
                //System.out.println(" " + c.getId());
                //System.out.println("" + c);
                String cid = c.getId();
                ChainLite cl = ml.getChain(new ChainId(cid));

                //for (ResidueLite r : cl.getResidues()) {
                //    System.out.println(r.getId());
                //}
                compareChains(c, cl);
            }
            //System.out.println(i + " " + chains.size());
        }
    }

    private void compareChains(Chain c, ChainLite cl) {
        List<Group> groups = c.getAtomGroups();
        Collection<ResidueLite> residues = cl.getResidues();
        check(groups.size() == residues.size(), groups.size() + " = "
                + c.getId() + " = " + residues.size());
        Iterator<ResidueLite> ri = residues.iterator();
        for (Group g : groups) {
            ResidueLite r = ri.next();
            assert r != null;
            //System.out.println(" " + r.toString() + " --- " + g.toString());
            compareNumbers(g.getResidueNumber().getSeqNum(), r.getId().getSequenceNumber());
            if (g.getResidueNumber().getInsCode() == null) {
                assert r.getId().getInsertionCode() == null
                        || r.getId().getInsertionCode() == '\0' :
                        "*" + r.getId().getInsertionCode() + "*";
            } else {
                ae(Character.toUpperCase(g.getResidueNumber().getInsCode()),
                        r.getId().getInsertionCode());
            }
            assert g.getPDBName().equalsIgnoreCase(r.getId().getName());
            
            if (!g.hasAltLoc()) { // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                Iterator<AtomLite> ai = r.getAtoms().iterator();
                assert r.getAtoms().size() == g.getAtoms().size();
                for (Atom a : g.getAtoms()) {
                    AtomLite al = ai.next();
                    compareAtoms(a, al);
                }
            }
        }
    }

    private void compareAtoms(Atom a, AtomLite al) {
        compareNumbers(a.getPDBserial(), al.getSerialNumber());
        compareNumbers(a.getX(), al.getX());
        if (a.getY() != al.getY()) {
            System.out.println(a.getY());
            System.out.println(al.getY());
            System.out.println("-----------");
        }
        compareNumbers(a.getY(), al.getY());
        compareNumbers(a.getZ(), al.getZ());
        assert a.getName().equalsIgnoreCase(al.getName());
    }

}
