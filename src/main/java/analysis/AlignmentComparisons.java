package analysis;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.SVDSuperimposer;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.align.model.AFPChain;
import org.biojava.nbio.structure.align.util.AlignmentTools;

public class AlignmentComparisons {
	public static void run(AFPChain afpChain, Atom[] ca1, Atom[] ca2) {
		try {
			SVDSuperimposer si = AlignmentTools.getSuperimposer(afpChain, ca1, ca2, new Atom[ca1.length],
					new Atom[ca2.length]);
			si.getRotation();
			si.getTranslation();
		} catch (StructureException e) {
			throw new RuntimeException(e);
		}
	}
}
