package grid.sparse;

import grid.sparse.arrays.FullArray;
import grid.sparse.arrays.Array;
import java.util.ArrayList;
import java.util.List;

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
	private final int bracketN;
	private final boolean[] cycle;
	private final int dim;
	
	//public List<FullArray> nodes = new ArrayList<>();
	
	public MultidimensionalArray(int maxSize, int dimensions, int dimensionSize) {
		this.bracketN = dimensionSize;
		tree = new FullArray(dimensionSize);
		//tree = new SparseArrayByMap();
		levelA = new Buffer(maxSize);
		levelB = new Buffer(maxSize);
		buckets = new Buffer<>(maxSize);
		this.dim = dimensions;
		cycle = new boolean[dim];
	}

	public void setCycle(int i) {
		cycle[i] = true;
	}


	public void insert(int[] coords, T t) {
		Array x = tree;
		for (int d = 0; d < coords.length - 1; d++) {
			int c = coords[d];
			FullArray y = (FullArray) x.get(c);
			if (y == null) {
				y = new FullArray(bracketN);
				//y = new SparseArrayByMap();
				x.put(c, y);
				//nodes.add(y);
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
		result.clear();
		levelA.clear();
		levelA.add(tree);
		for (int d = 0; d < dim - 1; d++) {
			int l = lo[d];
			int h = hi[d];
			levelB.clear();
			for (int i = 0; i < levelA.size(); i++) {
				Array a = (Array) levelA.get(i);
				a.getRange(l, h, cycle[d], levelB);
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
			bs.getRange(l, h, cycle[dim - 1], buckets);
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
}
