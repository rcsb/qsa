package grid.sparse;

import java.util.Random;

/**
 *
 * @author Antonin Pavelka
 *
 * Proof of concept, to measure the speed. Or tradeoff accuracy / speed.
 *
 */
@Deprecated
public class VectorRangeSearch<T> {

	private MultidimensionalArray a;
	private float[] lo, hi;
	private int dim;
	private int bracketN = 10;
	private Buffer result;

	public VectorRangeSearch(float[][] vectors, T[] ts) {
		dim = vectors[0].length;
		lo = new float[dim];
		hi = new float[dim];
		for (int i = 0; i < ts.length; i++) {
			float[] v = vectors[i];
			for (int d = 0; d < dim; d++) {
				if (v[d] < lo[d]) {
					lo[d] = v[d];
				}
				if (v[d] > hi[d]) {
					hi[d] = v[d];
				}
			}
		}

		a = new MultidimensionalArray(ts.length, dim, bracketN);
		for (int i = 0; i < ts.length; i++) {
			float[] v = vectors[i];
			int[] index = vectorToIndex(v);
			a.insert(index, ts[i]);
		}

		result = new Buffer(a.size());
	}

	private int[] vectorToIndex(float[] v) {
		int[] index = new int[dim];
		for (int d = 0; d < dim; d++) {
			float range = hi[d] - lo[d];
			int i = (int) Math.floor((v[d] - lo[d]) / range * bracketN);
			if (i == bracketN) {
				i = bracketN - 1;
			}
			index[d] = i;
		}
		//for (int d = 0; d < dim; d++) {
		//	System.out.print(index[d] + " ");
		//}
		//System.out.println("");
		return index;
	}

	public void getRange(float[] low, float[] high) {
		int[] l = vectorToIndex(low);
		int[] h = vectorToIndex(high);
		a.getRange(l, h, result);
	}

	public Buffer getResult() {
		return result;
	}

	public void reset() {
		result.clear();
	}

	private static void manualTest() {
		float[][] vectors = {{0.09f, 0.11f}, {0.5f, 0.4f}, {0, 0}, {1, 1}};
		VectorRangeSearch vrs = new VectorRangeSearch(vectors, vectors);
		float[] low = {0.5f, 0.5f};
		float[] high = {1, 1};
		vrs.getRange(low, high);
	}

	// try on real data, impossible to tell if useful and when
	// how much would it take to do exhaustive on frags?
	private static void randomTest() {
		long t0 = System.nanoTime();
		int n = 10000;
		int dim = 100;
		Random random = new Random(1);
		float[][] vectors = new float[n][dim];
		for (int i = 0; i < n; i++) {
			for (int d = 0; d < dim; d++) {
				vectors[i][d] = random.nextFloat();
			}
		}
		VectorRangeSearch vrs = new VectorRangeSearch(vectors, vectors);
		float[] low = new float[dim];
		float[] high = new float[dim];
		for (int d = 0; d < dim; d++) {
			low[d] = 0.0f;
			high[d] = 0.9999f;
		}
		long t1 = System.nanoTime();
		// tree search
		vrs.getRange(low, high);
		long t2 = System.nanoTime();
		System.out.println("tree hit " + vrs.result.size());

		// exhaustive search
		int hit = 0;
		for (int i = 0; i < n; i++) {
			float[] v = vectors[i];
			boolean in = true;
			for (int d = 0; d < dim; d++) {
				if (low[d] > v[d] || v[d] > high[d]) {
					in = false;
				}
			}
			if (in) {
				hit++;
			}
		}
		long t3 = System.nanoTime();
		System.out.println("exhaustive hit " + hit);

		System.out.println("init time " + (t1 - t0) / 1000000);
		System.out.println("tree time " + (t2 - t1) / 1000000);
		System.out.println("exhaustive time " + (t3 - t2) / 1000000);

	}

	public static void main(String[] args) {
		randomTest();
	}
}
