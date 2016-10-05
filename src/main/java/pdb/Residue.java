package pdb;

import java.io.Serializable;

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
	private int index_;

	public Residue(int index, Group g) {
		for (Atom a : g.getAtoms()) {
			if (a.getName().toUpperCase().equals("CA")) {
				position_ = new Point(a.getCoords());
			}
		}
		index_ = index;
	}

	public Residue(int index, Point3d x) {
		index_ = index;
		position_ = new Point(x.x, x.y, x.z);
	}

	public Residue(int index, float x, float y, float z) {
		index_ = index;
		position_ = new Point(x, y, z);
	}

	/**
	 * WARNING: ignoring insertion codes.
	 *
	 * @return
	 */
	public int getIndex() {
		return index_;
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

	public int compareTo(Residue other) {
		return Integer.compare(index_, other.index_);
	}	
	
}
