package alignment;

import geometry.NumberedPoint;

/**
 *
 * @author Antonin Pavelka
 */
public class PointPair implements Comparable<PointPair> {

    private int x_, y_;
    private double d_;

    public PointPair(int x, int y, double d) {
        x_ = x;
        y_ = y;
        d_ = d;
    }

    @Override
    public int compareTo(PointPair other) {
        return Double.compare(d_, other.d_);
    }

    @Override
    public String toString() {
        return x_ + " " + y_ + " " + d_;
    }

    public int getX() {
        return x_;
    }

    public int getY() {
        return y_;

    }

    public double getD() {
        return d_;
    }
}
