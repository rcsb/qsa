package geometry;

import geometry.exceptions.CoordinateSystemException;
import geometry.primitives.CoordinateSystem;
import geometry.primitives.Point;
import junit.framework.TestCase;

/**
 *
 * @author Antonin Pavelka
 */
public class CoordinateSystemTest extends TestCase {

	public CoordinateSystemTest(String testName) {
		super(testName);
	}

	public void testExpresPoint() throws CoordinateSystemException {
		trivialCase();
		realisticCase();
	}

	private void trivialCase() throws CoordinateSystemException {
		Point zero = new Point(0, 0, 0);
		Point u = new Point(1, 0, 0);
		Point v = new Point(0, 1, 0);
		CoordinateSystem cs = new CoordinateSystem(zero, u, v);

		Point a = cs.expresPoint(new Point(1, 1, 1));
		Point b = cs.expresPoint(new Point(1, 0, 0));
		Point c = cs.expresPoint(new Point(0, 1, 0));
		Point d = cs.expresPoint(new Point(0, 0, 1));
		Point e = cs.expresPoint(new Point(-1, -2, 3));

		assert a.close(new Point(1, 1, 1));
		assert b.close(new Point(1, 0, 0));
		assert c.close(new Point(0, 1, 0));
		assert d.close(new Point(0, 0, 1));
		assert e.close(new Point(-1, -2, 3));
	}

	private void realisticCase() throws CoordinateSystemException {
		Point origin = new Point(2, 3, 4);
		Point u = new Point(1, 1, 0);
		Point v = new Point(1, -1, 0);
		CoordinateSystem cs = new CoordinateSystem(origin, u, v);

		Point a = cs.expresPoint(new Point(2, 3, 4));
		Point b = cs.expresPoint(new Point(2 + 1, 3, 4));

		assert a.close(new Point(0, 0, 0));
		double s = Math.sqrt(2) / 2;
		assert b.close(new Point(s, s, 0));
	}

}
