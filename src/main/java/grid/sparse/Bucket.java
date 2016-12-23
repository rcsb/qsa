package grid.sparse;

import java.util.ArrayList;
import java.util.List;

public class Bucket<T> {

    private List<T> list = new ArrayList<>();

    public void add(T t) {
        list.add(t);
    }

    public List<T> get() {
        return list;
    }
}
