package biword;

import algorithm.BiwordedStructure;
import algorithm.BiwordsFactory;
import global.FlexibleLogger;
import global.Parameters;
import global.io.Directories;
import java.io.IOException;
import java.util.Iterator;
import pdb.SimpleStructure;
import pdb.Structures;

/**
 *
 * @author Antonin Pavelka
 */
public class BiwordsCreator implements Iterable<BiwordedStructure> {

	private final Parameters parameters;
	private final Directories dirs;
	private final Structures structureProvider;
	private final boolean permute;

	public BiwordsCreator(Parameters parameters, Directories dirs, Structures sp, boolean permute) {
		this.parameters = parameters;
		this.dirs = dirs;
		this.structureProvider = sp;
		this.permute = permute;
	}

	private BiwordedStructure createBiwords(SimpleStructure structure) throws IOException {
		BiwordsFactory biwordsFactory = new BiwordsFactory(parameters, dirs, structure, parameters.getSkipY(), permute);
		return biwordsFactory.getBiwords();
	}

	@Override
	public Iterator<BiwordedStructure> iterator() {
		return new Iterator<BiwordedStructure>() {

			Iterator<SimpleStructure> it = structureProvider.iterator();

			int index = 0;

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public BiwordedStructure next() {
				while (hasNext()) { // return first succesfully initialized biwords
					try {
						SimpleStructure s = it.next();
						return createBiwords(s);
					} catch (Exception ex) {
						FlexibleLogger.error(ex);
					}
				}
				return null;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Removals are not supported");
			}
		};
	}

}
