package biword;

import fragments.Biwords;
import fragments.BiwordsFactory;
import fragments.FlexibleLogger;
import fragments.Parameters;
import io.Directories;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import pdb.SimpleStructure;
import pdb.StructureProvider;

/**
 *
 * @author Antonin Pavelka
 */
public class BiwordsProvider implements Iterable<Biwords> {

	private final Parameters params = Parameters.create();
	private final BiwordsFactory bf;
	private final StructureProvider structureProvider;
	private final boolean permute;

	public BiwordsProvider(Directories dirs, StructureProvider sp, boolean permute) {		
		bf = new BiwordsFactory(dirs);
		this.structureProvider = sp;
		this.permute = permute;
	}

	private Biwords createBiwords(SimpleStructure s) throws IOException {
		return bf.create(s, params.getWordLength(), 1, permute);
	}

	@Override
	public Iterator<Biwords> iterator() {
			return new Iterator<Biwords>() {

			Iterator<SimpleStructure> it = structureProvider.iterator();

			int index = 0;

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public Biwords next() {
				while (hasNext()) { // return first succesfully initialized biwords
					try {
						SimpleStructure s = it.next();
						if (s.size() <= 10000) {
							return createBiwords(s);
						}
					} catch (IOException ex) {
						FlexibleLogger.error(ex);
					}
				}
				throw new NoSuchElementException("No more positions available");
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Removals are not supported");
			}
		};
	}

}
