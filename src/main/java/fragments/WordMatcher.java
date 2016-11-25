package fragments;

import geometry.Transformer;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Matrix3d;

public class WordMatcher {

    private Word[] as, bs;
    private double[][] m;
    private Matrix3d[][] t;
    private boolean transformation;
    private double maxRmsd;
    private int tc = 0;
    private List<Awp> alignedWords;

    public WordMatcher(Word[] as, Word[] bs, boolean transformation, double maxRmsd) {
        this.as = as;
        this.bs = bs;
        m = new double[as.length][bs.length];
        t = new Matrix3d[as.length][bs.length];
        this.transformation = transformation;
        this.maxRmsd = maxRmsd;
        if (transformation) {
            alignedWords = new ArrayList<>();
        }
        run();
    }

    private void check() {
        for (int i = 0; i < as.length; i++) {
            if (as[i].getId() != i) {
                throw new RuntimeException();
            }
        }
        for (int i = 0; i < bs.length; i++) {
            if (bs[i].getId() != i) {
                throw new RuntimeException();
            }
        }
    }

    private final void run() {
        Transformer tr = new Transformer();
        check();
        for (int ia = 0; ia < as.length; ia++) {
            for (int ib = 0; ib < bs.length; ib++) {
                tr.set(as[ia].getPoints3d(), bs[ib].getPoints3d());
                double rmsd = tr.getRmsd();
                m[ia][ib] = rmsd;
                if (rmsd <= maxRmsd) {
                    Matrix3d m = tr.getRotationMatrix();
                    t[ia][ib] = m;
                    if (alignedWords != null) {
                        alignedWords.add(new Awp(as[ia], bs[ib], rmsd, m));
                    }
                    tc++;
                }
            }
        }
    }

    public List<Awp> getAlignedWords() {
        return alignedWords;
    }

    public int getTs() {
        return tc;
    }

    public double getRmsd(int a, int b) {
        return m[a][b];
    }
}
