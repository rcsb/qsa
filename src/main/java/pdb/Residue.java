package pdb;

import java.io.Serializable;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

import org.biojava.nbio.structure.Atom;

import geometry.Point;
import util.Counter;

/**
 *
 * @author Antonin Pavelka
 *
 * Encapsulates Group to provide application specific functionality.
 *
 */
public class Residue implements Serializable, Comparable<Residue> {

	private static final long serialVersionUID = 1L;
	private Point position_;
	private final ResidueId id_;
	private final int atomSerial;
	private double[][] atoms;
	private String[] atomNames;
	private Double phi;
	private Double psi;
	private Atom[] phiPsiAtoms;
	private Residue next;
	private Residue previous;
	private final int index; // unique id withing structure, 0 .. n - 1, where n is number of residues

	/*public Residue(int index, ResidueId id, int atomSerial, Point3d x) {
		this.index = index;
		this.id_ = id;
		this.atomSerial = atomSerial;
		this.position_ = new Point(x.x, x.y, x.z);
	}

	public Residue(int index, ResidueId id, int atomSerial, float x, float y, float z) {
		this.index = index;
		this.id_ = id;
		this.atomSerial = atomSerial;
		this.position_ = new Point(x, y, z);
	}

	public Residue(int index, ResidueId id, int atomSerial, double x, double y, double z) {
		this.index = index;
		this.id_ = id;
		this.atomSerial = atomSerial;
		this.position_ = new Point(x, y, z);
	}*/

	public Residue(int index, ResidueId id, int atomSerial, double[] carbonAlpha, double[][] atoms,
		String[] atomNames, Double phi, Double psi, Atom[] phiPsiAtoms) {
		this.index = index;
		this.id_ = id;
		this.atomSerial = atomSerial;
		this.position_ = new Point(carbonAlpha[0], carbonAlpha[1], carbonAlpha[2]);
		this.atoms = atoms;
		this.atomNames = atomNames;
		this.phi = phi;
		this.psi = psi;
		this.phiPsiAtoms = phiPsiAtoms;
	}

	public Residue(Residue r) {
		index = r.index;
		position_ = new Point(r.position_);
		id_ = r.id_;
		atomSerial = r.atomSerial;
	}

	public void setNext(Residue r) {
		next = r;
	}

	public void setPrevious(Residue r) {
		previous = r;
	}

	private boolean isWithinAhead(Residue r, int distance) {
		int d = 0;
		Residue q = this;
		while (q != null && d <= distance) {
			if (r.equals(q)) {
				return true;
			}
			q = q.next;
			d++;
		}
		return false;
	}

	public boolean isWithin(Residue r, int distance) {
		return this.isWithinAhead(r, distance) || r.isWithinAhead(this, distance);
	}

	public Residue getNext() {
		return next;
	}

	public Residue getPrevious() {
		return previous;
	}

	public Atom[] getPhiPsiAtoms() {
		return phiPsiAtoms;
	}

	public boolean follows(Residue next) {
		return next.getId().follows(getId());
	}

	public int getIndex() {
		return index;
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
		return new Point3d(position_.x, position_.y, position_.z);
	}

	public double[][] getAtoms() {
		return atoms;
	}

	public double[] getAtom(String name) {
		for (int i = 0; i < atoms.length; i++) {
			if (atomNames[i].equals(name)) {
				return atoms[i];
			}
		}
		return null;
	}

	private Point3d p(double[] c) {
		return new Point3d(c[0], c[1], c[2]);
	}

	public Point3d[] getCaCN() {
		Point3d[] backbone = {p(getAtom("CA")), p(getAtom("C")), p(getAtom("N"))};
		return backbone;
	}

	public Point getCa() {
		return new Point(getAtom("CA"));
	}

	public Point[] getCaCNPoints() throws BackboneNotFound {
		Point[] backbone;
		try {
			Point[] back = {new Point(getAtom("CA")), new Point(getAtom("C")), new Point(getAtom("N"))};
			backbone = back;
		} catch (Exception ex) {
			throw new BackboneNotFound();
		}
		double a = backbone[0].distance(backbone[1]);
		double b = backbone[2].distance(backbone[1]);
		double c = backbone[0].distance(backbone[2]);
		double min = 1.0;
		double max = 2.6;
		if (a < min || a > max || b < 1.2 || b > 3.4 || c < min || c > max) {
			System.err.println("CaCN suspicios distances: " + a + " " + b + " " + c);
		}
		Point u = backbone[1].minus(backbone[0]);
		Point v = backbone[2].minus(backbone[0]);
		double dot = u.normalize().dot(v.normalize());

		//System.out.println(" dot " + dot);
		if (Math.abs(dot) < 0.01) {
			System.err.println("Warning: colinear backbone.");
			//throw new RuntimeException("" + dot);
		}
		return backbone;
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

	public Double getPhi() {
		return phi;
	}

	public Double getPsi() {
		return psi;
	}

	public static Residue[] merge(Residue[] a, Residue[] b) {
		Residue[] c = new Residue[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}

}
