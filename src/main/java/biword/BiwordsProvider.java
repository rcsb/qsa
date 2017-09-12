package biword;

import fragments.Biwords;
import fragments.BiwordsFactory;
import fragments.Parameters;
import fragments.vector.PdbDataset;
import io.Directories;
import java.util.List;
import pdb.SimpleStructure;
import pdb.StructureFactory;

/**
 *
 * @author Antonin Pavelka
 */
public class BiwordsProvider {

	private final Directories dirs = Directories.createDefault();
	private final Parameters params = Parameters.create();
	private final PdbDataset pd = new PdbDataset();
	private final List<String> ids = pd.loadAll();
	private final BiwordsFactory bf = new BiwordsFactory();
	private int pdbCounter = 0;

	public Biwords next() {
		Biwords bs = null;
		while (bs == null) {
			if (pdbCounter >= ids.size()) {
				return null;
			}
			try {
				String id = ids.get(pdbCounter);
				StructureFactory provider = new StructureFactory(dirs);
				SimpleStructure ss = StructureFactory.convertFirstModel(provider.getStructure(id), id);
				if (ss.size() <= 10000) {
					bs = bf.create(ss, params.getWordLength(), 1);
				} else {
					System.out.println("avoiging large structure " + id + " " + ss.size());
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			} catch (Error er) {
				er.printStackTrace();
			}
			pdbCounter++;
		}
		return bs;
	}

}
