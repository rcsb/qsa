package fragments;

import javax.vecmath.Point3d;
import pdb.Residue;

public interface WordInterface {

	public int getId();

	public Residue[] getResidues();

	public Point3d[] getPoints3d();
}
