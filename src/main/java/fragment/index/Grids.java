package fragment.index;

import fragment.serialization.KryoFactory;
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
public class Grids {

	private final Parameters parameters;
	private final Directories dirs;
	private final Map<String, Grid> inMemory = new HashMap<>();

	public Grids(Parameters parameters, Directories dirs) {
		this.parameters = parameters;
		this.dirs = dirs;
	}

	public Grid getGrid(Structures structures) {
		String id = structures.getId();
		Grid index = inMemory.get(id);
		if (index == null) {
			index = load(id);
		}
		if (index == null) {
			index = create(structures);
			save(index, id);
		}
		return index;
	}
	
	private Grid create(Structures structures) {
		GridFactory indexFactory = new GridFactory(parameters, dirs, structures);
		Grid index = indexFactory.getIndex();
		inMemory.put(structures.getId(), index);
		return index;
	}

	private Grid load(String id) {
		File file = dirs.getIndex(id);
		if (!file.exists()) {
			return null;
		}
		try (Input input = new Input(new FileInputStream(file))) {
			Kryo kryo = getKryo();
			Grid index = kryo.readObject(input, Grid.class);
			inMemory.put(id, index);
			return index;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void save(Grid index, String id) {
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
