package biword;

import fragments.Biwords;
import fragments.BiwordsFactory;
import fragments.Parameters;
import java.io.IOException;
import pdb.SimpleStructure;
import pdb.StructureProvider;

/**
 *
 * @author Antonin Pavelka
 */
public class BiwordsProvider {

	private final Parameters params = Parameters.create();
	private final BiwordsFactory bf = new BiwordsFactory();
	private final StructureProvider structureProvider;

	public BiwordsProvider(StructureProvider sp) {
		this.structureProvider = sp;
	}

	public Biwords next(boolean permute) throws IOException {
		Biwords bs = null;
		while (bs == null) {
			if (structureProvider.hasNext()) {
				SimpleStructure ss = structureProvider.next();
				if (ss.size() <= 10000) {
					bs = bf.create(ss, params.getWordLength(), 1, permute);
				} else {
					System.out.println("Avoiging large structure " + ss.size());
				}
			}
		}
		return bs;
	}

	public boolean hasNext() {
		return structureProvider.hasNext();
	}

	public void restart() {
		structureProvider.restart();
	}

}
