package algorithm;

import java.util.Arrays;
import java.util.Random;
import util.Timer;

/**
 *
 * @author kepler
 */
public class ClusterMerging {

    Random random = new Random(1);

    int[] generateIds(int n) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = random.nextInt(1000);
        }
        return a;
    }

    void run() {
        int an = 1000 * 1000 * 100;
        int bn = 1000 * 100 * 100;
        //int an = 5;
        //int bn = 3;
        int[] as = generateIds(an);
        int[] bs = generateIds(bn);
        int[][] cs = {as, bs};
        Arrays.sort(cs[0]);
        Arrays.sort(cs[1]);
        Timer.start();
        int[] pointers = {0, 0};
        int y = 0;
        int primary = cs[0][0] <= cs[1][0] ? 0 : 1;
        int secondary = 1 - primary;        
        while (pointers[0] < an && pointers[1] < bn) {

            //System.out.println(pointers[0] + " " + pointers[1]);

            while (pointers[0] < an && pointers[1] < bn
                    && cs[primary][pointers[primary]] <= cs[secondary][pointers[secondary]]) { // todo solve = after this                
                pointers[primary]++;
            }
            primary = secondary;
            secondary = 1 - primary;
        }
        Timer.stop();
        System.out.println("time " + Timer.get());
    }

    public static void main(String[] args) {
        ClusterMerging cm = new ClusterMerging();
        cm.run();
    }

}
