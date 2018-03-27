package embedding;

import vectorization.dimension.Dimensions;
import geometry.exceptions.CoordinateSystemException;
import geometry.primitives.AxisAngle;
import geometry.primitives.CoordinateSystem;
import geometry.primitives.Point;
import geometry.primitives.Versor;
import geometry.superposition.Superposer;
import global.io.LineFile;
import language.Pair;
import language.Util;
import structure.VectorizationException;
import util.Counter;
import vectorization.dimension.DimensionOpen;

/**
 *
 * Transforms a pair of rigid bodies into a tuple of real numbers capturing their orientation: Euclidean and Chebyshev
 * distance between the tuples corresponds well to RMSD between two pairs of rigid bodies.
 *
 * Both rigid bodies are required to be similar, at least so that their superposition cannot be ambiguous.
 *
 * @author Antonin Pavelka
 */
public class QuaternionAveragedObjectPairVectorizer implements ObjectPairVectorizer {

	private static Dimensions dimensions = new Dimensions(
		new DimensionOpen(), new DimensionOpen(), new DimensionOpen(), new DimensionOpen(), // quaternion for object rotations
		new DimensionOpen(), new DimensionOpen(), new DimensionOpen(),/* unit vector in cartesian coordinates representing center-center line in coordinate 
		system of first object (but averaged with superposed second object) */
		new DimensionOpen()); // distance center-center

	private LineFile file = new LineFile("e:/data/qsa/visualization/vec01.pdb");
	private Counter serial = new Counter();

	public QuaternionAveragedObjectPairVectorizer() {
		file.clean();
	}

	private void save(RigidBody b, int number) {
		file.write(b.toPdb(number, serial));
	}

	private void kill() {
		if (true) {
			System.exit(0);
		}
	}

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
		//save(b1.center(), 1);
		//save(b2.center(), 2);
		CoordinateSystem system = computeCoordinateSystem(b1, b2, imageNumber);
		Pair<RigidBody> expressed = new Pair(b1.express(system), b2.express(system));
		Pair<RigidBody> expressed2 = new Pair(b1.center().express(system), b2.center().express(system));
		//save(expressed._1.center().plusFiveX(), 3);
		//save(expressed._2.center().plusFiveX(), 4);
		//kill();
		float[] rotation = getRotation(expressed2, imageNumber);
		float[] translation = getTranslation(expressed);
		return Util.merge(rotation, translation);
	}

	
	
	private Versor alternate(Versor v, int imageNumber) {
		AxisAngle aa = v.toAxisAngle();
		if (aa.getAngle() > Math.PI) {
			//System.out.println("big " + v);
			v = v.negate();
		}
		//System.out.println("sma " + v);
		if (imageNumber == 1) {
			v = v.negate();
		}
		return v;
	}

	private CoordinateSystem computeCoordinateSystem(RigidBody b1, RigidBody b2, int imageNumber) throws CoordinateSystemException {
		//System.out.println(" " + b1.rmsd(b2));
		RigidBody bodyCentered1 = b1.center();
		RigidBody bodyCentered2 = b2.center();

		Superposer superposer = new Superposer(true); // bodies are in origin already
		superposer.set(bodyCentered2.getAllPoints(), bodyCentered1.getAllPoints()); // rotation 2 -> 1
		superposer.getMatrix();

		Versor v = superposer.getVersor();

		//System.out.println(v.toAngleAxis() + " !");
		v = alternate(v, imageNumber);

		//System.out.println(v.negate().toAngleAxis() + " !");
		Versor angularAverage = halfRotation(v);
		RigidBody rotated1 = bodyCentered1.rotate(angularAverage.inverse());
		RigidBody rotated2 = bodyCentered2.rotate(angularAverage); // rotate in the oposite direction

		//RigidBody full = bodyCentered2.rotate(v);
		RigidBody averaged = rotated1.average(rotated2);

		/*save(bodyCentered1, 1);
		save(bodyCentered2, 2);
		save(rotated1, 3);
		save(rotated2, 4);
		//save(full, 5);
		kill();
		 */
		//assert rotated1.rmsd(rotated2) < 0.2;
		//assert averaged.rmsd(rotated2) < 0.2;
		//assert averaged.rmsd(rotated1) < 0.2;
		// now x and y are superposed, rotated 
		
		
		CoordinateSystem system = createSystem(averaged.getAllPoints());
		//CoordinateSystem system = createSystem(b1.getAllPoints()); // works // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

		return system;
	}

	private Versor halfRotation(Versor v) {
		AxisAngle whole = v.toAxisAngle();
		assert whole.getAxis().size() < 1.0001 && whole.getAxis().size() > 0.9999;
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
		//assert imageNumber == 0 || imageNumber == 1;
		Superposer superposer = new Superposer(true);
		superposer.set(pair._2.center().getAllPoints(), pair._1.center().getAllPoints()); // 2 -> 1, does not matter now
		Versor versor = superposer.getVersor();
		versor = alternate(versor, imageNumber);
		return versor.toFloats();
	}

	/*Translation captured as distance and unit vector specifying its direction. This separation was chosen because
	the direction is coupled with rotation of the first residue. 0*/
	private float[] getTranslationClassic(Pair<RigidBody> pair) {
		Point direction = pair._1.getCenter().minus(pair._2.getCenter()); // does not matter if _1 or _2
		//double r = 0.95;
		double r = 1;
		Point unit = direction.normalize().multiply(r);
		float[] translation = {
			(float) (direction.size()),
			(float) unit.x,
			(float) unit.y,
			(float) unit.z,};
		return translation;
	}

	private float[] getTranslation(Pair<RigidBody> pair) {
		Point direction = pair._1.getCenter().minus(pair._2.getCenter()); // does not matter if _1 or _2
		double r = 1;
		Point unit = direction.normalize().multiply(r);
		float[] translation = {
			(float) (direction.size()),
			(float) unit.x,
			(float) unit.y,
			(float) unit.z,};
		return translation;
	}
	
}
