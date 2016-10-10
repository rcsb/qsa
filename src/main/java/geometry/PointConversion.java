package geometry;

import javax.vecmath.Point3d;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.AtomImpl;
import org.netlib.util.doubleW;

public class PointConversion {

	public static Point[] getPoints(Point3d[] a) {
		Point[] b = new Point[a.length];
		for (int i = 0; i < a.length; i++) {
			Point3d x = a[i];
			Point y = new Point(x.x, x.y, x.z);
			b[i] = y;
		}
		return b;
	}

	public static NumberedPoint[] getNumberedPoints(Point3d[] a) {
		NumberedPoint[] b = new NumberedPoint[a.length];
		for (int i = 0; i < a.length; i++) {
			Point3d x = a[i];
			NumberedPoint y = new NumberedPoint(i, new Point(x.x, x.y, x.z));
			b[i] = y;
		}
		return b;
	}

	public static Point3d[] getPoints3d(Point[] ps) {
		Point3d[] ps3d = new Point3d[ps.length];
		for (int i = 0; i < ps.length; i++) {
			Point p = ps[i];
			ps3d[i] = new Point3d(p.getX(), p.getY(), p.getZ());
		}
		return ps3d;
	}

	public static Point3d[] getPoints3d(double[][] ds) {
		Point3d[] ps3d = new Point3d[ds.length];
		for (int i = 0; i < ds.length; i++) {			
			ps3d[i] = new Point3d(ds[i]);
		}
		return ps3d;
	}

	
	public static Atom[] getAtoms(Point3d[] ps) {
		Atom[] atoms = new Atom[ps.length];
		for (int i = 0; i < ps.length; i++) {
			Atom a = new AtomImpl();
			Point3d p = ps[i];
			a.setX(p.x);
			a.setY(p.y);
			a.setZ(p.z);
			atoms[i] = a;
		}
		return atoms;
	}

}
