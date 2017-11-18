/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package range;

import global.Parameters;
import grid.sparse.Buffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import junit.framework.TestCase;

/**
 *
 * @author kepler
 */
public class TinyMapTest extends TestCase {

	private final Random random = new Random(1);
	private int cases = 0;
	private int minCases = 1000;

	public TinyMapTest(String testName) {
		super(testName);
	}

	public void testGetRange() {
		//manualTest();
		//randomTest(Parameters.create().getIndexBrackets());
	}

	private void manualTest() {
		TinyMap m = new TinyMap();
		m.put((byte) 5, 1);
		m.put((byte) 6, 2);
		m.put((byte) 4, 3);
		m.print();
		Buffer buffer = new Buffer(100);
		m.getRange((byte) 1, (byte) 5, false, buffer);
		System.out.println(buffer.toString());
	}

	private void randomTest(byte max) {
		byte size = (byte) random.nextInt(max);
		TinyMap m = new TinyMap();
		byte[] keys = new byte[size];
		int[] values = new int[size];
		Set<Byte> occupied = new HashSet<>();
		random.nextBytes(keys);
		for (int i = 0; i < size; i++) {
			if (!occupied.contains(keys[i])) {
				values[i] = i;
				m.put(keys[i], i);
				occupied.add(keys[i]);
			}
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
				m.getRange(range[0], range[1], true, out);
				List<Integer> correct = getRange(keys, values, range[0], range[1]);
				cases += correct.size();
				assert out.size() == correct.size();
			}
		}
		if (cases < minCases) {
			fail("Inconclusive, only " + cases + " cases, " + minCases + " needed.");
		}
	}

	private List<Integer> getRange(byte[] keys, int[] values, byte a, byte b) {
		List<Integer> result = new ArrayList<>();
		for (int i = 0; i < keys.length; i++) {
			if (a <= keys[i] && keys[i] <= b) {
				result.add(values[i]);
			}
		}
		return result;
	}

}
