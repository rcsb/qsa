package fragments.clustering;

import fragments.Biword;
import geometry.Coordinates;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Cluster implements Coordinates {

    private List<Biword> fs = new ArrayList<>();
    private Biword representant;

    public Biword getRepresentant() {
        return representant;
    }

    public void add(Biword f) {
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

    public List<Biword> getFragments(int max) {
        if (fs.size() <= max) {
            return fs;
        } else {
            Random random = new Random(1);
            List<Biword> list = new ArrayList<>();
            for (int i = 0; i < max; i++) {
                list.add(fs.get(random.nextInt(fs.size())));
            }
            return list;
        }
    }
}
