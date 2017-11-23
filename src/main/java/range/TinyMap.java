package range;

import grid.sparse.Buffer;

/**
 * Efficient mapping byte -> Object, supporting addition and range search.
 *
 * @author Antonin Pavelka
 */
public class TinyMap implements Array {

	private byte[] indexes;
	private Object[] content;

	public TinyMap() {
	}

	@Override
	public Object get(byte index) {
		if (indexes == null) {
			return null;
		}
		for (int i = 0; i < indexes.length; i++) {
			if (index == indexes[i]) {
				return content[i];
			}
		}
		return null;
	}

	public int size() {
		return indexes.length;
	}

	@Override
	public void put(byte index, Object object) {
		if (indexes == null) {
			indexes = new byte[1];
			content = new Object[1];
			indexes[0] = index;
			content[0] = object;
		} else {
			int n = indexes.length;
			byte[] newIndexes = new byte[n + 1];
			Object[] newContent = new Object[n + 1];
			boolean inserted = false;
			for (int i = 0; i < n + 1; i++) {
				if (inserted) {
					assert index != indexes[i - 1];
					newIndexes[i] = indexes[i - 1];
					newContent[i] = content[i - 1];
				} else {
					if (i == n) {
						newIndexes[n] = index;
						newContent[n] = object;
					} else if (index > indexes[i]) {
						newIndexes[i] = indexes[i];
						newContent[i] = content[i];
					} else {
						assert index != indexes[i] : "position already filled";
						newIndexes[i] = index;
						newContent[i] = object;
						inserted = true;
					}
				}
			}
			indexes = newIndexes;
			content = newContent;
			if (indexes.length > Byte.MAX_VALUE) {
				throw new RuntimeException();
			}
		}
	}

	private boolean checkBoundaries(int a, int b, int bins) {
		assert a < bins : a + " " + bins;
		assert b < bins : b + " " + bins;
		assert a >= 0;
		assert b >= 0;
		return true;
	}

	private void getRange(byte a, byte b, int bins, Buffer out) {
		assert checkBoundaries(a, b, bins);
		for (int i = 0; i < indexes.length; i++) {
			byte index = indexes[i];
			if (a <= index && index <= b) {
				// TODO optimize using the fact indexes are sorted
				// also use out.addAll?
				out.add(content[i]);
			}
		}
	}

	@Override
	public void getRange(byte a, byte b, boolean cyclic, int bins, Buffer out) {
		assert content.length == indexes.length;
		if (a < 0) {
			if (cyclic) {
				assert bins + a >= 0 : bins + " " + a;
				getRange((byte) (bins + a), (byte) (bins - 1), bins, out);
			}
			a = 0;
		} else if (b >= bins) {
			if (cyclic) {
				getRange((byte) 0, (byte) (b - bins), bins, out);
			}
			b = (byte) (bins - 1);
		}
		getRange(a, b, bins, out);
	}

	public void print() {
		for (int i = 0; i < indexes.length; i++) {
			System.out.print(indexes[i] + ":" + content[i] + " ");
		}
		System.out.println();
	}

}
