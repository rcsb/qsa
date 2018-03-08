package vectorization;

import geometry.exceptions.CoordinateSystemException;
import geometry.primitives.CoordinateSystem;
import geometry.primitives.Point;
import geometry.primitives.Versor;
import geometry.superposition.Superposer;
import language.Pair;
import structure.VectorizationException;

/**
 *
 * @author Antonin Pavelka
 */
public class QuaternionObjectPairVectorizer implements ObjectPairVectorizer {
	// TODO st like triangles or origin + u,v instead of RigidBody

	@Override
	public float[] vectorize(RigidBody b1, RigidBody b2) throws VectorizationException {
		try {
			return vectorizeUncatched(b1, b2);
		} catch (CoordinateSystemException ex) {
			throw new VectorizationException(ex);
		}
	}

	private float[] vectorizeUncatched(RigidBody b1, RigidBody b2) throws VectorizationException, CoordinateSystemException {
		CoordinateSystem system = computeCoordinateSystem(b1, b2);
		Pair<Point[]> expressed = express(b1, b2, system);
		float[] rotation = getRotation(expressed);
		float[] translation = getTranslation(expressed);

		//Point translation = system.expresPoint(b2.getCenter()); // captures the translation
		//return merge(translation.getCoordsAsFloats(), transformer.getQuaternion().toFloats());
		return rotation;

	}

	private CoordinateSystem computeCoordinateSystem(RigidBody b1, RigidBody b2) throws CoordinateSystemException {
		Pair<Point[]> superposed = getSuperposedPoints(b1, b2);
		Point[] averaged = average(superposed);
		return createSystem(averaged); // positioned essentially at b1		
	}

	/* Superposed onto first. */
	private Pair<Point[]> getSuperposedPoints(RigidBody b1, RigidBody b2) {
		Superposer transformer = getTransformer(b1, b2);
		Point[] x = transformer.getXPoints();
		Point[] y = transformer.getTransformedYPoints();
		return new Pair(x, y);
	}

	/* Superposes the second on the first. */
	private Superposer getTransformer(RigidBody b1, RigidBody b2) {
		Superposer transformer = new Superposer();
		transformer.set(b1.getAllPoints(), b2.getAllPoints());
		transformer.getMatrix();
		return transformer;
	}

	private Point[] average(Pair<Point[]> superposed) {
		Point[] x = superposed._1;
		Point[] y = superposed._2;
		Point[] averaged = new Point[x.length];
		for (int i = 0; i < averaged.length; i++) {
			averaged[i] = x[i].plus(y[i]).divide(2);
		}
		return averaged;
	}

	private CoordinateSystem createSystem(Point[] points) throws CoordinateSystemException {
		Point origin = points[0];
		Point u = Point.vector(origin, points[1]);
		Point v = Point.vector(origin, points[2]);
		return new CoordinateSystem(origin, u, v);
	}

	private Pair<Point[]> express(RigidBody b1, RigidBody b2, CoordinateSystem system) {
		Point[] body1 = b1.getAllPoints();
		Point[] body2 = b2.getAllPoints();
		Pair<Point[]> points = new Pair(new Point[3], new Point[3]);
		for (int i = 0; i < 3; i++) {
			points._1[i] = system.expresPoint(body1[i]);
			points._2[i] = system.expresPoint(body2[i]);
		}
		return points;
	}

	private float[] getRotation(Pair<Point[]> pair) {
		Superposer transformer = new Superposer();
		transformer.set(pair._1, pair._2);
		Versor versor = transformer.getQuaternion();
		return versor.toFloats();
	}

	private float[] getTranslation(Pair<Point[]> pair) {
		Point otherOrigin = pair._2[0];
		return otherOrigin.getCoordsAsFloats();
	}

	private float[] merge(float[] a, float[] b) {
		float[] c = new float[a.length + b.length];
		for (int i = 0; i < a.length; i++) {
			c[i] = a[i];
		}
		for (int i = 0; i < b.length; i++) {
			c[i + a.length] = b[i];
		}
		return c;
	}

}
