package pdb.cath;

import junit.framework.TestCase;
import pdb.ChainId;
import pdb.ResidueId;

/**
 *
 * @author Antonin Pavelka
 */
public class SegmentTest extends TestCase {

	ChainId chainA = new ChainId('A');

	public SegmentTest(String testName) {
		super(testName);
	}

	public void testContains() {
		segment();
		segmentWithInsertion();
	}

	public ResidueId r(int index) {
		return ResidueId.createWithoutInsertion(chainA, index);
	}

	public ResidueId ri(int index) {
		return new ResidueId(chainA, index, 'B');
	}

	public void segment() {
		Segment segment = new Segment(r(2), r(9));
		assert segment.contains(r(2));
		assert segment.contains(r(3));
		assert segment.contains(r(9));
		assert !segment.contains(r(1));
		assert !segment.contains(r(-1));
		assert !segment.contains(r(10));
		assert segment.contains(r(9));
		assert segment.contains(r(2));
	}

	public void segmentWithInsertion() {
		Segment segment = new Segment(ri(2), r(9));
		assert !segment.contains(r(2));
	}
	
}
