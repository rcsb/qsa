package fragment.cluster.visualize;

import fragment.cluster.Cluster;
import fragment.cluster.Clusters;
import fragment.cluster.Fragment;
import geometry.superposition.Superposer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import structure.visual.PdbLine;
import util.Counter;

/**
 *
 * @author Antonin Pavelka
 */
public class Visualizer {

	private Clusters clusters;
	private final int countInCluster = 100;
	private final int countTotal = 20;
	private Random random = new Random(1);

	public Visualizer(Clusters clusters) {
		this.clusters = clusters;
	}

	public void save(File pdbFile) {
		int model = 1;
		Counter serial = new Counter();
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(pdbFile))) {

			clusters.shuffle(random);
			for (int i = 0; i < Math.min(countTotal, clusters.size()); i++) {
				Cluster cluster = clusters.get(i);
				save(cluster, model++, serial, bw);
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/// confused transform order or st, do this at cluster construction
	public void save(Cluster cluster, int model, Counter serial, BufferedWriter bw) throws IOException {
		//System.out.println("saving");
		//assert cluster.validate();
		bw.write(PdbLine.getModelString(model) + "\n");
		int residueNumber = 1;
		//FragmentPoints centroid = cluster.getCentroid();
		for (Fragment fragment : getSample(cluster)) {

			/*superposer.set(centroid.getPoints(), fragment.getPoints());
			double rrr = superposer.getRmsd();
			
			if (rrr  > 10) {
				System.out.println("??????????????????????");
			}*/
			//centroid.center();
			//superposer.set(centroid.getPoints(), fragment.getPoints());
			/*if (cluster.getRadius() > 10) {
				System.out.println("!!!!!!!!!!!!!!!!!!!!");
			}*/
			//Matrix4d matrix = superposer.getMatrix();
			//fragment.transform(matrix);
			fragment.saveAsPdb(residueNumber++, serial, bw);
		}
		assert cluster.validate();
		bw.write(PdbLine.getEndmdlString() + "\n");
	}

	private List<Fragment> getSample(Cluster cluster) {
		List<Fragment> content = cluster.getContent().getList();
		Collections.shuffle(content, random);
		List<Fragment> sample = content.subList(0, Math.min(countInCluster, content.size()));
		return sample;
	}
}
