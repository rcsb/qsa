package fragments;

import geometry.Point;
import geometry.Transformation;
import javax.vecmath.Matrix4d;

/**
 *
 * @author Antonin Pavelka
 */
public class FragmentPair implements Comparable<FragmentPair> {

	private Fragment x, y;
	private double dist;
	private Transformation transformation_;
	private boolean free = true;

	public boolean free() {
		return free;
	}

	public void capture() {
		free = false;
	}

	public Transformation getTransformation() {
		return transformation_;
	}

	public FragmentPair(Fragment a, Fragment b, double dist) {
		this.x = a;
		this.y = b;
		this.dist = dist;
	}

	public Point[] getPoints() {
		Point[] aps = x.getPoints();
		Point[] bps = y.getPoints();
		Point[] ps = new Point[aps.length + bps.length];
		assert aps.length == bps.length;
		for (int i = 0; i < aps.length; i++) {
			ps[i * 2] = aps[i];
			ps[i * 2 + 1] = bps[i];
		}
		return ps;
	}

	public Fragment[] get() {
		Fragment[] fs = { x, y };
		return fs;
	}

	@Override
	public int compareTo(FragmentPair other) {
		return Double.compare(dist, other.dist);
	}

	public void computeSuperposition() {
		Fragment[] fs = get();
		transformation_ = new Transformation(fs[0], fs[1]);
	}

	public double getFragmentDistance() {
		return x.getCenter().distance(y.getCenter());
	}

	public boolean isTranformationSimilar(FragmentPair other) {
		return transformation_.close(other.transformation_);
	}
}
