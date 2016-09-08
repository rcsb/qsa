package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Antonin Pavelka
 */
public class Randomness<T> {

    Random random = new Random(1);

    public List<T> subsample(List<T> data, int size) {
        List<T> result = new ArrayList<T>();
        int n = Math.min(data.size(), size);
        for (int i = 0; i < n; i++) {
            int r = random.nextInt(n - i);
            T t = data.remove(r);
            result.add(t);
        }
        return result;
    }

}
