package vectorization;

import fragments.Word;
import fragments.WordVectorizer;
import geometry.Point;

public class SmartVectorizer implements WordVectorizer {

    private Point[] thirds = {new Point(0, 0, 0), new Point(0, 0, 0)};
    private Word word;
    private Point[] ps;

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
    
    @Override
    public double[] getVector() {
        double[] v = new double[2];
        v[0] = getStraightness();
        v[1] = thirds[0].distance(thirds[1]);
        return v;
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

    public Point firstHalf() {
        return thirds[0];
    }

    public Point secondHalf() {
        return thirds[1];
    }

}
