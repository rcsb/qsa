package fragments;

import geometry.CoordinateSystem;
import geometry.Coordinates;
import javax.vecmath.Point3d;
import geometry.Point;
import javax.vecmath.Matrix3d;
import pdb.BackboneNotFound;
import pdb.Residue;
import pdb.SimpleStructure;
import spark.clustering.Clusterable;
import vectorization.SmartVectorizer;

/**
 *
 * @author Antonin Pavelka
 */
public class Biword implements Clusterable<Biword>, Coordinates {

	public static int DIMENSION = 6;
	public static long count;
	private final WordImpl a_;
	private final WordImpl b_;
	private final float wordDistance;
	private final float[] coords;
	private static final long serialVersionUID = 1L;
	private static final double maxWdd = Parameters.create().getMaxWordDistDiff();
	private static final double maxWr = Parameters.create().getMaxWordRmsd();
	//private Point3d[] ps3d;

	public Biword(WordImpl a, WordImpl b) {
		a_ = a;
		b_ = b;
		wordDistance = (float) a.getCenter().distance(b.getCenter());
		SmartVectorizer av = new SmartVectorizer(a_);
		SmartVectorizer bv = new SmartVectorizer(b_);
		if (false) {
			coords = new float[DIMENSION];
			coords[0] = (float) av.firstHalf().distance(bv.firstHalf());
			coords[1] = (float) av.secondHalf().distance(bv.secondHalf());
			coords[2] = (float) av.firstHalf().distance(bv.secondHalf());
			coords[3] = (float) av.secondHalf().distance(bv.firstHalf());
			coords[4] = (float) av.getStraightness();
			coords[5] = (float) bv.getStraightness();
		} else {
			coords = null;
		}

		//getSmartCoords(); // !!!!!!!!!!!!!!!!!!!!!!!!
		count++;
	}

	public Biword switchWords() {
		return new Biword(b_, a_);
	}

	public SimpleStructure getStructure() {
		return a_.getStructure();
	}

	public WordImpl[] getWords() {
		WordImpl[] w = {a_, b_};
		return w;
	}

	@Override
	public double[] getCoords() {
		double[] ds = new double[coords.length];
		for (int i = 0; i < ds.length; i++) {
			ds[i] = coords[i];
		}
		return ds;
	}

	/**
	 * A complete description of a pair of 3-residue by 10 dimensional vector. Decribes only C-alpha positions of outer
	 * residues, not rotation of their side chain.
	 */
	public double[] getSmartVector() {
		try {
			Residue ar = a_.getCentralResidue();
			Residue br = b_.getCentralResidue();
			//SuperPositionQCP qcp = new SuperPositionQCP();
			//qcp.set(ar.getCaCN(), br.getCaCN());
			//double rmsd = qcp.getRmsd();
			//double[] euler = getXYZEuler(qcp.getRotationMatrix());
			//if (rmsd > 0.1) {
			//System.out.println("backbone RMSD = " + rmsd);
			/*	System.out.println(br.getId());
			System.out.println(ar.getId());
			System.out.println(ar.getCaCN()[0] + " !");
			System.out.println(ar.getCaCN()[1] + " !");
			System.out.println(ar.getCaCN()[2] + " !");
			System.out.println(ar.getCaCN()[3] + " !");
			System.out.println(br.getCaCN()[0] + " !");
			System.out.println(br.getCaCN()[1] + " !");
			System.out.println(br.getCaCN()[2] + " !");
			System.out.println(br.getCaCN()[3] + " !");*/
			//}

			CoordinateSystem cs = new CoordinateSystem(ar.getCaCNPoints()); // first point as origin
			Point other1 = cs.expresPoint(new Point(br.getAtom("CA")));

			cs = new CoordinateSystem(br.getCaCNPoints()); // second point as origin
			Point other2 = cs.expresPoint(new Point(ar.getAtom("CA")));

			//double[] polar = CoordinateSystem.getPointAsPolar(other);
			if (ar.getPhi() == null || br.getPhi() == null || ar.getPsi() == null || br.getPsi() == null) {
				return null;
			}
			double[] vector = {
				ar.getPhi(), br.getPhi(), ar.getPsi(), br.getPsi(),
				//euler[0], euler[1], euler[2],
				//polar[0], polar[1], polar[2]
				other1.x, other1.y, other1.z,
				other2.x, other2.y, other2.z
			};
			return vector;
		} catch (BackboneNotFound ex) { // TODO solve getCaCN not found better, return nulls or so
			System.err.println("Backbone not found.");
			return null;
		}
	}

