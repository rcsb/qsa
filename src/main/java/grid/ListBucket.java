package grid;

import java.util.ArrayList;
import java.util.List;

public class ListBucket<T> {

    public class Bucket<T> {

        List<T> list = new ArrayList<>();

        public void add(T t) {
            list.add(t);
        }

    }

}
