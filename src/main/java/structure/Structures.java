package structure;

import global.FlexibleLogger;
import global.Parameters;
import global.io.Directories;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import cath.Cath;

/**
 *
 * Stores references (PDB codes or files) to structures and provides corresponding SimpleStructure objects.
 *
 * @author Antonin Pavelka
 */
public class Structures implements Iterable<SimpleStructure> {

	private final Parameters parameters;
	private final Directories dirs;
	private final StructuresId id;
	private final StructureFactory factory;
	private final Random random = new Random(1);
	private final List<StructureSource> sources = new ArrayList<>();
	private int max = Integer.MAX_VALUE;
	private StructureSizeFilter filter;
	private int failed;

	/*public Structures(Parameters parameters, Directories dirs) {
		this.parameters = parameters;
		this.dirs = dirs;
		this.id = null;
		factory = new StructureFactory(dirs);
	}*/
	public Structures(Parameters parameters, Directories dirs, Cath cath, StructuresId id) {
		this.parameters = parameters;
		this.dirs = dirs;
		this.id = id;
		this.factory = new StructureFactory(dirs, cath);
	}

	public StructuresId getId() {
		assert id != null;
		return id;
	}

	public StructureSource getSource(int i) {
		return sources.get(i);
	}

	public void setFilter(StructureSizeFilter filter) {
		this.filter = filter;
	}

	public void addFromDir(File dir) throws IOException {
		for (File f : dir.listFiles()) {
			sources.add(new StructureSource(f));
		}
	}

	public void addFromFile(File f) {
		sources.add(new StructureSource(f));
	}

	public void addFromClusters() {
		try (BufferedReader br = new BufferedReader(new FileReader(dirs.getPdbClusters30()))) {
			String line;
			while ((line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, " ");
				List<String> cluster = new ArrayList<>();
				while (st.hasMoreTokens()) {
					String id = st.nextToken().replace("_", "");
					if (id.length() == 5) {
						cluster.add(id);
					}
				}
				if (!cluster.isEmpty()) {
					sources.add(new StructureSource(cluster.get(random.nextInt(cluster.size()))));
				}
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Process text file with ids supported by StructureSource. TODO remove duplicity with Entries
	 */
	public void addFromIds(File file) {
		String line = null;
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			while ((line = br.readLine()) != null) {
				line = line.trim();
				StringTokenizer st = new StringTokenizer(line, " \t");
				if (line.startsWith("#")) {
					continue;
				}
				String code = st.nextToken();
				StructureSource source = new StructureSource(code);
				if (file.getName().equals("pdb_entry_type.txt") && st.hasMoreTokens()) {
					String type = st.nextToken();
					if (type.equals("prot")) {
						sources.add(source);
					}
				} else {
					sources.add(source);
				}
			}
		} catch (Exception ex) {
			System.err.println(line);
			throw new RuntimeException(ex);
		}
	}

	public void addAll(Collection<StructureSource> sources) {
		this.sources.addAll(sources);
	}

	public void addFromPdbCode(String pdbCode) {
		sources.add(new StructureSource(pdbCode));
	}

	public void add(StructureSource r) {
		sources.add(r);
	}

	public int size() {
		return Math.min(sources.size(), max);
	}

	public void setMaxSize(int max) {
		this.max = max;
	}

	public void shuffle() {
		Collections.shuffle(sources, random);
	}

	public SimpleStructure get(int index, int structureId) throws IOException, StructureParsingException {
		StructureSource ref = sources.get(index);
		SimpleStructure ss = factory.getStructure(structureId, ref);
		return ss;
	}

	public SimpleStructure getSingle() {
		if (sources.size() != 1) {
			throw new RuntimeException("Size must be 1, not " + sources.size());
		}
		SimpleStructure structure = iterator().next();
		return structure;
	}

	@Override
	public Iterator<SimpleStructure> iterator() {
		return new Iterator<SimpleStructure>() {

			int index = 0;
			int structureId = 0;

			@Override
			public boolean hasNext() {
				return index < size();
			}

			@Override
			public SimpleStructure next() {
				while (hasNext() && index < max) { // return first succesfully initialized structure 
					try {
						SimpleStructure structure = get(index++, structureId);
						if (structure != null
							&& (filter == null || filter.accept(structure))) {
							structureId++;
							return structure;
						}
					} catch (IOException | StructureParsingException ex) {
						failed++;
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

	public int getFailed() {
		return failed;
	}
}
