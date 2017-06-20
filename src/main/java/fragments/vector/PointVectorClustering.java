package fragments.vector;

import geometry.Transformer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.vecmath.Point3d;

/**
 *
 * @author Antonin Pavelka
 */
public class PointVectorClustering {

	public void cluster(double threshold, File in, File out) throws IOException, ClassNotFoundException {
		System.out.println("reading " + in);
		Point3d[][] words = PointVectorDataset.read(in);
		System.out.println("clustering " + words.length + " point vectors");
		shuffleArray(words);
		Transformer tr = new Transformer();
		List<Point3d[]> representants = new ArrayList<>();
		int counter = 0;		
		for (Point3d[] a : words) {
			boolean found = false;
			for (Point3d[] b : representants) {
				tr.set(a, b);
				double rmsd = tr.getRmsd();
				if (rmsd < threshold) {
					found = true;
					break;
				}
			}
			if (!found) {
				representants.add(a);
				System.out.println("clusters " + representants.size() + " " + counter + " / " + words.length);
			}
			counter++;
		}
		PointVectorDataset.save(representants, out);
	}

	public static void shuffleArray(Point3d[][] ar) {
		Random rnd = new Random(564);
		for (int i = ar.length - 1; i > 0; i--) {
			int index = rnd.nextInt(i + 1);
			Point3d[] a = ar[index];
			ar[index] = ar[i];
			ar[i] = a;
		}
	}
}
