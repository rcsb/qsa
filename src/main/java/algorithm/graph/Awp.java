package algorithm.graph;

import geometry.Coordinates;
import geometry.SmartTransformation;
import javax.vecmath.Matrix3d;
import biword.Fragment;

public class Awp implements Coordinates {

    private Fragment x;
    private Fragment y;
    private double rmsd;
    private Matrix3d m;
    private double[] coords;

    public Awp(Fragment x, Fragment y, double rmsd, Matrix3d m) {
        this.x = x;
        this.y = y;
        this.rmsd = rmsd;
        this.m = m;
    }

    public Matrix3d getRotationMatrix() {
        return m;
    }

    @Override
    public double[] getCoords() {
        if (coords == null) {
            coords = SmartTransformation.getXYZEuler(m);
        }
        return coords;
    }

}
