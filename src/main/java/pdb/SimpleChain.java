package pdb;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;

import spark.Alignable;

/**
 *
 * @author Antonin Pavelka
 */
public class SimpleChain implements Alignable {

    private List<Residue> residues = new ArrayList<>();

    public SimpleChain() {

    }

    public SimpleChain(Point3d[] centers) {
        for (int i = 0; i < centers.length; i++) {
            Point3d x = centers[i];
            if (x != null) {
                // why is it null, TODO create dummy by averaging neighhbors?
                residues.add(new Residue(i + 1, x));
            }
        }
    }

    public void add(Residue r) {
        residues.add(r);
    }

    public List<Residue> getResidues() {
        return residues;
    }

    public Point3d[] getPoints() {
        List<Point3d> list = new ArrayList<>();
        for (Residue r : residues) {
            Point3d x = r.getPosition3d();
            list.add(x);
        }
        Point3d[] a = new Point3d[list.size()];
        list.toArray(a);
        return a;
    }

    public int size() {
        return residues.size();
    }
}
