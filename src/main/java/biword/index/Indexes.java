package biword.index;

import biword.serialization.KryoFactory;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import global.Parameters;
import global.io.Directories;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import structure.Structures;

/**
 *
 * @author Antonin Pavelka
 */
public class Indexes {

	private final Parameters parameters;
	private final Directories dirs;
	private final Map<String, Index> inMemory = new HashMap<>();

	public Indexes(Parameters parameters, Directories dirs) {
		this.parameters = parameters;
		this.dirs = dirs;
	}

	public Index getIndex(Structures structures) {
		String id = structures.getId();
		Index index = inMemory.get(id);
		if (index == null) {
			index = load(id);
		}
		if (index == null) {
			index = create(structures);
			save(index, id);
		}
		return index;
	}

	/*public Index getIndexOneTime() {
		Index index = create(id, structures);
		return index;
	}*/
	private Index create(Structures structures) {
		IndexFactory indexFactory = new IndexFactory(parameters, dirs, structures);
		Index index = indexFactory.getIndex();
		inMemory.put(structures.getId(), index);
		return index;
	}

	private Index load(String id) {
		File file = dirs.getIndex(id);
		if (!file.exists()) {
			return null;
		}
		try (Input input = new Input(new FileInputStream(file))) {
			Kryo kryo = getKryo();
			Index index = kryo.readObject(input, Index.class);
			inMemory.put(id, index);
			return index;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void save(Index index, String id) {
		File file = dirs.getIndex(id);
		try (Output output = new Output(new FileOutputStream(file))) {
			Kryo kryo = getKryo();
			kryo.writeObject(output, index);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private Kryo getKryo() {
		KryoFactory factory = new KryoFactory();
		return factory.getKryoForIndex();
	}

}
