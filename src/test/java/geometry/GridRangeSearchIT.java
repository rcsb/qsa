package geometry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import junit.framework.TestCase;

/**
 *
 * @author Antonin Pavelka
 */
public class GridRangeSearchIT extends TestCase {

	private final Random random = new Random(1);
	private final int count = 10000;
	private final int queries = 10000;
	private final int d = 50;

	public GridRangeSearchIT(String testName) {
		super(testName);
	}

	private Point p() {
		return new Point(random.nextDouble() * d, random.nextDouble() * d, random.nextDouble() * d);
	}

	public void testSomeMethod() {

		GridRangeSearch g = new GridRangeSearch(5);
		List<Point> points = new ArrayList<>();
		g.buildGrid(points);
		for (int i = 0; i < count; i++) {
			points.add(p());
		}
		g.buildGrid(points);

		for (int i = 0; i < queries; i++) {
			Set<Point> gridResult = new HashSet<>();
			Set<Point> correctResult = new HashSet<>();
			double r = random.nextDouble() * 20;
			Point q = p();

			for (Point x : points) {
				if (x.distance(q) <= r) {
					correctResult.add(x);
				}
			}

			g.nearest(q, r, gridResult);
			System.out.println(correctResult.size() + " " + gridResult.size());

			if (!correctResult.equals(gridResult)) {
				System.out.println(correctResult.size() + " " + gridResult.size());
				fail();
			}
		}
	}

}
