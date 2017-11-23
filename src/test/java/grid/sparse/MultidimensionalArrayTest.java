package grid.sparse;

import global.TestVariables;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import junit.framework.TestCase;

/**
 * Tests if range queries returns the correct results determined by checking if vectors lie in the range one by one.
 *
 * @author Antonin Pavelka
 */
public class MultidimensionalArrayTest extends TestCase {

	private final int dim = 5;
	private final int bins = 5;
	private final int n = 10000;
	private final Random random = new Random(1);
	private final int minHits = 1000;
	private final int repetitions = 100;
	private int hits;

	public MultidimensionalArrayTest(String testName) {
		super(testName);
	}

	public void test() {
		for (int i = 0; i < repetitions; i++) {
			checkOnce();
		}
		checkHits();
	}

	private void checkOnce() {
		byte[][] vectors = createVectors();
		byte[] lo = createVector();
		byte[] hi = createVector();
		for (int d = 0; d < dim; d++) {
			hi[d] += lo[d];
		}
		List<Long> correctResult = getCorrectResult(vectors, lo, hi);
		List<Long> fastResult = getTestedResult(vectors, lo, hi);
		checkIfIdentical(correctResult, fastResult);
		hits += correctResult.size();
	}

	private void checkHits() {
		if (hits < minHits) {
			fail("Not enough hits " + hits + " to reach a conclusion.");
		}
	}

	public MultidimensionalArray createMultiArray(byte[][] vectors) {
		MultidimensionalArray multiArray = new MultidimensionalArray(dim, bins, n);
		for (int i = 0; i < n; i++) {
			multiArray.insert(vectors[i], i);
		}
		return multiArray;
	}

	public List<Long> getCorrectResult(byte[][] vectors, byte[] lo, byte[] hi) {
		List<Long> result = new ArrayList<>();
		for (int i = 0; i < vectors.length; i++) {
			byte[] vector = vectors[i];
			boolean inside = true;
			for (int d = 0; d < dim; d++) {
				if (vector[d] < lo[d] || hi[d] < vector[d]) {
					inside = false;
				}
			}
			if (inside) {
				result.add((long) i);
			}
		}
		return result;
	}

	public List<Long> getTestedResult(byte[][] vectors, byte[] lo, byte[] hi) {
		MultidimensionalArray multiArray = createMultiArray(vectors);
		BufferOfLong buffer = new BufferOfLong(n);
		multiArray.getRange(lo, hi, buffer);
		return buffer.toList();
	}

	public void checkIfIdentical(List<Long> listA, List<Long> listB) {
		Collections.sort(listA);
		Collections.sort(listB);
		if (listA.size() != listB.size()) {
			fail(listA.size() + " != " + listB.size());
		}
		int n = listA.size();
		for (int i = 0; i < n; i++) {
			long a = listA.get(i);
			long b = listB.get(i);
			if (a != b) {
				fail(i + ": " + a + " != " + b);
			}
		}
	}

	private byte[] createVector() {
		byte[] vector = new byte[dim];
		for (int i = 0; i < dim; i++) {
			vector[i] = (byte) random.nextInt(bins);
		}
		return vector;
	}

	private byte[][] createVectors() {
		byte[][] vectors = new byte[n][];
		for (int i = 0; i < n; i++) {
			vectors[i] = createVector();
		}
		return vectors;
	}

}
