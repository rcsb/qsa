package grid.sparse;

import vectorization.dimension.Dimensions;
import range.SmallMap;
import range.RangeMap;

/**
 *
 * @author Antonin Pavelka
 *
 * A sparse multidimensional array implemented as a tree, supporting retrieval of area specified by range of individual
 * coordinates.
 */
public class MultidimensionalArray {

	private RangeMap tree;
	private ArrayListUnchecked<Bucket> buckets;
	private int bins;
	private int maxResultSize;
	private Dimensions dimensions;

	/* For Kryo. */
	public MultidimensionalArray() {
	}

	public MultidimensionalArray(Dimensions dimensions, int bins, int maxResultSize) {
		this.bins = bins;
		this.maxResultSize = maxResultSize;
		this.tree = createArray();
		this.buckets = new ArrayListUnchecked<>(maxResultSize);
		this.dimensions = dimensions;
	}

	private RangeMap createArray() {
		return new SmallMap();
	}

	public void insert(byte[] vector, long value) {
		RangeMap activeNode = tree;
		for (int d = 0; d < vector.length - 1; d++) {
			byte bin = vector[d];
			assert activeNode != null;
			RangeMap nextNode = (RangeMap) activeNode.get(bin);
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
		assert maxResultSize > 0;
		ArrayListUnchecked levelA = new ArrayListUnchecked(maxResultSize);
		ArrayListUnchecked levelB = new ArrayListUnchecked(maxResultSize);
		result.clear();
		levelA.clear();
		levelA.add(tree);
		for (int d = 0; d < dimensions.number() - 1; d++) {
			byte l = lo[d];
			byte h = hi[d];
			levelB.clear();
			for (int i = 0; i < levelA.size(); i++) {
				RangeMap a = (RangeMap) levelA.get(i);
				a.getRange(l, h, dimensions.isCyclic(d), bins, levelB);
			}
			if (levelB.isEmpty()) {
				return;
			}
			ArrayListUnchecked b = levelA;
			levelA = levelB;
			levelB = b;
		}
		for (int i = 0; i < levelA.size(); i++) {
			RangeMap<Bucket> bs = (RangeMap<Bucket>) levelA.get(i);
			byte l = lo[dimensions.number() - 1];
			byte h = hi[dimensions.number() - 1];
			buckets.clear();
			bs.getRange(l, h, dimensions.isCyclic(dimensions.number() - 1), bins, buckets);
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
