package vectorization;

import vectorization.dimension.Dimensions;
import geometry.exceptions.CoordinateSystemException;
import geometry.primitives.Angles;
import geometry.primitives.AxisAngle;
import geometry.primitives.CoordinateSystem;
import geometry.primitives.Point;
import geometry.primitives.Versor;
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
public class BallsDihedralVectorizer implements ObjectPairVectorizer {

	private static Dimensions dimensions = createDimensions();

	private static Dimensions createDimensions() {
		Dimension open = new DimensionOpen();
		Dimension cyclic = new DimensionCyclic(0, 1);
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
		float[] dihedral = {getDihedral(a, b)};
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

	private float getDihedral(RigidBody a, RigidBody b) {
		AxisAngle aa = getAngleAxis(a, b);
		Point oo = Point.vector(a.getCenter(), b.getCenter()).normalize();
		Point rotationAxis = aa.getAxis().normalize();
		double dihedralPartOfAxis = oo.dot(rotationAxis); // how big part of rotation is performed around this axis, just coefficient, angle is magnitude
		assert -1 <= dihedralPartOfAxis && dihedralPartOfAxis <= 1;
		double angle = aa.getAngle();
		assert 0 <= angle && angle <= 2 * Math.PI;
		double dihedralAngle = Angles.wrap(angle * dihedralPartOfAxis);
		double dihedralAngleNormalized = dihedralAngle / (2 * Math.PI);
		assert 0 <= dihedralAngleNormalized && dihedralAngleNormalized <= 1 : dihedralAngleNormalized;
		return (float) dihedralAngleNormalized;
	}

	private AxisAngle getAngleAxis(RigidBody a, RigidBody b) {
		RigidBody aCentered = a.center();
		RigidBody bCentered = b.center();
		Superposer superposer = new Superposer(true); // bodies are in zero origin
		superposer.set(aCentered.getAuxiliaryPoints(), bCentered.getAuxiliaryPoints());
		Versor versor = superposer.getVersor();
		if (versor.toAngleAxis().getAngle() > Math.PI) {
			versor = versor.negate();
		}
		AxisAngle aa = versor.toAngleAxis();
		return aa;
	}

	private float getDistance(RigidBody a, RigidBody b) {
		return (float) a.getCenter().distance(b.getCenter());
	}

	public static void main(String[] args) {

		/*Point x = new Point(1,-0.1,0);
		Point y = new Point(1,0.1,0);
		System.out.println("!!! " + x.dot(y));*/
		RandomBodies rb = new RandomBodies();
		Pair<RigidBody> bodies = rb.createDummiesX(new Point(0, 0, 0), Angles.toRadians(-10));

		//System.out.println(bodies._1);
		//System.out.println(bodies._2);

		BallsDihedralVectorizer vectorizer = new BallsDihedralVectorizer();
		System.out.println("dihedral: " + Angles.toDegrees(vectorizer.getDihedral(bodies._1, bodies._2) * Math.PI * 2));

	}

}
