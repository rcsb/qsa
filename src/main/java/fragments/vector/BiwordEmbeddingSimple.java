package fragments.vector;

import io.Directories;
import java.io.File;
import java.util.Random;
import javax.vecmath.Point3d;
import util.Randomness;

/**
 *
 * @author Antonin Pavelka
 */
public class BiwordEmbeddingSimple {

	private final Directories dirs = Directories.createDefault();
	private double threshold;

	private Randomness rand = new Randomness(1);

	public BiwordEmbeddingSimple(double threshold) {
		this.threshold = threshold;
	}

	private void initSeed(int seed) {
		rand = new Randomness(seed);
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

			Point3d[][] arbitrary = PointVectorDataset.read(dirs.getBiwordDatasetShuffled(), all);
			Point3d[][] clustered = PointVectorDataset.read(dirs.getBiwordRepresentants(3.6), all);

			Point3d[][] objects = rand.subsample(100, clustered);

			GraphEmbedding ge = new GraphEmbedding(objects);

			for (int i = 0; i < 10; i++) {
				initSeed(i * 100);
				Point3d[][] test = rand.subsample(10000, arbitrary);
				System.out.println(test[0][0].x);
				ge.test(test, 3);
			}

			//recalls.add(recall);
			//}
			//for (int i = 0; i < recalls.size(); i++) {
			//	System.out.println("recall " + i + " " + recalls.get(i));
			//}
		}
	}

	public static void main(String[] args) throws Exception {
		BiwordEmbeddingSimple m = new BiwordEmbeddingSimple(3.4);
		m.run();
		/*double threshold = 3.8;
		for (int i = 0; i < 10; i++) {			
			BiwordEmbedding m = new BiwordEmbedding(threshold - i * 0.2);
			m.run();
		}*/
	}

}
