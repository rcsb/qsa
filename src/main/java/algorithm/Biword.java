package algorithm;

import vectorization.dimension.Dimensions;
import fragment.Word;
import fragment.BiwordId;
import javax.vecmath.Point3d;
import geometry.primitives.Point;
import structure.VectorizationException;
import structure.Residue;
import util.Counter;
import vectorization.BiwordVectorizer;
import vectorization.QuaternionObjectPairVectorizerOldVersion;

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

	private static BiwordVectorizer vectorizer = new BiwordVectorizer(new QuaternionObjectPairVectorizerOldVersion());

	public static Dimensions getDimensions() {
		return vectorizer.getDimensions();
	}

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

	public Word getFirstWord() {
		return firstWord;
	}

	public Word getSecondWord() {
		return secondWord;
	}

	/**
	 * A complete description of a pair of 3-residue by 10 dimensional vector. Describes only C-alpha positions of outer
	 * residues, not rotation of their side chain.
	 */
	public float[] getVector(int imageNumber) throws VectorizationException {
		float[] vector = vectorizer.vectorize(this, imageNumber);
		return vector;
	}

	public int getNumberOfImages() {
		return vectorizer.getNumberOfImages();
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
