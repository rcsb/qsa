package grid;

public class BufferProcessor implements Processor {

    private static Object[] a = new Object[1000000];
    private int index;

    public BufferProcessor() {
        index = 0;
    }

    public void process(Object o) {
        a[index++] = o;
    }

    public int size() {
        return index;
    }
    
    public Object get(int i) {
        return a[i];
    }
    
}
