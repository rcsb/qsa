package grid.sparse;

import range.Array;
import range.ArrayFactory;
import range.FullArray;
import range.TinyMap;

/**
 *
 * @author Antonin Pavelka
 *
 * A multidimensional array, suporting retrieval of area specified by range of individual coordinates.
 */
public class MultidimensionalArray<T> {

	private final Array tree;
	private final ArrayFactory arrayFactory = new ArrayFactory();
	private Buffer levelA, levelB;
	private final Buffer<Bucket> buckets;
	private final int bracketN;
	private final boolean[] cycle;
	private final int dim;

	//public List<FullArray> nodes = new ArrayList<>();
	public MultidimensionalArray(int maxSize, int dimensions, int dimensionSize) {
		this.bracketN = dimensionSize;
		tree = createArray();
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

	private Array createArray() {
		if (false) { 
			return new FullArray(bracketN);
		} else {
			return new TinyMap();
		}
	}

	public void insert(byte[] vector, T t) {
		Array activeNode = tree;
		for (int d = 0; d < vector.length - 1; d++) {
			byte bin = vector[d];
			assert activeNode != null;
			Array nextNode = (Array) activeNode.get(bin);
			//assert nextNode != null;
			if (nextNode == null) {
				nextNode = createArray();
				activeNode.put(bin, nextNode);
			}
			activeNode = nextNode;
		}
		// TODO use increasing arrays for buckets
		byte c = vector[vector.length - 1];
		Object o = activeNode.get(c);
		if (o == null) {
			Bucket<T> b = new Bucket(t);
			activeNode.put(c, b);
		} else {
			Bucket<T> b = (Bucket<T>) o;
			b.add(t);
		}
	}

	public void getRange(byte[] lo, byte[] hi, Buffer<T> result) {
		result.clear();
		levelA.clear();
		levelA.add(tree);
		for (int d = 0; d < dim - 1; d++) {
			byte l = lo[d];
			byte h = hi[d];
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
			byte l = lo[dim - 1];
			byte h = hi[dim - 1];
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
