package pdb;

import java.io.Serializable;
import javax.vecmath.Point3d;

/**
 *
 * @author Antonin Pavelka
 */
public class CompactStructure implements Serializable {

    private PdbChain id_;
    private Point3d[] centers_;

    public CompactStructure(PdbChain id, Point3d[] centers) {
        id_ = id;
        centers_ = centers;
    }

    public PdbChain getId() {
        return id_;
    }

    public Point3d[] getPoints() {
        return centers_;
    }

}
