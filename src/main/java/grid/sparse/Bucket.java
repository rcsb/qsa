package grid.sparse;

/**
 *
 * @author Antonin Pavelka
 */
public class Bucket {

	private long[] content;
	private int size;

	public Bucket(long firstValue) {
		content = new long[1];
		content[0] = firstValue;
		size = 1;
	}

	public void add(long value) {
		assert size <= content.length;
		if (size == content.length) {
			content = extendedArray();
		}
		content[size] = value;
		size++;
	}

	private long[] extendedArray() {
		int length;
		if (size < 5) {
			length = size + 1;
		} else {
			length = size + size / 5;
		}
		long[] newContent = new long[length];
		System.arraycopy(content, 0, newContent, 0, size);
		return newContent;

	}

	public int size() {
		return size;
	}

	public long get(int i) {
		assert i < size;
		return content[i];
	}

}
