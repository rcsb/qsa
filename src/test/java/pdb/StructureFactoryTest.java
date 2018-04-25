package pdb;

import structure.ResidueId;
import structure.StructureSource;
import structure.ChainId;
import structure.SimpleStructure;
import testing.TestResources;
import junit.framework.TestCase;
import structure.Residue;
import structure.SimpleChain;

/**
 *
 * @author Antonin Pavelka
 *
 */
public class StructureFactoryTest extends TestCase {

	private TestResources resources = TestResources.getInstance();

	public StructureFactoryTest(String testName) {
		super(testName);

	}

	public void testGetStructure() throws Exception {
		pdbCode();
		cathDomain();
		//nonZeroSize(); // for occasional debugging
	}

	public void pdbCode() throws Exception {
		SimpleStructure structure = resources.create(new StructureSource("1iz7"));
		assertEquals(structure.size(), 294);
		String residueName = structure.getResidue(ResidueId.createWithoutInsertion(new ChainId('A'), 50)).getName();
		assertEquals(residueName, "PRO");
	}

	public void cathDomain() throws Exception {
		SimpleStructure structure = resources.create(new StructureSource("4prdA01"));
		assertEquals(structure.size(), 181);
		String residueName = structure.getResidue(ResidueId.createWithoutInsertion(new ChainId('A'), 1)).getName();
		assertEquals(residueName, "GLY");
	}

	public void nonZeroSize() throws Exception {
		SimpleStructure structure;
		structure = resources.create(new StructureSource("2kdc"));
		assert structure.size() > 0;
		structure = resources.create(new StructureSource("2kdcB00"));
		assert structure.size() > 0;
		structure = resources.create(new StructureSource("3zxuB01"));
		assert structure.size() > 0;
		structure = resources.create(new StructureSource("1k4sA01"));
		assert structure.size() > 0;
	}

	public void disabledTtestStructureWihBiwordFailure() throws Exception {
			SimpleStructure structure = resources.create(new StructureSource("2z2mD02"));
			System.out.println(structure.size());
			System.out.println(structure.getChains().size());
			for(SimpleChain chain : structure.getChains()) {
				System.out.println(chain.getId() + " " + chain.size());
				for (Residue r : chain.getResidues() ) {
					System.out.println(r.getId());
				}
				System.out.println("---");
			}
	}

}
