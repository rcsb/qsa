package fragments.vector;

import fragments.Biword;
import fragments.Biwords;
import fragments.BiwordsFactory;
import geometry.Point;
import io.Directories;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Random;
import pdb.SimpleStructure;
import pdb.StructureFactory;

/**
 *
 * @author Antonin Pavelka
 */
public class BiwordDataset {

	private final Random random = new Random(1);
	private final Directories dirs = Directories.createDefault();

	public void generate() throws IOException {
		int pdbCounter = 0;
		int counter = 0;
		PdbDataset pd = new PdbDataset();
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dirs.getBiwordDataset()))) {
			List<String> ids = pd.loadRandomRepresentants();
			for (String id : ids) {
				try {
					System.out.println(id + " " + (pdbCounter++) + " / " + ids.size() + " " + counter);
					StructureFactory provider = new StructureFactory(dirs);
					SimpleStructure ss = StructureFactory.convert(provider.getSingleChain(id), id);
					BiwordsFactory bf = new BiwordsFactory();
					Biwords bs = bf.create(ss, 10, 1);

					for (Biword bw : bs.getBiwords()) {
						if (random.nextInt(100) == 0) {
							counter++;
							Point[] points = bw.getPoints();
							double[] a = new double[points.length * 3];
							for (int i = 0; i < points.length; i++) {
								Point p = points[i];
								a[i * 3] = p.x;
								a[i * 3 + 1] = p.y;
								a[i * 3 + 2] = p.z;
							}
							oos.writeObject(a);
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				} catch (Error er) {
					er.printStackTrace();
				}
			}
		}
	}

}
