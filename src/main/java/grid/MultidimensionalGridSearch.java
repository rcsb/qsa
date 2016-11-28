package grid;

import geometry.Coordinates;

public class MultidimensionalGridSearch<T extends Coordinates> {

    private final double[] range;
    private final Object[][] cells;
    private final int[] bins;
    private final double[] origin;
    private final double[] edge;
    private final int[] segments;
    private final int dim;
    private final int cellN;
    private final int[] boxIndexes;
    private final int boxNumber;

    public MultidimensionalGridSearch(double[] min, double[] max, double[] range) {
        dim = min.length;
        origin = new double[dim];
        for (int i = 0; i < dim; i++) {
            // creating empty zone to prevent underflows under zero
            origin[i] = min[i] - range[i];
            /* overflows are ignored, you just get some objects from cells
             on the other size, but filtering is needed anyway, so who cares */
        }
        this.range = range;
        segments = new int[dim];
        edge = new double[dim];
        bins = new int[dim];
        int cellNumber = 1;
        int bn = 1;
        for (int i = 0; i < dim; i++) {
            segments[i] = 3; // odd numbers only, 1 possible
            edge[i] = 2 * range[i] / (segments[i] - 1);
            bins[i] = (int) Math.round(Math.ceil((max[i] - origin[i]) / edge[i]));
            cellNumber *= bins[i];
            bn *= segments[i];
        }
        this.boxNumber = bn;
        if (cellNumber > Integer.MAX_VALUE - 5) {
            throw new RuntimeException("Grid is too big.");
        }
        cellN = (int) cellNumber;
        cells = new Object[cellN][];
        boxIndexes = new int[bn];
        int[] pointer = new int[dim];
        for (int i = 0; i < bn; i++) {
            boxIndexes[i] = index(pointer);
            pointer[0]++;
            int j = 1;
            while (j < dim && pointer[j - 1] >= segments[j - 1]) {
                pointer[j - 1] = 0;
                pointer[j]++;
                j++;
            }
        }
    }

    private int[] discrete(double[] c) {
        int[] x = new int[dim];
        for (int i = 0; i < dim; i++) { // Math.floor
            x[i] = (int) ((long) ((c[i] - origin[i]) / edge[i]));
        }
        return x;
    }

    private int[] discreteCorner(double[] c) {
        int[] x = new int[dim];
        for (int i = 0; i < dim; i++) { // Math.floor
            x[i] = (int) ((long) ((c[i] - range[i] - origin[i]) / edge[i]));
        }
        return x;
    }

    private int index(int[] x) {
        int index = 0;
        for (int i = dim - 1; i > 0; i--) {
            index += x[i];
            index *= segments[i - 1];
        }
        index += x[0];
        return index;
    }

    public void add(T t) {
        int i = index(discrete(t.getCoords()));
        Object[] bucket = cells[i];
        if (bucket == null) {
            Object[] a = {t};
            cells[i] = a;
        } else {
            Object[] a = new Object[bucket.length + 1];
            System.arraycopy(bucket, 0, a, 0, bucket.length);
            a[bucket.length] = t;
            cells[i] = a;
        }
    }

    public void addAll(T[] ts) {
        for (T t : ts) {
            add(t);
        }
    }

    public void search(Coordinates q, Processor processor) {
        double[] c = q.getCoords(); // TODO global
        int[] x = discreteCorner(c);
        int shift = index(x);
        for (int i = 0; i < boxNumber; i++) {
            int j = shift + boxIndexes[i];
            if (j < cells.length) {
                Object[] bucket = cells[j];
                if (null != bucket) {
                    for (Object t : bucket) {
                        processor.process(t);
                    }
                }
            }
        }
    }
}
