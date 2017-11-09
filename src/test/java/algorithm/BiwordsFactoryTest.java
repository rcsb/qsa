package algorithm;

import geometry.Point;
import global.Parameters;
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

/**
 *
 * @author Antonin Pavelka
 */
public class BiwordsFactoryTest extends TestCase {

	private Parameters parameters = Parameters.create();

	public BiwordsFactoryTest(String testName) {
		super(testName);
	}

	public void testBiwordsAreNotMissing() throws IOException {

		// TODO test this also for first method?
		Directories dirs = Directories.createDefault();
		dirs.createJob();
		dirs.createTask("test");
		StructureFactory structureFactory = new StructureFactory(dirs);
		StructureSource structureSource = new StructureSource("1ZNI");
		SimpleStructure structure = structureFactory.getStructure(0, structureSource);
		BiwordsFactory biwordsFactory = new BiwordsFactory(dirs, structure, 1, true);
		Biwords biwords = biwordsFactory.create();

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

	private void createPairs(Biwords biwords) {

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
