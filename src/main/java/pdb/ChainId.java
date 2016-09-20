package pdb;

import java.io.Serializable;

/**
 *
 * @author Antonin Pavelka
 */
public class ChainId implements Comparable<ChainId>, Serializable {

	private static final long serialVersionUID = 1L;
	String c_;
	String name_;

	public ChainId(char c) {
		c_ = Character.toString(c);
	}

	public ChainId(String c) {
		c_ = Character.toString(c.charAt(0));
	}

	public static ChainId createEmpty() {
		return new ChainId("_");
	}

	public ChainId(String c, String name) {
		c_ = Character.toString(c.charAt(0));
		name_ = name;
	}

	public String getId() {
		return c_;
	}

	public String getName() {
		return name_;
	}

	@Override
	public boolean equals(Object o) {
		ChainId other = (ChainId) o;
		return c_.toUpperCase().equals(other.c_.toUpperCase());
	}

	@Override
	public int compareTo(ChainId other) {
		return c_.compareTo(other.c_);
	}

	public String toString() {
		return c_;
	}
}
