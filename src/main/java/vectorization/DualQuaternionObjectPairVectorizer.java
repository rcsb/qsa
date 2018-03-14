package vectorization;

import biword.index.Dimensions;
import geometry.exceptions.CoordinateSystemException;
import geometry.primitives.CoordinateSystem;
import geometry.primitives.Point;
import geometry.superposition.Superposer;
import info.laht.dualquat.Quaternion;
import language.Pair;
import language.Util;
import structure.VectorizationException;

/**
 *
 * Transforms a pair of rigid bodies into a tuple of real numbers capturing their orientation: Euclidean and Chebyshev
 * distance between the tuples corresponds well to RMSD between two pairs of rigid bodies.
 *
 * Both rigid bodies are required to be similar, at least so that their superposition cannot be ambiguous.
 *
 * @author Antonin Pavelka
 */
public class DualQuaternionObjectPairVectorizer implements ObjectPairVectorizer {

	private static Dimensions dimensions = new Dimensions(
		false, false, false, false,
		false, false, false, false);

	@Override
	public int getNumberOfImages() {
		return 4; // quaternion and its image on the oposite side of 4D sphere
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

	private float[] vectorizeUncatched(RigidBody b1, RigidBody b2, int imageNumber) throws CoordinateSystemException {
		CoordinateSystem system = computeCoordinateSystem(b1, b2);
		Pair<Point[]> expressed = express(b1, b2, system);

		Quaternion rotation = getRotation(expressed, imageNumber);
		//float[] translation = getTranslation(expressed, imageNumber);
		
		Point t = b1.getCenter().minus(b2.getCenter());
		Quaternion translation = new Quaternion(t.x, t.y, t.z, 0);
		
		translation = translation.multiplyScalar(0.5);
		translation = translation.multiply(rotation);

		if (imageNumber == 1 ) {
			rotation = rotation.negate();
		}
		
		if (imageNumber == 2) {
			translation = translation.negate();
		}
		
		if (imageNumber == 3) {
			rotation = rotation.negate();
			translation = translation.negate();
		}
		//return Util.merge(rotation.toFloats(), translation.toFloats());
		return Util.merge(rotation.toFloats(), translation.toFloats());
	}

	private CoordinateSystem computeCoordinateSystem(RigidBody b1, RigidBody b2) throws CoordinateSystemException {
		Pair<Point[]> superposed = getSuperposedPoints(b1, b2);
		Point[] averaged = average(superposed);
		return createSystem(averaged); // positioned essentially at b1		
	}

	private Pair<Point[]> getSuperposedPoints(RigidBody b1, RigidBody b2) {
		Superposer superposer = superpose(b1, b2);
		Point[] x = superposer.getXPoints();
		Point[] y = superposer.getTransformedYPoints();
		return new Pair(x, y);
	}

	private Superposer superpose(RigidBody b1, RigidBody b2) {
		Superposer transformer = new Superposer();
		transformer.set(b1.getAllPoints(), b2.getAllPoints());
		transformer.getMatrix();
		return transformer;
	}

	// to decrease risk of the first object being colinear, it is less likely both would be colinear
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

	// Expresses the coordinates of b1, b2 in the new coordinate system.
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

	// rotation exactly captured by one of two possible versors
	private Quaternion getRotation(Pair<Point[]> pair, int imageNumber) {
		//assert imageNumber == 0 || imageNumber == 1;
		Superposer transformer = new Superposer();
		transformer.set(pair._1, pair._2); // !!!
		//transformer.set(pair._2, pair._1); // !!!
		Quaternion quaternion = transformer.getQuaternion();

		return quaternion;
	}

	private float[] getTranslation(Pair<Point[]> pair, int imageNumber) {
		Point otherOrigin = pair._1[0].minus(pair._2[0]);
		Point unit = otherOrigin.normalize();
		Quaternion q = new Quaternion();
		q.setFromUnitVectors(new Point(1, 0, 0), unit);
		if (imageNumber == 0 || imageNumber == 1) {
			q = q.negate();
		}
		float[] size = {(float) otherOrigin.size()};
		return Util.merge(q.toFloats(), size);

		/*Quaternion translation = new Quaternion(unit.x, unit.y, unit.z, 0);
		//Quaternion translation = new Quaternion(0, otherOrigin.x, otherOrigin.y, otherOrigin.z);
		return translation;*/
	}

}
