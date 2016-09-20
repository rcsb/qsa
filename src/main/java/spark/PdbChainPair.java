package spark;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import pdb.PdbChainId;

/**
 *
 * @author Antonin Pavelka
 */
public class PdbChainPair {

    private Set<PdbChainId> set = new HashSet<>();

    public PdbChainPair(PdbChainId a, PdbChainId b) {
        set.add(a);
        set.add(b);
    }

    public PdbChainId[] get() {
        assert set.size() == 2;
        PdbChainId[] a = new PdbChainId[2];
        set.toArray(a);
        return a;
    }

    @Override
    public boolean equals(Object o) {
        PdbChainPair other = (PdbChainPair) o;
        return set.equals(other.set);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.set);
        return hash;
    }
}
