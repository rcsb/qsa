package pdb;

import fragments.FlexibleLogger;
import io.Directories;
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
public class StructureProvider implements Iterable<SimpleStructure> {

	private final Directories dirs = Directories.createDefault();
	private final StructureFactory factory = new StructureFactory(dirs);
	private final Random random = new Random(1);
	private final List<StructureReference> ids = new ArrayList<>();
	private int max = Integer.MAX_VALUE;

	private StructureProvider() {
	}

	public static StructureProvider createFromDir(File dir) throws IOException {
		StructureProvider sp = new StructureProvider();
		for (File f : dir.listFiles()) {
			sp.ids.add(new StructureReference(f));
		}
		return sp;
	}

	public static StructureProvider createFromFile(File f) {
		StructureProvider sp = new StructureProvider();
		sp.ids.add(new StructureReference(f));
		return sp;
	}

	public static StructureProvider createFromClusters() throws IOException {
		StructureProvider sp = new StructureProvider();
		try (BufferedReader br = new BufferedReader(new FileReader(sp.dirs.getPdbClusters50()))) {
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
					sp.ids.add(new StructureReference(cluster.get(sp.random.nextInt(cluster.size()))));
				}
			}
		}
		return sp;
	}

	public static StructureProvider createFromPdbCodes() {
		StructureProvider sp = new StructureProvider();
		String line = null;
		try (BufferedReader br = new BufferedReader(new FileReader(sp.dirs.getPdbEntryTypes()))) {
			while ((line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, " \t");
				String code = st.nextToken();
				String type = st.nextToken();
				if (type.equals("prot")) {
					sp.ids.add(new StructureReference(code));
				}
			}
		} catch (Exception ex) {
			System.err.println(line);
			throw new RuntimeException(ex);
		}
		return sp;
	}

	public static StructureProvider createFromPdbCode(String pdbCode) {
		StructureProvider sp = new StructureProvider();
		sp.ids.add(new StructureReference(pdbCode));
		return sp;
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
		StructureReference ref = ids.get(i);
		SimpleStructure ss = factory.getStructure(ref, i);
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
				throw new NoSuchElementException("No more positions available");
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Removals are not supported");
			}
		};
	}


	/*public boolean hasNext() {
		return pdbCounter < ids.size();
	}

	public SimpleStructure next() throws IOException {
		return get(pdbCounter++);
	}

	public SimpleStructure getRandom() throws IOException {
		int r = random.nextInt(ids.size());
		return get(r);
	}

	public SimpleStructure get(String pdbCode) throws IOException {
		StructureFactory factory = new StructureFactory(dirs);
		SimpleStructure ss = StructureFactory.convertFirstModel(factory.getStructure(pdbCode), -1);
		return ss;
	}
	 */
}
