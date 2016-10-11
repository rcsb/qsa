package fragments;

import java.util.ArrayList;
import java.util.List;

import org.biojava.nbio.structure.Atom;

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

	public FragmentPair getCore() {
		return core;
	}

	public List<FragmentPair> getFragmentPairs() {
		return list;
	}

	@Deprecated
	public Transformation getTransformation() {
		return core.getTransformation();
	}

	public void add(FragmentPair p) {
		list.add(p);
		p.capture();
	}

	@Deprecated
	public void tryToAdd(FragmentPair p) {
		if (Math.abs(core.getFragmentDistance() - p.getFragmentDistance()) <= Parameters.create()
				.getMaxFragmentDist()) {
			if (core.isTranformationSimilar(p)) {
				add(p);
			}
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