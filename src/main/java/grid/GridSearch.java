package grid;

import geometry.Coordinates;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class implements a structure containing set of points and allows
 * efficient identification of the points in a sphere.
 */
public class GridSearch<T extends Coordinates> {

    private final double[] ds;
    private final double[] diffs;
    private Bucket[][][] cells;
    private final double[] origin = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
    private final double[] max = {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
    private final int[] lengths = new int[3];
    private int l1, l2, l3;

    public GridSearch(double[] sizes, double[] diffs) {
        // ds - size of cells in individual dimensions
        this.ds = sizes;
        this.diffs = diffs;
    }

    private int index(double zero, double value, int dim) {
        int i = (int) Math.round((float) (value - zero) / ds[dim]);
        //if (i >= lengths[dim]) {
        //    i = lengths[dim] - 1;
        //}
        //if (i < 0) {
        //    i = 0;
        //}
        return i;
    }

    public void buildGrid(Collection<T> ts) {
        for (T t : ts) {
            for (int i = 0; i < 3; i++) {
                if (t.getCoords()[i] < this.origin[i]) {
                    this.origin[i] = t.getCoords()[i];
                }
                if (max[i] < t.getCoords()[i]) {
                    max[i] = t.getCoords()[i];
                }
            }
        }
        cells = new Bucket[index(origin[0], max[0], 0) + 1][index(origin[1], max[1], 1) + 1][index(origin[2], max[2], 2) + 1];
        for (T t : ts) {
            int[] g = new int[3];
            for (int i = 0; i < 3; i++) {
                g[i] = index(this.origin[i], t.getCoords()[i], i);
            }
            Bucket bucket;
            if (cells[g[0]][g[1]][g[2]] == null) {
                bucket = new Bucket();
                cells[g[0]][g[1]][g[2]] = bucket;
            } else {
                bucket = cells[g[0]][g[1]][g[2]];
            }
            bucket.add(t);
        }
        lengths[0] = cells.length - 1;
        lengths[1] = cells[0].length - 1;
        lengths[2] = cells[0][0].length - 1;
        l1 = cells.length - 1;
        l2 = cells[0].length - 1;
        l3 = cells[0][0].length - 1;
    }

    /**
     * @return distance to the nearest point in the sphere centered at {@code q}
     * of radius {@code  r}.
     * @param q center of the query sphere
     * @param r radius of the sphere
     */
    public List<T> nearest(Coordinates q) {
        List<T> result = new ArrayList<>();
        double[] c = q.getCoords();
        double[] low = {c[0] - diffs[0], c[1] - diffs[1], c[2] - diffs[2]};
        double[] high = {c[0] + diffs[0], c[1] + diffs[1], c[2] + diffs[2]};

        /*for (int i = 0; i < 3; i++) {
            if (low[i] > max[i]) {
                low[i] = max[i];
            } else if (low[i] < origin[i]) {
                low[i] = origin[i];
            }
            if (high[i] > max[i]) {
                high[i] = max[i];
            } else if (high[i] < origin[i]) {
                high[i] = origin[i];
            }
        }*/
        int[] lo = {index(origin[0], low[0], 0), index(origin[1], low[1], 1), index(origin[2], low[2], 2)};
        int[] hi = {index(origin[0], high[0], 0), index(origin[1], high[1], 1), index(origin[2], high[2], 2)};

        for (int i = 0; i < 3; i++) {
            if (lo[i] > lengths[i]) {
                lo[i] = lengths[i];
            } else if (lo[i] < 0) {
                lo[i] = 0;
            }
            if (hi[i] > lengths[i]) {
                hi[i] = lengths[i];
            } else if (high[i] < 0) {
                high[i] = 0;
            }
        }

		// save boundaries
		// manual fors: x++, if too high y++ etc., lowest dimension is bucket
        for (int x = lo[0]; x <= hi[0]; x++) {
            for (int y = lo[1]; y <= hi[1]; y++) {
                for (int z = lo[2]; z <= hi[2]; z++) {
                    Bucket bucket = cells[x][y][z];
                    if (bucket != null) {
                        for (Object o : bucket) {
                            T t = (T) o;
                            //double[] oc = t.getCoords();
                            //if (Math.abs(c[0] - oc[0]) <= diffs[0]
                            //        && Math.abs(c[1] - oc[1]) <= diffs[1]
                            //        && Math.abs(c[2] - oc[2]) <= diffs[2]) {
                            result.add(t);
                            //}
                        }
                    }
                }
            }
        }
        return result;
    }
}
