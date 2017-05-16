package fragments;

import geometry.Coordinates;
import javax.vecmath.Point3d;
import geometry.Point;
import pdb.Residue;
import pdb.SimpleStructure;
import spark.clustering.Clusterable;
import vectorization.SmartVectorizer;

/**
 *
 * @author Antonin Pavelka
 */
public class Biword implements Clusterable<Biword>, Coordinates {

	public static long count;
	private final WordImpl a_;
	private final WordImpl b_;
	private final float wordDistance;
	private final float[] coords = new float[6];
	private static final long serialVersionUID = 1L;
	private static final double maxWdd = Parameters.create().getMaxWordDistDiff();
	private static final double maxWr = Parameters.create().getMaxWordRmsd();
	//private Point3d[] ps3d;

	public Biword(WordImpl a, WordImpl b) {
		a_ = a;
		b_ = b;
		wordDistance = (float) a.getCenter().distance(b.getCenter());
		SmartVectorizer av = new SmartVectorizer(a_);
		SmartVectorizer bv = new SmartVectorizer(b_);
		coords[0] = (float) av.firstHalf().distance(bv.firstHalf());
		coords[1] = (float) av.secondHalf().distance(bv.secondHalf());
		coords[2] = (float) av.firstHalf().distance(bv.secondHalf());
		coords[3] = (float) av.secondHalf().distance(bv.firstHalf());
		coords[4] = (float) av.getStraightness();
		coords[5] = (float) bv.getStraightness();
		count++;
	}

	public Biword switchWords() {
		return new Biword(b_, a_);
	}

	public SimpleStructure getStructure() {
		return a_.getStructure();
	}

	public WordImpl[] getWords() {
		WordImpl[] w = {a_, b_};
		return w;
	}

	@Override
	public double[] getCoords() {
		double[] ds = new double[coords.length];
		for (int i = 0; i < ds.length; i++) {
			ds[i] = coords[i];
		}
		return ds;
	}

	public double[] coordDiff(Biword other) {
		double[] diff = new double[coords.length];
		for (int i = 0; i < coords.length; i++) {
			diff[i] = Math.abs(coords[i] - other.coords[i]);
		}
		return diff;
	}

	public boolean isSimilar(Biword other, WordMatcher wm) {
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

	public Point[] getPoints() {
		Point[] aps = a_.getPoints();
		Point[] bps = b_.getPoints();
		Point[] ps = new Point[aps.length + bps.length];
		System.arraycopy(aps, 0, ps, 0, aps.length);
		System.arraycopy(bps, 0, ps, aps.length, bps.length);
		return ps;
	}

	public Point3d[] getPoints3d() {
		Point3d[] a = a_.getPoints3d();
		Point3d[] b = b_.getPoints3d();
		Point3d[] c = new Point3d[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
		/*if (ps3d == null) {
			Point[] ps = getPoints();
			ps3d = new Point3d[ps.length];
			for (int i = 0; i < ps.length; i++) {
				ps3d[i] = new Point3d(ps[i].getCoords());
			}
		}
		return ps3d;*/
	}

	public Residue[] getResidues() {
		Residue[] a = a_.getResidues();
		Residue[] b = b_.getResidues();
		return Residue.merge(a, b);
	}
}
