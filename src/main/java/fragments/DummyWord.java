package fragments;

import javax.vecmath.Point3d;
import pdb.Residue;

public class DummyWord implements Word {

	private int id;

	public DummyWord(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public Residue[] getResidues() {
		return null;
	}

	public Point3d[] getPoints3d() {
		return null;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		Word other = (Word) o;
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
