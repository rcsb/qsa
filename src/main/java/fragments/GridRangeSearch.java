package fragments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * This class implements a structure containing set of points and allows 
 * efficient identification of the points in a sphere.
 */
public class GridRangeSearch<T extends Coordinates> {

    double d;
    double[] ds;
    Bucket[][][] cells;
    double[] origin = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};

    public GridRangeSearch(double d) {
        // d - size of cell
        this.d = d;
    }
    
      public GridRangeSearch(double[] ds) {
        // ds - size of cells in individual dimensions
        this.ds = ds;
    }

    private int index(double zero, double value) {
        return (int) Math.round((float) (value - zero) / d);
    }

    public void buildGrid(Collection<T> ts) {
        double[] max = {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
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
        cells = new Bucket[index(origin[0], max[0]) + 1][index(origin[1], max[1]) + 1][index(origin[2], max[2]) + 1];
        for (T t : ts) {
            int[] g = new int[3];
            for (int i = 0; i < 3; i++) {
                g[i] = index(this.origin[i], t.getCoords()[i]);
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
    }

    /**
     * @return distance to the nearest point in the sphere centered at 
     * {@code q} of radius {@code  r}.
     * @param q  center of the query sphere
     * @param r  radius of the sphere     
     */
    // BECAUSE TODO: continue out of loops
    @Deprecated    
    public List<T> nearest(Coordinates q, double r) {
        List<T> result = new ArrayList<>();
        double sq_r = r * r;
        double[] low = {q.getCoords()[0] - r, q.getCoords()[1] - r, q.getCoords()[2] - r};
        double[] high = {q.getCoords()[0] + r, q.getCoords()[1] + r, q.getCoords()[2] + r};
        int[] lo = {index(origin[0], low[0]), index(origin[1], low[1]), index(origin[2], low[2])};
        int[] hi = {index(origin[0], high[0]), index(origin[1], high[1]), index(origin[2], high[2])};
        for (int x = lo[0]; x <= hi[0]; x++) {
            if (x < 0 || cells.length <= x) {
                continue;
            }
            for (int y = lo[1]; y <= hi[1]; y++) {
                if (y < 0 || cells[x].length <= y) {
                    continue;
                }
                for (int z = lo[2]; z <= hi[2]; z++) {
                    if (z < 0 || cells[x][y].length <= z) {
                        continue;
                    }
                    Bucket bucket = cells[x][y][z];
                    if (bucket != null) {
                        for (Object o : bucket) {
                            T t = (T) o;
                            double sq_d = 0;
                            double[] a = q.getCoords();
                            double[] b = t.getCoords();

                            for (int i = 0; i < 3; i++) {
                                double diff = a[i] - b[i];
                                sq_d += diff * diff;
                            }
                            if (sq_d <= sq_r) {
                                result.add(t);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
}

class Bucket implements Iterable {

    List list = new ArrayList<>();

    public void add(Object t) {
        list.add(t);
    }

    @Override
    public Iterator iterator() {
        return list.iterator();
    }
}
