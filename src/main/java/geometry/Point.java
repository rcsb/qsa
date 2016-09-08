package geometry;

import java.io.Serializable;
import java.util.Collection;
import java.util.Random;
import org.biojava.nbio.structure.Atom;

/*
 * Represents a point in 3D space. The point can be treated as a vector and 
 * this class provides vector operations such as addition and dot product.
 */
public class Point implements Coordinates, Serializable {

    private final double x_;
    private final double y_;
    private final double z_;

    private Point() {
        x_ = 8888;
        y_ = 8888;
        z_ = 8888;
    }

    public Point(float x, float y, float z) {
        this.x_ = x;
        this.y_ = y;
        this.z_ = z;

        assert check();
    }

    public Point(double x, double y, double z) {
        this.x_ = x;
        this.y_ = y;
        this.z_ = z;
        assert check();
    }

    public Point(double[] coordinates) {
        x_ = coordinates[0];
        y_ = coordinates[1];
        z_ = coordinates[2];
        assert check();
    }

    public Point(float[] coordinates) {
        x_ = coordinates[0];
        y_ = coordinates[1];
        z_ = coordinates[2];
        assert check();
    }

    public Point(Atom a) {
        x_ = a.getX();
        y_ = a.getY();
        z_ = a.getZ();
    }

    public boolean check() {
        if (Double.isNaN(x_)) {
            throw new RuntimeException();
        }
        if (Double.isNaN(y_)) {
            throw new RuntimeException();
        }
        if (Double.isNaN(z_)) {
            throw new RuntimeException();
        }
        return true;
    }

    public Point(Point p) {
        x_ = p.x_;
        y_ = p.y_;
        z_ = p.z_;
    }

    public static Point createShattered(Point p, double maxDev, Random random) {
        return new Point(
                shatter(p.getX(), maxDev, random),
                shatter(p.getY(), maxDev, random),
                shatter(p.getZ(), maxDev, random));
    }

    /*
     * d - number to randomly change maxDeviation - maximum difference between d
     * and returned value
     */
    private static double shatter(double d, double maxDev, Random random) {
        assert 0 < maxDev;
        double r = (random.nextDouble() - 0.5) * 2 * maxDev;
        assert r <= maxDev;
        double value = d + r;
        return value;
    }

    /*
     * Returns geometric center of Point collection i. e. center of gravity
     * where each point has unit weight.
     */
    public static Point center(Collection<Point> points) {
        Point t = new Point(0, 0, 0);
        for (Point p : points) {
            t = t.plus(p);
        }
        t.divide(points.size());
        return t;
    }

    public Point plus(Point p) {
        return new Point(getX() + p.getX(), getY() + p.getY(), getZ() + p.getZ());
    }

    public Point minus(Point p) {
        return new Point(getX() - p.getX(), getY() - p.getY(), getZ() - p.getZ());
    }

    public double squaredSize() {
        return getX() * getX() + getY() * getY() + getZ() * getZ();
    }

    public double size() {
        return Math.sqrt(squaredSize());
    }

    public double distance(Point p) {
        return Math.sqrt(this.minus(p).squaredSize());
    }

    public double squaredDistance(Point p) {
        return this.minus(p).squaredSize();
    }

    public Point multiply(double d) {
        return new Point(d * getX(), d * getY(), d * getZ());
    }

    public Point divide(double d) {
        assert d != 0;
        return new Point(getX() / d, getY() / d, getZ() / d);
    }

    /*
     * Dot product. Skalarni soucin.
     */
    public double dot(Point p) {
        return getX() * p.getX() + getY() * p.getY() + getZ() * p.getZ();
    }

    public Point cross(Point p) {
        double x = y_ * p.z_ - p.y_ * z_;
        double y = z_ * p.x_ - p.z_ * x_;
        double z = x_ * p.y_ - p.x_ * y_;
        Point v = new Point(x, y, z);
        return v;
    }

    @Override
    public String toString() {
        return "[" + getX() + ", " + getY() + ", " + getZ() + "]";
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (int) (Double.doubleToLongBits(this.getX())
                ^ (Double.doubleToLongBits(this.getX()) >>> 32));
        hash = 83 * hash + (int) (Double.doubleToLongBits(this.getY())
                ^ (Double.doubleToLongBits(this.getY()) >>> 32));
        hash = 83 * hash + (int) (Double.doubleToLongBits(this.getZ())
                ^ (Double.doubleToLongBits(this.getZ()) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        Point x = (Point) o;
        return x.x_ == x_ && x.y_ == y_ && x.z_ == z_;
    }

    public boolean close(Point p) {
        return squaredDistance(p) < 0.00001;
    }

    public boolean quiteClose(Point p) {
        return squaredDistance(p) < 0.01;
    }

    public Point normalize() {
        double size = size();
        assert size != 0;
        return divide(size);
    }

    public double getX() {
        return x_;
    }

    public double getY() {
        return y_;
    }

    public double getZ() {
        return z_;
    }

    @Override
    public double[] getCoords() {
        double[] cs = {x_, y_, z_};
        return cs;
    }
}
