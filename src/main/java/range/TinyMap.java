package range;

import global.Parameters;
import grid.sparse.Buffer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonin Pavelka
 */
public class TinyMap implements Array {

	private byte[] indexes;
	private Object[] content;
	private static byte max = Parameters.create().getIndexBrackets();
	//public static int count;
	//public static List<TinyMap> list = new ArrayList<>();

	public TinyMap() {
		//count++;
		//list.add(this);
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

	private boolean checkBoundaries(int a, int b) {
		assert a < max : a + " " + max;
		assert b < max : b + " " + max;
		assert a >= 0;
		assert b >= 0;
		return true;
	}

	private void getRange(byte a, byte b, Buffer out) {
		assert checkBoundaries(a, b);
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
	public void getRange(byte a, byte b, boolean cyclic, Buffer out) {
		assert content.length == indexes.length;
		if (a < 0) {
			if (cyclic) {
				assert max + a >= 0 : max + " " + a;
				getRange((byte) (max + a), (byte) (max - 1), out);
			}
			a = 0;
		} else if (b >= max) {
			if (cyclic) {
				getRange((byte) 0, (byte) (b - max), out);
			}
			b = (byte) (max - 1);
		}
		getRange(a, b, out);
	}

	public void print() {
		for (int i = 0; i < indexes.length; i++) {
			System.out.print(indexes[i] + ":" + content[i] + " ");
		}
		System.out.println();
	}

}
