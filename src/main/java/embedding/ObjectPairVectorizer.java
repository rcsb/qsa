package embedding;

import vectorization.dimension.Dimensions;
import structure.VectorizationException;

/**
 *
 * @author Antonin Pavelka
 */
public interface ObjectPairVectorizer {

	/**
	 *
	 * @param a coordinates of the first object
	 * @param b coordinates of the second object
	 * @param imageNumber numbers larger than 0 will generate alternative vectors. This is needed for quaternions.
	 * Number of altarnatives is defined by getNumberOfImages().
	 * @return a vector capturing the relative orientation of the two objects
	 *
	 * The returned vector (a tuple of real numbers) should correspond to the similarity of two objects. I.e., the
	 * vectors are similar if and only if the objects are similar. This is usually not possible, therefore the
	 * implementation should try to violate this property as little as possible instead.
	 *
	 * Chebyshev distance is usually the first choice of a measure of vector similarity, as it corresponds to orthogonal
	 * range search. Euclidean or other distances could be used if a data structure for similarity search in such space
	 * is available (sublinear search) or objects are compared pair by pair (linear search).
	 *
	 * The author believes that for a moderate dimensionality (roughly 5 - 15 dimensions) and a fixed range,
	 * OrthogonalGrid is an appropriate data structure.
	 * @throws structure.VectorizationException
	 *
	 *
	 *
	 */
	public float[] vectorize(RigidBody a, RigidBody b, int imageNumber) throws VectorizationException;

	/**
	 * How many random vectors need to be generated for one of the objects to measure the distance correctly, as a
	 * minimum of all these alternative options.
	 *
	 * @return Number of alternative vectors needed to put the object into space.
	 */
	public int getNumberOfImages();

	public Dimensions getDimensions();
}
