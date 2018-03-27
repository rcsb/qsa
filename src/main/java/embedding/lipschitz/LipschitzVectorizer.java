package embedding.lipschitz;

import embedding.*;
import vectorization.dimension.Dimensions;
import geometry.exceptions.CoordinateSystemException;
import language.Util;
import structure.VectorizationException;
import vectorization.dimension.Dimension;
import vectorization.dimension.DimensionOpen;
import vectorization.force.RigidBodyPair;

/**
 *
 * @author Antonin Pavelka
 */
public class LipschitzVectorizer implements ObjectPairVectorizer {

	Bases bases = new Bases(20);
	private Dimensions dimensions = createDimensions();

	private Dimensions createDimensions() {
		Dimension open = new DimensionOpen();
		Dimension[] dimensions = new Dimension[bases.size() + 1];
		for (int i = 0; i < dimensions.length; i++) {
			dimensions[i] = open;
		}
		return new Dimensions(dimensions);
	}

	@Override
	public int getNumberOfImages() {
		return 1;
	}

	@Override
	public Dimensions getDimensions() {
		return dimensions;
	}

	@Override
	public float[] vectorize(RigidBody b1, RigidBody b2, int imageNumber) throws VectorizationException {
		try {
			return vectorizeUncatched(b1, b2, imageNumber);
		} catch (CoordinateSystemException ex) {
			throw new VectorizationException(ex);
		}
	}

	private float[] vectorizeUncatched(RigidBody a, RigidBody b, int imageNumber) throws CoordinateSystemException {
		RigidBodyPair pair = new RigidBodyPair(a, b);
		float[] lipschitz = getLipschitzVector(a, b);
		float[] distance = {getDistance(a, b)};
		return Util.merge(lipschitz, distance);
	}

	private float[] getLipschitzVector(RigidBody a, RigidBody b) {
		RigidBodyPair pair = new RigidBodyPair(a, b);
		float[] vector = new float[bases.size()];
		for (int i = 0; i < bases.size(); i++) {
			vector[i] = (float) bases.getCoordinate(i, pair);
		}
		return vector;
	}

	private float getDistance(RigidBody a, RigidBody b) {
		return (float) a.getCenter().distance(b.getCenter());
	}
}
