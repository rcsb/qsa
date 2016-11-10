package pdb;

import java.io.Serializable;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Group;

import geometry.Point;

/**
 *
 * @author Antonin Pavelka
 *
 *         Encapsulates Group to provide application specific functionality.
 *
 */
public class Residue implements Serializable, Comparable<Residue> {

	private static final long serialVersionUID = 1L;
	private Point position_;
	private ResidueId id_;
	private int atomSerial;

	public Residue(ResidueId index, int atomSerial, Group g) {
		for (Atom a : g.getAtoms()) {
			if (a.getName().toUpperCase().equals("CA")) {
				position_ = new Point(a.getCoords());
			}
		}
		id_ = index;
		this.atomSerial = atomSerial;
	}

	public Residue(ResidueId index, int atomSerial, Point3d x) {
		id_ = index;
		this.atomSerial = atomSerial;
		position_ = new Point(x.x, x.y, x.z);
	}

	public Residue(ResidueId index, int atomSerial, float x, float y, float z) {
		id_ = index;
		this.atomSerial = atomSerial;
		position_ = new Point(x, y, z);
	}

	public Residue(Residue r) {
		position_ = new Point(r.position_);
		id_ = r.id_;
		atomSerial = r.atomSerial;
	}

	/**
	 * This would be named getId() if Eclipse refactoring did not suck so much.
	 */
	@Deprecated
	public ResidueId getIndex() {
		return id_;
	}

	public ResidueId getId() {
		return id_;
	}

	public int getAtomSerial() {
		return atomSerial;
	}

	public Point getPosition() {
		return position_;
	}

	public Point3d getPosition3d() {
		return new Point3d(position_.getX(), position_.getY(), position_.getZ());
	}

	public double distance(Residue other) {
		return position_.distance(other.position_);
	}

	public double[] getCoords() {
		return position_.getCoords();
	}

	public void transform(Matrix4d m) {
		Point3d x = getPosition3d();
		m.transform(x);
		position_ = new Point(x.x, x.y, x.z);
	}

	@Override
	public boolean equals(Object o) {
		Residue other = (Residue) o;
		return id_.equals(other.id_);
	}

	@Override
	public int hashCode() {
		return id_.hashCode();
	}

	@Override
	public int compareTo(Residue other) {
		return id_.compareTo(other.id_);
	}

	public static Residue[] merge(Residue[] a, Residue[] b) {
		Residue[] c = new Residue[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}

}
