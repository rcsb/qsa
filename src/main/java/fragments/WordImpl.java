package fragments;

import java.io.Serializable;

import javax.vecmath.Point3d;

import geometry.Point;
import pdb.Residue;

/**
 *
 * @author Antonin Pavelka
 */
public class WordImpl implements Serializable, Word {

	private static final long serialVersionUID = 1L;
	private final Residue[] residues_;
	private Point center;
	private final int id;
	private final Point3d[] points;

	public WordImpl(int id, Residue[] residues) {
		this.residues_ = residues;
		//computeInternalDistances();
		this.id = id;
		this.points = computePoints3d();
	}

	public WordImpl invert(int id) {
		int n = residues_.length;
		Residue[] inv = new Residue[n];
		int k = n - 1;
		for (int i = 0; i < n; i++) {
			inv[i] = residues_[k--];
		}
		return new WordImpl(id, inv);
	}

	public int getId() {
		return id;
	}

	public int seqDist(WordImpl other) {
		int d = Integer.MAX_VALUE;
		for (Residue x : residues_) {
			for (Residue y : other.residues_) {
				int diff = Math.abs(x.getIndex().getSequenceNumber() - y.getIndex().getSequenceNumber());
				if (diff < d) {
					d = diff;
				}
			}
		}
		return d;
	}

	public boolean isInContactAndNotOverlapping(WordImpl other, double threshold) {
		Residue a1 = residues_[0];
		Residue a2 = residues_[residues_.length - 1];
		Residue b1 = other.residues_[0];
		Residue b2 = other.residues_[residues_.length - 1];

		int n1 = a1.getId().getSequenceNumber();
		int n2 = a2.getId().getSequenceNumber();
		int m1 = b1.getId().getSequenceNumber();
		int m2 = b2.getId().getSequenceNumber();
		if ((n1 <= m1 && m1 <= n2) || (n1 <= m2 && m2 <= n2) || (m1 <= n1 && n1 <= m2) || (m1 <= n2 && n2 <= m2)) {
			return false;
		} else {
			for (int x = 0; x < residues_.length; x++) {
				for (int y = 0; y < other.residues_.length; y++) {
					double d = residues_[x].distance(other.residues_[y]);
					if (d <= threshold) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public final Point[] getPoints() {
		Point[] points = new Point[residues_.length];
		for (int i = 0; i < residues_.length; i++) {
			points[i] = residues_[i].getPosition();
		}
		return points;
	}

	private Point3d[] computePoints3d() {
		Point3d[] ps = new Point3d[residues_.length];
		for (int i = 0; i < residues_.length; i++) {
			ps[i] = residues_[i].getPosition3d();
		}
		return ps;
	}

	@Override
	public final Point3d[] getPoints3d() {
		return points;
	}

	@Override
	public Residue[] getResidues() {
		return residues_;
	}

	public Point getCenter() {
		if (center == null) {
			Point sum = new Point(0, 0, 0);
			Point[] ps = getPoints();
			for (Point p : ps) {
				sum = sum.plus(p);
			}
			center = sum.divide(ps.length);
		}
		return center;
	}

	public Point[] getCenters(int n) {
		Point[] ps = getPoints();
		double f = (double) ps.length / n;
		Point[] centers = new Point[n];
		for (int i = 0; i < n; i++) {
			int lo = (int) Math.floor(i * f);
			int hi = (int) Math.ceil((i + 1) * f);
			Point x = new Point(0, 0, 0);
			for (int j = lo; j < hi; j++) {
				x = x.plus(ps[j]);
			}
			x = x.divide(hi - lo + 1);
			centers[i] = x;
		}
		return centers;
	}

	public void print() {
		for (Residue r : residues_) {
			System.out.print(r.getId().toString() + " ");
		}
		System.out.println();
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		WordImpl other = (WordImpl) o;
		return id == other.id;
	}

	@Override
	public String toString() {
		return Integer.toString(id);
	}

	@Override
	public int size() {
		return residues_.length;
	}
}
