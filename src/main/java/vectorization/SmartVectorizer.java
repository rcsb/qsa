package vectorization;

import fragments.WordImpl;
import fragments.WordVectorizer;
import geometry.Point;
import geometry.Angles;

public class SmartVectorizer implements WordVectorizer {

    private WordImpl word;
    private Point[] ps;
    private static int angleIndex;
    private Point[] x3;
    private Point[] x4;

    public SmartVectorizer(WordImpl w) {
        word = w;
        ps = word.getPoints();
        x3 = w.getCenters(3);
        x4 = w.getCenters(4);
    }

    // TODO also various percentiles?
    public double getAvgIntDist() {
        return 0;
    }

    public static int getAngleIndex() {
        return angleIndex;
    }

    @Override
    public double[] getVector() {
        double[] torsion = getTorsionAngles();
        double[] angle = getAngles();
        angleIndex = 3;
        int start = 5;
        double[] v = new double[start + torsion.length + angle.length];
        //TODO internal angles
        // skipping torsions and angles
        v[0] = getStraightness();
        v[1] = x3[0].distance(x3[1]);
        v[2] = x4[0].distance(x4[3]);
        v[3] = Angles.angle(x3[0], x3[1], x3[2]);
        v[4] = Angles.torsionAngle(x4[0], x4[1], x4[2], x4[3]);
        for (int i = 0; i < torsion.length; i++) {
            v[start + i] = torsion[i];
        }
        for (int i = 0; i < angle.length; i++) {
            v[start + torsion.length + i] = angle[i];
        }
        return v;
    }

    public double[] getTorsionAngles() {
        double[] angles = new double[ps.length - 3];
        for (int i = 0; i < ps.length - 3; i++) {
            double a = Angles.torsionAngle(
                    ps[i], ps[i + 1], ps[i + 2], ps[i + 3]);
            angles[i] = a;
        }
        return angles;
    }

    public double[] getAngles() {
        double[] angles = new double[ps.length - 2];
        for (int i = 0; i < ps.length - 2; i++) {
            double a = Angles.angle(
                    ps[i], ps[i + 1], ps[i + 2]);
            angles[i] = a;
        }
        return angles;
    }

    // TODO also at different segments?
    public double getStraightness() {
        double straightness = 0;
        for (int i = 0; i < ps.length - 4; i++) {
            double d = ps[i].distance(ps[i + 4]);
            straightness += d;
        }
        straightness /= ps.length - 4;
        return straightness;
    }

    public double getStraightness(int skip) {
        double straightness = 0;
        for (int i = 0; i < ps.length - skip; i++) {
            double d = ps[i].distance(ps[i + skip]);
            straightness += d;
        }
        straightness /= ps.length - skip;
        return straightness;
    }

    public Point firstHalf() {
        return x3[0];
    }

    public Point secondHalf() {
        return x3[2];
    }

}
