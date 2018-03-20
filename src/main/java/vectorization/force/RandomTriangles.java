package vectorization.force;

import geometry.primitives.Point;
import geometry.random.RandomGeometry;
import vectorization.RigidBody;

public class RandomTriangles {
	
	private static RandomGeometry rg = new RandomGeometry();
	private static final double s3 = Math.sqrt(3) / 2;
	
	public RigidBodyPair generate() {
		RigidBody b1 = RigidBody.create(randomTriangle());
		RigidBody b2 = RigidBody.create(randomTriangle());
		b2 = b2.translate(rg.randomVector().multiply(5));
		return new RigidBodyPair(b1, b2);
	}
	
	private Point[] randomTriangle() {
		return rg.rotateRandomly(createTriangle());
	}
	
	private Point[] createTriangle() {
		Point[] triangle = {
			new Point(s3, 0.5, 0),
			new Point(-s3, 0.5, 0),
			new Point(0, -1, 0)
		};
		
		return triangle;
	}
	
}
