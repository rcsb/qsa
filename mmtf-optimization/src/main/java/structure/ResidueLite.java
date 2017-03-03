package structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonin Pavelka
 *
 */
public class ResidueLite implements Serializable/*, Comparable<ResidueLite>*/ {

	private static final long serialVersionUID = 1L;
	private ResidueId id_;
	private List<AtomLite> atomList;
	//private AtomLite[] atoms;

	public ResidueLite(ResidueId index) {
		this.id_ = index;
		atomList = new ArrayList<>();
	}

	public void addAtom(AtomLite a) {
		atomList.add(a);
	}

	public ResidueId getId() {
		return id_;
	}

	public List<AtomLite> getAtoms() {
		return atomList;
	}

	@Override
	public boolean equals(Object o) {
		ResidueLite other = (ResidueLite) o;
		return id_.equals(other.id_);
	}

	@Override
	public int hashCode() {
		return id_.hashCode();
	}

	/*
    @Override
    public int compareTo(ResidueLite other) {
        return id_.compareTo(other.id_);
    }
	 */
	public String toString() {
		return id_.toString() + "(" + atomList.size() + ")";
	}

	public String getAtomInfo() {
		StringBuilder sb = new StringBuilder();
		for (AtomLite a : getAtoms()) {
			sb.append(a + "\n");
		}
		return sb.toString();
	}
}
