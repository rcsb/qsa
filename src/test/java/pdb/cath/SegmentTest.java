package pdb.cath;

import junit.framework.TestCase;

/**
 *
 * @author Antonin Pavelka
 */
public class SegmentTest extends TestCase {

	public SegmentTest(String testName) {
		super(testName);
	}

	public void testContains() {
		segment();
		segmentWithInsertion();
	}

	public void segment() {
		Character insertion = null;
		Segment segment = new Segment("A", 2, insertion, "A", 9, insertion);
		assert segment.contains("A", 2, insertion);
		assert segment.contains("A", 3, insertion);
		assert segment.contains("A", 9, insertion);
		assert !segment.contains("A", 1, insertion);
		assert !segment.contains("A", -1, insertion);
		assert !segment.contains("A", 10, insertion);
		assert !segment.contains("A", 9, 'A');
		assert segment.contains("A", 2, 'A');
	}

	public void segmentWithInsertion() {
		Character insertion = null;
		Segment segment = new Segment("A", 2, 'A', "A", 9, insertion);
		assert !segment.contains("A", 2, insertion);

	}
}
