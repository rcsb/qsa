package grid;

public class BufferProcessor implements Processor {

    private static Object[] a = new Object[1000000];
    private int i;

    public BufferProcessor() {
        i = 0;
    }

    public void process(Object o) {
        a[i++] = o;
    }

}
