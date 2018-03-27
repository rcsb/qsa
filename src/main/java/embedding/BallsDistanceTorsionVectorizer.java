package embedding;

import vectorization.dimension.Dimensions;
import geometry.exceptions.CoordinateSystemException;
import geometry.angles.Angles;
import geometry.primitives.AxisAngle;
import geometry.primitives.AxisAngleFactory;
import geometry.primitives.CoordinateSystem;
import geometry.primitives.Point;
import geometry.primitives.Versor;
import geometry.random.RandomGeometry;
import geometry.superposition.Superposer;
import geometry.test.RandomBodies;
import global.io.LineFile;
import info.laht.dualquat.Quaternion;
import language.Pair;
import language.Util;
import structure.VectorizationException;
import util.Counter;
import vectorization.dimension.Dimension;
import vectorization.dimension.DimensionCyclic;
import vectorization.dimension.DimensionOpen;

/**
 *
 * @author Antonin Pavelka
 */
public class BallsDistanceTorsionVectorizer implements ObjectPairVectorizer {

	//private static double dihedralFactor = 0.85;
	private static double torsionFactor = 1;
	private static Dimensions dimensions = createDimensions();

	private LineFile file = new LineFile("e:/data/qsa/visualization/vec01.pdb");
	private Counter serial = new Counter();

	public BallsDistanceTorsionVectorizer() {
		file.clean();
	}
	
	
	private static Dimensions createDimensions() {
		Dimension open = new DimensionOpen();
		Dimension cyclic = new DimensionCyclic(-torsionFactor, torsionFactor);
		return new Dimensions(
			open, open, open, // ball1
			open, open, open, // ball2
			cyclic, // dihedral
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
			
			float[] v = vectorizeUncatched(b1, b2, imageNumber);
			System.out.print("VECTOR ");
			for (float f : v) {
				System.out.print(f + " ");
			}
			System.out.println();
			return v;
			
			
		} catch (CoordinateSystemException ex) {
			throw new VectorizationException(ex);
		}
	}

		private void save(RigidBody b, int number) {
		file.write(b.toPdb(number, serial));
	}
	
	private static int count = 0;
		
	private float[] vectorizeUncatched(RigidBody a, RigidBody b, int imageNumber) throws CoordinateSystemException {
		
		
		save(a, count++);
		save(b, count++);
		
		float[] ball1 = getBall(a, b);
		float[] ball2 = getBall(b, a);
		float[] torsion = {getTorsion(a, b, imageNumber)};
		float[] distance = {getDistance(a, b)};
		return Util.merge(ball1, ball2, torsion, distance);
	}

	private float[] getBall(RigidBody a, RigidBody b) throws CoordinateSystemException {
		Point origin = a.getCenter();
		Point u = Point.vector(origin, a.getFirstAuxiliary());
		Point v = Point.vector(origin, a.getSecondAuxiliary());
		CoordinateSystem system = new CoordinateSystem(origin, u, v);
		Point position = system.expresPoint(b.getCenter());
		return position.normalize().getCoordsAsFloats();
	}

	private float getTorsionNaive(RigidBody b1, RigidBody b2, int imageNumber) throws CoordinateSystemException {
		CoordinateSystem s1 = createSystem(b1.getAllPoints());
		CoordinateSystem s2 = createSystem(b2.getAllPoints());

		// proste torzni uhel tam kde je ta mensi z os nejvetsi?
		// co kdyz jsou obe male...nechat byt?
		Point a = s1.getOrigin().plus(s1.getXAxis());
		Point b = s1.getOrigin();
		Point c = s2.getOrigin();
		Point d = s2.getOrigin().plus(s2.getXAxis());

		double torsion = Angles.torsionAngle(a, b, c, d);
		System.out.println(Angles.toDegreeInt(torsion));

		float coordinate = (float) (torsion / Math.PI);
		if (Float.isNaN(coordinate)) {
			coordinate = 0f;
		}
		return coordinate;
	}

	private float getTorsion(RigidBody b1, RigidBody b2, int imageNumber) throws CoordinateSystemException {

		System.out.println("b1");
		System.out.println(b1.center());
		System.out.println("b2");
		System.out.println(b2.center());
		
		//CoordinateSystem s1 = createSystem(b1.center().getAllPoints());
		//CoordinateSystem s2 = createSystem(b2.center().getAllPoints());
		Point anchor1 = Point.vector(b1.getCenter(), b2.getCenter()).normalize();

		Quaternion q = new Quaternion().setFromUnitVectors(anchor1, new Point(1,0,0));

		//System.out.println(s1);
		
		//System.out.println("q " + q);
		
		RigidBody c1 = b1.center().rotate(q);
		RigidBody c2 = b2.center().rotate(q);

		System.out.println("c1");
		System.out.println(c1);
		System.out.println("c2");
		System.out.println(c2);
		
		
		Superposer superposer = new Superposer(true);
		superposer.set(c1.getAuxiliaryPoints(), c2.getAuxiliaryPoints());
		Versor versor = superposer.getVersor();

		double torsion = getFirstEulerAngle(versor);

		float coordinate = (float) (torsion * torsionFactor / Math.PI);
		//coordinate=0f;
		return coordinate;
	}

	private double getFirstEulerAngle(Versor q) {
		double sinr = 2 * (q.w * q.x + q.y * q.z);
		double cosr = 1 - 2 * (q.x * q.x + q.y * q.y);
		double roll = Math.atan2(sinr, cosr);
		return roll;
	}

