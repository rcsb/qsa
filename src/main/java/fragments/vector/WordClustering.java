package fragments.vector;

import geometry.Transformer;
import io.Directories;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.vecmath.Point3d;

/**
 *
 * @author Antonin Pavelka
 */
public class WordClustering {

	private static Directories dirs = Directories.createDefault();
	private static final double THRESHOLD = 1.7;

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		Point3d[][] words = WordDataset.readWords(dirs.getWordDataset());
		System.out.println("clustering " + words.length + " words");
		shuffleArray(words);
		Transformer tr = new Transformer();
		List<Point3d[]> representants = new ArrayList<>();
		int counter=0;
		for (Point3d[] a : words) {
			boolean found = false;
			for (Point3d[] b : representants) {
				tr.set(a, b);
				double rmsd = tr.getRmsd();
				if (rmsd < THRESHOLD) {
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
		WordDataset.saveWords(representants, dirs.getWordRepresentants(THRESHOLD + ""));
	}

	static void shuffleArray(Point3d[][] ar) {
		Random rnd = new Random(1);
		for (int i = ar.length - 1; i > 0; i--) {
			int index = rnd.nextInt(i + 1);
			Point3d[] a = ar[index];
			ar[index] = ar[i];
			ar[i] = a;
		}
	}
}
