package grid.sparse;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonin Pavelka
 */
public class Bucket<T> {

	private Object[] content;
	//public static int count;
	//public static List<Bucket> list = new ArrayList<>();

	public Bucket(T t) {
		//count++;
		//list.add(this);
		content = new Object[1];
		content[0] = t;
	}

	public void add(T t) {
		int n = content.length;
		Object[] newContent = new Object[n + 1];
		System.arraycopy(content, 0, newContent, 0, n);
		newContent[n] = t;
		content = newContent;
	}

	public int size() {
		return content.length;
	}

	public T get(int i) {
		return (T) content[i];
	}
}
