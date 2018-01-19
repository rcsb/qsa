package range;

import grid.sparse.Buffer;

public interface RangeMap<T> {

	public T get(byte i);

	public void getRange(int a, int b, boolean cycle, int bins, Buffer<T> buffer);

	public void put(byte i, T t);
}
