package vectorization;

import geometry.exceptions.CoordinateSystemException;
import geometry.primitives.CoordinateSystem;
import geometry.primitives.Point;
import geometry.superposition.Superposer;
import language.Pair;
import structure.VectorizationException;

/**
 *
 * @author Antonin Pavelka
 */
public class QuaternionObjectPairVectorizer implements ObjectPairVectorizer {

	@Override
	public float[] vectorize(RigidBody b1, RigidBody b2) throws VectorizationException {

		Superposer transformer = getTransformer(b1, b2);
		Point[] averaged = average(getSuperposedPoints(transformer));

		try {
			CoordinateSystem sharedSystem = createSystem(averaged);
//which system to express now
			//return transformer.getQuaternion().toFloat();
			return null;
		} catch (CoordinateSystemException ex) {
			throw new VectorizationException(ex);
		}
	}

	private Pair<Point[]> getSuperposedPoints(Superposer transformer) {
		Point[] x = transformer.getXPoints();
		Point[] y = transformer.getTransformedYPoints();
		return new Pair(x, y);
	}

	private Point[] average(Pair<Point[]> aligned) {
		Point[] x = aligned._1;
		Point[] y = aligned._2;
		Point[] averaged = new Point[x.length];
		for (int i = 0; i < averaged.length; i++) {
			averaged[i] = x[i].plus(y[i]).divide(2);
		}
		return averaged;
	}

	/* Superposes the second on the first. */
	private Superposer getTransformer(RigidBody b1, RigidBody b2) {
		Superposer transformer = new Superposer();
		transformer.set(b1.getAllPoints(), b2.getAllPoints());
		return transformer;
	}

	private CoordinateSystem createSystem(Point[] points) throws CoordinateSystemException {
		assert points.length == 3;
		Point origin = points[0];
		Point u = Point.vector(origin, points[1]);
		Point v = Point.vector(origin, points[2]);
		return new CoordinateSystem(origin, u, v);
	}

}
