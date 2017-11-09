package grid.sparse;

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
public class MultidimensionalArrayIT extends TestCase {

	private final int dim = 4;
	private final int dimensionSize = 20;
	private final int n = 10000;
	private final Random random = new Random(1);
	private final int minHits = 1000;
	private final int repetitions = 100;
	private int hits;

	public MultidimensionalArrayIT(String testName) {
		super(testName);
	}

	public void test() {
		for (int i = 0; i < repetitions; i++) {
			checkOnce();
		}
		checkHits();
	}

	private void checkOnce() {
		int[][] vectors = createVectors();
		int[] lo = createVector();
		int[] hi = createVector();
		for (int d = 0; d < dim; d++) {
			hi[d] += lo[d];
		}
		List<Integer> correctResult = getCorrectResult(vectors, lo, hi);
		List<Integer> fastResult = getTestedResult(vectors, lo, hi);
		checkIfIdentical(correctResult, fastResult);
		hits += correctResult.size();
	}

	private void checkHits() {
		if (hits < minHits) {
			fail("Not enough hits " + hits + " to reach a conclusion.");
		}
	}

	public MultidimensionalArray createMultiArray(int[][] vectors) {
		MultidimensionalArray multiArray = new MultidimensionalArray(n, dim, dimensionSize);
		for (int i = 0; i < n; i++) {
			multiArray.insert(vectors[i], i);
		}
		return multiArray;
	}

	public List<Integer> getCorrectResult(int[][] vectors, int[] lo, int[] hi) {
		List<Integer> result = new ArrayList<>();
		for (int i = 0; i < vectors.length; i++) {
			int[] vector = vectors[i];
			boolean inside = true;
			for (int d = 0; d < dim; d++) {
				if (vector[d] < lo[d] || hi[d] < vector[d]) {
					inside = false;
				}
			}
			if (inside) {
				result.add(i);
			}
		}
		return result;
	}

	public List<Integer> getTestedResult(int[][] vectors, int[] lo, int[] hi) {
		MultidimensionalArray multiArray = createMultiArray(vectors);
		Buffer buffer = new Buffer(n);
		multiArray.getRange(lo, hi, buffer);
		return buffer.toList();
	}

	public void checkIfIdentical(List<Integer> listA, List<Integer> listB) {
		Collections.sort(listA);
		Collections.sort(listB);
		if (listA.size() != listB.size()) {
			fail(listA.size() + " != " + listB.size());
		}
		int n = listA.size();
		for (int i = 0; i < n; i++) {
			int a = listA.get(i);
			int b = listB.get(i);
			if (a != b) {
				fail(i + ": " + a + " != " + b);
			}
		}
	}

	private int[] createVector() {
		int[] vector = new int[dim];
		for (int i = 0; i < dim; i++) {
			vector[i] = random.nextInt(dimensionSize);
		}
		return vector;
	}

	private int[][] createVectors() {
		int[][] vectors = new int[n][];
		for (int i = 0; i < n; i++) {
			vectors[i] = createVector();
		}
		return vectors;
	}

}
