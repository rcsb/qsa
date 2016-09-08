package statistics;

import java.util.SortedMap;
import java.util.TreeMap;

public class Distribution {

    SortedMap<Integer, Integer> counts = new TreeMap<>();

    public void add(int number) {
        if (counts.containsKey(number)) {
            counts.put(number, counts.get(number) + 1);
        } else {
            counts.put(number, 1);
        }
    }

    public void print(int lowest) {
        for (int number : counts.keySet()) {
            if (lowest <= number) {
                System.out.println(number + "x: " + counts.get(number));
            }
        }
    }

    public int size() {
        return counts.size();
    }
}
