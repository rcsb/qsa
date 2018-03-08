package geometry.superposition;

import geometry.primitives.Point;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;
import junit.framework.TestCase;

public class SuperposerTest extends TestCase {

	public SuperposerTest(String testName) {
		super(testName);
	}

	public void testSet_Point3dArr_Point3dArr() {
	}

	public void testSet_PointArr_PointArr() {
	}

	public void testGetQuaternion() {
		Superposer superposer = new Superposer();
		Point[] a = {new Point(1, 0, 0), new Point(0, 1, 0), new Point(0, 0, 1)};
		Point[] b = {new Point(1, 0, 0), new Point(0, 0, 1), new Point(0, 1, 0)};
		superposer.set(a, b);
		//System.out.println(superposer.getQuaternion());
		
		
		
		
		Quat4d q;
		
		Point3d p;
		
	}

	public void testGetRmsd() {
	}

	public void testGetTransformedY() {
	}

	public void testGetXPoints() {
	}

	public void testGetTransformedYPoints() {
	}

	public void testGetMatrix() {
	}

	public void testGetRotationMatrix() {
	}

}
