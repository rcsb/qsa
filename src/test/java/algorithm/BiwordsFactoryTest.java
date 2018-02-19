package algorithm;

import testing.TestResources;
import global.io.Directories;
import junit.framework.TestCase;
import structure.Residue;
import structure.SimpleStructure;
import structure.StructureFactory;
import structure.StructureSource;
import cath.Cath;

/**
 *
 * @author Antonin Pavelka
 */
public class BiwordsFactoryTest extends TestCase {

	//private Parameters parameters = Parameters.create();
	TestResources vars = new TestResources();
	Directories dirs = vars.getDirectoris();

	public BiwordsFactoryTest(String testName) {
		super(testName);
	}

	public void testBiwordsAreNotMissing() throws Exception {
		dirs.createJob();
		dirs.createTask("test");
		Cath cath = new Cath(dirs);
		StructureFactory structureFactory = new StructureFactory(dirs, cath);
		//StructureSource structureSource = new StructureSource("1ZNI");
		StructureSource structureSource = new StructureSource("2z2mD02");
		SimpleStructure structure = structureFactory.getStructure(0, structureSource);
		BiwordsFactory biwordsFactory = new BiwordsFactory(vars.getParameters(), vars.getDirectoris(), structure, 1, true);
		BiwordedStructure biwords = biwordsFactory.getBiwords();

		check(biwords);
	}

	private void check(BiwordedStructure biwordedStructure) {
		SimpleStructure structure = biwordedStructure.getStructure();
		Biword[] biwords = biwordedStructure.getBiwords();
		System.out.println(structure.size() + " " + biwords.length);
		
	}
	

}

class ResiduePair {

	public ResiduePair(Residue residueX, Residue residueY) {

	}
}
