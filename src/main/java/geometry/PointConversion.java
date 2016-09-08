package geometry;

import javax.vecmath.Point3d;

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
}
