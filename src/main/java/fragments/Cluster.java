package fragments;

import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Matrix4d;

/**
 *
 * @author Antonin Pavelka
 */
public class Cluster implements Comparable<Cluster> {

    private List<Pair> list = new ArrayList<>();
    private Pair core;

    public Cluster(Pair p) {
        list.add(p);
        core = p;
    }

    public Matrix4d getTransformation() {
        return core.getMatrix();
    }

    private void add(Pair p) {
        list.add(p);
    }

    public void tryToAdd(Pair p) {
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
