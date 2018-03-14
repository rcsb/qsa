/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geometry.primitives;

import java.util.Random;
import junit.framework.TestCase;

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
	}

	public void testInverse() {
		Versor v = Versor.create(random);
		Versor w = v.inverse();
		Versor a = v.multiply(w);
		Versor b = w.multiply(v);
	}

	public void testToAngleAxis() {
		Random random = new Random(1);
		for (int i = 0; i < 1000000; i++) {
			double x = (random.nextDouble() - 0.5) * 1000;
			double y = (random.nextDouble() - 0.5) * 1000;
			double z = (random.nextDouble() - 0.5) * 1000;
			Point axis = new Point(x, y, z).normalize();
			double angle = (random.nextDouble() - 0.5) * Math.PI * 2;
			assert angle <= Math.PI && angle >= -Math.PI;
			AxisAngle a = new AxisAngle(axis, angle);
			Versor v = Versor.create(a);
			AxisAngle b = v.toAngleAxis();
			AxisAngle c = v.negate().toAngleAxis();						
			if (!a.isSimilar(b) && !a.isSimilar(c)) {
				System.err.println(a);
				System.err.println(b);
				System.err.println(c);
				fail("i = " + i);
			}
		}
	}

}
