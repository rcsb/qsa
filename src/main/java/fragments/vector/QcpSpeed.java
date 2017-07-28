package fragments.vector;

import geometry.Transformer;
import java.util.Random;
import javax.vecmath.Point3d;

public class QcpSpeed {

	private final Transformer transformer = new Transformer();

	public QcpSpeed(Point3d[][] objects) {
		long a = System.nanoTime();
		long count = 0;
		for (int x = 0; x < 10000; x++) {
			for (int y = 0; y < x; y++) {
				transformer.set(objects[x], objects[y]);
				transformer.getRmsd();
				count++;
			}
		}
		long b = System.nanoTime();

		System.out.println("QCP");
		System.out.println((b - a) / 1000000000);
		System.out.println((b - a) / count);
		System.out.println(1000L * 1000 * 1000 * count / (b - a) + " QCP per second");
		System.out.println("count " + count);
		System.out.println("total time " + (b - a));
		System.out.println("---");

	}

	public static void main(String[] args) {
		int n = 1000;
		int dim = 60;
		int[][] vectors = new int[n][dim];
		Random random = new Random(1);
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < dim; j++) {
				vectors[i][j] = Math.abs(random.nextInt()) / 10;
			}
		}
		long time1 = System.nanoTime();
		long count = 0;
		for (int x = 0; x < n; x++) {
			for (int y = 0; y < x; y++) {
				count++;
				int[] a = vectors[x];
				int[] b = vectors[y];
				int max = -9999999;
				for (int i = 0; i < dim; i++) {
					int d = a[i] - b[i];
					if (d < 0) {
						d = -d;
					}
					if (d > max) {
						max = d;
					}
				}
			}
		}
		
		long time2 = System.nanoTime();
		long time = time2-time1;

		System.out.println(time / 1000000000);
		System.out.println(time / count);
		System.out.println(1000L * 1000 * 1000 * count / time + " vectors per second");
		System.out.println("count " + count);
		System.out.println("total time " + time);
		System.out.println("---");
	}
}
