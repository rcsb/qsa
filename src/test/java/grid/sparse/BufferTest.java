/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grid.sparse;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import junit.framework.TestCase;

/**
 *
 * @author kepler
 */
public class BufferTest extends TestCase {

	private Random random = new Random(1);

	public BufferTest(String testName) {
		super(testName);
	}

	public void testClear() {
		ArrayListUnchecked buffer = new ArrayListUnchecked();
		buffer.clear();
		assert buffer.size() == 0;

		buffer.add(1);
		buffer.add(2);
		assert buffer.toList().size() == 2;
	}

	public void testAdd() {
		int[] a = generate(1000);
		ArrayListUnchecked<Integer> buffer = new ArrayListUnchecked<>();
		for (int i : a) {
			buffer.add(i);
		}
		List<Integer> list = buffer.toList();
		Collections.sort(list);
		Arrays.sort(a);
		for (int i = 0; i < a.length; i++) {
			assert a[i] == list.get(i);
		}
	}

	private int[] generate(int n) {
		int[] a = new int[n];
		for (int i = 0; i < n; i++) {
			a[i] = random.nextInt(1000);
		}
		return a;
	}
}
