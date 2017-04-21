package fragments;

import javax.vecmath.Point3d;

import geometry.Point;
import pdb.Residue;
import spark.clustering.Clusterable;

/**
 *
 * @author Antonin Pavelka
 */
public class FragmentSingleWord implements Clusterable<FragmentSingleWord> {

	private static final long serialVersionUID = 1L;
	private WordImpl a_;
	private Point3d[] ps3d;
	private Point[] centeredPoints;

	public FragmentSingleWord(WordImpl a) {
		a_ = a;
	}

	@Deprecated
	public Point[] getCenteredPoints() {
		if (centeredPoints == null) {
			Point[] a = a_.getPoints();
			centeredPoints = new Point[a.length];
			Point c = getCenter();
			for (int i = 0; i < a.length; i++) {
				centeredPoints[i] = a[i].minus(c);
			}
		}
		return centeredPoints;
	}

	public Point getCenter() {
		return a_.getCenter();
	}

	public Point3d getCenter3d() {
		Point p = getCenter();
		return new Point3d(p.getCoords());
	}

	public Point[] getPoints() {
		return a_.getPoints();
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

	public Residue[] getResidues() {
		return a_.getResidues();

	}

}
