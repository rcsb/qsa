package fragments;

import java.util.ArrayList;
import java.util.List;

import geometry.Transformation;

/**
 *
 * @author Antonin Pavelka
 */
public class Cluster implements Comparable<Cluster> {

    private List<FragmentPair> list = new ArrayList<>();
    private FragmentPair core;

    public Cluster(FragmentPair p) {
        list.add(p);
        core = p;
    }

    public Transformation getTransformation() {
        return core.getTransformation();
    }

    private void add(FragmentPair p) {
        list.add(p);
    }

    public void tryToAdd(FragmentPair p) {
        if (core.isTranformationSimilar(p)) {
            add(p);
            p.capture();
        }
    }

    public int size() {
        return list.size();
    }

    @Override
    public int compareTo(Cluster other) {
        return Integer.compare(other.size(), size());
    }
}
