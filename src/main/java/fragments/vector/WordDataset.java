package fragments.vector;

import fragments.WordImpl;
import fragments.Words;
import fragments.WordsFactory;
import geometry.Point;
import io.Directories;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import javax.vecmath.Point3d;
import pdb.SimpleStructure;
import pdb.StructureFactory;

/**
 *
 * @author Antonin Pavelka
 */
public class WordDataset {

	private static final int WORD_LENGTH = 10;
	private Directories dirs = Directories.createDefault();
	private Random random = new Random(1);

	private List<String> loadRandomRepresentants() throws IOException {
		List<String> representants = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(dirs.getPdbClusters50()))) {
			String line;
			while ((line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, " ");
				List<String> cluster = new ArrayList<>();
				while (st.hasMoreTokens()) {
					String id = st.nextToken().replace("_", "");
					if (id.length() == 5) {
						cluster.add(id);
					}
				}
				if (!cluster.isEmpty()) {
					representants.add(cluster.get(random.nextInt(cluster.size())));
				}
			}
		}
		Collections.shuffle(representants, random);
		return representants;
	}

	private void saveWords() throws IOException {
		int counter = 0;
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dirs.getWordDataset()))) {
			List<String> ids = loadRandomRepresentants();
			for (String id : ids) {
				try {
					System.out.println(id + " " + (counter++) + " / " + ids.size());
					StructureFactory provider = new StructureFactory(dirs);
					SimpleStructure ss = StructureFactory.convert(provider.getSingleChain(id), id);
					WordsFactory wf = new WordsFactory(ss, 10);
					Words ws = wf.create();
					for (WordImpl w : ws.get()) {
						Point[] points = w.getPoints();
						double[] a = new double[points.length * 3];
						for (int i = 0; i < points.length; i++) {
							Point p = points[i];
							a[i * 3] = p.x;
							a[i * 3 + 1] = p.y;
							a[i * 3 + 2] = p.z;
						}
						oos.writeObject(a);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	public static void saveWords(List<Point3d[]> words, File file) throws IOException {
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
			for (Point3d[] w : words) {
				double[] a = new double[w.length * 3];
				for (int i = 0; i < w.length; i++) {
					Point3d p = w[i];
					a[i * 3] = p.x;
					a[i * 3 + 1] = p.y;
					a[i * 3 + 2] = p.z;
				}
				oos.writeObject(a);
			}
		}
	}

	public static Point3d[][] readWords(File file) throws IOException, ClassNotFoundException {
		Point3d[][] words;
		int wordN = 0;
		try (ObjectInputStream oos = new ObjectInputStream(new FileInputStream(file))) {
			try {
				while (true) {
					oos.readObject();
					wordN++;
				}
			} catch (EOFException ex) {
			}
		}
		words = new Point3d[wordN][WORD_LENGTH];
		int index = 0;
		try (ObjectInputStream oos = new ObjectInputStream(new FileInputStream(file))) {
			try {
				while (true) {
					Object o = oos.readObject();
					double[] a = (double[]) o;
					Point3d[] word = new Point3d[WORD_LENGTH];
					for (int i = 0; i < a.length / 3; i++) {
						Point3d p = new Point3d(a[i * 3], a[i * 3 + 1], a[i * 3 + 2]);
						word[i] = p;
					}
					words[index++] = word;
				}
			} catch (EOFException ex) {
			}
		}
		return words;
	}

	public static void main(String[] args) throws IOException {
		WordDataset m = new WordDataset();
		m.saveWords();
	}
}
