package grid.sparse;

import java.util.ArrayList;
import java.util.List;

/*
 *  Basically ArrayList, but without range checks (20 % speedup). Also clear() is more efficient, but keeps pointers 
 * to objects, so whole Buffer must be GCed before those objects are too.
 */
public class ArrayListUnchecked<T> {

	public Object[] values;
	public int size;

	public ArrayListUnchecked(int n) {
		values = new Object[n];
	}

	public ArrayListUnchecked() {
		this(1000000);
	}

	public void clear() {
		size = 0;
	}

	public void add(T t) {
		if (values.length <= size) {
			extend();
		}
		values[size++] = t;
	}

	private void extend() {
		Object[] newValues = new Object[size * 2];
		System.arraycopy(values, 0, newValues, 0, size);
		values = newValues;
	}

	public int size() {
		return size;
	}

	public T get(int i) {
		return (T) values[i];
	}

	public boolean isEmpty() {
		return size == 0;
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

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append(values[i]);
			if (i != size - 1) {
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

}
