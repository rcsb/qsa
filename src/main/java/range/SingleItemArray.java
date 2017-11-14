package range;

import grid.sparse.Buffer;

/**
 *
 * @author Antonin Pavelka
 */
public class SingleItemArray implements Array {

	private static byte NULL = Byte.MIN_VALUE;
	private byte index = NULL;
	private Object content;

	public SingleItemArray() {

	}

	public SingleItemArray(byte index, Object content) {
		this.index = index;
		this.content = content;

	}

	@Override
	public Object get(byte i) {
		if (i == this.index) {
			return content;
		} else {
			return null;
		}
	}

	@Override
	public void getRange(byte a, byte b, boolean cycle, Buffer buffer) {
//		if (cycle) {
//			throw new UnsupportedOperationException();
		//}
		System.out.println("a");
		if (a <= index && index <= b) {
			System.out.println("retru");
			buffer.add(content);
		}
	}

	@Override
	public void put(byte i, Object o) {
		assert content == null : index + " " + content;
		index = i;
		content = o;
	}

	public byte getIndex() {
		if (index == NULL) {
			throw new RuntimeException();
		}
		return index;
	}

	public boolean isEmpty() {
		return index == NULL;
	}

	public Object getContent() {
		return content;
	}
}
