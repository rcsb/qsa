package geometry.angles;

import geometry.primitives.Point;
import language.MathUtil;

public class Angles {

	public static double toRadians(double degrees) {
		return degrees * Math.PI / 180;
	}

	public static double toDegrees(double radians) {
		return radians * 180 / Math.PI;
	}

	public static int toDegreeInt(double radians) {
		return (int) Math.round(radians * 180 / Math.PI);
	}

	public static final double wrap(double radians) {
		return MathUtil.wrap(radians, 0, 2 * Math.PI);
	}

	public static final double torsionAngle(Point a, Point b, Point c, Point d) {
		Point ab = a.minus(b);
		Point cb = c.minus(b);
		Point bc = b.minus(c);
		Point dc = d.minus(c);
		Point abc = ab.cross(cb);
		Point bcd = bc.cross(dc);
		double ang = abc.angle(bcd);
		// calc the sign:
		Point vecprod = abc.cross(bcd);
		double val = cb.dot(vecprod);
		if (val < 0.0) {
			ang = -ang;
		}
		if (ang < -Math.PI || ang > Math.PI) {
			System.err.println("bad torsion angle " + ang);
		}
		return ang;
	}

	public static final double angle(Point a, Point b, Point c) {
		double ang = a.minus(b).angle(c.minus(b));
		if (ang < 0 || ang > Math.PI) {
			System.err.println("bad  angle " + ang);
		}
		return ang;
	}

}
