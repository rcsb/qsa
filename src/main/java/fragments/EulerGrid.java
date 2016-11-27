package fragments;

import grid.GridRangeSearch;
import java.util.List;

public class EulerGrid {

    private final GridRangeSearch grid;
    private final double distance = Parameters.create().getMaxEulerDistance();

    public EulerGrid(List<Awp> alignedWords) {
        grid = new GridRangeSearch(2 * Math.PI / 12); 
        grid.buildGrid(alignedWords);
    }

    public List<Awp> search(Awp awp) {
        return grid.nearest(awp, distance);
    }
}
