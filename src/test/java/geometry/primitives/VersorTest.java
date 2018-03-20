/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geometry.primitives;

import geometry.superposition.Superposer;
import java.util.Random;
import javax.vecmath.Matrix3d;
import junit.framework.TestCase;
import vectorization.RigidBody;

/**
 *
 * @author kepler
 */
public class VersorTest extends TestCase {

	Random random = new Random(1);

	public VersorTest(String testName) {
		super(testName);
	}

	public void testCreateRandom() {
		Point x = Point.unit(random);
		assert isOne(x.size());
		Versor v = Versor.create(random);
		Point y = v.rotate(x);
		assert isOne(y.size());
	}

	private boolean isOne(double d) {
		return Math.abs(1 - d) < 0.000001;
	}

	public void testToString() {
	}

	public void testToFloats() {
	}

	public void testRotate() {
		Versor v = Versor.create(random);
		Point x = Point.unit(random);
		Point y = v.rotate(x);
		Versor vi = v.inverse();
		Point z = vi.rotate(y);

		_testSuperposer();

	}

	private void _testSuperposer() {
		//Point[] a = {new Point(1, 0, 0), new Point(0, 1, 0)};
		//Point[] b = {new Point(0, -1, 0), new Point(1, 0, 0)};
		Point[] a = {new Point(1, 2, 4).normalize(), new Point(0, 1, 0).normalize()};
		Point[] b = {new Point(0, -1, 0).normalize(), new Point(1, 0, 0).normalize()};
		Superposer superposer = new Superposer(true);
		superposer.set(a, b);
		Versor v = superposer.getVersor();
		System.out.println("versor " + v);

		Matrix3d matrix = superposer.getRotationMatrix();

		AxisAngle a1 = AxisAngleFactory.toAxisAngle(matrix);
		AxisAngle a2 = v.toAxisAngle();
		// wrong quaternion? construct it through matrix, compare
		
		Versor correct = Versor.create(a1);
		System.out.println("corect versor vs bad");
		System.out.println(correct);
		System.out.println(v);
		
		
		System.out.println("axi angle");
		System.out.println(a1);
		System.out.println(a2);

		RigidBody x = RigidBody.createWithCenter(new Point(0, 0, 0), a);
		RigidBody y = RigidBody.createWithCenter(new Point(0, 0, 0), b);
		RigidBody z = x.rotate(v.inverse());
		RigidBody w = y.rotate(v.inverse());
		System.out.println(x);
		System.out.println(y);
		System.out.println(z);
		System.out.println(w);
	}

	public void testInverse() {
		Versor v = Versor.create(random);
		Versor w = v.inverse();
		Versor a = v.multiply(w);
		Versor b = w.multiply(v);
	}

	public void testToAngleAxis() {
		Random random = new Random(1);
		for (int i = 0; i < 10; i++) {
			double x = (random.nextDouble() - 0.5) * 1000;
			double y = (random.nextDouble() - 0.5) * 1000;
			double z = (random.nextDouble() - 0.5) * 1000;
			Point axis = new Point(x, y, z).normalize();
			double angle = (random.nextDouble() - 0.5) * Math.PI * 2;
			assert angle <= Math.PI && angle >= -Math.PI;
			AxisAngle a = new AxisAngle(axis, angle);
			Versor v = Versor.create(a);
			AxisAngle b = v.toAxisAngle();
			AxisAngle c = v.negate().toAxisAngle();
			if (!a.isSimilar(b) && !a.isSimilar(c)) {
				System.out.println("---");
				System.err.println(a);
				System.err.println(b);
				System.err.println(c);
				fail("i = " + i);
			}
		}
	}

}
