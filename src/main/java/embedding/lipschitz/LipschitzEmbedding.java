package embedding.lipschitz;

import java.util.Random;
import language.search.Best;
import metric.Chebyshev;
import statistics.KahanSumDouble;

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
	private int optimizationCycles;
	private ObjectPairs objectPairs;
	private int pairSampleSize = 1000;

	public LipschitzEmbedding(Similar[] objects, int numberOfBases, int optimizationCycles) {
		this.objects = objects;
		this.bases = new Base[numberOfBases];
		this.optimizationCycles = optimizationCycles;
		this.objectPairs = new ObjectPairs(objects, pairSampleSize);
		initializeBases();
	}

	/*private void initializeBases() {
		bases[0] = selectRandomBase(objects);
		for (int i = 1; i < bases.length; i++) {
			bases[i] = selectFarthestBase();
		}
	}*/
	private void initializeBases() {
		for (int i = 0; i < bases.length; i++) {
			bases[i] = selectBestBase(i);
		}
	}

	private Base selectRandomBase(Similar[] objects) {
		int n = 1;
		Similar[] refs = new Similar[n];
		for (int i = 0; i < n; i++) {
			refs[i] = objects[random.nextInt(objects.length)];
		}
		return new Base(refs);
	}

	private Base selectBestBase(int alreadySelected) {
		Best<Base> smallestDistorsion = Best.createSmallest();
		Base[] partialBases = new Base[alreadySelected + 1];
		System.arraycopy(bases, 0, partialBases, 0, alreadySelected);
		for (int k = 0; k < optimizationCycles; k++) {
			Base randomBase = selectRandomBase(objects);
			partialBases[alreadySelected] = randomBase;
			double distorsion = distorsion(partialBases);
			smallestDistorsion.update(randomBase, distorsion);
		}
		System.out.println("distorsion " + (smallestDistorsion.getBestProperty() / pairSampleSize));
		return smallestDistorsion.getBestObject();
	}

	private double distorsion(Base[] bases) {
		KahanSumDouble sum = new KahanSumDouble();
		for (ObjectPair pair : objectPairs) {
			float[] a = getCoordinates(pair.a, bases);
			float[] b = getCoordinates(pair.b, bases);
			double vectorDistance = Chebyshev.distance(a, b);
			double distorsion = Math.abs(pair.rmsd - vectorDistance);
			sum.add(distorsion);
		}
		return sum.value();
	}

	private Base selectFarthestBase() {
		Best<Base> farthest = Best.createGreatest();
		for (int k = 0; k < optimizationCycles; k++) {
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

	public float[] getCoordinates(Similar object) {
		return getCoordinates(object, bases);
	}

	public float[] getCoordinates(Similar object, Base[] bases) {
		float[] coords = new float[bases.length];
		for (int d = 0; d < bases.length; d++) {
			coords[d] = (float) bases[d].getDistance(object);
		}
		return coords;
	}

	public int size() {
		return bases.length;
	}
}
