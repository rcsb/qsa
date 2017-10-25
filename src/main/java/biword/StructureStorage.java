package biword;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import fragments.Biword;
import fragments.Biwords;
import fragments.Word;
import fragments.WordImpl;
import geometry.Point;
import io.Directories;
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

/**
 *
 * Provides processed structures on demand. Structures are stored on HDD, which is much faster than recreating them
 * again.
 *
 * @author Antonin Pavelka
 */
public class StructureStorage implements Iterable<Biwords> {

	private final Directories dirs = Directories.createDefault();
	private final Kryo kryo = new Kryo();
	private final List<Integer> structureIds = new ArrayList<>();

	public StructureStorage() {
		kryo.setReferences(true);
		kryo.register(Biword.class);
		kryo.register(Residue.class);
		kryo.register(ResidueId.class);
		kryo.register(WordImpl.class);
		kryo.register(Word.class);
		kryo.register(String.class);
		kryo.register(Point.class);
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
			kryo.writeObject(output, o);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private Biwords load(File f) {
		try (Input input = new Input(new FileInputStream(f))) {
			return kryo.readObject(input, Biwords.class);
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
