package pdb;

import java.io.Serializable;

/**
 *
 * @author Antonin Pavelka
 *
 */
public class ChainId implements Comparable<ChainId>, Serializable {

	private String id; // is filled with name if id is not available
	private String name; // for compatibility with datasets using old one letter ids
	// Biojava can fill both fields, datasets can later utilize any of them

	public ChainId() {

	}

	/**
	 * Best way, supposed to be used for conversion from BioJava.
	 * @param c
	 * @param name
	 */
	public ChainId(String c, String name) {
		this.id = c;
		this.name = name;
	}

	/**
	 * Legacy single letter identifier.
	 * @param c
	 */
	public ChainId(char c) {
		this.id = Character.toString(c);
		this.name = Character.toString(c);
	}

	public ChainId(String chainId) {
		this.id = chainId;
		this.name = chainId;
	}
	
	public String getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public boolean equals(Object o) {
		ChainId other = (ChainId) o;
		return this.id.equals(other.id);
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	@Override
	public int compareTo(ChainId other) {
		return this.id.compareTo(other.id);
	}

	@Override
	public String toString() {
		return this.id;
	}
}
