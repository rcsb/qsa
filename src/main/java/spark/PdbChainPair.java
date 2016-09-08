package spark;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import pdb.PdbChain;

/**
 *
 * @author Antonin Pavelka
 */
public class PdbChainPair {

    private Set<PdbChain> set = new HashSet<>();

    public PdbChainPair(PdbChain a, PdbChain b) {
        set.add(a);
        set.add(b);
    }

    public PdbChain[] get() {
        assert set.size() == 2;
        PdbChain[] a = new PdbChain[2];
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
