package geometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

class Bucket implements Iterable {

    List list = new ArrayList<>();

    public void add(Object t) {
        list.add(t);
    }

    public Iterator iterator() {
        return list.iterator();
    }
}

public class GridRangeSearch<T extends Coordinates> {

    double d;
    Bucket[][][] cells;
    Random random = new Random(1);
    double[] origin = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};

    public GridRangeSearch(double d) {
        // d - size of cell
        this.d = d;

    }

    private int index(double zero, double value) {
        return (int) Math.round((float) (value - zero) / d);
    }

    private int[] point_to_index(double[] p) {
        int[] index = new int[3];
        for (int i = 0; i < 3; i++) {
            index[i] = index(origin[i], p[i]);
            //index.append(self.index(self.origin[i], p[i]));
        }
        return index;
    }

    public void buildGrid(T[] ts) {
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

    public List<T> search(Coordinates q, double r) {
        double sq_r = r * r;
        List<T> hits = new ArrayList<T>();
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
                                hits.add(t);
                            }
                        }
                    }
                }
            }
        }
        return hits;
    }
    /*
     private double rand():
     return (random.random() * 2 - 1) * 50

     def main():
     g = Grid(5, 9)
     points = []
     for i in range(1000):
     points.append((rand(), rand(), rand()))
     g.build_grid(points)

     for i in range(10000):
     l = g.search((10, 0, 0))

     if __name__ == "__main__":
     main()

     */
}
