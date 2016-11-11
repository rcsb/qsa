package util;

import javax.vecmath.Point3d;

public class NumericUtil {
	public static Point3d[] doubleToPoint(double[][] ds) {
		Point3d[] ps = new Point3d[ds.length];
		for (int i = 0; i < ds.length; i++) {
			ps[i] = new Point3d(ds[i]);
		}
		return ps;
	}
}
