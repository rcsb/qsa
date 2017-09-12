/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fragments.vector;

import fragments.WordImpl;
import fragments.Words;
import fragments.WordsFactory;
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
 * @author kepler
 * @deprecated
 */
public class WordDataset {

	private Directories dirs = Directories.createDefault();
	private Random random = new Random(1);

	private void saveWords() throws IOException {
		int counter = 0;

		PdbDataset pd = new PdbDataset();
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dirs.getWordDataset()))) {
			List<String> ids = pd.loadRandomRepresentants();
			for (String id : ids) {
				try {
					System.out.println(id + " " + (counter++) + " / " + ids.size());
					StructureFactory provider = new StructureFactory(dirs);
					SimpleStructure ss = StructureFactory.convertProteinChains(provider.getSingleChain(id), id);
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

}
