package embedding.measure;

import algorithm.Biword;
import algorithm.BiwordedStructure;
import cath.Cath;
import fragment.biword.BiwordId;
import fragment.biword.BiwordPairFiles;
import fragment.biword.BiwordPairReader;
import fragment.biword.BiwordPairWriter;
import fragment.index.Grids;
import fragment.index.Grid;
import fragment.serialization.BiwordLoader;
import geometry.superposition.Superposer;
import global.Parameters;
import global.io.Directories;
import grid.sparse.BufferOfLong;
import java.util.Random;
import structure.Structures;
import structure.StructuresId;
import structure.VectorizationException;
import testing.TestResources;
import util.Timer;

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
		int query = createRandomQuery();
		BiwordedStructure queryBiwords = biwordLoader.load(query);
		//efficient(queryBiwords);
		allQcp(queryBiwords);
	}

	private void efficient(BiwordedStructure queryBiwords) throws VectorizationException {
		dirs.createTask("tree_test_");

		BufferOfLong buffer = new BufferOfLong();
		System.out.println("total: " + index.size());
		Biword[] biwords = queryBiwords.getBiwords();
		Timer.start();
		BiwordPairWriter writer = new BiwordPairWriter(dirs, structures.size());
		long sum = 0;
		System.out.println("query size: " + biwords.length);
		for (Biword x : biwords) {
			buffer.clear();
			index.query(x, buffer);
			System.out.println("returned: " + buffer.size());
			sum += buffer.size();
			for (int i = 0; i < buffer.size(); i++) {
				long encoded = buffer.get(i);
				BiwordId y = BiwordId.decode(encoded);
				writer.write(x.getIdWithingStructure(), y.getStructureId(), y.getIdWithinStructure());
			}
		}

		Timer.stop();
		System.out.println("time: " + Timer.get() + " for " + biwords.length);
		System.out.println("average size " + (sum / biwords.length));

		Timer.start();

		Superposer superposer = new Superposer();
		long tp = 0;
		BiwordPairFiles biwordPairFiles = new BiwordPairFiles(dirs);
		for (BiwordPairReader reader : biwordPairFiles.getReaders()) {
			int targetStructureId = reader.getTargetStructureId();
			BiwordedStructure targetBiwords = biwordLoader.load(targetStructureId);
			while (reader.readNextBiwordPair()) {
				int queryBiwordId = reader.getQueryBiwordId();
				int targetBiwordId = reader.getTargetBiwordId();

				Biword x = queryBiwords.get(queryBiwordId);
				Biword y = targetBiwords.get(targetBiwordId);
				// TODO: filter by vectors
				superposer.set(x.getPoints3d(), y.getPoints3d());
				double rmsd = superposer.getRmsd();
				if (rmsd <= parameters.getMaxFragmentRmsd()) {
					tp++;
				}
			}
		}
		Timer.stop();
		System.out.println("tp + fp = " + sum);
		System.out.println("tp      = " + tp);
		System.out.println("qcp " + Timer.get());
	}

	private void allQcp(BiwordedStructure queryBiwords) {
		dirs.createTask("linear_");
		Biword[] biwords = queryBiwords.getBiwords();
		Superposer superposer = new Superposer();
		int hit=0;
		int total=0;
		Timer.start();
		for (Biword x : biwords) {
			for (BiwordedStructure bs : biwordLoader) {
				for (Biword y : bs.getBiwords()) {
					/*superposer.set(x.getPoints3d(), y.getPoints3d());
					double rmsd = superposer.getRmsd();
					if (rmsd < parameters.getMaxFragmentRmsd()) {
						hit++;
					}*/
					total++;
				}				
			}
			System.out.println(hit);
		}
		Timer.stop();
		System.out.println("hit = " + hit);
		System.out.println("total = " + total);
		System.out.println("time = " + Timer.get());
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
