package pdb.cath.tree;

import cath.Classification;
import junit.framework.TestCase;

/**
 *
 * @author Antonin Pavelka
 */
public class ClassificationTest extends TestCase {

	public ClassificationTest(String testName) {
		super(testName);
	}

	public void testCreateParrent() {
		Classification a = new Classification("1.2.3.4");
		Classification b = new Classification("1.2.3");
		Classification c = a.createParrent();
		assert b.equals(c);
	}

	public void createTestDepth() {
		Classification a = new Classification("154.52.6563.44");
		assert a.getDepth() == 4;
		assert a.createParrent().getDepth() == 3;
	}

}
