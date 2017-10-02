package biword;

import fragments.Biword;
import fragments.Biwords;
import grid.sparse.Buffer;
import grid.sparse.MultidimensionalArray;
import pdb.StructureProvider;
import util.Timer;

/**
 * In memory index and biword database.
 */
public class Index {

	private final double[] globalMin = new double[10];
	private final double[] globalMax = new double[10];
	private final int bracketN = 20;
	private int biwordN = 0;
	private MultidimensionalArray grid;
	private Buffer out;
	private final BiwordsProvider biwordsProvider;

	public Index(StructureProvider structureProvider) {
		biwordsProvider = new BiwordsProvider(structureProvider);
		build();
	}

	private void build() {
		while (biwordsProvider.hasNext()) {
			try {
				Biwords bs = biwordsProvider.next();
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
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		out = new Buffer(biwordN);

		System.out.println("BOUNDARIES");
		for (int d = 0; d < globalMin.length; d++) {
			System.out.println(globalMin[d] + " - " + globalMax[d] + " | ");
		}
		System.out.println("----");

		System.out.println("inserting...");
		Timer.start();

		grid = new MultidimensionalArray(biwordN, 10, bracketN);
		for (int i = 0; i < 4; i++) {
			grid.setCycle(i);
		}
		biwordsProvider.restart();
		while (biwordsProvider.hasNext()) {
			try {
				Biwords bs = biwordsProvider.next();
				for (Biword bw : bs.getBiwords()) {
					grid.insert(discretize(bw.getSmartVector()), bw);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		Timer.stop();
		System.out.println("...finished " + Timer.get());
	}

	public Buffer query(Biword bw) {
		float a = 90;
		float shift = 4;
		float[] box = {a, a, a, a, shift, shift, shift, shift, shift, shift};

		float[] vector = bw.getSmartVector();

		Timer.start();

		// query
		int dim = vector.length;
		float[] min = new float[dim];
		float[] max = new float[dim];
		for (int i = 0; i < dim; i++) {
			min[i] = vector[i] - box[i];
			max[i] = vector[i] + box[i];
		}

		out.clear();

		grid.getRange(discretize(min), discretize(max), out);

		Timer.stop();
		//System.out.println("grid   " + out.size() + " in " + Timer.get());
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
