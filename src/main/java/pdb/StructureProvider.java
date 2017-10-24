package pdb;

import fragments.vector.PdbDataset;
import io.Directories;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Antonin Pavelka
 */
public class StructureProvider {

	private final Directories dirs = Directories.createDefault();
	private final PdbDataset pd = new PdbDataset();
	private final List<String> ids;
	private int pdbCounter = 0;
	private final Random random = new Random(1);
	private SimpleStructure[] structures;

	public StructureProvider() {
		ids = pd.loadAll();
		structures = new SimpleStructure[ids.size()];
	}

	public int size() {
		return ids.size();
	}

	public StructureProvider(int max) {
		List<String> list = pd.loadAll();
		ids = new ArrayList<>();
		for (int i = 0; i < Math.min(list.size(), max); i++) {
			int r = random.nextInt(list.size());
			String s = list.remove(r);
			ids.add(s);
		}
		structures = new SimpleStructure[ids.size()];
	}

	public boolean hasNext() {
		return pdbCounter < ids.size();
	}

	public SimpleStructure next() throws IOException {
		return get(pdbCounter++);
	}

	public SimpleStructure getRandom() throws IOException {
		int r = random.nextInt(ids.size());
		return get(r);
	}

	private SimpleStructure get(int i) throws IOException {
		if (structures[i] == null) {
			String id = ids.get(i);
			StructureFactory provider = new StructureFactory(dirs);
			SimpleStructure ss = StructureFactory.convertFirstModel(provider.getStructure(id), i);
			structures[i] = ss;
		}
		return structures[i];
	}

	public SimpleStructure getStructure(int id) {
		return structures[id];
	}

	public void restart() {
		pdbCounter = 0;
	}
}
