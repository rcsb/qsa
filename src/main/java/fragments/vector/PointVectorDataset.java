package fragments.vector;

import io.Directories;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;
import javax.vecmath.Point3d;

/**
 *
 * @author Antonin Pavelka
 */
public class PointVectorDataset {

	private final Directories dirs = Directories.createDefault();
	private final Random random = new Random(1);

	public PointVectorDataset() {
	}

	public static void save(Iterable<Point3d[]> vectors, File file) throws IOException {
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
			for (Point3d[] w : vectors) {
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

	public void save(Point3d[][] vectors, File file) throws IOException {
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
			for (Point3d[] w : vectors) {
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

	public void shuffle(File in, File out) throws Exception {
		Point3d[][] pointVectors = PointVectorDataset.read(in);
		PointVectorClustering.shuffleArray(pointVectors);
		save(pointVectors, out);
	}

	public static Point3d[][] read(File file) throws IOException, ClassNotFoundException {
		return read(file, -1);
	}

	public static Point3d[][] read(File file, int max) throws IOException, ClassNotFoundException {
		Point3d[][] words;
		int n = 0;
		Integer dim = null;
		try (ObjectInputStream oos = new ObjectInputStream(new FileInputStream(file))) {
			try {
				while ((max <= 0) || n < max) {
					//System.out.println("reading ...");
					Object o = oos.readObject();
					if (dim == null) {
						double[] a = (double[]) o;
						assert a.length % 3 == 0;
						dim = a.length / 3;
					}
					n++;
				}
			} catch (EOFException ex) {
			}
		}
		if (max > 0) {
			n = Math.min(max, n);
		}
		System.out.println("Reading " + n + " vectors of length " + dim);
		words = new Point3d[n][dim];
		int index = 0;
		try (ObjectInputStream oos = new ObjectInputStream(new FileInputStream(file))) {
			try {
				for (int x = 0; x < words.length; x++) {
					Object o = oos.readObject();
					double[] a = (double[]) o;
					Point3d[] word = new Point3d[dim];
					Point3d center = new Point3d(0, 0, 0);
					for (int i = 0; i < dim; i++) {
						Point3d p = new Point3d(a[i * 3], a[i * 3 + 1], a[i * 3 + 2]);
						center.add(p);
						word[i] = p;
					}
					center.scale(-1.0 / dim);
					for (int i = 0; i < dim; i++) {
						word[i].add(center);
					}
					words[index++] = word;
				}
			} catch (EOFException ex) {
			}
		}
		return words;
	}

}
