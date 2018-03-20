package vectorization.force;

import structure.VectorizationException;
import vectorization.BallsDistanceVectorizer;
import vectorization.ObjectPairVectorizer;
import vectorization.RigidBody;
import vectorization.dimension.DimensionCyclic;
import vectorization.dimension.Dimensions;

/**
 * Adds a dimension and sets it to zero.
 */
public class ExtendedVectorizer implements ObjectPairVectorizer {

	private static double factor = 1;
	ObjectPairVectorizer vectorizer = new BallsDistanceVectorizer();
	Dimensions dimensions = vectorizer.getDimensions().merge(new Dimensions(new DimensionCyclic(-factor, factor)));
	int n = dimensions.number() - 1;
	int m = dimensions.number();

	@Override
	public float[] vectorize(RigidBody a, RigidBody b, int imageNumber) throws VectorizationException {
		float[] v = new float[m];
		float[] w = vectorizer.vectorize(a, b, imageNumber);
		System.arraycopy(w, 0, v, 0, n);
		return v;
	}

	@Override
	public int getNumberOfImages() {
		return 1;
	}

	@Override
	public Dimensions getDimensions() {
		return dimensions;
	}

}
