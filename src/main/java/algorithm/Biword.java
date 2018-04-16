package algorithm;

import embedding.lipschitz.object.AlternativeMode;
import fragment.word.Word;
import fragment.biword.BiwordId;
import javax.vecmath.Point3d;
import geometry.primitives.Point;
import structure.Residue;
import util.Counter;
import embedding.lipschitz.object.AlternativePointTuples;
import embedding.lipschitz.object.PointTuple;
import geometry.GeometryUtil;

/**
 *
 * @author Antonin Pavelka
 */
public class Biword implements AlternativePointTuples {

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

	public Word getFirstWord() {
		return firstWord;
	}

	public Word getSecondWord() {
		return secondWord;
	}

	public Point getCenter() {
		return firstWord.getCenter().plus(secondWord.getCenter()).divide(2);
	}

	public Point3d getCenter3d() {
		Point p = getCenter();
		return new Point3d(p.getCoords());
	}

	public Point[] getPoints() {
		return GeometryUtil.merge(firstWord.getPoints(), secondWord.getPoints());
	}

	public Point3d[] getPoints3d() {
		return getPoints3d(firstWord, secondWord);
	}

	private Point3d[] getPoints3d(Word first, Word second) {
		return GeometryUtil.merge(first.getPoints3d(), second.getPoints3d());
	}

	public Residue[] getResidues() {
		Residue[] a = firstWord.getResidues();
		Residue[] b = secondWord.getResidues();
		return Residue.merge(a, b);
	}

	@Override
	public PointTuple getCanonicalTuple() {
		return new PointTuple(getPoints3d());
	}

	@Override
	public PointTuple getAlternative(int index, AlternativeMode alternativeMode) {
		BiwordAlternativeMode mode = (BiwordAlternativeMode) alternativeMode;
		if (mode.interchangeable && mode.invertible) {
			switch (index) {
				case 0:
					return getCanonicalTuple();
				case 1:
					return new PointTuple(getPoints3d(secondWord, firstWord));
				case 2: {
					Point3d[] inverted1 = invert(firstWord.getPoints3d());
					Point3d[] inverted2 = invert(secondWord.getPoints3d());
					return new PointTuple(GeometryUtil.merge(inverted1, inverted2));
				}
				case 3: {
					Point3d[] inverted1 = invert(firstWord.getPoints3d());
					Point3d[] inverted2 = invert(secondWord.getPoints3d());
					return new PointTuple(GeometryUtil.merge(inverted2, inverted1));
				}
				default:
					throw new RuntimeException();
			}
		} else if (mode.interchangeable) {
			switch (index) {
				case 0:
					return getCanonicalTuple();
				case 1:
					return new PointTuple(getPoints3d(secondWord, firstWord));
				default:
					throw new RuntimeException();
			}
		} else if (mode.invertible) {
			switch (index) {
				case 0:
					return getCanonicalTuple();
				case 1: {
					Point3d[] inverted1 = invert(firstWord.getPoints3d());
					Point3d[] inverted2 = invert(secondWord.getPoints3d());
					return new PointTuple(GeometryUtil.merge(inverted1, inverted2));
				}
				default:
					throw new RuntimeException();
			}
		} else {
			if (index != 0) {
				throw new RuntimeException();
			} else {
				return getCanonicalTuple();
			}
		}
	}

	private Point3d[] invert(Point3d[] a) {
		Point3d[] b = new Point3d[a.length];
		for (int i = 0; i < a.length; i++) {
			b[b.length - i - 1] = a[i];
		}
		return b;
	}
	
}
