package alignment;

import java.util.Objects;
import pdb.SimpleStructure;
import pdb.StructureSource;

/**
 *
 * @author Antonin Pavelka
 */
public class StructureSourcePair {

	private StructureSource a, b;
	private StructureSource first, second;

	public StructureSourcePair(SimpleStructure[] structures) {
		this(structures[0].getSource(), structures[1].getSource());
	}

	public StructureSourcePair(StructureSource a, StructureSource b) {
		this.first = a;
		this.second = b;
		assert a != null;
		assert b != null;
		assert a.toString() != null;
		assert b.toString() != null;
		if (a.toString().compareTo(b.toString()) < 0) {
			this.a = a;
			this.b = b;
		} else {
			this.a = b;
			this.b = a;
		}
	}

	public StructureSource getFirst() {
		return first;
	}

	public StructureSource getSecond() {
		return second;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 83 * hash + Objects.hashCode(this.a);
		hash = 83 * hash + Objects.hashCode(this.b);
		return hash;
	}

	@Override
	public boolean equals(Object o) {
		StructureSourcePair other = (StructureSourcePair) o;
		return a.toString().equals(other.a.toString()) && b.toString().equals(other.b.toString());
	}
}
