package pdb.cath;

/**
 *
 * @author A continuous sequence, part of of a domain.
 */
public class Segment {

	private final String startChain;
	private final int startNumber;
	private final Character startInsertion;
	private final String endChain;
	private final int endNumber;
	private final Character endInsertion;

	public Segment(String startChain, int startNumber, Character startInsertion,
		String endChain, int endNumber, Character endInsertion) {
		this.startChain = startChain;
		this.startNumber = startNumber;
		if (startInsertion != null) {
			this.startInsertion = Character.toUpperCase(startInsertion);
		} else {
			this.startInsertion = null;
		}
		this.endChain = endChain;
		this.endNumber = endNumber;
		if (endInsertion != null) {
			this.endInsertion = Character.toUpperCase(endInsertion);
		} else {
			this.endInsertion = null;
		}
		assert startChain.equals(endChain);
	}

	public boolean contains(String chain, int number, Character insertion) {
		if (insertion != null) {
			insertion = Character.toUpperCase(insertion);
		}
		if (!chain.equals(startChain)) {
			return false;
		}
		if (startNumber < number && number < endNumber) {
			return true;
		} else if (startNumber == number) {
			if (startInsertion == null && insertion == null) {
				return true;
			} else if (startInsertion == null) {
				return true;
			} else if (insertion == null) {
				return false;
			} else {
				return startInsertion < insertion;
			}
		} else if (endNumber == number) {
			if (endInsertion == null && insertion == null) {
				return true;
			} else if (endInsertion == null) {
				return false;
			} else if (insertion == null) {
				return true;
			} else {
				return insertion < endInsertion;
			}
		} else {
			return false;
		}
	}
}
