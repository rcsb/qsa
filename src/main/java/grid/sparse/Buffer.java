package grid.sparse;

import java.util.ArrayList;
import java.util.List;

public class Buffer<T> {

	Object[] a;
	int s;

	// For Kryo.
	public Buffer() {

	}

	public Buffer(int n) {
		a = new Object[n];
	}

	public void clear() {
		s = 0;
	}

	public void add(T t) {
		a[s++] = t;
	}

	public int size() {
		return s;
	}

	public T get(int i) {
		return (T) a[i];
	}

	public boolean isEmpty() {
		return s == 0;
	}

	public void addAll(Iterable<T> it) {
		for (T t : it) {
			add(t);
		}
	}

	public void addAll(T[] ts) {
		for (T t : ts) {
			add(t);
		}
	}

	public void addAll(Buffer b) {
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

	public List<T> toList() {
		List<T> list = new ArrayList<>();
		for (int i = 0; i < size(); i++) {
			list.add(get(i));
		}
		return list;
	}

	public static void main(String[] args) {
		Buffer a = new Buffer(100);
		Buffer b = new Buffer(100);
		System.out.println(a);
		System.out.println(b);
		a.add(1);
		a.add(2);
		a.add(3);
		b.add(11);
		b.add(12);

		System.out.println(a);
		System.out.println(b);
		a.addAll(b);
		System.out.println(a);
	}
}
