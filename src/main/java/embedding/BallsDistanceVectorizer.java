package embedding;

import vectorization.dimension.Dimensions;
import geometry.exceptions.CoordinateSystemException;
import geometry.primitives.CoordinateSystem;
import geometry.primitives.Point;
import language.Util;
import structure.VectorizationException;
import vectorization.dimension.Dimension;
import vectorization.dimension.DimensionOpen;

/**
 *
 * @author Antonin Pavelka
 */
public class BallsDistanceVectorizer implements ObjectPairVectorizer {

	private static Dimensions dimensions = createDimensions();

	private static Dimensions createDimensions() {
		Dimension open = new DimensionOpen();
		return new Dimensions(
			open, open, open, // ball1
			open, open, open, // ball2
			open // distance
		);
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
		float[] ball1 = getBall(a, b);
		float[] ball2 = getBall(b, a);
		float[] distance = {getDistance(a, b)};
		return Util.merge(ball1, ball2, distance);
	}

	private float[] getBall(RigidBody a, RigidBody b) throws CoordinateSystemException {
		Point origin = a.getCenter();
		Point u = Point.vector(origin, a.getFirstAuxiliary());
		Point v = Point.vector(origin, a.getSecondAuxiliary());
		CoordinateSystem system = new CoordinateSystem(origin, u, v);
		Point position = system.expresPoint(b.getCenter());
		return position.normalize().getCoordsAsFloats();
	}

	private float getDistance(RigidBody a, RigidBody b) {
		return (float) a.getCenter().distance(b.getCenter());
	}

}
