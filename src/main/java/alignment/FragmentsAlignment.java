package alignment;

import javax.vecmath.Matrix4d;

import pdb.PdbChain;
import spark.interfaces.Alignment;

/**
 *
 * @author Antonin Pavelka
 */
public class FragmentsAlignment implements Alignment {

    private PdbChain a_, b_;
    private double[] metrics_;
    private static String s = ",";
    private Matrix4d transformation_;

    public FragmentsAlignment(PdbChain a, PdbChain b, Matrix4d transformation,
            double[] metrics) {
        a_ = a;
        b_ = b;
        transformation_ = transformation;
        metrics_ = metrics;
    }

    public PdbChain getA() {
        return a_;
    }

    public PdbChain getB() {
        return b_;
    }

    public String getLine() {
        StringBuilder sb = new StringBuilder(a_.toString()).append(s);
        sb.append(b_.toString()).append(s);
        for (double d : metrics_) {
            sb.append(d).append(s);
        }
        return sb.toString();
    }

    public Matrix4d getTransformation() {
        return transformation_;
    }

}
