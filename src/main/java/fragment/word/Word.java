package fragment.word;

import java.io.Serializable;

import javax.vecmath.Point3d;

import geometry.primitives.Point;
import structure.Residue;
import fragment.FragmentOfPolymer;

/**
 * A sequence of residues of a fixed length.
 *
j * @author Antonin Pavelka
 */
public class Word implements Serializable, FragmentOfPolymer, Comparable<Word> {

	private static final long serialVersionUID = 1L;
	private Residue[] residues_;
	private Point center;
	private int id;
	private Point3d[] points;
	private double boundingRadius;

	public Word() {
	}

	public Word(int id, Residue[] residues) {
		this.residues_ = residues;
		this.id = id;
		this.points = computePoints3d();
		Point s = getCenter();
		for (Point x : getPoints()) {
			double d = s.distance(x);
			if (d > boundingRadius) {
				boundingRadius = d;
			}
		}
	}

	public Word invert(int id) {
		int n = residues_.length;
		Residue[] inv = new Residue[n];
		int k = n - 1;
		for (int i = 0; i < n; i++) {
			inv[i] = residues_[k--];
		}
		return new Word(id, inv);
	}

	public int getId() {
		return id;
	}

	public boolean overlaps(Word other) {
		for (Residue xr : getResidues()) {
			for (Residue yr : other.getResidues()) {
				if (xr.equals(yr)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isInContact(Word other) {
		Residue[] xs = this.getResidues();
		Residue[] ys = other.getResidues();
		return xs[xs.length - 1].isFollowedBy(ys[0]) || ys[ys.length - 1].isFollowedBy(xs[0]);
	}

	public boolean isInContactAndNotOverlapping(Word other, double threshold) {

		double dist = getCenter().distance(other.getCenter());
		if (dist > boundingRadius + other.boundingRadius + threshold) {
			return false;
		}
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
			double squaredThreshold = threshold * threshold;
			for (int x = 0; x < residues_.length; x++) {
				for (int y = 0; y < other.residues_.length; y++) {
					double d = residues_[x].getPosition().squaredDistance(other.residues_[y].getPosition());
					if (d <= squaredThreshold) {
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

	public double[][] getAtoms() {
		//List<double[]> atoms = new ArrayList<>();
		int n = 0;
		for (Residue r : getResidues()) {
			n += r.getAtoms().length;
		}
		double[][] atoms = new double[n][];

		int index = 0;
		for (Residue r : getResidues()) {
			double[][] ra = r.getAtoms();
			System.arraycopy(ra, 0, atoms, index, ra.length);
			index += ra.length;
		}
		return atoms;
	}

	public final Residue getCentralResidue() {
		return residues_[residues_.length / 2];
	}

	public final Point getCenter() {
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
		Word other = (Word) o;
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

	@Override
	public int compareTo(Word other) {
		return this.getCentralResidue().compareTo(other.getCentralResidue());
	}
}
