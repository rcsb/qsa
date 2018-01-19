/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package range;

import grid.sparse.Buffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import junit.framework.TestCase;

/**
 *
 * @author Antonin Pavelka
 */
public class SmallMapTest extends TestCase {

	private final Random random = new Random(1);
	private int cases = 0;
	private final int minCases = 1000;
	private final int BUFFER_SIZE = 1000000;

	private int bins = 100;

	public SmallMapTest(String testName) {
		super(testName);
	}

	public void testPut() {
		manualPutTest();
		randomPutTest();
	}

	public void testGetRange() {
		manualAcyclic();
		manualCyclic1();
		manualCyclic2();
		randomAcyclic((byte) 100);
	}

	public void manualAcyclic() {
		byte[] keys = {5, 6, 4};
		int[] values = {1, 2, 3};
		boolean cyclic = true;
		int[] range = {1, 5};
		int[] correctResult = {1, 3};
		assertManualCaseWorks(keys, values, cyclic, range, correctResult);
	}

	public void manualCyclic1() {
		byte[] keys = {5, 6, 4};
		int[] values = {1, 2, 3};
		boolean cyclic = true;
		int[] range = {1, 5};
		int[] correctResult = {1, 3};
		assertManualCaseWorks(keys, values, cyclic, range, correctResult);
	}

	public void manualCyclic2() {
		byte[] keys = {0, 2, 3};
		int[] values = {1, 2, 3};
		int[] range = {-1, 2};
		int[] correctCyclicResult = {0, 2};
		int[] correctOpenResult = {0};
		assertManualCaseWorks(keys, values, true, range, correctCyclicResult);
		assertManualCaseWorks(keys, values, false, range, correctOpenResult);
	}

	public void randomAcyclic(byte max) {
		SmallMap tinyMap = new SmallMap();
		RangeTestDataset data = createDataset();
		RangeGenerator rangeGenerator = new RangeGenerator(bins);
		for (int i = 0; i < 1000; i++) {
			int[] range;
			if (random.nextInt(5) == 0) {
				range = rangeGenerator.reasonable();
			} else {
				range = rangeGenerator.crazy();
			}
			Buffer out = new Buffer(max);
			tinyMap.getRange(range[0], range[1], false, bins, out);
			List<Integer> correct = data.getRangeAcyclic(range);
			cases += correct.size();
			if (out.size() != correct.size()) {
				fail(out.size() + " != " + correct.size() + " " + range[0] + "-" + range[1]);
				print("buffer", out.toList());
				print("correct", correct);
			}
		}
		if (cases < minCases) {
			fail("Inconclusive, only " + cases + " cases, " + minCases + " needed.");
		}
	}

	private void manualPutTest() {
		SmallMap map = new SmallMap();
		byte a = 1;
		byte b = 2;
		map.put(a, a);
		assertEquals(map.size(), 1);
		map.put(b, b);
		assertEquals(map.size(), 2);
		map.put(b, b);
		assertEquals(map.size(), 2);
		map.put(a, a);
		assertEquals(map.size(), 2);
	}

	private void randomPutTest() {
		RangeTestDataset data = createDataset();
		SmallMap map = new SmallMap();
		for (int i = 0; i < data.size(); i++) {
			map.put(data.getKey(i), data.getValue(i));
			if (random.nextInt(2) == 0) { // try some duplicates, nothing should happen
				map.put(data.getKey(i), data.getValue(i));
			}
		}
		assertEquals(data.getUniqueKeys().size(), map.size());
	}

	private RangeTestDataset createDataset() {
		RangeTestDataset data = new RangeTestDataset();
		data.setBins(100);
		data.setSize(80);
		data.init();
		return data;
	}

	private void assertMultisetsAreIdentical(List<Integer> list, int[] array) {
		assert list.size() == array.length : list.size() + " " + array.length;
		Collections.sort(list);
		Arrays.sort(array);
		for (int i = 0; i < array.length; i++) {
			if (list.get(i) != array[i]) {
				print("result ", list);
				print("correct", array);
				fail();
			}
		}
	}

	private void assertManualCaseWorks(byte[] keys, int[] values, boolean cyclic, int[] range, int[] correctResult) {
		SmallMap m = new SmallMap();
		for (int i = 0; i < keys.length; i++) {
			m.put(keys[i], values[i]);
		}
		Buffer buffer = new Buffer(BUFFER_SIZE);
		m.getRange(range[0], range[1], cyclic, bins, buffer);
		assertMultisetsAreIdentical(buffer.toList(), correctResult);
	}

	private void print(String name, Collection collection) {
		System.out.println("=== " + name + " ===");
		for (Object o : collection) {
			System.out.print(o + " ");
		}
		System.out.println();
	}

	private void print(String name, int[] array) {
		System.out.println("=== " + name + " ===");
		for (Object o : array) {
			System.out.print(o + " ");
		}
		System.out.println();
	}
}
