package metric;

import geometry.primitives.Point;

/**
 *
 * @author Antonin Pavelka
 */
public class EuclideanDistance implements MetricDistance {

	public double distance(Point x, Point y) {
		return x.distance(y);
	}

}
