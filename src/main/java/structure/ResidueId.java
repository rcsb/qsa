package structure;

import java.io.Serializable;

/*
 * Uniquelly identifies residue within a (quaternary) structure. 
 */
public class ResidueId implements Comparable<ResidueId>, Serializable {

	private ChainId chain;
	private int number; // residue sequence number
	private Character insertion; // insertion code, e.g. 2 2A 2B 3

	public ResidueId() {

	}

	public ResidueId(ChainId chain, int number, Character insertionCode) {
		this.chain = chain;
		this.number = number;
		if (insertionCode != null) {
			this.insertion = Character.toUpperCase(insertionCode);
		}
	}

	public static ResidueId createWithoutInsertion(ChainId chain, int number) {
		return new ResidueId(chain, number, null);
	}

	public static ResidueId createFromString(ChainId chain, String numberWithInsertion) {
		int number;
		Character insertion;
		char last = numberWithInsertion.charAt(numberWithInsertion.length() - 1);
		if (Character.isDigit(last)) {
			number = Integer.parseInt(numberWithInsertion);
			insertion = null;
		} else {
			int length = numberWithInsertion.length();
			String numberPart = numberWithInsertion.substring(0, length - 1);
			number = Integer.parseInt(numberPart);
			insertion = numberWithInsertion.charAt(length - 1);
		}
		return new ResidueId(chain, number, insertion);
	}

	public ChainId getChain() {
		return chain;
	}

	public void setChain(ChainId c) {
		this.chain = c;
	}

	public int getSequenceNumber() {
		return number;
	}

	/*
	 * See PDB file format guide, ATOM
	 * http://www.bmsc.washington.edu/CrystaLinks/man/pdb/part_62.html
	 */
	public Character getInsertionCode() {
		return insertion;
	}

	/**
	 * Guesses if nextResidue might be the next residue in the sequence after this. Insertion codes can cause some false
	 * positives.
	 *
	 * @param nextResidue
	 * @return true if next residue might follow this residue considering their ids only
	 */
	public boolean isFollowedBy(ResidueId nextResidue) {
		if (!this.chain.equals(nextResidue.chain)) {
			return false;
		} else if (this.number == nextResidue.number) {
			return isFollowedWithEqualIndexes(nextResidue);
		} else if (this.number + 1 == nextResidue.number) {
			return isFollowedWhenNumbersFolow(nextResidue);
		} else {
			return false;
		}
	}

	private boolean isFollowedWithEqualIndexes(ResidueId nextResidue) {
		if (this.insertion == null && nextResidue.insertion == null) {
			return false; // 1 1
		} else if (this.insertion == null) { // (null) (not null)
			return Character.toUpperCase(nextResidue.insertion) == 'A'; // 1 1A
		} else if (nextResidue.insertion == null) { // (not null) (null)
			return false; // 1A 1
		} else { // both insertions exist
			return this.insertion + 1 == nextResidue.insertion; // 1B 1C
		}
	}

	private boolean isFollowedWhenNumbersFolow(ResidueId nextResidue) {
		if (nextResidue.insertion == null) {
			return true; // 1 2 | 1B 2
		} else {
			return false; // 1A 2A - missing 2: 1A 2 2A
		}
	}

	@Override
	public String toString() {
		return chain + ":" + number + "" + (insertion == null ? "" : insertion);
	}

	public String getPdbString() {
		return number + "" + (insertion == null ? "" : insertion);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ResidueId)) {
			return false;
		}
		ResidueId other = (ResidueId) o;

		return this.chain.equals(other.chain)
			&& this.number == other.number
			&& areInsertionsEqual(this.insertion, other.insertion);
	}

	private boolean areInsertionsEqual(Character a, Character b) {
		if (a == null) {
			return b == null;
		} else {
			return a.equals(b);
		}
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 37 * hash + this.chain.hashCode();
		hash = 37 * hash + this.number;
		hash = 37 * hash + (this.insertion != null ? this.insertion.hashCode() : 0);
		return hash;
	}

	@Override
	public int compareTo(ResidueId other) {
		int c = this.chain.compareTo(other.chain);
		if (c == 0) {
			c = Integer.compare(this.number, other.number);
			if (c == 0) {
				if (this.insertion != null && other.insertion != null) {
					c = this.insertion.compareTo(other.insertion);
				} else if (this.insertion == null && other.insertion != null) {
					c = -1;
				} else if (this.insertion != null && other.insertion == null) {
					c = 1;
				} // else both are null, c == 0 remains
			}
		}
		return c;
	}
}
