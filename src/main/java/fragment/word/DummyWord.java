package fragment.word;

import fragment.Fragment;
import fragment.Fragment;
import geometry.primitives.Point;
import javax.vecmath.Point3d;
import structure.Residue;

public class DummyWord implements Fragment {

	private final int id;

	public DummyWord(int id) {
		this.id = id;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public Residue[] getResidues() {
		return null;
	}

	@Override
	public Point3d[] getPoints3d() {
		return null;
	}

	@Override
	public Point getCenter() {
		return null;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		Fragment other = (Fragment) o;
		return getId() == other.getId();
	}

	@Override
	public String toString() {
		return Integer.toString(id);
	}

	public int size() {
		return 0;
	}

}
