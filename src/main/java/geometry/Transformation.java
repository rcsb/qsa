package geometry;

import javax.vecmath.Point3d;

import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.SVDSuperimposer;
import org.biojava.nbio.structure.jama.Matrix;

/**
 *
 * @author Antonin Pavelka
 */
public class Transformation {

	private Point translation_;
	private Matrix rotation_;
	private SVDSuperimposer svd;
	private double[] euler;

	public Transformation(SVDSuperimposer svd) {
		this.svd = svd;
		this.translation_ = new Point(svd.getTranslation().getCoords());
		this.rotation_ = svd.getRotation();
		euler = Calc.getXYZEuler(this.rotation_);
	}

	public boolean close(Transformation other) {
		Point t = translation_.minus(other.translation_);
		
		double r = rotation_.minus(other.rotation_).size();
		return t < 10 && r < 0.3;
	}

	private Point[] toPoints(Point3d[] a) {
		Point[] ps = new Point[a.length];
		for (int i = 0; i < ps.length; i++) {
			ps[i] = new Point(a[i].x, a[i].y, a[i].z);
		}
		return ps;
	}

	private Point3d[] toPoints3d(Point[] a) {
		Point3d[] ps = new Point3d[a.length];
		for (int i = 0; i < ps.length; i++) {
			ps[i] = new Point3d(a[i].getX(), a[i].getY(), a[i].getZ());
		}
		return ps;
	}

}
