package geometry;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

import org.biojava.nbio.structure.geometry.CalcPoint;


/**
 *
 * @author Antonin Pavelka
 */
public class Transformation {

    private Point translation_, rotation_;
    private Matrix4d m_;

    public Matrix4d getMatrix() {
        return m_;
    }

    public Transformation(Matrix4d m) {
        this.m_ = m;
        double d = 1;
        Point3d[] anchor = {
            new Point3d(0, 0, 0),
            new Point3d(d, 0, 0),
            new Point3d(0, d, 0),
            new Point3d(0, 0, d)};
        Point[] a = toPoints(anchor);
        CalcPoint.transform(m, anchor);
        Point[] b = toPoints(anchor);
        Point translation = a[0].minus(b[0]);
        Point rotation = null;
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 1; i <= 3; i++) {
            Point r = a[i].minus(b[i].minus(b[0]));
            assert r != null;
            double rd = r.size();
            assert Double.isFinite(rd);
            if (rd > max) {
                rotation = r;
            }
        }
        assert translation != null;
        assert rotation != null;
        this.translation_ = translation;
        this.rotation_ = rotation;
    }

    public boolean close(Transformation other) {
        double t = translation_.minus(other.translation_).size();
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
