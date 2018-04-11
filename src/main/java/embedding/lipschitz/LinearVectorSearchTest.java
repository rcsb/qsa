package embedding.lipschitz;

import util.Timer;

/**
 *
 * Measures the speed of vector comparisons in case of CATH search with average number of biword per domain 600.
 * 
 */
public class LinearVectorSearchTest {

	public static void main(String[] args) {
		int dim = 10;
		int bw = 600;
		int domains = 6000;
		float[][] database = new float[bw * domains][dim];
		float[][] query = new float[bw][dim];

		Timer.start();
		for (int x = 0; x < query.length; x++) {
			for (int y = 0; y < database.length; y++) {
				float[] q = query[x];
				float[] db = database[y];
				int max = 0;
				for (int d = 0; d < dim; d++) {
					double dist = Math.abs(q[d] - db[d]);
					if (dist > max) {
						dist = max;
					}
				}
			}
		}
		Timer.stop();
		System.out.println(" " + Timer.get());
	}
}
