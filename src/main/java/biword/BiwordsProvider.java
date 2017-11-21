package biword;

import algorithm.Biwords;
import algorithm.BiwordsFactory;
import global.FlexibleLogger;
import global.Parameters;
import global.io.Directories;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import pdb.SimpleStructure;
import pdb.Structures;

/**
 *
 * @author Antonin Pavelka
 */
public class BiwordsProvider implements Iterable<Biwords> {

	private final Parameters parameters = Parameters.create();
	private final Directories dirs;
	private final Structures structureProvider;
	private final boolean permute;

	public BiwordsProvider(Directories dirs, Structures sp, boolean permute) {
		this.dirs = dirs;
		this.structureProvider = sp;
		this.permute = permute;
	}

	private Biwords createBiwords(SimpleStructure structure) throws IOException {
		BiwordsFactory biwordsFactory = new BiwordsFactory(dirs, structure, parameters.skipY(), permute);
		return biwordsFactory.getBiwords();
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
						} else {
							System.out.println("Skipped too big structure " + s.getSource());
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
