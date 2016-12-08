package fragments.range;

import java.util.Random;

/*
 * Vectors labeled either as negative or positive.
 */
public class VectorData {

    private final int N = 0;
    private final int P = 1;
    private final double[][][] data; // [0-1][datset size][vector size]
    private final double[] hi;
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
        findMax();
        Random random = new Random(1);
        double[] box = new double[dim];
        for (int i = 0; i < 1000; i++) {
            System.out.print("box ");
            for (int d = 0; d < dim; d++) {
                box[d] = random.nextDouble() * hi[d];
                System.out.print(box[d] + " ");
            }
            System.out.println("");
            accuracy(box);
        }
    }

    private String nice(int v, int max) {
        return "" + ((long) v * 10000 / max);
    }

    public void accuracy(double[] box) {
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
        System.out.println("box similar: " + nice(t[P], total));
        System.out.println("out disimilar: " + nice(t[N], total));
        System.out.println("box disimilar: " + nice(f[P], total));
        System.out.println("out similar: " + nice(f[N], total));
        
        double efficiency = (double) (t[P] + f[P]) / total;
        System.out.println("eff " + efficiency);
    }
}
