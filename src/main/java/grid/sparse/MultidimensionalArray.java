package grid.sparse;

import fragments.Filter;
import java.util.Random;
import util.Timer;

/**
 *
 * @author Antonin Pavelka
 *
 * A multidimensional array, suporting retrieval of area specified by range of individual coordinates.
 */
public class MultidimensionalArray<T> {

	private final Array tree;
	private Buffer levelA, levelB;
	private final Buffer<Bucket> buckets;
	private final int size;

	public MultidimensionalArray(int n) {
		//tree = new FullArray(ranges[0]);
		tree = new SparseArrayByMap();
		levelA = new Buffer(n);
		levelB = new Buffer(n);
		buckets = new Buffer<>(n);
		size = n;
	}

	public int size() {
		return size;
	}

	public void insert(int[] coords, T t) {
		Array x = tree;
		for (int d = 0; d < coords.length - 1; d++) {
			int c = coords[d];
			Array y = (Array) x.get(c);
			if (y == null) {
				//y = new FullArray(ranges[d]);
				y = new SparseArrayByMap();
				x.put(c, y);
			}
			x = y;
		}
		int c = coords[coords.length - 1];
		Object o = x.get(c);
		if (o == null) {
			Bucket<T> b = new Bucket();
			b.add(t);
			x.put(c, b);
		} else {
			Bucket<T> b = (Bucket<T>) o;
			b.add(t);
		}
	}

	public void getRange(int[] lo, int[] hi, Buffer<T> result) {
		int dim = lo.length;
		result.clear();
		levelA.clear();
		levelA.add(tree);
		for (int d = 0; d < dim - 1; d++) {
			int l = lo[d];
			int h = hi[d];
			levelB.clear();
			for (int i = 0; i < levelA.size(); i++) {
				Array a = (Array) levelA.get(i);
				a.getRange(l, h, levelB);
			}
			if (levelB.isEmpty()) {
				return;
			}
			Buffer b = levelA;
			levelA = levelB;
			levelB = b;
		}
		for (int i = 0; i < levelA.size(); i++) {
			Array<Bucket> bs = (Array<Bucket>) levelA.get(i);
			int l = lo[dim - 1];
			int h = hi[dim - 1];
			buckets.clear();
			bs.getRange(l, h, buckets);
			for (int j = 0; j < buckets.size(); j++) {
				Bucket<T> b = buckets.get(j);
				for (int k = 0; k < b.size(); k++) {
					T t = b.get(k);
					//if (true || filter.include(t)) {
					result.add(t);
					//}
				}
			}
		}
	}

	private static void test() {
		int dim = 10;
		int[] ranges = new int[dim];
		for (int d = 0; d < dim; d++) {
			ranges[d] = 10;
		}
		int n = 1000 * 1000;
		int q = 1000;
		int[][] lo = new int[q][dim];
		int[][] hi = new int[q][dim];
		MultiIndex[] elements = new MultiIndex[n];
		Random random = new Random(1);
		int range = 5;
		for (int i = 0; i < q; i++) {
			lo[i] = new int[dim];
			hi[i] = new int[dim];
			for (int d = 0; d < dim; d++) {
				lo[i][d] = random.nextInt(ranges[d] - range);
				hi[i][d] = lo[i][d] + range;
			}
		}
		MultidimensionalArray<MultiIndex> g = new MultidimensionalArray<>(n);
		Timer.start();
		for (int i = 0; i < n; i++) {
			int[] c = new int[dim];
			for (int d = 0; d < dim; d++) {
				c[d] = random.nextInt(ranges[d]);
			}
			MultiIndex mi = new MultiIndex(c);
			elements[i] = mi;
			g.insert(c, mi);
			//Buffer result = new Buffer(n);
			//g.getRange(c, c, result);
			//if (result.size() < 1) {
			//    System.out.println("  " + result.size());
			//    throw new RuntimeException();
			//}
		}
		Timer.stop();
		System.out.println(
			"creation: " + Timer.get());
		Timer.start();
		long found1 = 0;
		long found2 = 0;
		Buffer<MultiIndex> result = new Buffer(n);
		for (int i = 0; i < q; i++) {
			g.getRange(lo[i], hi[i], result);
			found1 += result.size();
			for (int k = 0; k < result.size(); k++) {
				MultiIndex mi = result.get(k);
				boolean in = true;
				for (int d = 0; d < dim; d++) {
					int e = mi.get()[d];
					if (e < lo[i][d] || hi[i][d] < e) {
						in = false;
						break;
					}
				}
				if (!in) {
					System.out.println("");
					for (int x = 0; x < dim; x++) {
						System.out.print(mi.get()[x] + " ");
					}
					System.out.println("");
					for (int x = 0; x < dim; x++) {
						System.out.print(lo[i][x] + " ");
					}
					System.out.println("");
					for (int x = 0; x < dim; x++) {
						System.out.print(hi[i][x] + " ");
					}
					System.out.println("");
					throw new RuntimeException();
				}
			}
		}
		Timer.stop();
		System.out.println(
			"found by tree " + found1);
		System.out.println(
			"quering " + Timer.get());

		if (true) {
			Timer.start();
			for (int i = 0; i < q; i++) {
				for (int j = 0; j < n; j++) {
					boolean in = true;
					for (int d = 0; d < dim; d++) {
						int e = elements[j].get()[d];
						if (e < lo[i][d] || hi[i][d] < e) {
							in = false;
							break;
						}
					}
					if (in) {
						found2++;
						/*for (int x = 0; x < dim; x++) {
                        System.out.print(elements[j][x] + " ");
                    }
                    System.out.println("");
                    for (int x = 0; x < dim; x++) {
                        System.out.print(lo[i][x] + " ");
                    }
                    System.out.println("");
                    for (int x = 0; x < dim; x++) {
                        System.out.print(hi[i][x] + " ");
                    }
                    System.out.println("");
                    System.out.println("");
						 */
					}
				}
			}
			Timer.stop();
			System.out.println(
				"exhaustive quering " + Timer.get());
		}
		System.out.println(
			"found by tree " + found1);
		System.out.println(
			"found by exhaustive " + found2);

		// compare speed with just comparing vectors all vs all!!!
		// check if treemap is optimal and compare speeds + max. elem. count
		// TODO test korektnosti, pak zkusit aplikovat na fragmenty, treba to bude stacit
	}

	private static void small() {
		int[] rs = {10, 10};
		MultidimensionalArray g = new MultidimensionalArray(2);
		int[] c = {30, 4};
		int[] lo = {30, 1};
		int[] hi = {30, 4};
		g.insert(c, 1);
		g.insert(c, 2);
		Buffer<Integer> result = new Buffer(1000);
		g.getRange(lo, hi, result);

		System.out.println("results ");
		for (int i = 0; i < result.size(); i++) {
			System.out.println(result.get(i));
		}
	}

	public static void main(String[] args) {
		//small();
		test();
	}
}
