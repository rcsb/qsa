package embedding.lipschitz;

import java.util.Random;
import language.search.Best;

/**
 *
 * Lipschitz embedding into Chebyshev metric from a space defined by distances between objects. Bases are selected so
 * that they are dissimilar.
 *
 * @author Antonin Pavelka
 */
public class LipschitzEmbedding {

	private final Base[] bases;
	private final Random random = new Random(1);
	private final Similar[] objects;

	public LipschitzEmbedding(Similar[] objects, int numberOfBases) {
		this.objects = objects;
		this.bases = new Base[numberOfBases];
		initializeBases();
	}

	private void initializeBases() {
		bases[0] = selectRandomBase(objects);
		for (int i = 1; i < bases.length; i++) {
			bases[i] = selectFarthestBase();
		}
	}

	private Base selectRandomBase(Similar[] objects) {
		return new Base(objects[random.nextInt(objects.length)]);
	}

	private Base selectFarthestBase() {
		Best<Base> farthest = Best.createGreatest();
		for (int k = 0; k < 1000000; k++) {
			Base randomBase = selectRandomBase(objects);
			double distance = getSmallestDistance(randomBase, bases);
			farthest.update(randomBase, distance);
		}
		System.out.println(farthest.getBestProperty());
		return farthest.getBestObject();
	}

	private double getSmallestDistance(Base other, Base[] bases) {
		double min = Double.MAX_VALUE;
		for (Base base : bases) {
			if (base == null) { // reached unintialized bases
				return min;
			}
			double d = base.getDistance(other);
			if (d < min) {
				min = d;
			}
		}
		return min;
	}

	public double getCoordinate(int dimension, Similar object) {
		return bases[dimension].getDistance(object);
	}

	public float[] getCoordinates(Similar object) {
		float[] coords = new float[bases.length];
		for (int d = 0; d < coords.length; d++) {
			coords[d] = (float) getCoordinate(d, object);
		}
		return coords;
	}

	public int size() {
		return bases.length;
	}
}
