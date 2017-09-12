package fragments.space;

import fragments.vector.*;
import fragments.Biword;
import fragments.Biwords;
import fragments.BiwordsFactory;
import fragments.WordImpl;
import geometry.Transformer;
import io.Directories;
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
public class BiwordSpace {

	private static Random random = new Random(1);
	private static Directories dirs = Directories.createDefault();

	private List<SimpleStructure> structures = new ArrayList<>();
	private List<WordImpl> a = new ArrayList<>(); // biword
	private List<WordImpl> b = new ArrayList<>(); // biword
	private List<Cluster> clusters = new ArrayList<>();
	private Transformer tr = new Transformer();
	private double threshold = 5;
	private int bwCount;

	public BiwordSpace(double theshold) {
		this.threshold = threshold;
	}

	private double rmsd(Point3d[] x, Point3d[] y) {
		tr.set(x, y);
		double rmsd = tr.getRmsd();
		return rmsd;
	}

	public void cluster() {
		int pdbCounter = 0;
		PdbDataset pd = new PdbDataset();
		List<String> ids = pd.loadAll();
		BiwordsFactory bf = new BiwordsFactory();
		System.out.println(ids.size() + " PDB codes");
		for (String id : ids) {
			try {
				StructureFactory provider = new StructureFactory(dirs);
				SimpleStructure ss = StructureFactory.convertFirstModel(provider.getStructure(id), id);
				if (ss.size() <= 10000) {
					structures.add(ss);
					Biwords bs = bf.create(ss, 10, 1);
					for (Biword bw : bs.getBiwords()) {
						add(bw);
					}

				} else {
					System.out.println("avoiging large structure " + id + " " + ss.size());
				}
				//System.out.println((pdbCounter++) + " / " + ids.size());
			} catch (Exception ex) {
				ex.printStackTrace();
			} catch (Error er) {
				er.printStackTrace();
			}
		}
	}

	// store cluster content on HDD, just repre in mem?
	public void add(Biword bw) {
		Point3d[] x = bw.getPoints3d();
		Cluster chosen = null;
		double chosenRmsd = Double.POSITIVE_INFINITY;
		for (Cluster c : clusters) {
			Point3d[] y = c.getPoints();
			tr.set(x, y);
			double rmsd = tr.getRmsd();
			if (rmsd <= threshold && rmsd < chosenRmsd) { // select cluster with closest representant
				chosen = c;
			}
		}

		if (chosen == null) {
			clusters.add(new Cluster(bw));
		} else {
			chosen.add(bw);
		}
		bwCount++;
		if (bwCount % 1000 == 0) {
			System.out.println(bwCount + " " + clusters.size());
		}
	}

	public void run() {
		cluster();
	}

	public static void main(String[] args) {
		BiwordSpace m = new BiwordSpace(2.0);
		m.run();
	}

}
