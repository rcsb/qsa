package vectorization;

import fragments.Word;
import fragments.WordVectorizer;
import geometry.Point;
import geometry.TorsionAngle;
import java.util.Random;

public class SmartVectorizer implements WordVectorizer {

    private Point[] thirds = {new Point(0, 0, 0), new Point(0, 0, 0)};
    private Word word;
    private Point[] ps;
    private static int angleIndex;

    public SmartVectorizer(Word w) {
        word = w;
        ps = word.getPoints();
        int third = (int) Math.round(Math.ceil((double) ps.length / 3));
        for (int i = 0; i < third; i++) {
            thirds[0] = thirds[0].plus(ps[i]);
        }
        thirds[0] = thirds[0].divide(third);
        for (int i = ps.length - third; i < ps.length; i++) {
            thirds[1] = thirds[1].plus(ps[i]);
        }
        thirds[1] = thirds[1].divide(third);
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
        int cn = 0;
        double[] angles = getTorsionAngles();
        //angles = new double[0]; // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        angleIndex = 2;
        double[] v = new double[angleIndex + angles.length];
        
        //TODO internal angles
        // skipping torsions and angles
        v[0] = getStraightness();
        v[1] = thirds[0].distance(thirds[1]);
        Random random = new Random(10);
        for (int i = 0; i < angles.length; i++) {
            v[angleIndex + i] = angles[i];
            //v[2 + i] = getCombo(random);
            //v[2 + i] = getSingle(i);
        }
        /*for (int i = 1; i <= 9; i++) {
        v[1 + i] = getStraightness(i);
        }*/
        return v;
    }

    public double[] getTorsionAngles() {
        double[] angles = new double[ps.length - 3];
        for (int i = 0; i < ps.length - 3; i++) {
            double a = TorsionAngle.torsionAngle(
                    ps[i], ps[i + 1], ps[i + 2], ps[i + 3]);
            angles[i] = a;
        }
        return angles;
    }

    public double getCombo(Random random) {
        double sum = 0;
        for (double d : word.getInternalDistances()) {
            sum += d * random.nextDouble();
        }
        return sum;
    }

    public double getSingle(int i) {
        return word.getInternalDistances()[i];
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
        return thirds[0];
    }

    public Point secondHalf() {
        return thirds[1];
    }

}
