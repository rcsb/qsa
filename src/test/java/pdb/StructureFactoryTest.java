package pdb;

import structure.ResidueId;
import structure.StructureSource;
import structure.StructureFactory;
import structure.ChainId;
import structure.SimpleStructure;
import global.TestVariables;
import java.io.IOException;
import junit.framework.TestCase;

/**
 *
 * @author Antonin Pavelka
 *
 */
public class StructureFactoryTest extends TestCase {

	private TestVariables testVariables = new TestVariables();

	public StructureFactoryTest(String testName) {
		super(testName);

	}

	public void testGetStructure() throws Exception {
		pdbCode();
		cathDomain();
		//nonZeroSize(); // for occasional debugging
	}

	public void pdbCode() throws Exception {
		SimpleStructure structure = create(new StructureSource("1iz7"));
		assertEquals(structure.size(), 294);
		String residueName = structure.getResidue(ResidueId.createWithoutInsertion(new ChainId('A'), 50)).getName();
		assertEquals(residueName, "PRO");
	}

	public void cathDomain() throws Exception {
		SimpleStructure structure = create(new StructureSource("4prdA01"));
		assertEquals(structure.size(), 181);
		String residueName = structure.getResidue(ResidueId.createWithoutInsertion(new ChainId('A'), 1)).getName();
		assertEquals(residueName, "GLY");
	}

	public void nonZeroSize() throws Exception {
		SimpleStructure structure;
		structure = create(new StructureSource("2kdc"));
		assert structure.size() > 0;
		structure = create(new StructureSource("2kdcB00"));
		assert structure.size() > 0;
		structure = create(new StructureSource("3zxuB01"));
		assert structure.size() > 0;
		structure = create(new StructureSource("1k4sA01"));
		assert structure.size() > 0;
	}

	private SimpleStructure create(StructureSource source) throws Exception {
		StructureFactory structureFactory = new StructureFactory(testVariables.getDirectoris(), testVariables.getCath());
		SimpleStructure structure = structureFactory.getStructure(0, source);
		return structure;
	}

}
