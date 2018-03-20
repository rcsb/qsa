package vectorization;

import vectorization.dimension.Dimensions;
import geometry.exceptions.CoordinateSystemException;
import geometry.primitives.Angles;
import geometry.primitives.AxisAngle;
import geometry.primitives.AxisAngleFactory;
import geometry.primitives.CoordinateSystem;
import geometry.primitives.Point;
import geometry.primitives.Versor;
import geometry.superposition.Superposer;
import geometry.test.RandomBodies;
import info.laht.dualquat.Quaternion;
import language.Pair;
import language.Util;
import structure.VectorizationException;
import vectorization.dimension.Dimension;
import vectorization.dimension.DimensionCyclic;
import vectorization.dimension.DimensionOpen;

/**
 *
 * @author Antonin Pavelka
 */
public class BallsDihedralVectorizer implements ObjectPairVectorizer {

	//private static double dihedralFactor = 0.85;
	private static double dihedralFactor = 1;
	private static Dimensions dimensions = createDimensions();

	private static Dimensions createDimensions() {
		Dimension open = new DimensionOpen();
		Dimension cyclic = new DimensionCyclic(-dihedralFactor, dihedralFactor);
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
			return vectorizeUncatched(b1, b2, imageNumber);
		} catch (CoordinateSystemException ex) {
			throw new VectorizationException(ex);
		}
	}

	private float[] vectorizeUncatched(RigidBody a, RigidBody b, int imageNumber) throws CoordinateSystemException {
		float[] ball1 = getBall(a, b);
		float[] ball2 = getBall(b, a);
		float[] dihedral = {getDihedral(a, b, imageNumber)};
		float[] distance = {getDistance(a, b)};
		return Util.merge(ball1, ball2, dihedral, distance);
	}

	private float[] getBall(RigidBody a, RigidBody b) throws CoordinateSystemException {
		Point origin = a.getCenter();
		Point u = Point.vector(origin, a.getFirstAuxiliary());
		Point v = Point.vector(origin, a.getSecondAuxiliary());
		CoordinateSystem system = new CoordinateSystem(origin, u, v);
		Point position = system.expresPoint(b.getCenter());
		return position.normalize().getCoordsAsFloats();
	}

	// TODO test? think through the geometry, all situations? test new components? visualize Waypoints?
	private float getDihedral(RigidBody b1, RigidBody b2, int imageNumber) throws CoordinateSystemException {
		Point anchor1 = Point.vector(b1.getCenter(), b2.getCenter());
		// find origin1 - origin2 in system1
		CoordinateSystem s1 = createSystem(b1.center().getAllPoints());
		CoordinateSystem s2 = createSystem(b2.center().getAllPoints());
		Point anchor1expressed = s1.expresPoint(anchor1);
		Point anchor2 = s2.realizeCoordinates(anchor1expressed);
		Quaternion q = new Quaternion().setFromUnitVectors(anchor2, anchor1expressed);
		RigidBody decomposed = b2.center().rotate(q); // rotate backwards around axis perpendicular to anchor1
		Versor torsion = b1.computeRotation(decomposed);
		double angle = torsion.toAxisAngle().getAngle();
		return (float) (angle / Math.PI - 1);
		// find corresponding combination of axis of system2
		// rotate by angle defined by those vectors
		// find dihedral superposing rigid bodies
	}

	private float getDihedralX(RigidBody b1, RigidBody b2, int imageNumber) throws CoordinateSystemException {
		CoordinateSystem s1 = createSystem(b1.getAllPoints());
		CoordinateSystem s2 = createSystem(b2.getAllPoints());
		Point a = s1.getOrigin().plus(s1.getXAxis());
		Point b = s1.getOrigin();
		Point c = s2.getOrigin();
		Point d = s2.getOrigin().plus(s2.getYAxis());
		return (float) (Angles.torsionAngle(a, b, c, d) * dihedralFactor / Math.PI);
	}

	private CoordinateSystem createSystem(Point[] points) throws CoordinateSystemException {
		Point origin = points[0];
		Point u = Point.vector(origin, points[1]);
		Point v = Point.vector(origin, points[2]);
		return new CoordinateSystem(origin, u, v);
	}

	private float getDihedralAlaVectorNonsense(RigidBody a, RigidBody b, int imageNumber) {
		//System.out.println();
		AxisAngle aa = getAngleAxis(a, b);
		Point axis = aa.getAxis().normalize();
		//System.out.println(aa);
		double angle = aa.getAngle();
		assert 0 <= angle && angle < Math.PI * 2;
		if (angle > Math.PI) {
			angle = Math.PI * 2 - angle;
			axis = axis.negate();
		}
		assert 0 <= angle && angle <= Math.PI;
		/*if (imageNumber == 1) {
			angle = -angle;
			axis = axis.negate();
			//angle = Angles.wrap(2 * Math.PI - angle);
		}*/
		Point oo = Point.vector(a.getCenter(), b.getCenter()).normalize();
		double dihedralPartOfAxis = 1/*oo.dot(axis)*/; // how big part of rotation is performed around this axis, just coefficient, angle is magnitude
		//System.out.println(oo.dot(axis) + "    " + angle);		
		assert dihedralPartOfAxis >= -1;
		assert dihedralPartOfAxis <= 1;
		double dihedralAngle = angle * dihedralPartOfAxis;
		double dihedralAngleNormalized = dihedralAngle / Math.PI;
		assert -1 <= dihedralAngleNormalized && dihedralAngleNormalized <= 1 : dihedralAngleNormalized;
		return (float) 0;//(dihedralAngleNormalized * dihedralFactor);
	}

	private AxisAngle getAngleAxis(RigidBody a, RigidBody b) {

		RigidBody aCentered = a.center();
		RigidBody bCentered = b.center();

		//System.out.println("");
		//System.out.println(aCentered);
		//System.out.println(bCentered);
		Superposer superposer = new Superposer(true); // bodies are in zero origin
		superposer.set(aCentered.getAuxiliaryPoints(), bCentered.getAuxiliaryPoints());
		//System.out.println(superposer.getRotationMatrix());

		// slightly better than AxisAngleFactory from 3x3 matrix
		Versor versor = superposer.getVersor();

		//System.out.println("v " + versor);
		//if (versor.toAxisAngle().getAngle() > Math.PI) {
		//	versor = versor.negate();
		//}
		AxisAngle aa = versor.toAxisAngle();
		return aa;

//		return AxisAngleFactory.toAxisAngle(superposer.getRotationMatrix());
		/*		
		Versor versor = superposer.getVersor();
		System.out.println("versor " + versor);
		if (versor.toAngleAxis().getAngle() > Math.PI) {
			versor = versor.negate();
		}
		AxisAngle aa = versor.toAngleAxis();
		return aa;*/
	}

	private float getDistance(RigidBody a, RigidBody b) {
		return (float) a.getCenter().distance(b.getCenter());
	}

	public static void test(String[] args) throws CoordinateSystemException {
		RandomBodies rb = new RandomBodies();
		for (int i = 0; i < 1000; i++) {
			// add noise and measure average? decision if it is here
			// unclear how to add other angles
			double angle = Angles.toRadians(-90 - 20 * (Math.random() - 0.5));
			System.out.println(angle);
			Pair<RigidBody> bodies = rb.createDummiesX(new Point(0, 0, 0), angle);
			//System.out.println(bodies._1);
			//System.out.println(bodies._2);
			BallsDihedralVectorizer vectorizer = new BallsDihedralVectorizer();
			System.out.println("dihedral: " + Angles.toDegrees(vectorizer.getDihedral(bodies._1, bodies._2, 0)
				/ dihedralFactor * Math.PI));
			System.out.println("");
		}
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
