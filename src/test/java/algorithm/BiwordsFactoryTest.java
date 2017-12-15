package algorithm;

import geometry.Point;
import global.TestVariables;
import global.io.Directories;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import junit.framework.TestCase;
import pdb.Residue;
import pdb.SimpleStructure;
import pdb.StructureFactory;
import pdb.StructureSource;
import pdb.cath.Cath;

/**
 *
 * @author Antonin Pavelka
 */
public class BiwordsFactoryTest extends TestCase {

	//private Parameters parameters = Parameters.create();
	TestVariables vars = new TestVariables();
	Directories dirs = vars.getDirectoris();

	public BiwordsFactoryTest(String testName) {
		super(testName);
	}

	public void testBiwordsAreNotMissing() throws IOException {
		dirs.createJob();
		dirs.createTask("test");
		Cath cath = new Cath(dirs);
		StructureFactory structureFactory = new StructureFactory(dirs, cath);
		StructureSource structureSource = new StructureSource("1ZNI");
		SimpleStructure structure = structureFactory.getStructure(0, structureSource);
		BiwordsFactory biwordsFactory = new BiwordsFactory(vars.getParameters(), vars.getDirectoris(), structure, 1, true);
		BiwordedStructure biwords = biwordsFactory.getBiwords();

		createPairs(structure);
		createPairs(biwords);
	}

	private boolean areAllResiduesInContact(Residue residueX, Residue residueY) {
		for (double[] atomX : residueX.getAtoms()) {
			for (double[] atomY : residueY.getAtoms()) {
				Point x = new Point(atomX);
				Point y = new Point(atomY);
				return x.distance(y) <= 6;
			}
		}
		return false;
	}

	private void createPairs(SimpleStructure structure) {
		Collection<Residue> residues = structure.getResidues().values();
		List<ResiduePair> pairs = new ArrayList<>();
		for (Residue residueX : residues) {
			for (Residue residueY : residues) {
				//if (areResiduesInContact(residueX, residueY)) {
				//	pairs.add(new ResiduePair(residueX, residueY));
				//}
			}
		}
	}

	private void createPairs(BiwordedStructure biwords) {

	}

	//private Biwords createBiwords() {
	//}
	public void testSimilarBiwordsExist() {

	}

}

class ResiduePair {

	public ResiduePair(Residue residueX, Residue residueY) {

	}
}
