package grid;

import java.util.HashSet;
import java.util.Set;

public class SetProcessor implements Processor {

    private static Set ps = new HashSet();

    public SetProcessor() {
        ps.clear();
    }

    public void process(Object o) {
        ps.add(o);
    }

    public boolean contains(Object o) {
        return ps.contains(o);
    }
}
