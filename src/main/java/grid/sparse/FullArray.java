package grid.sparse;

public class FullArray<T> implements Array<T> {

    private Object[] content;

    public FullArray(int n) {
        content = new Object[n];
    }

    public T get(int i) {
        if (i >= content.length) {
            return null;
        }
        return (T) content[i];
    }

    public void getRange(int a, int b, Buffer<T> out) {
        for (int i = a; i <= b; i++) {
            Object o = content[i];
            if (o != null) {
                out.add((T) o);
            }
        }
    }

    public void put(int i, T t) {
        content[i] = t;
    }
}
