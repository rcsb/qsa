package geometry;

import java.util.StringTokenizer;

public class Sphere {

    private Point s_;
    private double r_;

    private Sphere() {
    }

    public Sphere(double x, double y, double z, double r) {
        assert !Double.isNaN(x);
        assert !Double.isNaN(y);
        assert !Double.isNaN(z);
        this.s_ = new Point(x, y, z);
        this.r_ = r;
    }

    public Sphere(Point s, double r) {
        this.s_ = s;
        this.r_ = r;
    }

    public void setR(double r) {
        r_ = r;
    }

    public String save() {
        return s_.getX() + ", " + s_.getY() + ", " + s_.getZ() + ", " + r_;
    }

    public static Sphere load(String s) {
        StringTokenizer st = new StringTokenizer(s, ", ");
        double x = Double.parseDouble(st.nextToken());
        double y = Double.parseDouble(st.nextToken());
        double z = Double.parseDouble(st.nextToken());
        double r = Double.parseDouble(st.nextToken());
        Sphere sphere = new Sphere(x, y, z, r);
        return sphere;
    }

    public Sphere move(double dx, double dy, double dz) {
        Point d = new Point(dx, dy, dz);
        Sphere sphere = new Sphere(s_.plus(d), r_);
        return sphere;
    }

    public Point getCenter() {
        return s_;
    }

    public double getR() {
        return r_;
    }

    public double distance(Point p) {
        double d = p.distance(getCenter()) - getR();
        if (d < 0) {
            d = 0;
        }
        return d;
    }

    public boolean contains(Point p) {
        return p.distance(getCenter()) <= getR();
    }

    public double distance(Sphere other) {
        double d = other.distance(getCenter()) - getR() - other.getR();
        if (d < 0) {
            d = 0;
        }
        return d;
    }

    @Override
    public String toString() {
        return s_.toString() + " r=" + r_;
    }
}
