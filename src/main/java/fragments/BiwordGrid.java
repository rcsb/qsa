package fragments;

import grid.GridSearch;
import java.util.List;

public class BiwordGrid {

    private final GridSearch grid;
    private final double[] diffs = {3, 3, 1.2};
    //private final double[] diffs = {5, 5, 1.5};
    private final double[] sizes = {1, 1, 0.4};

    public BiwordGrid(List<Biword> fragments) {
        grid = new GridSearch(sizes, diffs);
        grid.buildGrid(fragments);
    }

    public List<Biword> search(Biword f) {
        return grid.nearest(f);
    }
}
