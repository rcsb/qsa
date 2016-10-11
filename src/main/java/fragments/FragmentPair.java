package fragments;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

import geometry.Point;
import geometry.RotationPair;
import geometry.Transformation;
import geometry.Transformer;

/**
 *
 * @author Antonin Pavelka
 */
public class FragmentPair implements Comparable<FragmentPair> {

	private Fragment[] f;
	private double rmsd;
	private Transformation transformation_;
	private Matrix3d[] m;
	private boolean free = true;

	public FragmentPair(Fragment a, Fragment b, double rmsd) {
		this.f = new Fragment[2];
		this.f[0] = a;
		this.f[1] = b;
		this.rmsd = rmsd;
	}

	public Fragment[] get() {
		return f;
	}

	public boolean free() {
		return free;
	}

	public void capture() {
		free = false;
	}

	@Deprecated
	public Transformation getTransformation() {
		return transformation_;
	}

	public Point[] getPoints() {
		Point[] aps = f[0].getPoints();
		Point[] bps = f[1].getPoints();
		Point[] ps = new Point[aps.length + bps.length];
		assert aps.length == bps.length;
		for (int i = 0; i < aps.length; i++) {
			ps[i * 2] = aps[i];
			ps[i * 2 + 1] = bps[i];
		}
		return ps;
	}

	@Override
	public int compareTo(FragmentPair other) {
		return Double.compare(rmsd, other.rmsd);
	}

	public void computeSuperposition() {
		Fragment[] fs = get();
		transformation_ = new Transformation(fs[0], fs[1]);
	}

	public double getFragmentDistance() {
		return f[0].getCenter().distance(f[1].getCenter());
	}

	@Deprecated
	public boolean isTranformationSimilar(FragmentPair other) {
		return transformation_.close(other.transformation_);
	}

	public boolean isCompatible(FragmentPair other) {
		if (m == null) {
			m = new Matrix3d[2];
			for (int i = 0; i < 2; i++) {
				Transformer t = new Transformer();
				t.set(f[i].getPoints3d(), other.f[i].getPoints3d());
				m[i] = t.getRotationMatrix();
			}
		}
		RotationPair rp = new RotationPair(m[0], m[1]);
		if (rp.compareRotations() > Parameters.create().getMaxRotationCompatibilityAngle()) {
			return false;
		}
		Matrix3d avg = rp.average();
		Point3d[] centers = new Point3d[2];
		Point[] origins = new Point[2];
		Point[] ts = new Point[2];
		for (int i = 0; i < 2; i++) {
			centers[i] = f[i].getCenter3d();
			avg.transform(centers[i]);
			origins[i] = new Point(centers[i].x, centers[i].y, centers[i].z);
			ts[i] = other.f[i].getCenter().minus(origins[i]);
		}
		return ts[0].minus(ts[1]).size() < Parameters.create().getMaxTranslationDifference();
	}
}