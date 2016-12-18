package grid;

/**
 *
 * @author kepler
 *
 * A multidimensional array, suporting retrieval of area specified by range of
 * individual coordinates.
 */
public class SparseGrid<T> {

    Object[] tree;

    public void add(int[] coords, T t) {
        Object[] level = tree;
        for (int d = 0; d < coords.length; d++) {
            Object n = level[d];
            if (n == null) {
                
            }
        }
    }

    public void get(int[] coords) {

    }
}
