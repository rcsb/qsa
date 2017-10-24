package biword;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import fragments.Biwords;
import io.Directories;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Provides processed structures on demand. Structures are stored on HDD, which is much faster than recreating them
 * again.
 *
 * @author Antonin Pavelka
 */
public class StructureStorage {

	private final Directories dirs = Directories.createDefault();
	private final Kryo kryo = new Kryo();
	private final List<Integer> structureIds = new ArrayList<>();

	public StructureStorage() {
		kryo.setReferences(true);
	}

	public void save(int structureId, Biwords bws) {
		structureIds.add(structureId);
		save(bws, dirs.getBiwordsFile(structureId));
	}

	public Biwords load(int structureId) {
		return (Biwords) load(dirs.getBiwordsFile(structureId));
	}

	public List<Integer> getStructureIds() {
		return structureIds;
	}

	private void save(Object o, File f) {
		try (Output output = new Output(new FileOutputStream(f))) {
			kryo.writeObject(output, o);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private Object load(File f) {
		try (Input input = new Input(new FileInputStream(f))) {
			Object o = kryo.readObject(input, Object.class);
			return o;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

}