	private float getTorsionByRotationDecomposition(RigidBody b1, RigidBody b2, int imageNumber) throws CoordinateSystemException {
		CoordinateSystem s1 = createSystem(b1.center().getAllPoints());
		CoordinateSystem s2 = createSystem(b2.center().getAllPoints());
		Point anchor1 = Point.vector(b1.getCenter(), b2.getCenter()).normalize(); // default coordinate system
		Point anchorIn1 = s1.expresPoint(anchor1);
		Point anchor2 = s2.realizeCoordinates(anchorIn1);
		Quaternion q = new Quaternion().setFromUnitVectors(anchor2, anchor1);
		RigidBody oneWithTorsion = b2.center().rotate(q);
		Versor torsion = b1.center().computeRotation(oneWithTorsion); // inverted q after torsion should be full rotation b1.centered() -> b2.centered()
		AxisAngle aa = torsion.toAxisAngle();
		double angle = aa.getAngle();
		if (anchor1.minus(aa.getAxis()).size() > 1) { // should be either 0 or 2, they are either identical or opposite
			angle = 2 * Math.PI - angle;
		}
		//double anchorAngle = anchor1.angle(anchor2);
		//System.out.println("angle correction " + Angles.toDegreeInt(angle) +   "   " + Angles.toDegreeInt(anchorAngle));
		//angle = angle / (anchor + anchorAngle * Math.sin(anchorAngle / 2));
		angle = Angles.wrap(angle);

		float coordinate = (float) (angle / Math.PI);
		//System.out.println(" " + Math.round(Angles.toDegrees(angle)));
		return coordinate;
	}

	private float getDihedralX(RigidBody b1, RigidBody b2, int imageNumber) throws CoordinateSystemException {
		CoordinateSystem s1 = createSystem(b1.getAllPoints());
		CoordinateSystem s2 = createSystem(b2.getAllPoints());
		Point a = s1.getOrigin().plus(s1.getXAxis());
		Point b = s1.getOrigin();
		Point c = s2.getOrigin();
		Point d = s2.getOrigin().plus(s2.getYAxis());
		return (float) (Angles.torsionAngle(a, b, c, d) * torsionFactor / Math.PI);
	}

	private CoordinateSystem createSystem(Point[] points) throws CoordinateSystemException {
		Point origin = points[0];
		Point u = Point.vector(origin, points[1]);
		Point v = Point.vector(origin, points[2]);
		return new CoordinateSystem(origin, u, v);
	}

	private AxisAngle getAngleAxis(RigidBody a, RigidBody b) {
		RigidBody aCentered = a.center();
		RigidBody bCentered = b.center();
		Superposer superposer = new Superposer(true); // bodies are in zero origin
		superposer.set(aCentered.getAuxiliaryPoints(), bCentered.getAuxiliaryPoints());
		Versor versor = superposer.getVersor();
		AxisAngle aa = versor.toAxisAngle();
		return aa;
	}

	private float getDistance(RigidBody a, RigidBody b) {
		return (float) a.getCenter().distance(b.getCenter());
	}
	static int fail;

	public static void test(String[] args) throws CoordinateSystemException {
		RandomBodies rb = new RandomBodies();
		for (int i = 0; i < 1000; i++) {

			double xAngle = Angles.toDegrees(rg.randomAngle());
			//System.out.println(xAngle + " !@!!");

			setTorsion(Angles.toRadians(xAngle), rb);
			setTorsion(Angles.toRadians(-xAngle), rb);
		}
		System.out.println("fails " + fail);
	}

	static RandomGeometry rg = new RandomGeometry();

	private static void setTorsion(double torsion, RandomBodies rb) throws CoordinateSystemException {
		double in = Angles.wrap(torsion);//Angles.wrap();
		Pair<RigidBody> bodies = rb.createDummiesX(new Point(0, 0, 0), in);
		BallsDistanceTorsionVectorizer vectorizer = new BallsDistanceTorsionVectorizer();
		double out = (double) vectorizer.getTorsion(bodies._1, bodies._2, 0);
		out = out / torsionFactor * Math.PI;
		//System.out.println(Angles.wrap(in)  -  out);
		System.out.println(Angles.wrap(in) + " " + Angles.wrap(out));
		/*if (Math.abs(in - out) > 0.0001) {
			System.err.println(Angles.toDegrees(in) + " " + Angles.toDegrees(out));
			fail++;//throw new RuntimeException(in +" " + out);
		}*/
	}

	private static AxisAngle getAngle(RigidBody a, RigidBody b) {
		Superposer superposer = new Superposer(true); // bodies are in zero origin
		superposer.set(a.getAuxiliaryPoints(), b.getAuxiliaryPoints());
		AxisAngle aa = AxisAngleFactory.toAxisAngle(superposer.getRotationMatrix());
		return aa;
	}

	public static void main(String[] args) throws CoordinateSystemException {
		test(args);
		/*RigidBody a = RigidBody.createWithCenter(new Point(0, 0, 0),
			new Point(1, 0, 0),
			new Point(0, 1, 0)
		);
		RigidBody b = RigidBody.createWithCenter(new Point(0, 0, 0),
			new Point(0, 0, 1),
			new Point(0, -0.8, -0.2)
		);
		RigidBody c = RigidBody.createWithCenter(new Point(0, 0, 0),
			new Point(0, 0, 1),
			new Point(-0.21, -0.8, -0.2)
		);

		System.out.println(getAngle(a, b));
		System.out.println(getAngle(a, c));*/
	}

}
