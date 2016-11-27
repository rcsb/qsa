package grid;

import geometry.Coordinates;
import geometry.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import util.Timer;

public class MultidimensionalGridSearch<T extends Coordinates> {

    private final double[] range;
    private final Bucket[] cells;
    private final int[] bins;
    private final double[] min;
    private final double[] max;
    private final double[] edge;
    private final int[] segments;
    private final int dim;
    private final int cellN;
    private final int[] boxIndexes;
    private final int boxNumber;
    
    public MultidimensionalGridSearch(double[] min, double[] max, double[] range) {
        dim = min.length;
        this.min = min;
        this.max = max;
        this.range = range;
        segments = new int[dim];
        edge = new double[dim];
        bins = new int[dim];
        int cellNumber = 1;
        int bn = 1;
        for (int i = 0; i < dim; i++) {
            segments[i] = 3; // odd numbers only, 1 possible
            edge[i] = 2 * range[i] / segments[i];
            bins[i] = (int) Math.round(Math.ceil((max[i] - min[i]) / edge[i]));
            cellNumber *= bins[i];
            bn *= segments[i];
        }
        this.boxNumber = bn;
        if (cellNumber > Integer.MAX_VALUE - 5) {
            throw new RuntimeException("Grid is too big.");
        }
        cellN = (int) cellNumber;
        cells = new Bucket[cellN];
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
        for (int i = 0; i < dim; i++) {
            x[i] = (int) Math.round(Math.floor(c[i] - range[i] - min[i]) / edge[i]); // TODO casting (long) from double? Double.intValue?
        }
        return x;
    }

    private int[] indexes() {

    }

    private int index(int[] x) {
        int index = 0;
        for (int i = dim - 1; i > 0; i--) {
            index += x[i];
            index *= bins[i - 1];
        }
        index += x[0];
        return index;
    }

    public void add(T t) {
        int i = index(discrete(t.getCoords()));
        Bucket bucket = cells[i];
        if (bucket == null) {
            bucket = new Bucket();
            cells[i] = bucket;
        } 
        bucket.add(t);

    }

   // public void consolidate() {
     //   bla; // TODO replace Buckets with arrays, return array of arrays
    //}
todo solve when any coordinate is smaller than origin
    public void search(Coordinates q) {
        double[] c = q.getCoords(); // TODO global
        int[] x = discrete(c);
        int shift = index(x);
        for (int i = 0; i < boxNumber; i++) {
            Bucket bucket = cells[shift + boxIndexes[i]];
        }
    }

    private static double r(Random random) {
        return (random.nextDouble()) * 30;
    }

    // 
    // TODO make it dynamical
    // general dimensions
    // test speed of types, counter instead
    public static void test() {
        int in = 50000;
        int out = 100000;
        List<Point> as = new ArrayList<>();
        List<Point> bs = new ArrayList<>();
        double[] min = {0, 0, 0};
        double[] max = {100, 100, 100};
        double[] range = {10, 10, 10};
        MultidimensionalGridSearch<Point> s = new MultidimensionalGridSearch<>(min, max, range);
        Random random = new Random(1);
        for (int i = 0; i < in; i++) {
            Point p = new Point(r(random), r(random), r(random));
            as.add(p);
        }
        for (int i = 0; i < out; i++) {
            bs.add(new Point(r(random), r(random), r(random)));
        }
        Timer.start();
        Timer.stop();
        System.out.println("build " + Timer.get());

        for (int j = 0; j < 5; j++) {
            Timer.start();
            for (int i = 0; i < bs.size(); i++) {
                 s.search(bs.get(i));
            }
            Timer.stop();
            System.out.println("search " + Timer.get());
        }
    }

    public static void main(String[] args) {
        test();
    }
}
