package biword;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import algorithm.Biword;
import algorithm.Biwords;
import algorithm.Word;
import geometry.Point;
import global.io.Directories;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import pdb.Residue;
import pdb.ResidueId;
import algorithm.Fragment;

/**
 *
 * Provides processed structures on demand. Biwords objects and associated data (most notably SimpleStructure) are
 * deserialized from HDD, which is much faster than recreating them again.
 *
 * @author Antonin Pavelka
 */
public class StructureStorage implements Iterable<Biwords> {

	private final Directories dirs;
	private final List<Integer> structureIds = new ArrayList<>();

	public StructureStorage(Directories dirs) {
		this.dirs = dirs;
	}

	private Kryo getKryo() {
		Kryo kryo = new Kryo();
		kryo.setReferences(true);
		// save few percent of space and some time
		kryo.register(Biword.class);
		kryo.register(Residue.class);
		kryo.register(ResidueId.class);
		kryo.register(Word.class);
		kryo.register(Fragment.class);
		kryo.register(String.class);
		kryo.register(Point.class);
		return kryo;

	}

	public void save(int structureId, Biwords bws) {
		structureIds.add(structureId);
		save(bws, dirs.getBiwordsFile(structureId));
	}

	public Biwords load(int structureId) {
		return load(dirs.getBiwordsFile(structureId));
	}

	public List<Integer> getStructureIds() {
		return structureIds;
	}

	private void save(Biwords o, File f) {
		try (Output output = new Output(new FileOutputStream(f))) {
			getKryo().writeObject(output, o);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private Biwords load(File f) {
		try (Input input = new Input(new FileInputStream(f))) {
			return getKryo().readObject(input, Biwords.class);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public Iterator<Biwords> iterator() {
		return new Iterator<Biwords>() {

			int index = 0;

			@Override
			public boolean hasNext() {
				return index < structureIds.size();
			}

			@Override
			public Biwords next() {
				if (hasNext()) {
					Biwords value = load(structureIds.get(index));
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
