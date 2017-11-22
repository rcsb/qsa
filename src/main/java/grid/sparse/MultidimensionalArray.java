package grid.sparse;

import global.Parameters;
import range.Array;
import range.TinyMap;

/**
 *
 * @author Antonin Pavelka
 *
 * A sparse multidimensional array implemented as a tree, supporting retrieval of area specified by range of individual
 * coordinates.
 */
public class MultidimensionalArray<T> {

	private final Array tree;
	private Buffer levelA, levelB;
	private final Buffer<Bucket> buckets;
	private final boolean[] cycle;
	private final int dim;
	private final int bins;

	public MultidimensionalArray(int dim, int bins, int maxSize) {
		this.dim = dim;
		this.bins = bins;
		this.tree = createArray();
		this.levelA = new Buffer(maxSize);
		this.levelB = new Buffer(maxSize);
		this.buckets = new Buffer<>(maxSize);
		this.cycle = new boolean[dim];
	}

	public void setCycle(int i) {
		cycle[i] = true;
	}

	private Array createArray() {
		return new TinyMap();
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
				a.getRange(l, h, cycle[d], bins, levelB);
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
			bs.getRange(l, h, cycle[dim - 1], bins, buckets);
			for (int j = 0; j < buckets.size(); j++) {
				Bucket<T> b = buckets.get(j);
				for (int k = 0; k < b.size(); k++) {
					T t = b.get(k);
					result.add(t);
				}
			}
		}
	}
}
