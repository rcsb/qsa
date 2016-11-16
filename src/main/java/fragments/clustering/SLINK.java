package fragments.clustering;

import java.util.Random;

//The SLINK algorithm for Agglomerative Hierarchical Clustering in O(n^2) time and O(n) space.
public class SLINK {
	public static final class SLINKClusteringResult {
		public final double[] height;
		public final int[] parent;

		public SLINKClusteringResult(double[] height, int[] parent) {
			this.height = height;
			this.parent = parent;
		}

		public void print() {
			for (int i : parent)
				System.out.print(i + " ");
			System.out.println();
			for (double d : height)
				System.out.print(d + " ");
		}
	}

	public static SLINKClusteringResult slink(Matrix distance) {
		int size = distance.size();
		double[] height = new double[size];
		int[] parent = new int[size];
		double[] distanceN = new double[size];
		for (int n = 0; n < size; n++) {
			// Step 1
			parent[n] = n;
			height[n] = Double.MAX_VALUE;
			// Step 2
			for (int i = 0; i < n; i++) {
				distanceN[i] = distance.apply(i, n);
			}
			// Step 3
			for (int i = 0; i < n; i++) {
				if (height[i] >= distanceN[i]) {
					distanceN[parent[i]] = Math.min(distanceN[parent[i]], height[i]);
					height[i] = distanceN[i];
					parent[i] = n;
				} else {
					distanceN[parent[i]] = Math.min(distanceN[parent[i]], distanceN[i]);
				}
			}
			// Step 4
			for (int i = 0; i < n; i++) {
				if (height[i] >= height[parent[i]]) {
					parent[i] = n;
				}
			}
		}
		return new SLINKClusteringResult(height, parent);
	}

	public static void main(String[] args) {
		SLINKClusteringResult r = slink(new Matrix(10));
		r.print();
	}
}

class Matrix {

	private int[] a = { 3, 6, 7, 9 };

	public Matrix(int n) {
		// a = //new int[n];
		/*
		 * Random rand = new Random(1); for (int i = 0; i < n; i++) { a[i] =
		 * rand.nextInt(100); System.out.print(a[i] + " "); }
		 */
		
		for (int i : a) {
			System.out.print(i + " ");
		}
		System.out.println();

	}

	public double apply(int x, int y) {
		return Math.abs(a[x] - a[y]);
	}

	public int size() {
		return a.length;
	}
}