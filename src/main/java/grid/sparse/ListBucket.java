package grid.sparse;

import java.util.ArrayList;
import java.util.List;

public class ListBucket<T> {

	public static int objects = 0;
	public static int buckets = 0;
	private List<T> list = new ArrayList<>();

	public ListBucket() {
		buckets++;
	}

	public void add(T t) {
		objects++;
		if (objects % 100000 == 0) {
			System.out.println("buckets: " + buckets + " objects: " + objects);
		}
		list.add(t);
	}

	public int size() {
		return list.size();
	}

	public T get(int i) {
		return list.get(i);
	}
}
