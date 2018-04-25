package biword;

import fragment.biword.BiwordId;
import java.util.Random;
import junit.framework.TestCase;

/**
 *
 * @author Antonin Pavelka
 */
public class BiwordIdTest extends TestCase {

	public BiwordIdTest(String testName) {
		super(testName);
	}

	public void testDecode() {
		Random random = new Random(1);
		for (int i = 0; i < 10; i++) {
			int structureId = random.nextInt(Integer.MAX_VALUE);
			int idWithinStructure = random.nextInt(Integer.MAX_VALUE);
			BiwordId a = new BiwordId(structureId, idWithinStructure);
			long value = a.endcode();
			BiwordId b = BiwordId.decode(value);
			assert a.equals(b);
		}
	}

}
