package fragments.vector;

import io.Directories;
import java.io.File;
import javax.vecmath.Point3d;

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
		if (false) {
			PointVectorClustering pvc = new PointVectorClustering();
			pvc.cluster(threshold, dirs.getBiwordDataset(), representants);
		}
		if (true) {
			int seed = 2283;
			int base = 1000;
			int all = Integer.MAX_VALUE;
			//List<Double> recalls = new ArrayList<>();
			//for (int i = 0; i < 10; i++) {
			//	base = i * 10000;

			Point3d[][] objects = PointVectorDataset.read(dirs.getBiwordRepresentants(3.4), 5000);
			Point3d[][] test = PointVectorDataset.read(dirs.getBiwordDatasetShuffled(), 10000);

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
