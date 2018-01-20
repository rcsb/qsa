package analysis;

import grid.sparse.ArrayListUnchecked;
import grid.sparse.BufferOfLong;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import util.Timer;

public class BufferListComparison {

	private Random random = new Random(1);
	long[] array = new long[200000000];

	private void buffer(long[] a, BufferOfLong buffer) {
		buffer.clear();
		for (long i : a) {
			buffer.add(i);
		}
	}

	private void list(long[] a, List<Long> list) {
		list.clear();
		for (long i : a) {
			list.add(i);
		}
	}

	private void array(long[] a, long[] array) {
		for (int i = 0; i < a.length; i++) {
			array[i] = a[i];
		}
	}

	public void testSpeed() {
		long[] a = generate(10 * 1000 * 1000);
		List<Long> list = new ArrayList<>();
		BufferOfLong buffer = new BufferOfLong(array.length);

		for (int i = 0; i < 1000; i++) {

			Timer.start();
			list(a, list);
			Timer.stop();
			long listTime = Timer.get();

			Timer.start();
			buffer(a, buffer);
			Timer.stop();
			long bufferTime = Timer.get();

			Timer.start();
			array(a, array);
			Timer.stop();
			long arrayTime = Timer.get();

			System.out.println("bufer " + bufferTime);
			System.out.println("list  " + listTime);
			System.out.println("array " + arrayTime);
			System.out.println();
		}

	}

	private long[] generate(int n) {
		long[] a = new long[n];
		for (int i = 0; i < n; i++) {
			a[i] = random.nextLong();
		}
		return a;
	}

	public static void main(String[] args) {
		BufferListComparison m = new BufferListComparison();
		m.testSpeed();
	}

}
