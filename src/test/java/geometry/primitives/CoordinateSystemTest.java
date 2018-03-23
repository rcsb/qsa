package geometry.primitives;

import geometry.angles.Angles;
import geometry.exceptions.CoordinateSystemException;
import geometry.random.RandomGeometry;
import junit.framework.TestCase;

/**
 *
 * @author Antonin Pavelka
 *
 */
public class CoordinateSystemTest extends TestCase {

	public CoordinateSystemTest(String testName) {
		super(testName);
	}

	private CoordinateSystem create(Input input) {
		try {
			return new CoordinateSystem(input.origin, input.u, input.v);
		} catch (CoordinateSystemException ex) {
			throw new RuntimeException(ex);
		}
	}

	private CoordinateSystem createStandard() {
		try {
			return new CoordinateSystem(new Point(0, 0, 0), new Point(1, 0, 0), new Point(0, 1, 0));
		} catch (CoordinateSystemException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void testGetOrigin() {
	}

	public void testExpresPoint() {
		CoordinateSystem s = createStandard();
		Point e = s.expresPoint(new Point(1, 2, 3));
		System.out.println(e);
	}

	public void testRealizeCoordinates() {
		CoordinateSystem s = createStandard();
		Point e = s.expresPoint(new Point(1, 2, 3));
		System.out.println(e);
		System.out.println(s.realizeCoordinates(e));
	}

	public void testGetXAxis() {
	}

	public void testGetYAxis() {
	}

	public void testGetZAxis() {
	}

	public void testToString() {
	}

}

class Input {

	public final Point origin;
	public final Point u;
	public final Point v;

	public Input() {
		RandomGeometry rg = new RandomGeometry();
		origin = rg.randomVector();
		u = rg.randomVector();
		Point vCandidate = null;
		boolean collinear = true;
		while (collinear) {
			vCandidate = rg.randomVector();
			collinear = vCandidate.angle(u) < Angles.toRadians(10);
		}
		v = vCandidate;
	}
}
