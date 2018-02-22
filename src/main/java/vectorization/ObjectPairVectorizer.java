package vectorization;

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
	 * @return a vector capturing the relative orientation of the two objects
	 *
	 * The returned vector should correspond to the similarity of two objects. I.e., the vectors are similar if and only
	 * if the objects are similar. This is usually not possible, therefore the implementation should try to violate this
	 * property as little as possible instead.
	 *
	 * Chebyshev distance is usually the first choice of a measure of vector similarity, as it corresponds to orthogonal
	 * range search. Euclidean or other distances could be used if a data structure for similarity search in such space
	 * is available.
	 *
	 * The author believes that for a moderate dimensionality (roughly 5 - 15 dimensions) and a fixed range, the most
	 * appropriate data structure is orthogonal grid, as implemented in OrthogonalGrid.
	 *
	 */
	public float[] vectorize(RigidBody a, RigidBody b) throws VectorizationException;
}
