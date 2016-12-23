package grid.sparse;

import java.util.Arrays;
import java.util.List;

// TODO simple, just create new array and move everything with insertion
// at the right place?
public class SparseArray<T> implements Array {

    private int[] indexes;
    private Object[] content;
    private int size;
    private int extra = 3;

    public SparseArray() {
        indexes = new int[extra];
        content = new Object[extra];
    }

    public T get(int i) {
        int index = Arrays.binarySearch(indexes, i);
        return (T) content[index];
    }

    public List<T> getRange(int a, int b) {
        int start = Arrays.binarySearch(indexes, a);
        return null;
    }

    public void put(int i, T t) {
        if (i < size) {
            
        }
        // new arrya, copy and insesrt on correct sorted position
        
    }
    
    private static int binarySearch0(int[] a, int fromIndex, int toIndex,
                                     int key) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midVal = a[mid];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }
    
    public static void main(String[] args) {
        SparseArray a =new SparseArray();
        a.put(5, 1);
    }
}
