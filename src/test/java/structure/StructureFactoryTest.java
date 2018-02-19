package structure;

import testing.TestResources;
import junit.framework.TestCase;

/**
 *
 * @author Antonin Pavelka
 */
public class StructureFactoryTest extends TestCase {

	TestResources resources = new TestResources();
	StructureFactory factory = new StructureFactory(
		resources.getDirectoris(), resources.getCath());

	public StructureFactoryTest(String testName) {
		super(testName);
	}

	public void testGetStructure() throws Exception {
		//factory.getStructure(0, new StructureSource("1aa5"));
	}

	public void testCreateBiojavaStructure() throws Exception {
	}

	public void testCreatePdbFile() throws Exception {
	}

	public void testDownloadPdbFile() throws Exception {
	}

}
