package fragments.clustering;

import fragments.Fragment;
import java.util.ArrayList;
import java.util.List;

public class Cluster {

    private List<Fragment> fs = new ArrayList<>();
    private Fragment representant;
    
    public Fragment getRepresentant() {
        return representant;
    }

    public void add(Fragment f) {
        if (representant == null) {
            representant = f;
        }
        fs.add(f);
    }
    
    public int size() {
        return fs.size();
    }
}
