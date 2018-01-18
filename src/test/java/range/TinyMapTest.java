/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package range;

import global.Parameters;
import global.TestVariables;
import grid.sparse.Buffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import junit.framework.TestCase;

/**
 *
 * @author Antonin Pavelka
 */
public class TinyMapTest extends TestCase {

	private final TestVariables vars = new TestVariables();
	private final Parameters parameters = vars.getParameters();
	private final Random random = new Random(1);
	private int cases = 0;
	private final int minCases = 1000;

	public TinyMapTest(String testName) {
		super(testName);
	}

	private Set<Byte> createUniqueBytes(int max) {
		byte[] bytes = new byte[max];
		random.nextBytes(bytes);
		Set<Byte> set = new HashSet<>();
		for (byte b : bytes) {
			set.add(b);
		}
		return set;
	}

	public void testPut() {
		manualPutTest();
		randomPutTest();
	}

	private void manualPutTest() {
		TinyMap map = new TinyMap();
		byte a = 1;
		byte b = 2;

		map.put(a, a);
		map.print();
		assertEquals(map.size(), 1);
		map.put(b, b);
		map.print();
		assertEquals(map.size(), 2);
		map.put(b, b);
		map.print();
		assertEquals(map.size(), 2);
		map.put(a, a);
		map.print();
		assertEquals(map.size(), 2);
	}

	private void randomPutTest() {
		Set<Byte> set = createUniqueBytes(100);
		TinyMap map = new TinyMap();
		for (byte b : set) {
			map.put(b, b);
			if (random.nextInt(2) == 0) { // try some duplicates, nothing should happen
				map.put(b, b);
			}
		}
		assertEquals(set.size(), map.size());
	}

	public void testGetRange() {
		testManual();
		testRandom((byte) 100);
	}

	public void testManual() {
		TinyMap m = new TinyMap();
		m.put((byte) 5, 1);
		m.put((byte) 6, 2);
		m.put((byte) 4, 3);
		m.print();
		Buffer buffer = new Buffer(100);
		m.getRange((byte) 1, (byte) 5, false, parameters.getIndexBins(), buffer);
		System.out.println(buffer.toString());
	}

	public void testRandom(byte max) {
		byte size = (byte) random.nextInt(max);
		TinyMap tinyMap = new TinyMap();
		byte[] keys = new byte[size];
		int[] values = new int[size];
		for (int i = 0; i < size; i++) {
			byte b = (byte) random.nextInt(100);
			keys[i] = b;
			values[i] = b;
			tinyMap.put(b, b);
		}
		for (int i = 0; i < 1000; i++) {
			byte[] range = new byte[2];
			random.nextBytes(range);
			if (range[0] > range[1]) {
				byte b = range[0];
				range[0] = range[1];
				range[1] = b;
			}
			System.out.println(range[0]);
			System.out.println(range[1]);
			System.out.println("**");
			if (range[1] - range[0] < max / 2) {

				// TODO test cyclicity too, overstep max, only sometimes, 
				Buffer out = new Buffer(max);
				tinyMap.getRange(range[0], range[1], true, parameters.getIndexBins(), out);
				List<Integer> correct = getCorrectRange(keys, values, range[0], range[1]);
				cases += correct.size();
				if (out.size() != correct.size()) {
					System.out.println(range[0] + "-" + range[1]);
					print("buffer", out.toList());
					print("correct", correct);
				}
				assert out.size() == correct.size() : out.size() + " != " + correct.size()
					+ " " + range[0] + "-" + range[1];
			}
		}
		if (cases < minCases) {
			fail("Inconclusive, only " + cases + " cases, " + minCases + " needed.");
		}
	}

	private void print(String name, Collection collection) {
		System.out.println("=== " + name + " ===");
		for (Object o : collection) {
			System.out.print(o + " ");
		}
		System.out.println();
	}

	private List<Integer> getCorrectRange(byte[] keys, int[] values, byte a, byte b) {
		List<Integer> result = new ArrayList<>();
		for (int i = 0; i < keys.length; i++) {
			if (a <= keys[i] && keys[i] <= b) {
				result.add(values[i]);
			}
		}
		return result;
	}

}
