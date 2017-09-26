package pdb;

import java.io.Serializable;

public class ResidueId implements Comparable<ResidueId>, Serializable {

	private static final long serialVersionUID = 1L;
	private ChainId chain_;
	private int number_;
	private Character insertion_;
	private static char EMPTY = ' ';

	public ResidueId() {

	}

	/**
	 * Serial number is a string (maybe hexa number) in Gromacs outputs. This is againstPDB file format guide, but we
	 * need to read PDB files produced by Gromacs.
	 *
	 * @param chain chain ID
	 * @param number serial number
	 * @param insertionCode insertion code
	 */
	public ResidueId(ChainId chain, int number, char insertionCode) {
		chain_ = chain;
		number_ = number;
		insertion_ = Character.toUpperCase(insertionCode);
	}

	public boolean follows(ResidueId next) {
		if (number_ == next.number_) {
			return insertion_ + 1 == next.insertion_;
		} else if (number_ + 1 == next.number_) {
			return true;
		} else {
			return false;
		}
	}

	public ResidueId(ChainId chain, int number) {
		chain_ = chain;
		number_ = number;
		insertion_ = EMPTY;
	}

	public ChainId getChain() {
		return chain_;
	}

	public void setChain(ChainId c) {
		chain_ = c;
	}

	public int getSequenceNumber() {
		return number_;
	}

	/*
	 * See PDB file format guide, ATOM
	 * http://www.bmsc.washington.edu/CrystaLinks/man/pdb/part_62.html
	 */
	public char getInsertionCode() {
		return insertion_;
	}

	@Override
	public String toString() {
		return chain_ + ":" + number_ + "" + (insertion_ == EMPTY ? "" : insertion_);
	}

	public String getPdbString() {
		return number_ + "" + (insertion_ == EMPTY ? "" : insertion_);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ResidueId)) {
			return false;
		}
		ResidueId other = (ResidueId) o;
		return chain_.equals(other.chain_) && number_ == other.number_ && insertion_ == other.insertion_;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 37 * hash + this.chain_.hashCode();
		hash = 37 * hash + this.number_;
		hash = 37 * hash + (this.insertion_ != null ? this.insertion_.hashCode() : 0);
		return hash;
	}

	@Override
	public int compareTo(ResidueId other) {
		int c = chain_.compareTo(other.chain_);
		if (0 == c) {
			c = Integer.compare(number_, other.number_);
			if (0 == c) {
				c = insertion_.compareTo(other.insertion_);
			}
		}
		return c;
	}
}
