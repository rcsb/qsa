package grid.sparse;

/**
 *
 * @author Antonin Pavelka
 */
public class Bucket<T> {

	private Object[] content;
	private int size;

	public Bucket(T t) {
		content = new Object[1];
		content[0] = t;
		size = 1;
	}

	public void add(T t) {
		assert size <= content.length;
		if (size == content.length) {
			content = extendedArray();
		}
		content[size] = t;
		size++;
	}

	private Object[] extendedArray() {
		int length;
		
		if (size < 5) {
			length = size + 1;
		} else {
			length = size + size / 5;
		}
		Object[] newContent = new Object[length];
		System.arraycopy(content, 0, newContent, 0, size);
		return newContent;

	}

	public int size() {
		return size;
	}

	public T get(int i) {
		assert i < size;
		return (T) content[i];
	}

}
