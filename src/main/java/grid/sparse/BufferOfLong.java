package grid.sparse;

import java.util.ArrayList;
import java.util.List;

public class BufferOfLong {

	long[] a;
	int s;

	// For Kryo.
	public BufferOfLong() {
	}

	public BufferOfLong(int n) {
		a = new long[n];
	}

	public void clear() {
		s = 0;
	}

	public void add(long value) {
		a[s++] = value;
	}

	public int size() {
		return s;
	}

	public long get(int i) {
		return a[i];
	}

	public boolean isEmpty() {
		return s == 0;
	}

	public void addAll(Iterable<Long> it) {
		for (long t : it) {
			add(t);
		}
	}

	public void addAll(long[] ts) {
		for (long t : ts) {
			add(t);
		}
	}

	public void addAll(BufferOfLong b) {
		System.arraycopy(b.a, 0, a, s, b.s);
		s += b.s;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s; i++) {
			sb.append(a[i]);
			if (i != s - 1) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}

	public List<Long> toList() {
		List<Long> list = new ArrayList<>();
		for (int i = 0; i < size(); i++) {
			list.add(get(i));
		}
		return list;
	}

}
