package embedding.measure;

import algorithm.Biword;
import algorithm.BiwordedStructure;
import cath.Cath;
import fragment.index.Grids;
import fragment.index.Grid;
import fragment.serialization.BiwordLoader;
import global.Parameters;
import global.io.Directories;
import grid.sparse.BufferOfLong;
import java.util.Random;
import structure.Structures;
import structure.StructuresId;
import structure.VectorizationException;
import testing.TestResources;

/**
 *
 * @author Antonin Pavelka
 */
public class TreeMeasurement {

	private final TestResources resources = TestResources.getInstance();
	private final Directories dirs = resources.getDirectoris();
	private final Parameters parameters = resources.getParameters();
	private final Random random = new Random(1);
	private final Structures structures;
	private final BiwordLoader biwordLoader;
	private final Grid index;

	public TreeMeasurement() {
		dirs.createJob();
		structures = createStructures();
		Grids indexes = new Grids(parameters, dirs);

		index = indexes.getGrid(structures);
		biwordLoader = new BiwordLoader(parameters, dirs, structures.getId());

	}

	private void run() throws VectorizationException {
		dirs.createTask("tree_test_");
		int query = createRandomQuery();
		BiwordedStructure queries = biwordLoader.load(query);
		BufferOfLong buffer = new BufferOfLong();
		System.out.println("total: " + index.size());
		for (Biword bw : queries.getBiwords()) {
			buffer.clear();
			index.query(bw, buffer);
			System.out.println("returned: "+buffer.size());
		}

	}

	private Structures createStructures() {
		Cath cath = resources.getCath();
		Structures structures = new Structures(parameters, dirs, cath, new StructuresId("custom_search1"));
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
