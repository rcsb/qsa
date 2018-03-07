package vectorization;

import geometry.exceptions.CoordinateSystemException;
import geometry.primitives.AxisAngle;
import geometry.primitives.AxisAngleFactory;
import geometry.primitives.CoordinateSystem;
import geometry.primitives.Point;
import geometry.superposition.Superposer;
import language.Pair;
import structure.VectorizationException;

/**
 *
 * @author Antonin Pavelka Possibly useful for 3D vectors, but 4D quaternions are probably better.
 */
@Deprecated
public class PoleGreenwichRotationVectorizer implements ObjectPairVectorizer {

	@Override
	public float[] vectorize(RigidBody b1, RigidBody b2) throws VectorizationException {
		try {
			Pair<CoordinateSystem> systems = new Pair(b1.getCoordinateSystem(), b2.getCoordinateSystem());

			float pole = getPoleDistance(systems);
			//float greenwich = getGreenwichDistance();
			//float rotation = getRotation();

			//float[] vector = {pole, greenwich, rotation};
			return null;
		} catch (CoordinateSystemException ex) {
			throw new VectorizationException(ex);
		}
	}

	private float getPoleDistance(Pair<CoordinateSystem> systems) {
		Point x1 = systems._1.getXAxis();
		Point x2 = systems._2.getXAxis();
		return 0;
	}

	private Pair<Point[]> getSuperposed(Superposer transformer) {
		Point[] x = transformer.getXPoints();
		Point[] y = transformer.getTransformedYPoints();
		return new Pair(x, y);
	}

	private AxisAngle getEigenvector(Superposer transformer) {
		AxisAngle axisAngle = AxisAngleFactory.toAxisAngle(transformer.getRotationMatrix());
		return axisAngle;
	}

	/* Superposes the second on the first. */
	private Superposer getTransformer(RigidBody b1, RigidBody b2) {
		Superposer transformer = new Superposer();
		transformer.set(b1.getAllPoints(), b2.getAllPoints());
		return transformer;
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

	private void print(Point[] a) {
		for (Point p : a) {
			System.out.println(p);
		}
	}

}
