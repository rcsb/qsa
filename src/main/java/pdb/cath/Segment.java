package pdb.cath;

/**
 *
 * @author A continuous sequence, part of of a domain.
 */
public class Segment {

	private final String startChain;
	private final int startNumber;
	private final String startInsertion;
	private final String endChain;
	private final int endNumber;
	private final String endInsertion;

	public Segment(String startChain, int startNumber, String startInsertion,
		String endChain, int endNumber, String endInsertion) {
		this.startChain = startChain;
		this.startNumber = startNumber;
		this.startInsertion = startInsertion;
		this.endChain = endChain;
		this.endNumber = endNumber;
		this.endInsertion = endInsertion;
		assert startChain.equals(endChain);
	}
	
	
}
