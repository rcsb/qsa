package biword;

import algorithm.Biword;
import algorithm.Biwords;
import global.Parameters;
import grid.sparse.Buffer;
import grid.sparse.MultidimensionalArray;
import global.io.Directories;
import pdb.Structures;
import util.Timer;

/**
 * In memory index and biword database.
 */
public class Index {

	private final Directories dirs;
	private final double[] globalMin = new double[10];
	private final double[] globalMax = new double[10];
	private final int bracketN = 20;
	private int biwordN = 0;
	private MultidimensionalArray<BiwordId> grid;
	private Buffer<BiwordId> out;
	private final BiwordsProvider biwordsProvider;
	private final float a = 90;
	private final float shift = 4;
	private final float[] box = {a, a, a, a, shift, shift, shift, shift, shift, shift};
	private final StructureStorage storage;
	Parameters pars = Parameters.create();

	public Index(Directories dirs, Structures structureProvider) {
		this.dirs = dirs;
		storage = new StructureStorage(dirs);
		biwordsProvider = new BiwordsProvider(dirs, structureProvider, true);
		build();
	}

	private void build() {
		Timer.start();
		for (Biwords bs : biwordsProvider) {
			System.out.println("Initialized structure " + bs.getStructure().getSource() + " " + bs.getStructure().getId());
			storage.save(bs.getStructure().getId(), bs);
			for (Biword bw : bs.getBiwords()) {
				float[] v = bw.getSmartVector();
				if (v == null) {
					continue;
				}
				biwordN++;
				for (int d = 0; d < v.length; d++) {
					if (v[d] < globalMin[d]) {
						globalMin[d] = v[d];
					}
					if (v[d] > globalMax[d]) {
						globalMax[d] = v[d];
					}
				}
			}
		}
		Timer.stop();
		System.out.println("creating structure, biwords and boundaries " + Timer.get());
		// now build the index tree using loading from HDD for each structure
		out = new Buffer(biwordN);

		//System.out.println("BOUNDARIES");
		//for (int d = 0; d < globalMin.length; d++) {
		//	System.out.println(globalMin[d] + " - " + globalMax[d] + " | ");
		//}
		//System.out.println("----");

		System.out.println("inserting...");
		Timer.start();

		grid = new MultidimensionalArray<>(biwordN, 10, bracketN);
		for (int i = 0; i < 4; i++) { // angles are cyclic - min and max values are neighbors
			grid.setCycle(i);
		}

		for (Biwords bs : storage) {
			try {
				for (Biword bw : bs.getBiwords()) {
					float[] v = bw.getSmartVector();
					if (v != null) {
						grid.insert(discretize(v), bw.getId());
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		Timer.stop();
		System.out.println("...finished " + Timer.get());
	}

	public StructureStorage getStorage() {
		return storage;
	}

	public Buffer<BiwordId> query(Biword bw) {
		float[] vector = bw.getSmartVector();
		int dim = vector.length;
		float[] min = new float[dim];
		float[] max = new float[dim];
		for (int i = 0; i < dim; i++) {
			min[i] = vector[i] - box[i];
			max[i] = vector[i] + box[i];
		}
		out.clear();
		grid.getRange(discretize(min), discretize(max), out);
		return out;
	}

	private int[] discretize(float[] x) {
		int[] indexes = new int[x.length];
		for (int i = 0; i < x.length; i++) {
			float v = x[i];
			indexes[i] = (int) Math.floor((v - globalMin[i]) / (globalMax[i] - globalMin[i]) * bracketN);
		}
		return indexes;
	}
}
