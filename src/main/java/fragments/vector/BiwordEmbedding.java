package fragments.vector;

import io.Directories;
import java.io.File;

/**
 *
 * @author Antonin Pavelka
 */
public class BiwordEmbedding {

	private final Directories dirs = Directories.createDefault();
	private double threshold;

	public BiwordEmbedding(double threshold) {
		this.threshold = threshold;
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
		if (true) {
			PointVectorClustering pvc = new PointVectorClustering();
			pvc.cluster(threshold, dirs.getBiwordDataset(), representants);
		}
		if (false) {
			int seed = 2283;
			int n = 100000;
			GraphSetEmbedding ge = new GraphSetEmbedding(dirs.getBiwordRepresentants(threshold), n, n, 1, seed);
			ge.test(dirs.getBiwordDatasetShuffled(), 10000, 3, seed + 1);
		}
	}

	public static void main(String[] args) throws Exception {
		double threshold = 3.8;
		for (int i = 0; i < 10; i++) {			
			BiwordEmbedding m = new BiwordEmbedding(threshold - i * 0.2);
			m.run();
		}
	}

}
