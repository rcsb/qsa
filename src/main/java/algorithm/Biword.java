package algorithm;

import fragment.Word;
import fragment.BiwordId;
import geometry.CoordinateSystem;
import javax.vecmath.Point3d;
import geometry.Point;
import javax.vecmath.Matrix3d;
import structure.BackboneNotFound;
import structure.Residue;
import util.Counter;

/**
 *
 * @author Antonin Pavelka
 */
public class Biword {

	public static long count;
	private int idWithinStructure;
	private int structureId;

	private Word firstWord;
	private Word secondWord;

	/**
	 * For Kryo.
	 */
	public Biword() {

	}

	public Biword(int structureId, Counter idWithinStructure, Word first, Word second) {
		this.structureId = structureId;
		this.idWithinStructure = idWithinStructure.value();
		idWithinStructure.inc();
		this.firstWord = first;
		this.secondWord = second;
		count++;
	}

	/**
	 *
	 * Compact representation, so that the object can be found when SimpleStructure and Biwords are deserialized.
	 */
	public BiwordId getId() {
		return new BiwordId(structureId, idWithinStructure);
	}

	public int getIdWithingStructure() {
		return idWithinStructure;
	}

	public int getStructureId() {
		return structureId;
	}

	public Biword switchWords(Counter idWithinStructure) {
		Biword bw = new Biword(getStructureId(), idWithinStructure, secondWord, firstWord);
		return bw;
	}

	public Word[] getWords() {
		Word[] w = {firstWord, secondWord};
		return w;
	}

	public Word getWordA() {
		return firstWord;
	}

	public Word getWordB() {
		return secondWord;
	}

	/**
	 * A complete description of a pair of 3-residue by 10 dimensional vector. Decribes only C-alpha positions of outer
	 * residues, not rotation of their side chain.
	 */
	public float[] getSmartVector() {
		try {
			Residue ar = firstWord.getCentralResidue();
			Residue br = secondWord.getCentralResidue();
			//SuperPositionQCP qcp = new SuperPositionQCP();
			//qcp.set(PointConversion.getPoints3d(ar.getPhiPsiAtoms()), PointConversion.getPoints3d(br.getPhiPsiAtoms()));
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

			//double[] polar1 = CoordinateSystem.getPointAsPolar(other1);
			//double[] polar2 = CoordinateSystem.getPointAsPolar(other2);
			if (ar.getPhi() == null || br.getPhi() == null || ar.getPsi() == null || br.getPsi() == null) {
				return null;
			}
			double[] vector = {
				ar.getPhi(), br.getPhi(), ar.getPsi(), br.getPsi(),
				//toDegrees(euler[0]), toDegrees(euler[1]), toDegrees(euler[2]),
				//polar1[0], toDegrees(polar1[1]), toDegrees(polar1[2]),
				//polar2[0], toDegrees(polar2[1]), toDegrees(polar2[2])
				other1.x, other1.y, other1.z,
				other2.x, other2.y, other2.z
			};
			float[] fv = new float[vector.length];
			for (int i = 0; i < vector.length; i++) {
				fv[i] = (float) vector[i];
			}
			return fv;
		} catch (BackboneNotFound ex) { // TODO solve getCaCN not found better, return nulls or so
			return null;
		}
	}

	private double toDegrees(double radians) {
		return radians / Math.PI * 180;
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

	public Point getCenter() {
		return firstWord.getCenter().plus(secondWord.getCenter()).divide(2);
	}

	public Point3d getCenter3d() {
		Point p = getCenter();
		return new Point3d(p.getCoords());
	}

	public Point[] getPoints() {
		Point[] aps = firstWord.getPoints();
		Point[] bps = secondWord.getPoints();
		Point[] ps = new Point[aps.length + bps.length];
		System.arraycopy(aps, 0, ps, 0, aps.length);
		System.arraycopy(bps, 0, ps, aps.length, bps.length);
		return ps;
	}

	public Point3d[] getPoints3d() {
		Point3d[] a = firstWord.getPoints3d();
		Point3d[] b = secondWord.getPoints3d();
		Point3d[] c = new Point3d[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}

	public Residue[] getResidues() {
		Residue[] a = firstWord.getResidues();
		Residue[] b = secondWord.getResidues();
		return Residue.merge(a, b);
	}
}
