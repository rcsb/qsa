package geometry;

public class TorsionAngle {

    public static final double torsionAngle(Point a, Point b, Point c, Point d) {
        Point ab = a.minus(b);
        Point cb = c.minus(b);
        Point bc = b.minus(c);
        Point dc = d.minus(c);
        Point abc = ab.cross(cb);
        Point bcd = bc.cross(dc);
        double angl = abc.angle(bcd);
        // calc the sign:
        Point vecprod = abc.cross(bcd);
        double val = cb.dot(vecprod);
        if (val < 0.0) {
            angl = -angl;
        }
        if (angl < -Math.PI || angl > Math.PI) {
            System.err.println("bad torsion angle " + angl);
        }
        return angl;
    }

}
