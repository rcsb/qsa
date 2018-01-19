/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package range;

import grid.sparse.Buffer;
import java.util.ArrayList;
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

	private final int binsAutomatic = 100;
	private final int binsManual = 10;

	public SmallMapTest(String testName) {
		super(testName);
	}

	public void testPut() {
		manualPutTest();
		randomPutTest();
	}

	public void testGetRange() {
		randomOpen();
		manualOpen();
		manualCyclic1();
		manualCyclic2();
		manualCyclic3();
		manualCyclic4();

	}

	public void manualOpen() {
		byte[] keys = {5, 6, 4};
		int[] values = {1, 2, 3};
		boolean cyclic = false;
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
		byte[] keys = {0, 2, 3, 9};
		int[] values = {1, 2, 3, 4};
		int[] range = {-1, 2};
		int[] correctCyclicResult = {1, 2, 4};
		int[] correctOpenResult = {1, 2};
		assertManualCaseWorks(keys, values, true, range, correctCyclicResult);
		assertManualCaseWorks(keys, values, false, range, correctOpenResult);
	}

	public void manualCyclic3() {
		byte[] keys = {9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
		int[] values = {9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
		int[] range = {-3, 4};
		int[] correctCyclicResult = {7, 8, 9, 0, 1, 2, 3, 4};
		int[] correctOpenResult = {0, 1, 2, 3, 4};
		assertManualCaseWorks(keys, values, true, range, correctCyclicResult);
		assertManualCaseWorks(keys, values, false, range, correctOpenResult);
	}

	public void manualCyclic4() {
		byte[] keys = {9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
		int[] values = {9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
		int[] range = {8, 11};
		int[] correctCyclicResult = {8, 9, 0, 1};
		int[] correctOpenResult = {8, 9};
		assertManualCaseWorks(keys, values, true, range, correctCyclicResult);
		assertManualCaseWorks(keys, values, false, range, correctOpenResult);
	}

	public void manualCyclic5() {
		byte[] keys = {9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
		int[] values = {9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
		int[] range = {-356, 99};
		int[] correctCyclicResult = {9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
		int[] correctOpenResult = {9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
		assertManualCaseWorks(keys, values, true, range, correctCyclicResult);
		assertManualCaseWorks(keys, values, false, range, correctOpenResult);
	}

	public void randomOpen() {
		SmallMap smallMap = new SmallMap();
		RangeTestDataset data = createDataset();
		for (int i = 0; i < data.size; i++) {
			smallMap.put(data.getKey(i), data.getValue(i));
		}
		RangeGenerator rangeGenerator = new RangeGenerator(binsAutomatic);
		for (int i = 0; i < 1000; i++) {
			int[] range;
			if (random.nextInt(5) == 0) {
				range = rangeGenerator.reasonable();
			} else {
				range = rangeGenerator.crazy();
			}
			Buffer out = new Buffer(BUFFER_SIZE);
			smallMap.getRange(range[0], range[1], false, binsAutomatic, out);
			List<Integer> correct = data.getRangeOpen(range);
			cases += correct.size();

			assertMultisetsAreIdentical(out.toList(), correct);

			/*if (out.size() != correct.size()) {
				print("buffer", out.toList());
				print("correct", correct);
				data.print();
				fail(out.size() + " != " + correct.size() + " " + range[0] + " - " + range[1]);
			}*/
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

	private void assertMultisetsAreIdentical(List<Integer> as, int[] bs) {
		List<Integer> cs = new ArrayList<>();
		for (int b : bs) {
			cs.add(b);
		}
		assertMultisetsAreIdentical(as, cs);
	}

	private void assertMultisetsAreIdentical(List<Integer> a, List<Integer> b) {
		Collections.sort(a);
		Collections.sort(b);
		if (a.size() != b.size()) {
			System.out.println(a.size() + " <> " + b.size());
			print("result ", a);
			print("correct", b);
			fail();
		}
		for (int i = 0; i < b.size(); i++) {
			if (a.get(i) != b.get(i)) {
				print("result ", a);
				print("correct", b);
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
		m.getRange(range[0], range[1], cyclic, binsManual, buffer);
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