	/**
	 * From the package org.biojava.nbio.structure.Calc.
	 *
	 * Convert a rotation Matrix to Euler angles. This conversion uses conventions as described on page:
	 * http://www.euclideanspace.com/maths/geometry/rotations/euler/index.htm Coordinate System: right hand Positive
	 * angle: right hand Order of euler angles: heading first, then attitude, then bank
	 *
	 * @param m the rotation matrix
	 * @return a array of three doubles containing the three euler angles in radians
	 */
	public static final double[] getXYZEuler(Matrix3d m) {
		double heading, attitude, bank;
		// Assuming the angles are in radians.
		if (m.m10 > 0.998) { // singularity at north pole
			heading = Math.atan2(m.m02, m.m22);
			attitude = Math.PI / 2;
			bank = 0;
		} else if (m.m10 < -0.998) { // singularity at south pole
			heading = Math.atan2(m.m02, m.m22);
			attitude = -Math.PI / 2;
			bank = 0;
		} else {
			heading = Math.atan2(-m.m20, m.m00);
			bank = Math.atan2(-m.m12, m.m11);
			attitude = Math.asin(m.m10);
		}
		return new double[]{heading, attitude, bank};
	}

	public double[] coordDiff(Biword other) {
		double[] diff = new double[coords.length];
		for (int i = 0; i < coords.length; i++) {
			diff[i] = Math.abs(coords[i] - other.coords[i]);
		}
		return diff;
	}

	public boolean isSimilar(Biword other, WordMatcher wm) {
		if (Math.abs(wordDistance - other.wordDistance) <= maxWdd) {
			if (wm.getRmsd(a_.getId(), other.a_.getId()) <= maxWr) {
				if (wm.getRmsd(b_.getId(), other.b_.getId()) <= maxWr) {
					return true;
				}
			}

		}
		return false;
	}

	public Point getCenter() {
		return a_.getCenter().plus(b_.getCenter()).divide(2);
	}

	public Point3d getCenter3d() {
		Point p = getCenter();
		return new Point3d(p.getCoords());
	}

	public Point[] getPoints() {
		Point[] aps = a_.getPoints();
		Point[] bps = b_.getPoints();
		Point[] ps = new Point[aps.length + bps.length];
		System.arraycopy(aps, 0, ps, 0, aps.length);
		System.arraycopy(bps, 0, ps, aps.length, bps.length);
		return ps;
	}
	
	public Point[] getPhiPsiPoints() {
		Point[] aps = a_.getCentralResidue().getPhiPsiAtoms();
		Point[] bps = b_.getCentralResidue().getPhiPsiAtoms();
		Point[] ps = new Point[aps.length + bps.length];
		System.arraycopy(aps, 0, ps, 0, aps.length);
		System.arraycopy(bps, 0, ps, aps.length, bps.length);
		return ps;
	}

	public Point3d[] getPoints3d() {
		Point3d[] a = a_.getPoints3d();
		Point3d[] b = b_.getPoints3d();
		Point3d[] c = new Point3d[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
		/*if (ps3d == null) {
			Point[] ps = getPoints();
			ps3d = new Point3d[ps.length];
			for (int i = 0; i < ps.length; i++) {
				ps3d[i] = new Point3d(ps[i].getCoords());
			}
		}
		return ps3d;*/
	}

	public Residue[] getResidues() {
		Residue[] a = a_.getResidues();
		Residue[] b = b_.getResidues();
		return Residue.merge(a, b);
	}

}
