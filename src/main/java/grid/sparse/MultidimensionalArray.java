package grid.sparse;

import range.Array;
import range.TinyMap;

/**
 *
 * @author Antonin Pavelka
 *
 * A sparse multidimensional array implemented as a tree, supporting retrieval of area specified by range of individual
 * coordinates.
 */
public class MultidimensionalArray {

	private Array tree;
	private Buffer levelA, levelB;
	private Buffer<Bucket> buckets;
	private boolean[] cycle;
	private int dim;
	private int bins;

	/* For Kryo. */
	public MultidimensionalArray() {
	}

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

	public void insert(byte[] vector, long value) {
		Array activeNode = tree;
		for (int d = 0; d < vector.length - 1; d++) {
			byte bin = vector[d];
			assert activeNode != null;
			Array nextNode = (Array) activeNode.get(bin);
			if (nextNode == null) {
				nextNode = createArray();
				activeNode.put(bin, nextNode);
			}
			activeNode = nextNode;
		}
		byte c = vector[vector.length - 1];
		Object o = activeNode.get(c);
		if (o == null) {
			Bucket b = new Bucket(value);
			activeNode.put(c, b);
		} else {
			Bucket b = (Bucket) o;
			b.add(value);
		}
	}

	public void getRange(byte[] lo, byte[] hi, BufferOfLong result) {
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
				Bucket b = buckets.get(j);
				for (int k = 0; k < b.size(); k++) {
					long t = b.get(k);
					result.add(t);
				}
			}
		}
	}
}
