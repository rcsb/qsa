/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grid;

import geometry.Point;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import util.Timer;

public class GridTest {

    double[] min = {50, 50, 50};
    double[] max = {100, 100, 100};
    double[] range = {10, 10, 10};
    Random random = new Random(1);
    //   int in = 100000;
    //  int out = 5000000;
    int in = 10000;
    int out = 10000;

    private Point[] createSet(int n) {
        Point[] l = new Point[n];
        for (int i = 0; i < n; i++) {
            Point p = r();
            l[i] = p;
        }
        return l;
    }

    private double range(int i) {
        return random.nextDouble() * (max[i] - min[i]) + min[i];
    }

    private Point r() {
        double[] c = new double[3];
        for (int i = 0; i < c.length; i++) {
            c[i] = range(i);
        }
        return new Point(c);
    }

    private Grid<Point> createGrid(Point[] set) {
        Timer.start();
        Grid<Point> s = new Grid<>(min, max, range);
        s.addAll(set);
        Timer.stop();
        System.out.println("build " + Timer.get());
        return s;
    }

    // TODO make it dynamical
    // general dimensions
    // test speed of types, counter instead
    public void testSpeed() {
        Point[] a = createSet(in);
        Point[] b = createSet(out);
        Grid<Point> s = createGrid(a);
        Timer.start();
        long count = 0;
        for (int y = 0; y < b.length; y++) {
            BufferProcessor processor = new BufferProcessor();
            s.search(b[y], processor);
        }
        Timer.stop();
        System.out.println(
                "search " + Timer.get());
        System.out.println("count " + count);
    }

    public boolean inBox(Point x, Point y) {
        Point z = x.minus(y);
        double[] c = z.getCoords();
        for (int i = 0; i < c.length; i++) {
            if (Math.abs(c[i]) > range[i]) {
                return false;
            }
        }
        return true;
    }

    public void testCorectness() {
        Point[] a = createSet(in);
        Point[] b = createSet(out);
        Grid<Point> s = createGrid(a);

        Timer.start();

        //System.exit(0);
        for (int y = 0; y < b.length; y++) {
            SetProcessor processor = new SetProcessor();
            s.search(b[y], processor);
            int ok = 0;
            int missing = 0;
            for (int x = 0; x < a.length; x++) {
                if (inBox(a[x], b[y])) {
                    if (!processor.contains(a[x])) {
                        //  System.err.println("!!!");
                        // System.err.println(a[x]);
                        // System.err.println(b[y]);
                        missing++;
                    } else {
                        ok++;
                        //   System.err.println("+++");
                        //  System.err.println(a[x]);
                        // System.err.println(b[y]);
                    }
                }
            }
            if (missing > 0) {
                System.out.println(ok + " ? " + missing);
                System.out.println(b[y]);
            }
        }

        Timer.stop();

        System.out.println(
                "search " + Timer.get());
    }

    public static void main(String[] args) {
        GridTest m = new GridTest();
        m.testSpeed();
        m.testCorectness();
    }
}
