package fragments.vector;

import io.Directories;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.vecmath.Point3d;

/**
 *
 * @author Antonin Pavelka
 */
public class BiwordEmbedding {

	private final Directories dirs = Directories.createDefault();
	private double threshold;

	private Random random = new Random(1);

	public BiwordEmbedding(double threshold) {
		this.threshold = threshold;
	}

	private int[] subsample(int howMany, int fromSize) {
		List<Integer> numbers = new ArrayList<>();
		int n = Math.min(howMany, fromSize);
		int[] sample = new int[n];
		for (int i = 0; i < n; i++) {
			numbers.add(i);
		}
		for (int i = 0; i < n; i++) {
			sample[i] = numbers.remove(random.nextInt(numbers.size()));
		}
		return sample;
	}

	private Point3d[][] subsample(int howMany, Point3d[][] from) {
		int n = Math.min(howMany, from.length);
		Point3d[][] result = new Point3d[n][];
		int[] indexes = subsample(n, from.length);
		for (int i = 0; i < n; i++) {
			result[i] = from[indexes[i]];
		}
		return result;
	}

	private void initSeed(int seed) {
		random = new Random(seed);
	}

	public void run() throws Exception {
		File representants = dirs.getBiwordRepresentants(threshold);
		if (false) {
			BiwordDataset bd = new BiwordDataset();
			bd.generate();
		}
		if (false) {
			PointVectorDataset pvd = new PointVectorDataset();
			pvd.shuffle(dirs.getBiwordDataset(), dirs.getBiwordDatasetShuffled());
		}
		if (false) {
			PointVectorClustering pvc = new PointVectorClustering();
			pvc.cluster(threshold, dirs.getBiwordDataset(), representants);
		}
		if (true) {
			int all = Integer.MAX_VALUE;
			//List<Double> recalls = new ArrayList<>();
			//for (int i = 0; i < 10; i++) {
			//	base = i * 10000;

			initSeed(122340);
			
			Point3d[][] arbitrary = PointVectorDataset.read(dirs.getBiwordDatasetShuffled(), 10000000);
			Point3d[][] clustered = PointVectorDataset.read(dirs.getBiwordRepresentants(4.0), all);

			Point3d[][] objects = subsample(1000, clustered);
			Point3d[][] test = subsample(10000, arbitrary);

			GraphEmbedding ge = new GraphEmbedding(objects);
			ge.test(test, 3);

			//recalls.add(recall);
			//}
			//for (int i = 0; i < recalls.size(); i++) {
			//	System.out.println("recall " + i + " " + recalls.get(i));
			//}
		}
	}

	public static void main(String[] args) throws Exception {
		BiwordEmbedding m = new BiwordEmbedding(3.4);
		m.run();
		/*double threshold = 3.8;
		for (int i = 0; i < 10; i++) {			
			BiwordEmbedding m = new BiwordEmbedding(threshold - i * 0.2);
			m.run();
		}*/
	}

}
