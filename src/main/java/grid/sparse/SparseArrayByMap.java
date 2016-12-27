package grid.sparse;

import java.util.SortedMap;
import java.util.TreeMap;

public class SparseArrayByMap<T> implements Array<T> {

    private final SortedMap<Integer, T> map = new TreeMap<>();

    @Override
    public T get(int i) {
        return map.get(i);
    }

    @Override
    public void getRange(int a, int b, Buffer<T> buffer) {
        for (T t :  map.subMap(a, b + 1).values()) {
            buffer.add(t);
        }
    }

    public void put(int i, T t) {
        map.put(i, t);
    }
}
