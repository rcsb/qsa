package fragments.clustering;

import fragments.Fragment;
import geometry.Coordinates;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Cluster implements Coordinates {

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

    @Override
    public double[] getCoords() {
        return representant.getCoords();
    }

    public List<Fragment> getFragments(int max) {
        if (fs.size() <= max) {
            return fs;
        } else {
            Random random = new Random(1);
            List<Fragment> list = new ArrayList<>();
            for (int i = 0; i < max; i++) {
                list.add(fs.get(random.nextInt(fs.size())));
            }
            return list;
        }
    }
}
