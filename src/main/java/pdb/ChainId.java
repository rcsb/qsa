package pdb;

import java.io.Serializable;

/**
 *
 * @author Antonin Pavelka
 */
public class ChainId implements Comparable<ChainId>, Serializable {

	String c_;
	String name_;

	public ChainId(String c) {
		if (c.length() > 2)
			throw new RuntimeException("obsolete ChainId usage " + c + ".");
		c_ = Character.toString(c.charAt(0));
	}

	public ChainId(String c, String name) {
		if (c.length() > 2)
			throw new RuntimeException("obsolete ChainId usage " + c + ".");
		c_ = Character.toString(c.charAt(0));
		name_ = name;
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
