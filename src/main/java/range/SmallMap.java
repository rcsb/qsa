package range;

import grid.sparse.Buffer;

/**
 * A mapping of bytes to Objects, supporting additions and range search. Designed to be time and memory efficient for
 * very small maps (byte range of keys).
 *
 * Range search can be made faster on the cost of construction speed if keys would be sorted (halving of interval).
 *
 * Construction speed can be improved by allocating arrays e.g. 150 % of actually needed size.
 *
 * @author Antonin Pavelka
 */
public class SmallMap implements RangeMap {

	private byte[] keys;
	private Object[] values;

	public SmallMap() {
		clear();
	}

	public final void clear() {
		keys = new byte[0];
		values = new Object[0];
	}

	public int size() {
		return keys.length;
	}

	@Override
	public void put(byte key, Object object) {
		int i = findKeyPosition(key);
		if (i == -1) { // key does not exist, adding to the end
			putNew(key, object);
		} else { // key exists, replace
			values[i] = object;
		}
	}

	private void putNew(byte key, Object object) {
		int length = keys.length;

		byte[] newKeys = new byte[length + 1];
		System.arraycopy(keys, 0, newKeys, 0, length);

		Object[] newValues = new Object[length + 1];
		System.arraycopy(values, 0, newValues, 0, length);

		newKeys[length] = key;
		newValues[length] = object;

		keys = newKeys;
		values = newValues;

		assert values.length == keys.length;
	}

	private int findKeyPosition(byte key) {
		for (int i = 0; i < keys.length; i++) {
			if (keys[i] == key) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public Object get(byte key) {
		if (keys == null) {
			return null;
		}
		for (int i = 0; i < keys.length; i++) {
			if (key == keys[i]) {
				return values[i];
			}
		}
		return null;
	}

	public void print() {
		for (int i = 0; i < keys.length; i++) {
			System.out.print(keys[i] + ":" + values[i] + " ");
		}
		System.out.println();
	}

	/**
	 * Returns all values with key lying between a and b, both inclusive.
	 *
	 * @param a
	 * @param b
	 * @param cyclic
	 * @param bins Maximum key value plus one (minimum key value is always zero).
	 * @param out
	 */
	@Override
	public void getRange(int a, int b, boolean cyclic, int bins, Buffer out) {
		if (cyclic) {
			getRangeCyclic(a, b, bins, out);
		} else {
			getRangeOpen(a, b, out);
		}
	}

	public void getRangeCyclic(int min, int max, int bins, Buffer out) {
		assert min <= max : "inverted range " + min + " - " + max;
		if (min <= 0 && bins - 1 <= max) {
			getAll(out);
		} else if (min < 0) {
			getRangeWithUnderflow(min, max, bins, out);
		} else if (bins <= max) {
			getRangeWithOverflow(min, max, bins, out);
		}
	}

	private void getAll(Buffer out) {
		for (Object o : values) {
			out.add(o);
		}
	}

	private void getRangeWithUnderflow(int min, int max, int bins, Buffer out) {
		int newMin = bins + min;
		if (newMin <= 0) {
			getAll(out);
		} else {
			getRangeUnderflow(newMin, out);
			getRangeOpen(0, max, out);
		}
	}

	private void getRangeUnderflow(int min, Buffer out) {
		for (int i = 0; i < keys.length; i++) {
			if (min <= keys[i]) {
				out.add(values[i]);
			}
		}
	}

	private void getRangeWithOverflow(int min, int max, int bins, Buffer out) {
		int newMax = max - bins;
		if (bins - 1 <= newMax) {
			getAll(out);
		} else {
			getRangeOpen(min, bins - 1, out);
			getRangeOverflow(newMax, out);
		}
	}

	private void getRangeOverflow(int max, Buffer out) {
		for (int i = 0; i < keys.length; i++) {
			if (keys[i] <= max) {
				out.add(values[i]);
			}
		}
	}

	private void getRangeOpen(int a, int b, Buffer out) {
		assert a <= b : "inverted range " + a + " - " + b;
		for (int i = 0; i < keys.length; i++) {
			byte key = keys[i];
			if (a <= key && key <= b) {
				out.add(values[i]);
			}
		}
	}

}
