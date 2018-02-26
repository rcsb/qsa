package geometry.metric;

import geometry.primitives.Point;

/**
 *
 * @author Antonin Pavelka
 */
public class ChebyshevDistance implements MetricDistance {

	public double distance(Point x, Point y) {
		return x.chebyshev(y);
	}
}
