package range;

import grid.sparse.Buffer;

public interface Array<T> {

	public T get(byte i);

	public void getRange(byte a, byte b, boolean cycle, int bins, Buffer<T> buffer);

	public void put(byte i, T t);
}
