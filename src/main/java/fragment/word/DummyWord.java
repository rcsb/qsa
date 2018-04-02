package fragment.word;

import geometry.primitives.Point;
import javax.vecmath.Point3d;
import structure.Residue;
import fragment.FragmentOfPolymer;
import fragment.FragmentOfPolymer;

public class DummyWord implements FragmentOfPolymer {

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
		FragmentOfPolymer other = (FragmentOfPolymer) o;
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
