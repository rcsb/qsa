package range;

import grid.sparse.ArrayListUnchecked;

public interface RangeMap<T> {

	public T get(byte i);

	public void getRange(int a, int b, boolean cycle, int bins, ArrayListUnchecked<T> buffer);

	public void put(byte i, T t);
}
