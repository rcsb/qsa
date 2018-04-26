package probability.sampling;

import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author kepler
 */
public class ReservoirSampleTest extends TestCase {

	public ReservoirSampleTest(String testName) {
		super(testName);
	}

	public void testGetArray() {
		ReservoirSample<Integer> sample = new ReservoirSample<>(10, 1);
		for (int i = 0; i < 100; i++) {
			sample.add(i);
		}
		List<Integer> a = sample.getList();
	}

}
