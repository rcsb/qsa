package fragments.range;

import java.util.Random;
import vectorization.ErrorMatrix;

/*
 * Vectors labeled either as negative or positive.
 */
public class VectorData {

    private final int N = 0;
    private final int P = 1;
    private final double[][][] data; // [0-1][datset size][vector size]
    private double[] hi;
    private final int dim;
    private final int[] n;
    private int[] t;
    private int[] f;

    public VectorData(double[][] negatives, double[][] positives) {
        data = new double[2][][];
        data[N] = negatives;
        data[P] = positives;
        n = new int[2];
        n[P] = data[P].length;
        n[N] = data[N].length;
        dim = data[P][0].length;
        hi = new double[dim];
        for (int d = 0; d < dim; d++) {
            hi[d] = Double.NEGATIVE_INFINITY;
        }
        t = new int[2];
        f = new int[2];
        findMax();
        System.out.println("dimensions " + dim);
    }

    private void findMax() {
        for (int d = 0; d < dim; d++) {
            for (int k = 0; k < 2; k++) {
                for (int i = 0; i < n[k]; i++) {
                    double c = data[k][i][d];
                    if (c > hi[d]) {
                        hi[d] = c;
                    }
                }
            }
        }
        for (int d = 0; d < dim; d++) {
            System.out.println("max " + hi[d]);
        }
    }

    public boolean inside(double[] box, double[] vector) {
        boolean inside = true;
        for (int d = 0; d < dim; d++) {
            double c = vector[d];
            double h = box[d];
            inside &= c <= h;
        }
        return inside;
    }

    public void optimize() {
        double delta = 0.1;
        Random random = new Random();
        double[] box = new double[dim];
        ErrorMatrix best = null;
        double[] bb = new double[dim];
        for (int d = 0; d < dim; d++) {
            bb[d] = Double.POSITIVE_INFINITY;
        }
        for (int i = 0; i < 10000000; i++) {
            for (int d = 0; d < dim; d++) {
                //box[d] = (1 - delta * (random.nextDouble() - 0.5)) * hi[d];
                box[d] = random.nextDouble() * hi[d];
            }
            ErrorMatrix em = evaluate(box);
            if (em.missing() < 0.2 && (best == null || em.inside() <= best.inside())) {
                best = em;
                for (int d = 0; d < dim; d++) {
                    bb[d] = Math.min(bb[d], box[d]);
                    hi[d] = bb[d] * 3;
                }
                System.out.print(n(em.inside()) + " " + n(em.specificity())
                        + " " + n(em.missing()) + " " + n(em.p()) + " | ");
                for (int d = 0; d < dim; d++) {
                    System.out.print(nn(box[d]) + " ");
                }
                System.out.println();
            }
        }
    }

    private String nice(int v, int max) {
        return "" + ((long) v * 10000 / max);
    }

    private String n(double v) {
        return "" + (double) (Math.round(v * 1000)) / 10;
    }
    
    private String nn(double v) {
        return "" + (double) (Math.round(v * 100)) / 100;
    }

    public ErrorMatrix evaluate(double[] box) {
        int total = 0;
        t = new int[2];
        f = new int[2];
        for (int k = 0; k < 2; k++) {
            for (int i = 0; i < n[k]; i++) {
                double[] v = data[k][i];
                total++;
                if (inside(box, v)) {
                    if (k == P) {
                        t[P]++;
                    } else {
                        f[P]++;
                    }
                } else {
                    if (k == P) {
                        f[N]++;
                    } else {
                        t[N]++;
                    }

                }
            }
        }
        //System.out.println("box similar: " + nice(t[P], total));
        //System.out.println("out disimilar: " + nice(t[N], total));
        //System.out.println("box disimilar: " + nice(f[P], total));
        //System.out.println("out similar: " + nice(f[N], total));
        ErrorMatrix em = new ErrorMatrix(t[P], t[N], f[P], f[N]);

        double efficiency = (double) (t[P] + f[P]) / total;
        //System.out.println("eff " + efficiency);
        return em;
    }
}
