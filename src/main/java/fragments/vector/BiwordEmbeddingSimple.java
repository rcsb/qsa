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
			//int all = Integer.MAX_VALUE;
			int all = 200000;
			System.out.println("WARNING ALL = " + all);
			//List<Double> recalls = new ArrayList<>();
			//for (int i = 0; i < 10; i++) {
			//	base = i * 10000;

			initSeed(122340);

			Point3d[][] arbitrary = PointVectorDataset.read(dirs.getBiwordDatasetShuffled(), all);
			Point3d[][] clustered = PointVectorDataset.read(dirs.getBiwordRepresentants(3.4), all);

			//QcpSpeed qs = new QcpSpeed(arbitrary);
			
			//for (int i = 0; i < 10; i++) {
			int dim = 100;//i * 10000;
			Point3d[][] objects = rand.subsample(dim, clustered);
			GraphEmbedding ge = new GraphEmbedding(objects);

			initSeed(1);
			Point3d[][] test = rand.subsample(10000, arbitrary);
			ge.test(test, 2.5);
			//}

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
