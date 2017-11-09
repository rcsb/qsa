package grid.sparse.arrays;

import grid.sparse.Buffer;

public interface Array<T> {

    public T get(int i);

    public void getRange(int a, int b, boolean cycle, Buffer<T> buffer);

    public void put(int i, T t);
}
