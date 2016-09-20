package pdb;

import java.io.Serializable;
import javax.vecmath.Point3d;

/**
 *
 * @author Antonin Pavelka
 */
public class CompactStructure implements Serializable {

    private PdbChainId id_;
    private Point3d[] centers_;

    public CompactStructure(PdbChainId id, Point3d[] centers) {
        id_ = id;
        centers_ = centers;
    }

    public PdbChainId getId() {
        return id_;
    }

    public Point3d[] getPoints() {
        return centers_;
    }

}
