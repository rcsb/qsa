package cath;

import testing.TestResources;
import java.util.HashSet;
import java.util.Set;
import junit.framework.TestCase;

/**
 *
 * @author Antonin Pavelka
 */
public class CathTest extends TestCase {

	TestResources testVariables = new TestResources();

	public CathTest(String testName) {
		super(testName);
	}

	public void testGetHomologousSuperfamilies() {

		Cath cath = testVariables.getCath();

		Level level = cath.getHomologousSuperfamilies();
		assert level.size() >= 6873;

		Group group = level.getGroup(new Classification("3.40.50.1820"));
		assert group.size() >= 3274;

		Set<String> pdbCodes = new HashSet<>();
		for (Domain domain : group.getMemberDoamains()) {
			pdbCodes.add(domain.getPdbCode());
		}
		assert pdbCodes.contains("1iz7");
		assert pdbCodes.contains("1cv2");
		assert !pdbCodes.contains("1ajv");

		int representantSize = group.getRepresentant().size();
		assert 200 < representantSize : representantSize;
		assert representantSize < 400 : representantSize;
	}

	public void testGetDomain() {
	}

}
