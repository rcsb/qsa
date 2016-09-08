package alignment;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureTools;
import org.biojava.nbio.structure.align.StructureAlignment;
import org.biojava.nbio.structure.align.StructureAlignmentFactory;
import org.biojava.nbio.structure.align.fatcat.FatCatRigid;
import org.biojava.nbio.structure.align.fatcat.calc.FatCatParameters;
import org.biojava.nbio.structure.align.model.AFPChain;

/**
 *
 * @author Antonin Pavelka
 */
public class ReferenceAlignment {

    public static double align(Structure structure1, Structure structure2) {
        try {
            // To run FATCAT in the flexible variant say  
            // FatCatFlexible.algorithmName below  
            StructureAlignment algorithm = StructureAlignmentFactory.getAlgorithm(FatCatRigid.algorithmName);
            Atom[] ca1 = StructureTools.getAtomCAArray(structure1);
            Atom[] ca2 = StructureTools.getAtomCAArray(structure2);

            // get default parameters  
            FatCatParameters params = new FatCatParameters();

            AFPChain afpChain = algorithm.align(ca1, ca2, params);
            return afpChain.getAlignScore();
            
            /*afpChain.setName1("name1");  

            afpChain.setName2("name2");

          // show original FATCAT output:  

            System.out.println(afpChain.toFatcat(ca1, ca2));  
            
          // show a nice summary print  

            //System.out.println(AfpChainWriter.toWebSiteDisplay(afpChain, ca1, ca2));  
            
          // print rotation matrices  

            System.out.println(afpChain.toRotMat());  
          //System.out.println(afpChain.toCE(ca1, ca2));  

            return afpChain.getTMScore();
*/
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
