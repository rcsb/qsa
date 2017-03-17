package org.rcsb.mmtf.benchmark;

import java.util.Objects;

/**
 *
 * @author Antonin Pavelka
 */
public class PdbEntry implements Comparable<PdbEntry> {

	private final String code;
	private final int numAtoms;

	public PdbEntry(String code, int numAtoms) {
		this.code = code.toLowerCase();
		this.numAtoms = numAtoms;
	}

	@Override
	public int compareTo(PdbEntry other) {
		int c = Integer.compare(this.numAtoms, other.numAtoms);
		if (c == 0) {
			c = this.code.compareTo(other.code);
		}
		return c;
	}

	public String getCode() {
		return code;
	}

	public int getNumAtoms() {
		return numAtoms;
	}

	@Override
	public boolean equals(Object o) {
		PdbEntry other = (PdbEntry) o;
		return this.code.equals(other.code);
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 23 * hash + Objects.hashCode(this.code);
		return hash;
	}

	@Override
	public String toString() {
		return code + "(" + numAtoms + ")";
	}
}
