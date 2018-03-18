package vectorization;

import algorithm.Biword;
import vectorization.dimension.Dimensions;
import fragment.Word;
import geometry.primitives.Point;
import language.Util;
import structure.VectorizationException;
import structure.Residue;
import vectorization.dimension.Dimension;
import vectorization.dimension.DimensionCyclic;

/**
 * Creates a tuple of real numbers representing Biword with the following property: Euclidean and Chebyshev distances
 * between tuples corresponds well to similarity of two Biword objects (e.g., RMSD).
 *
 * @author Antonin Pavelka
 */
public class BiwordVectorizer {

	private ObjectPairVectorizer vectorizer;

	private Dimensions dihedralAngles = getDihedralDimensions();
	private Dimensions dimensions;

	public BiwordVectorizer(ObjectPairVectorizer objectPairVectorizer) {
		this.vectorizer = objectPairVectorizer;
		this.dimensions = vectorizer.getDimensions().merge(dihedralAngles);
	}

	public Dimensions getDimensions() {
		return dimensions;
	}

	public int getNumberOfImages() {
		return vectorizer.getNumberOfImages();
	}

	public float[] vectorize(Biword biword, int imageNumber) throws VectorizationException {
		RigidBody b1 = createRigidBody(biword.getFirstWord());
		RigidBody b2 = createRigidBody(biword.getSecondWord());
		float[] orientation = vectorizer.vectorize(b1, b2, imageNumber);
		float[] dihedrals = getDihedrals(biword);
		return Util.merge(orientation, dihedrals);
	}

	private Dimensions getDihedralDimensions() {
		Dimension t = new DimensionCyclic(-Math.PI, Math.PI);
		return new Dimensions(t, t, t, t);
	}

	private float[] getDihedrals(Biword biword) {
		Residue r1 = biword.getFirstWord().getCentralResidue();
		Residue r2 = biword.getSecondWord().getCentralResidue();
		float[] dihedrals = {
			r1.getPhi().floatValue(),
			r1.getPsi().floatValue(),
			r2.getPhi().floatValue(),
			r2.getPsi().floatValue()
		};
		return dihedrals;
	}

	private RigidBody createRigidBody(Word word) throws VectorizationException {
		Residue residue = word.getCentralResidue();
		Point ca = residue.getC();
		Point c = residue.getC();
		Point n = residue.getN();
		return RigidBody.createWithCenter(ca, c, n);
	}

}
