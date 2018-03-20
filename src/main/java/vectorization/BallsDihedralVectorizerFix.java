package vectorization;

import vectorization.dimension.Dimensions;
import geometry.exceptions.CoordinateSystemException;
import geometry.primitives.Angles;
import geometry.primitives.AxisAngle;
import geometry.primitives.AxisAngleFactory;
import geometry.primitives.CoordinateSystem;
import geometry.primitives.Point;
import geometry.superposition.Superposer;
import geometry.test.RandomBodies;
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
public class BallsDihedralVectorizerFix implements ObjectPairVectorizer {

	private static double dihedralFactor = 2.25;
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
		return 2;
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

	private float getDihedral(RigidBody a, RigidBody b, int imageNumber) {
		AxisAngle aa = getAngleAxis(a, b);
		Point oo = Point.vector(a.getCenter(), b.getCenter()).normalize();
		Point rotationAxis = aa.getAxis().normalize();
		double dihedralPartOfAxis = Math.abs(oo.dot(rotationAxis)); // how big part of rotation is performed around this axis, just coefficient, angle is magnitude
		//System.out.println("part " + dihedralPartOfAxis);
		//System.out.println("___ " + aa.getAngle() + " - " + aa.getAxis());

		//System.out.println("   " + dihedralPartOfAxis + "  " + aa.getAngle());
		assert dihedralPartOfAxis >= -1;
		assert dihedralPartOfAxis <= 1;
		double angle = aa.getAngle();

		//if (imageNumber == 1 /*&& angle > Math.PI / 2 && angle < Math.PI * 3 / 2*/) {
		//	angle = Angles.wrap(2 * Math.PI - angle);
		//}
		//System.out.println(Angles.toDegrees(angle));
		//System.out.println(aa.getAngle());
		assert 0 <= angle && angle <= 2 * Math.PI;

		double anglePlusMinus = angle;
		if (angle >= Math.PI) {
			anglePlusMinus = angle - 2 * Math.PI;
		}

		/*	assert angle >= -Math.PI;
		assert angle <= Math.PI;
		 */
		double dihedralAngle = Math.sin(angle / 2) * anglePlusMinus * dihedralPartOfAxis;
		double dihedralAngleNormalized = dihedralAngle / Math.PI;
		
		//System.out.println(dihedralAngleNormalized);
		//double dihedralAngleNormalized = dihedralAngle;

		assert -1 <= dihedralAngleNormalized && dihedralAngleNormalized <= 1 : dihedralAngleNormalized;
		//System.out.println("rotation " + Angles.toDegrees(aa.getAngle()) + " " + aa.getAxis());
		//System.out.println("dihedral " + Angles.toDegrees(dihedralAngle));
		//System.out.println("! " + dihedralAngleNormalized);
		//System.out.println("");
		return (float) (dihedralAngleNormalized * dihedralFactor);
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

		return AxisAngleFactory.toAxisAngle(superposer.getRotationMatrix());
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

	public static void test(String[] args) {
		RandomBodies rb = new RandomBodies();
		for (int i = 0; i < 1000; i++) {
			// add noise and measure average? decision if it is here
			// unclear how to add other angles
			double angle = Angles.toRadians(90 + 20 * (Math.random() - 0.5));
			System.out.println(angle);
			Pair<RigidBody> bodies = rb.createDummiesX(new Point(0, 0, 0), angle);
			//System.out.println(bodies._1);
			//System.out.println(bodies._2);
			BallsDihedralVectorizerFix vectorizer = new BallsDihedralVectorizerFix();
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

	public static void main(String[] args) {
		RigidBody a = RigidBody.createWithCenter(new Point(0, 0, 0),
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
		System.out.println(getAngle(a, c));
	}

}
