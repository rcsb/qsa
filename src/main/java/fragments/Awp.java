package fragments;

import geometry.Coordinates;
import geometry.SmartTransformation;
import javax.vecmath.Matrix3d;

public class Awp implements Coordinates {

    private WordInterface x;
    private WordInterface y;
    private double rmsd;
    private Matrix3d m;
    private double[] coords;

    public Awp(WordInterface x, WordInterface y, double rmsd, Matrix3d m) {
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
