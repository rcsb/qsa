package fragment;

import geometry.primitives.Point;
import javax.vecmath.Point3d;
import structure.Residue;

public interface FragmentOfPolymer {

	public int getId();

	public Residue[] getResidues();

	public Point3d[] getPoints3d();

	public Point getCenter();

	public int size();
}
