package biword.serialization;

import com.esotericsoftware.kryo.io.Input;
import algorithm.BiwordedStructure;
import global.FlexibleLogger;
import global.io.Directories;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import global.Parameters;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * Provides processed structures on demand. Biwords objects and associated data (most notably SimpleStructure) are
 * deserialized from HDD, which is much faster than recreating them again.
 *
 * @author Antonin Pavelka
 */
public class BiwordLoader implements Iterable<BiwordedStructure> {

	private final Parameters parameters;
	private final Directories dirs;
	private final KryoFactory kryoFactory = new KryoFactory();
	private final List<Integer> structureIds;

	public BiwordLoader(Parameters parameters, Directories dirs) {
		this.parameters = parameters;
		this.dirs = dirs;
		this.structureIds = readIds();
	}

	private List<Integer> readIds() {
		Path dir;
		if (parameters.hasExternalBiwordSource()) {
			dir = dirs.getBiwordsDir(parameters.getExternalBiwordSource());
		} else {
			dir = dirs.getBiwordsDir();
		}
		List<Integer> ids = new ArrayList<>();
		for (File file : dir.toFile().listFiles()) {
			String name = file.getName();
			try {
				int id = Integer.parseInt(name.trim());
				ids.add(id);
			} catch (NumberFormatException ex) {
				FlexibleLogger.error("Not a biword id: " + name, ex);
			}
		}
		Collections.sort(ids);
		return ids.subList(0, Math.min(ids.size(), parameters.getMaxDbSize()));
	}

	public BiwordedStructure load(int structureId) {
		if (parameters.hasExternalBiwordSource()) {
			return load(dirs.getBiwordedStructure(structureId, parameters.getExternalBiwordSource()));
		} else {
			return load(dirs.getBiwordedStructure(structureId));
		}
	}

	private BiwordedStructure load(File f) {
		try (Input input = new Input(new FileInputStream(f))) {
			return kryoFactory.getKryoForBiwords().readObject(input, BiwordedStructure.class);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public Iterator<BiwordedStructure> iterator() {
		return new Iterator<BiwordedStructure>() {

			int index = 0;

			@Override
			public boolean hasNext() {
				return index < structureIds.size();
			}

			@Override
			public BiwordedStructure next() {
				if (hasNext()) {
					BiwordedStructure value = load(structureIds.get(index));
					index++;
					return value;
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
