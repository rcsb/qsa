package fragments;

import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point3d;
import geometry.Point;
import pdb.Residue;
import spark.clustering.Clusterable;

/**
 *
 * @author Antonin Pavelka
 */
public class Fragment implements Clusterable<Fragment> {

	private static final long serialVersionUID = 1L;
	private Word a_, b_;
	private double[] features_;
	private Point3d[] ps3d;
	private Point[] centeredPoints;
	private double wordDistance;
	private static double maxWdd = Parameters.create().getMaxWordDistDiff();
	private static double maxWr = Parameters.create().getMaxWordRmsd();

	public Fragment(Word a, Word b) {
		a_ = a;
		b_ = b;
		computeFeatures(a, b);
		wordDistance = a.getCenter().distance(b.getCenter());
	}

	public Fragment switchWords() {
		return new Fragment(b_, a_);
	}

	public Word[] getWords() {
		Word[] w = { a_, b_ };
		return w;
	}

	public boolean isSimilar(Fragment other, WordMatcher wm) {
		if (Math.abs(wordDistance - other.wordDistance) <= maxWdd) {
			if (wm.getRmsd(a_.getId(), other.a_.getId()) <= maxWr) {
				if (wm.getRmsd(b_.getId(), other.b_.getId()) <= maxWr) {
					return true;
				}
			}

		}
		return false;
	}

	public Point getCenter() {
		return a_.getCenter().plus(b_.getCenter()).divide(2);
	}

	public Point3d getCenter3d() {
		Point p = getCenter();
		return new Point3d(p.getCoords());
	}

	public double distance(Fragment other) {
		double sum = 0;
		for (int i = 0; i < features_.length; i++) {
			sum += Math.abs(features_[i] - other.features_[i]);
		}
		return sum / features_.length;
	}

	private void computeFeatures(Word a, Word b) {
		List<Double> features = new ArrayList<>();
		Point[] aps = a.getPoints();
		Point[] bps = b.getPoints();
		for (int x = 0; x < aps.length; x++) {
			for (int y = 0; y < x; y++) {
				double d = aps[x].distance(bps[y]);
				features.add(d);
			}
		}
		features_ = new double[features.size()];
		for (int i = 0; i < features_.length; i++) {
			features_[i] = features.get(i);
		}
	}

	public Point[] getPoints() {
		Point[] aps = a_.getPoints();
		Point[] bps = b_.getPoints();
		Point[] ps = new Point[aps.length + bps.length];
		System.arraycopy(aps, 0, ps, 0, aps.length);
		System.arraycopy(bps, 0, ps, aps.length, bps.length);
		return ps;
	}

	public Point3d[] getPoints3d() {
		if (ps3d == null) {
			Point[] ps = getPoints();
			ps3d = new Point3d[ps.length];
			for (int i = 0; i < ps.length; i++) {
				ps3d[i] = new Point3d(ps[i].getCoords());
			}
		}
		return ps3d;
	}

	public Point[] getCenteredPoints() {
		if (centeredPoints == null) {
			Point[] a = a_.getPoints();
			Point[] b = b_.getPoints();
			centeredPoints = new Point[a.length + b.length];
			Point c = getCenter();
			for (int i = 0; i < a.length; i++) {
				centeredPoints[i] = a[i].minus(c);
			}
			for (int i = 0; i < b.length; i++) {
				centeredPoints[a.length + i] = b[i].minus(c);
			}
		}
		return centeredPoints;
	}

	public Residue[] getResidues() {
		Residue[] a = a_.getResidues();
		Residue[] b = b_.getResidues();
		return Residue.merge(a, b);
	}
}
