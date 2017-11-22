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

	private final Parameters parameters;
	private final Directories dirs;
	private final Structures structureProvider;
	private final boolean permute;

	public BiwordsProvider(Parameters parameters, Directories dirs, Structures sp, boolean permute) {
		this.parameters = parameters;
		this.dirs = dirs;
		this.structureProvider = sp;
		this.permute = permute;
	}

	private Biwords createBiwords(SimpleStructure structure) throws IOException {
		BiwordsFactory biwordsFactory = new BiwordsFactory(parameters, dirs, structure, parameters.getSkipY(), permute);
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
						if (s.size() <= parameters.getMaxResidues()) {
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
