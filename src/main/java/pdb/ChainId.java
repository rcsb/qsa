package pdb;

import java.io.Serializable;

/**
 *
 * @author Antonin Pavelka
 */
public class ChainId implements Comparable<ChainId>, Serializable {

	private final long serialVersionUID = 1L;
	String c_;
	String name_;

	public ChainId() {

	}

	public ChainId(char c) {
		this.c_ = Character.toString(c);
		this.name_ = Character.toString(c);
	}

	/*public ChainId(String c) {
		this.c_ = c;
	}*/
	public static ChainId createEmpty() {
		return new ChainId('_');
	}

	public ChainId(String c, String name) {
		c_ = c;
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
		return c_.equals(other.c_);
	}

	@Override
	public int hashCode() {
		return c_.hashCode();
	}

	@Override
	public int compareTo(ChainId other) {
		return c_.compareTo(other.c_);
	}

	public String toString() {
		return c_;
	}
}
