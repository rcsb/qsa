package pdb.cath;

import pdb.ResidueId;

/**
 *
 * @author A continuous sequence, part of of a domain.
 */
public class Segment {

	private final ResidueId start;
	private final ResidueId end;

	public Segment(ResidueId start, ResidueId end) {
		this.start = start;
		this.end = end;
	}

	public boolean contains(ResidueId other) {
		return smallerOrEqual(start, other) && smallerOrEqual(other, end);
	}

	private boolean smallerOrEqual(ResidueId a, ResidueId b) {
		return a.compareTo(b) <= 0;
	}

	/**
	 * @return Approximate domain size.
	 */
	public int size() {
		return end.getSequenceNumber() - start.getSequenceNumber() + 1;
	}

}
