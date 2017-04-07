package geometry;

/*
 * Represents a point in 3D space. The point can be treated as a vector and 
 * this class provides vector operations such as addition and dot product.
 */
public final class NumberedPoint extends Point {

    private int number_;

    public NumberedPoint(int number, Point p) {
        super(p);
        number_ = number;
    }

    public int getNumber() {
        return number_;
    }
}
