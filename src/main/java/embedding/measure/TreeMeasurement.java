package embedding.measure;

import algorithm.Biword;
import algorithm.BiwordedStructure;
import cath.Cath;
import fragment.index.Indexes;
import fragment.index.OrthogonalGrid;
import fragment.serialization.BiwordLoader;
import global.Parameters;
import global.io.Directories;
import grid.sparse.BufferOfLong;
import java.util.Random;
import structure.Structures;
import structure.VectorizationException;
import testing.TestResources;

/**
 *
 * @author Antonin Pavelka
 */
public class TreeMeasurement {

	private final TestResources resources = new TestResources();
	private final Directories dirs = resources.getDirectoris();
	private final Parameters parameters = resources.getParameters();
	private final Random random = new Random(1);
	private final Structures structures;
	private final BiwordLoader biwordLoader;
	private final OrthogonalGrid index;

	public TreeMeasurement() {
		dirs.createJob();
		structures = createStructures();
		Indexes indexes = new Indexes(parameters, dirs);
		
		index = indexes.getIndex(structures);
		biwordLoader = new BiwordLoader(parameters, dirs, structures.getId());
		
	}

	private void run() throws VectorizationException {
		dirs.createTask("tree_test_");
		int query = createRandomQuery();
		BiwordedStructure queries = biwordLoader.load(query);
		BufferOfLong buffer = new BufferOfLong();
		for (Biword bw : queries.getBiwords()) {
			index.query(bw, buffer);
		}

	}

	private Structures createStructures() {
		Cath cath = resources.getCath();
		Structures structures = new Structures(parameters, dirs, cath, "custom_search1");
		//structure.setFilter(new StructureSizeFilter(parameters.getMinResidues(), parameters.getMaxResidues()));
		structures.addAll(cath.getHomologousSuperfamilies().getRepresentantSources());
		return structures;
	}

	private int createRandomQuery() {
		return random.nextInt(structures.size());
	}

	public static void main(String[] args) throws Exception {
		TreeMeasurement m = new TreeMeasurement();
		m.run();
	}
}
