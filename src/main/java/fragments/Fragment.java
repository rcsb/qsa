package fragments;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.SVDSuperimposer;
import org.biojava.nbio.structure.StructureException;

import geometry.Point;
import geometry.PointConversion;
import geometry.Transformation;
import spark.clustering.Clusterable;

/**
 *
 * @author Antonin Pavelka
 */
public class Fragment implements Clusterable<Fragment> {

	private static final long serialVersionUID = 1L;
	private Word a_, b_;
	private double[] features_;

	private Point[] centeredPoints;

	public Fragment(Word a, Word b) {
		a_ = a;
		b_ = b;
		computeFeatures(a, b);
		// System.out.println(features_.length);
	}

	public Fragment switchWords() {
		return new Fragment(b_, a_);
	}

	public Point getCenter() {
		return a_.getCenter().plus(b_.getCenter()).divide(2);
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

	/*@Deprecated
	public Transformation superpose(Fragment other) {
		Point3d[] ap = PointConversion.getPoints3d(getPoints());
		Point3d[] bp = PointConversion.getPoints3d(other.getPoints());
		// Matrix4d m = SuperPosition.superposeWithTranslation(ap, bp);
		// Transformation t = new Transformation(m);
		try {
			SVDSuperimposer svd = new SVDSuperimposer(ap, bp);
			return new Transformation(svd);
		} catch (StructureException e) {
			throw new RuntimeException(e);
		}
	}*/

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

	public static void main(String[] args) throws Exception {
		Point3d[] a = { new Point3d(10, 0, 0), new Point3d(0, 10, 0), new Point3d(0, 0, 10) };
		Point3d[] b = { new Point3d(10, 0, 0), new Point3d(0, 0, 10), new Point3d(0, -10, 0) };

		SVDSuperimposer svd = new SVDSuperimposer(a, b);
		System.out.println(svd.getTranslation().getCoords()[0]);
		System.out.println(svd.getTranslation().getCoords()[1]);
		System.out.println(svd.getTranslation().getCoords()[2]);

		Point e = new Point(Calc.getXYZEuler(svd.getRotation()));
		System.out.println(e.getX() + " " + e.getY() + " " + e.getZ());

		Atom[] atoms = PointConversion.getAtoms(b);
		// Calc.transform(atoms, svd.getTransformation());

		Calc.rotate(atoms, svd.getRotation());
		// Calc.shift(atoms, svd.getTranslation());

		for (Atom at : atoms) {
			System.out.println(at.getX() + " " + at.getY() + " " + at.getZ());
		}
		// System.out.println(svd.getR);
	}

}
