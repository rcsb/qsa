package fragments.space;

import fragments.Parameters;
import fragments.vector.*;
import fragments.Word;
import fragments.Words;
import fragments.WordsFactory;
import geometry.Transformer;
import io.Directories;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.vecmath.Point3d;
import pdb.SimpleStructure;
import pdb.StructureFactory;

/**
 *
 * @author Antonin Pavelka
 */
public class WordSpace {

	private static Parameters params = Parameters.create();
	private static Random random = new Random(1);
	private static Directories dirs = Directories.createDefault();
	private Transformer tr = new Transformer();
	public final double threshold = params.getWordClusteringThreshold();
	private int bwCount;

	public WordSpace() {
	}

	public void createWords() throws FileNotFoundException, IOException {
		PdbDataset pd = new PdbDataset();
		List<String> ids = pd.loadAll();
		int pdbCounter = 0;
		try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(
			dirs.getWordDatabase())))) {
			for (String id : ids) {
				try {
					StructureFactory provider = new StructureFactory(dirs);
					SimpleStructure ss = StructureFactory.convertFirstModel(provider.getStructure(id), id);
					WordsFactory wf = new WordsFactory(ss, params.getWordLength());
					Words ws = wf.create();
					Word[] words = ws.toArray();
					for (int i = 0; i < words.length; i++) {
						save(words[i].getPoints3d(), dos);
					}
					System.out.println((pdbCounter++) + " / " + ids.size());
					//if (pdbCounter > 100) { // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
					//	break;
					//}
				} catch (Exception ex) {
					ex.printStackTrace();
				} catch (Error er) {
					er.printStackTrace();
				}
			}
		}
	}

	private static Point3d[] read(DataInputStream dis) throws IOException {
		int length = dis.readByte();
		Point3d[] points = new Point3d[length];
		for (int i = 0; i < length; i++) {
			points[i] = new Point3d(dis.readFloat(), dis.readFloat(), dis.readFloat());
		}
		return points;
	}

	public static Point3d[][] readAll(File f) throws IOException {
		List<Point3d[]> words = new ArrayList<>();
		try (DataInputStream dis = new DataInputStream(new FileInputStream(f))) {
			boolean open = true;
			while (open) {
				try {
					Point3d[] x = read(dis);
					words.add(x);
				} catch (EOFException ex) {
					open = false;
				}
			}
		}
		int n = words.size();
		int window = words.get(0).length;
		Point3d[][] a = words.toArray(new Point3d[n][window]);
		return a;
	}

	public void save(Point3d[] w, DataOutputStream dos) throws IOException {
		dos.writeByte(w.length);
		for (int i = 0; i < w.length; i++) {
			dos.writeFloat((float) w[i].x);
			dos.writeFloat((float) w[i].y);
			dos.writeFloat((float) w[i].z);
		}
	}

	public List<Cw> cluster() throws IOException, ClassNotFoundException {
		List<Cw> clusters = new ArrayList<>();
		try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(
			dirs.getWordDatabase())))) {
			boolean open = true;
			while (open) {
				try {
					Point3d[] x = read(dis);
					Cw chosen = null;
					double chosenRmsd = Double.POSITIVE_INFINITY;
					for (Cw c : clusters) {
						Point3d[] y = c.getRepresentant();
						tr.set(x, y);
						double rmsd = tr.getRmsd();
						if (rmsd <= threshold && rmsd < chosenRmsd) { // select cluster with closest representant
							chosen = c;
						}
					}

					if (chosen == null) {
						clusters.add(new Cw(x));
					} else {
						//chosen.add(x);
					}
					bwCount++;
					if (bwCount % 1000 == 0) {
						System.out.println(bwCount + " " + clusters.size());
					}
				} catch (EOFException ex) {
					open = false;
				}
			}
		}
		return clusters;
	}

	private void visualize(List<Cw> clusters) {
		Directories dirs = Directories.createDefault();
		for (int i = 0; i < clusters.size(); i++) {
			Cw c = clusters.get(i);
			c.visualize(dirs.getClusterPdb(i));
		}
	}

	private void saveRepresentants(List<Cw> clusters) throws IOException {
		try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(
			dirs.getWordRepresentants(threshold))))) {
			for (int i = 0; i < clusters.size(); i++) {
				save(clusters.get(i).getRepresentant(), dos);
			}
		}
	}

	private void labelWords() throws IOException {
		Point3d[][] reps = readAll(dirs.getWordRepresentants(threshold));
		try (DataInputStream dis = new DataInputStream(new FileInputStream(dirs.getWordDatabase()))) {
			boolean open = true;
			while (open) {
				try {
					Point3d[] x = read(dis);
					List<Integer> labels = new ArrayList<>();
					for (int i = 0; i < reps.length; i++) {
						Point3d[] rep = reps[i];
						double rmsd = rmsd(x, rep);
						if (rmsd <= params.getWordLabelThreshold()) {
							labels.add(i);
						}
					}
					System.out.println("labels: " + labels.size() + " / " + reps.length);
				} catch (EOFException ex) {
					open = false;
				}
			}
		}
	}

	private double rmsd(Point3d[] x, Point3d[] y) {
		tr.set(x, y);
		double rmsd = tr.getRmsd();
		return rmsd;
	}

	public void run() throws IOException, ClassNotFoundException {
		//createWords();
		
		//saveRepresentants(cluster());
		
		labelWords();
		
		//cluster();
		//visualize(cluster(createWords()));
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		WordSpace m = new WordSpace();
		m.run();
	}

}
