package grid.sparse.arrays;

import grid.sparse.Buffer;

public class FullArray<T> implements Array<T> {

	private Object[] content;
	private static int count = 0;

	public FullArray(int n) {
		content = new Object[n];
		count++;
		if (count % 100000 == 0) {
			System.out.println("arrays: " + count + " (" + n + ")");
		}
	}

	public T get(int i) {
		if (i >= content.length) {
			return null;
		}
		return (T) content[i];
	}

	/*public void getRange(int a, int b, Buffer<T> out) {
		for (int i = a; i <= b; i++) {
			Object o = content[i];
			if (o != null) {
				out.add((T) o);
			}
		}
	}*/
	public void getRange(int a, int b, boolean cyclic, Buffer<T> out) {
		//if ((a < 0) && (b >= content.length)) {
		//	throw new RuntimeException("Box bigger than space.");
		//}
		// for the query, maybe should be resolved one level up, before discretization instead
		if (a < 0) {
			a = 0;
		}
		if (b >= content.length) {
			b = content.length - 1;
		}
		if (a < 0) {
			if (cyclic) {
				for (int i = content.length + a; i < content.length; i++) {
					Object o = content[i];
					if (o != null) {
						out.add((T) o);
					}
				}
			}
			a = 0;
		}
		if (b >= content.length) {
			if (cyclic) {
				for (int i = 0; i <= b - content.length; i++) {
					Object o = content[i];
					if (o != null) {
						out.add((T) o);
					}
				}
			}
			b = content.length - 1;
		}
		for (int i = a; i <= b; i++) {
			Object o = content[i];
			if (o != null) {
				out.add((T) o);
			}
		}
	}

	public int size() {
		int size = 0;
		for (Object o : content) {
			if (o != null) {
				size++;
			}
		}
		return size;
	}

	public void put(int i, T t) {
		if (i >= content.length) { // for maximum, for inserstions only, as queries need cycles
			i = content.length - 1;
		}
		content[i] = t;
	}
}
