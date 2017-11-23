package pdb;

import global.FlexibleLogger;
import global.io.Directories;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.StringTokenizer;

/**
 *
 * Stores array of references (PDB codes or files) to structures and provides SimpleStructure objects.
 *
 * @author Antonin Pavelka
 */
public class Structures implements Iterable<SimpleStructure> {

	private final Directories dirs;
	private final StructureFactory factory;
	private final Random random = new Random(1);
	private final List<StructureSource> ids = new ArrayList<>();
	private int max = Integer.MAX_VALUE;

	public Structures(Directories dirs) {
		this.dirs = dirs;
		factory = new StructureFactory(dirs);
	}

	public void addFromDir(File dir) throws IOException {
		for (File f : dir.listFiles()) {
			ids.add(new StructureSource(f));
		}
	}

	public void addFromFile(File f) {
		ids.add(new StructureSource(f));
	}

	public void addFromClusters() throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(dirs.getPdbClusters50()))) {
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
					ids.add(new StructureSource(cluster.get(random.nextInt(cluster.size()))));
				}
			}
		}
	}

	public void addFromPdbCodes() {
		String line = null;
		try (BufferedReader br = new BufferedReader(new FileReader(dirs.getPdbEntryTypes()))) {
			while ((line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, " \t");
				String code = st.nextToken();
				String type = st.nextToken();
				if (type.equals("prot")) {
					ids.add(new StructureSource(code));
				}
			}
		} catch (Exception ex) {
			System.err.println(line);
			throw new RuntimeException(ex);
		}
	}

	public void addFromPdbCode(String pdbCode) {
		ids.add(new StructureSource(pdbCode));
	}

	public void add(StructureSource r) {
		ids.add(r);
	}

	public int size() {
		return Math.min(ids.size(), max);
	}

	public void setMax(int max) {
		this.max = max;
	}

	public void shuffle() {
		Collections.shuffle(ids, random);
	}

	public SimpleStructure get(int i) throws IOException {
		StructureSource ref = ids.get(i);
		SimpleStructure ss = factory.getStructure(i, ref);
		return ss;
	}

	@Override
	public Iterator<SimpleStructure> iterator() {
		return new Iterator<SimpleStructure>() {

			int index = 0;

			@Override
			public boolean hasNext() {
				return index < size();
			}

			@Override
			public SimpleStructure next() {
				while (hasNext() && index < max) { // return first succesfully initialized structure 
					try {
						return get(index++);
					} catch (IOException ex) {
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
