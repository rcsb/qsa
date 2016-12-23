package grid.sparse;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import util.Timer;

/**
 *
 * @author kepler
 *
 * A multidimensional array, suporting retrieval of area specified by range of
 * individual coordinates.
 */
public class SparseGrid<T> {

    private FullArray tree;
    private int[] ranges;
    private Buffer<FullArray> levelA, levelB;
    private Buffer<T> result;

    public SparseGrid(int[] ranges, int n) {
        tree = new FullArray(ranges[0]);
        this.ranges = ranges;
        levelA = new Buffer(n);
        levelB = new Buffer(n);
        result = new Buffer(n);
    }

    private void insert(int[] coords, T t) {
        FullArray x = tree;
        Bucket<T> bucket = null;
        for (int d = 0; d < coords.length; d++) {
            int c = coords[d];
            Object y = x.get(c);
            if (d < coords.length - 1) {
                if (y == null) {
                    y = new FullArray(ranges[d]);
                    x.put(c, y);
                }
                x = (FullArray) y;
            } else {
                if (y == null) {
                    y = new Bucket();
                    x.put(c, y);
                }
                bucket = (Bucket) y;
            }
        }
        bucket.add(t);
    }

    /*
    TODO 
    create buffer size of elements for retrieval instead of list
    for both result and y
    try Arrrays binary search and system of two arrays - sparse index and objects
    convert from maps in the end
    because now too many nulls, but does it matter?
     */
    public Buffer<T> getRange(int[] lo, int[] hi) {
        levelA.add(tree);
        for (int d = 0; d < lo.length; d++) {
            int l = lo[d];
            int h = hi[d];
            for (int i = 0; i < levelA.size(); i++) {
                FullArray a = levelA.get(i);
                a.getRange(l, h, levelB);
            }
            if (d < lo.length - 1) {
                if (levelB.isEmpty()) {
                    return null;
                }
                Buffer b = levelA;
                levelA = levelB;
                levelB = levelA;
                levelB.clear();
            } else {
                result.clear();
                for (int i = 0; i < levelB.size(); i++) {
                    Object o = levelB.get(i);
                    Bucket b = (Bucket) o;
                    result.addAll(b.get());
                }
                return result;
            }
        }
        throw new IllegalStateException();
    }
    
    public static void main(String[] args) {
        int dim = 25;
        int[] ranges = new int[dim];
        for (int d = 0; d < dim; d++) {
            ranges[d] = 110;
        }
        int n = 1000 * 1000;
        int q = 1000;
        int cubeSize = 100;
        Random random = new Random(1);
        SparseGrid<Integer> g = new SparseGrid<>(ranges, n);
        Timer.start();
        for (int i = 0; i < n; i++) {
            int[] c = new int[dim];
            for (int d = 0; d < dim; d++) {
                c[d] = random.nextInt(cubeSize);
            }
            g.insert(c, i);
        }
        Timer.stop();
        System.out.println("creation: " + Timer.get());
        Timer.start();
        for (int i = 0; i < q; i++) {
            int[] lo = new int[dim];
            int[] hi = new int[dim];
            for (int d = 0; d < dim; d++) {
                lo[d] = random.nextInt(cubeSize);
                hi[d] = lo[d] + 5;
            }
            Buffer<Integer> r = g.getRange(lo, hi);
            //System.out.println(r.size());
        }
        Timer.stop();
        System.out.println("quering " + Timer.get());
        
       // TODO test korektnosti, pak zkusit aplikovat na fragmenty, treba to bude stacit
    }
}
