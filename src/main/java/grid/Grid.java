package grid;

import geometry.Coordinates;
import java.util.Arrays;

// probably dense grid
public class Grid<T extends Coordinates> {

    private final int dim; // dimensions
    private final double[] range; // how far to search in each dimension
    private final Object[][] cells;
    /**
     * Objects organized in orthogonal grid structure. Meaning of the first
     * coordinate of cells is defined by the method index(). The array cells[i]
     * contains all objects in given cell of the grid (a hyperbox or dimensions
     * determined by edge.
     */
    private final double[] min;
    private final double[] max;
    private final double[] origin;
    private final double[] edge; // length of edge of cells (hyperboxs)
    private final int[] segments; // how many cells to search in a dimension
    private final int[] boxIndexes; // cells to search
    private int underflow;
    private int overflow;
    long size;

    public Grid(double[] min, double[] max, double[] range) {
        dim = min.length;
        this.min = min;
        this.max = max;
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
        int[] bins = new int[dim];
        long cellNumber = 1;
        int bn = 1;
        for (int i = 0; i < dim; i++) {
            segments[i] = 3; // odd numbers only, 1 possible
            edge[i] = 2 * range[i] / (segments[i] - 1);
            bins[i] = (int) Math.round(Math.ceil((max[i] - origin[i]) / edge[i]));
            cellNumber *= bins[i];
            bn *= segments[i];
        }
        if (cellNumber > Integer.MAX_VALUE - 5) {
            throw new RuntimeException("Grid is too big.");
        }
        int cellN = (int) cellNumber;
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

    /**
     * Coordinates of the cell coresponding to accurate coordinates.
     */
    private int[] discrete(double[] c) {
        int[] x = new int[dim];
        for (int i = 0; i < dim; i++) { // Math.floor
            x[i] = (int) ((long) ((c[i] - origin[i]) / edge[i]));
        }
        return x;
    }

    /**
     * Cell in the "top left" of the cells to be searches.
     */
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

    /**
     * Efficiency of search and memory are considered to be more important than
     * construction speed, therefore the inefficient array copying here.
     */
    public void add(T t) {
        double[] c = t.getCoords();
        for (int i = 0; i < dim; i++) {
            if (c[i] < min[i]) {
                c[i] = min[i];
                underflow++;
                System.out.println("UNDERFLOW " + Arrays.toString(c));
            }
            if (c[i] > max[i]) {
                c[i] = max[i];
                overflow++;
                System.out.println("OVERRFLOW " + Arrays.toString(c));
            }
        }
        int i = index(discrete(c));
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
        size++;
    }

    public long size() {
        return size;
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
        for (int i = 0; i < boxIndexes.length; i++) {
            int j = shift + boxIndexes[i];
            if (j < cells.length) {
                Object[] bucket = cells[j];
                if (null != bucket) {
                    for (Object t : bucket) {
                        Coordinates r = (Coordinates) t;
                        double[] d = r.getCoords();
                        boolean hit = true;
                        for (int k = 0; k < dim; k++) {
                            if (Math.abs(c[k] - d[k]) > range[k]) {
                                hit = false;
                                break;
                            }
                        }
                        if (hit) {
                            processor.process(t);
                        }
                    }
                }
            }
        }
    }
}
