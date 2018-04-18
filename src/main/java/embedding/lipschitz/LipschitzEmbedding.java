package embedding.lipschitz;

import embedding.Vectorizer;
import embedding.lipschitz.object.AlternativeMode;
import embedding.lipschitz.object.AlternativePointTuples;
import embedding.lipschitz.object.PointTuple;
import embedding.lipschitz.object.PointTupleDistanceMeasurement;
import geometry.superposition.Superposer;
import java.util.Random;
import language.search.Best;
import metric.Chebyshev;
import statistics.KahanSumDouble;
import vectorization.dimension.DimensionOpen;
import vectorization.dimension.Dimensions;

/**
 *
 * Lipschitz embedding into Chebyshev metric from a space defined by distances between objects. Bases are selected so
 * that they are dissimilar.
 *
 * Bases have the alternatives, to save memory.
 *
 * @author Antonin Pavelka
 */
public class LipschitzEmbedding implements Vectorizer {

	private final Base[] bases;
	private final Random random = new Random(1);
	private final AlternativePointTuples[] objects;
	private int optimizationCycles;
	private ObjectPairs objectPairs;
	private int pairSampleSize;
	private AlternativeMode alternativeMode;

	public LipschitzEmbedding(AlternativePointTuples[] objects, int numberOfBases, int optimizationCycles,
		int pairSampleSize, AlternativeMode alternativeMode) {
		this.objects = objects;
		this.bases = new Base[numberOfBases];
		this.optimizationCycles = optimizationCycles;
		this.pairSampleSize = pairSampleSize;
		this.alternativeMode = alternativeMode;
		this.objectPairs = new ObjectPairs(new Superposer(), objects, pairSampleSize, this.alternativeMode);
		initializeBases();
	}

	@Override
	public float[] getCoordinates(PointTuple object) {
		return getCoordinates(object, bases);
	}

	@Override
	public Dimensions getDimensions() {
		return new Dimensions(new DimensionOpen(), bases.length);
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

	private Base selectRandomBase(AlternativePointTuples[] objects) {
		AlternativePointTuples chosen = objects[random.nextInt(objects.length)];
		PointTuple[] tuples = new PointTuple[alternativeMode.numberOfPointTuples()];
		for (int i = 0; i < tuples.length; i++) {
			tuples[i] = chosen.getAlternative(i, alternativeMode);
		}
		PointTupleDistanceMeasurement measurement = new Superposer();
		return new Base(measurement, tuples);
	}

	private Base selectBestBase(int alreadySelected) {
		Best<Base> smallestDistorsion = Best.createSmallest();
		Base[] partialBases = new Base[alreadySelected + 1];
		System.arraycopy(bases, 0, partialBases, 0, alreadySelected);
		for (int k = 0; k < optimizationCycles; k++) {
			Base randomBase = selectRandomBase(objects);
			partialBases[alreadySelected] = randomBase;
			//double distorsion = distorsion(partialBases);
			double distorsion = fp(partialBases);
			smallestDistorsion.update(randomBase, distorsion);
		}
		System.out.println("distorsion " + (smallestDistorsion.getBestProperty() / pairSampleSize));
		return smallestDistorsion.getBestObject();
	}

	private double distorsion(Base[] bases) {
		KahanSumDouble sum = new KahanSumDouble();
		for (ObjectPair pair : objectPairs) {
			float[] a = getCoordinates(pair.a.getCanonicalTuple(), bases);
			float[] b = getCoordinates(pair.b.getCanonicalTuple(), bases);
			double vectorDistance = Chebyshev.distance(a, b);
			double distorsion = Math.abs(pair.getDistance() - vectorDistance);
			sum.add(distorsion);
		}
		return sum.value();
	}

	private double fp(Base[] bases) {
		int count = 0;
		for (ObjectPair pair : objectPairs) {
			float[] a = getCoordinates(pair.a.getCanonicalTuple(), bases);
			float[] b = getCoordinates(pair.b.getCanonicalTuple(), bases);
			double vectorDistance = Chebyshev.distance(a, b);
			if (pair.getDistance() > 1.5 && vectorDistance < 1.5) {
				count++;
			}
		}
		return count;
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

	private float[] getCoordinates(PointTuple object, Base[] bases) {
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
