package embedding.lipschitz;

import vectorization.force.RandomTriangles;
import vectorization.force.RigidBodyPair;

/**
 *
 * @author Antonin Pavelka
 *
 */
@Deprecated
public class BasesSynthetic {

	private Base[] bases;
	private int number;
	private RandomTriangles randomTriangles;

	public BasesSynthetic(int number) {
		this.number = number;
		this.randomTriangles = new RandomTriangles();
		this.bases = createBases(number);
	}

	private Base[] createBases(int number) {
		Base[] bases = new Base[number];
		bases[0] = createRandomBase();
		for (int i = 1; i < number; i++) {
			double max = Double.NEGATIVE_INFINITY;
			Base furthestBase = null;
			for (int k = 0; k < 1000000; k++) {
				Base b = createRandomBase();
				double distance = getSmallestDistance(b, bases);
				if (distance > max) {
					max = distance;
					furthestBase = b;
				}
			}
			System.out.println(max);
			bases[i] = furthestBase;
		}
		return bases;
	}

	private double getSmallestDistance(Base other, Base[] bases) {
		double min = Double.MAX_VALUE;
		for (Base base : bases) {
			if (base == null) { // unintialized yet
				continue;
			}
			double d = base.getDistance(other);
			if (d < min) {
				min = d;
			}
		}
		return min;
	}

	private Base createRandomBase() {
		RigidBodyPair pair = randomTriangles.generate();
		Base base = new Base(pair);
		return base;
	}

	public double getCoordinate(int dimension, RigidBodyPair object) {
		return bases[dimension].distance(object);
	}

	public int size() {
		return number;
	}
}
