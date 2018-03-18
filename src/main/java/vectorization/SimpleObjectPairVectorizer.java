package vectorization;

import vectorization.dimension.Dimensions;
import geometry.exceptions.CoordinateSystemException;
import geometry.primitives.CoordinateSystem;
import geometry.primitives.Point;
import structure.VectorizationException;

/**
 *
 * @author Antonin Pavelka
 */
public class SimpleObjectPairVectorizer implements ObjectPairVectorizer {

	@Override
	public float[] vectorize(RigidBody b1, RigidBody b2, int imageNumber) throws VectorizationException {
		CoordinateSystem c1 = create(b1);
		CoordinateSystem c2 = create(b2);
		Point p1 = c1.expresPoint(b2.getCenter());
		Point p2 = c2.expresPoint(b1.getCenter());
		float[] vector = new float[6];
		vector[0] = (float) p1.x;
		vector[1] = (float) p1.y;
		vector[2] = (float) p1.z;
		vector[3] = (float) p2.x;
		vector[4] = (float) p2.y;
		vector[5] = (float) p2.z;
		return vector;
	}

	private CoordinateSystem create(RigidBody b) throws VectorizationException {
		Point[] auxiliary = b.getAuxiliaryPoints();
		if (auxiliary.length != 2) {
			throw new VectorizationException();
		}
		Point center = b.getCenter();
		Point u = auxiliary[0].minus(center);
		Point v = auxiliary[1].minus(center);
		try {
			return new CoordinateSystem(center, u, v);
		} catch (CoordinateSystemException ex) {
			throw new VectorizationException(ex);
		}
	}

	@Override
	public int getNumberOfImages() {
		return 1;
	}

	@Override
	public Dimensions getDimensions() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

}
