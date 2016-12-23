package grid.sparse;

public class Buffer<T> {

    Object[] a;
    int s;

    public Buffer(int n) {
        a = new Object[n];
    }

    public void clear() {
        s = 0;
    }

    public void add(T t) {
        a[s++] = t;
    }

    public int size() {
        return s;
    }

    public T get(int i) {
        return (T) a[i];
    }

    public boolean isEmpty() {
        return s == 0;
    }

    public void addAll(Iterable<T> a) {
        for (T t : a) {
            add(t);
        }
    }

}
