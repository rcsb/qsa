package grid.sparse;

import java.util.SortedMap;
import java.util.TreeMap;

public class SparseArray<T> {

    private SortedMap<Integer, T> map = new TreeMap<>();

    public SortedMap<Integer, T> getRange(int a, int b) {
        return map.subMap(a, b + 1);
    }

    public void put(int i, T t) {
        map.put(i, t);
    }
}
