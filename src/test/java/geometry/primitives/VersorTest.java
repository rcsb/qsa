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
		
		System.out.println("rotate");
		System.out.println(x);
		System.out.println(y);
		System.out.println(z);
	}

	public void testInverse() {
		Versor v = Versor.create(random);
		Versor w = v.inverse();
		Versor a = v.multiply(w);
		Versor b = w.multiply(v);
		System.out.println(a);
		System.out.println(b);
	}

	public void testMultiply() {
	}

	public void testGetVector() {
	}

}
