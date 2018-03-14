package vectorization;

import biword.index.Dimensions;
import geometry.exceptions.CoordinateSystemException;
import geometry.primitives.AxisAngle;
import geometry.primitives.CoordinateSystem;
import geometry.primitives.Point;
import geometry.primitives.Versor;
import geometry.superposition.Superposer;
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
public class QuaternionObjectPairVectorizer implements ObjectPairVectorizer {

	private static Dimensions dimensions = new Dimensions(
		false, false, false, false, // quaternion for object rotations
		false, false, false, /* unit vector in cartesian coordinates representing center-center line in coordinate 
		system of first object (but averaged with superposed second object) */
		false); // distance center-center

	@Override
	public int getNumberOfImages() {
		return 2; // quaternion and its image on the oposite side of 4D sphere
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
		CoordinateSystem system = computeCoordinateSystem(b1, b2, imageNumber);
		Pair<RigidBody> expressed = new Pair(b1.express(system), b2.express(system));
		float[] rotation = getRotation(expressed, imageNumber);
		float[] translation = getTranslation(expressed);
		return Util.merge(rotation, translation);
	}

	/*private CoordinateSystem computeCoordinateSystemB1(RigidBody b1, RigidBody b2) throws CoordinateSystemException {
		Pair<Point[]> superposed = getSuperposedPointsB1(b1, b2);
		Point[] averaged = average(superposed);
		return createSystem(averaged); // positioned essentially at b1		
	}*/

	private CoordinateSystem computeCoordinateSystem(RigidBody b1, RigidBody b2, int imageNumber) throws CoordinateSystemException {
		RigidBody bodyCentered1 = b1.center();
		RigidBody bodyCentered2 = b2.center();
		Superposer superposer = new Superposer(true); // bodies are in origin already
		superposer.set(bodyCentered1.getAllPoints(), bodyCentered2.getAllPoints()); // rotation 2 -> 1
		superposer.getMatrix();
		
		Versor v = superposer.getVersor();
		if (imageNumber == 1) {
			v = v.negate();
		}
		Versor angularAverage = halfRotation(v);
		RigidBody rotated1 = bodyCentered1.rotate(angularAverage.inverse());
		RigidBody rotated2 = bodyCentered1.rotate(angularAverage); // rotate in the oposite direction
		
		// TODO check they are superposed?
		
		RigidBody averaged = rotated1.average(rotated2);
		// now x and y are superposed, rotated 
		//CoordinateSystem system = createSystem(averaged.getAllPoints());
		CoordinateSystem system = createSystem(b2.getAllPoints());
		
		
		
		return system;
	}
	
	private Versor halfRotation(Versor v) {
		AxisAngle whole = v.toAngleAxis();
		AxisAngle half = new AxisAngle(whole.getAxis(), whole.getAngle() / 2);
		return Versor.create(half);
	}

	/*private Pair<Point[]> getSuperposedPointsB1(RigidBody b1, RigidBody b2) {
		Superposer superposer = superpose(b1, b2);
		Point[] x = superposer.getXPoints();
		Point[] y = superposer.getTransformedYPoints();
		return new Pair(x, y);
	}*/


	// to decrease risk of the first object being colinear, it is less likely both would be colinear
	/*private Point[] average(Pair<Point[]> superposed) {
		Point[] x = superposed._1;
		Point[] y = superposed._2;
		Point[] averaged = new Point[x.length];
		for (int i = 0; i < averaged.length; i++) {
			averaged[i] = x[i].plus(y[i]).divide(2);
		}
		return averaged;
	}*/

	private CoordinateSystem createSystem(Point[] points) throws CoordinateSystemException {
		Point origin = points[0];
		Point u = Point.vector(origin, points[1]);
		Point v = Point.vector(origin, points[2]);
		return new CoordinateSystem(origin, u, v);
	}

	// Expresses the coordinates of b1, b2 in the new coordinate system.
	/*private Pair<Point[]> express(RigidBody b1, RigidBody b2, CoordinateSystem system) {
		Point[] body1 = b1.getAllPoints();
		Point[] body2 = b2.getAllPoints();
		Pair<Point[]> points = new Pair(new Point[3], new Point[3]);
		for (int i = 0; i < 3; i++) {
			points._1[i] = system.expresPoint(body1[i]);
			points._2[i] = system.expresPoint(body2[i]);
		}
		return points;
	}*/

	// rotation exactly captured by one of two possible versors
	private float[] getRotation(Pair<RigidBody> pair, int imageNumber) {
		assert imageNumber == 0 || imageNumber == 1;
		Superposer superposer = new Superposer(true);
		superposer.set(pair._1.center().getAllPoints(), pair._2.center().getAllPoints()); // 2 -> 1, does not matter now
		Versor versor = superposer.getVersor();
		if (imageNumber == 1) {
			versor = versor.negate();
		}
		return versor.toFloats();
	}

	/*Translation captured as distance and unit vector specifying its direction. This separation was chosen because
	the direction is coupled with rotation of the first residue. 0*/
	private float[] getTranslation(Pair<RigidBody> pair) {
		Point direction = pair._1.getCenter(); // does not matter if _1 or _2
		Point unit = direction.normalize();
		float[] translation = {
			(float) (direction.size() / Math.sqrt(2)),
			(float) unit.x,
			(float) unit.y,
			(float) unit.z
		};
		return translation;
	}

}
