package fragments;

import grid.sparse.Buffer;
import grid.sparse.MultidimensionalArray;
import java.util.List;

public class UniversalBiwordGrid {

	private MultidimensionalArray grid;
	private final int dim = Biword.DIMENSION;
	private final double[] diffs = {3, 3, 1.2};
	//private final double[] diffs = {5, 5, 1.5};
	private final double[] sizes = {1, 1, 0.4};
	private final int bins = 4;
	Double[] globalMin, globalMax;
	double[] ranges = Parameters.create().getRanges();

	private UniversalBiwordGrid() {
	}

	public int size() {
		return grid.size();
	}

	public UniversalBiwordGrid(List<Biword> biwords) {
		// determine ranges
		globalMin = new Double[dim];
		globalMax = new Double[dim];
		for (Biword b : biwords) {
			double[] x = b.getCoords();
			for (int i = 0; i < x.length; i++) {
				if (globalMin[i] == null || x[i] < globalMin[i]) {
					globalMin[i] = x[i];
				}
				if (globalMax[i] == null || x[i] > globalMax[i]) {
					globalMax[i] = x[i];
				}
			}
		}

		grid = new MultidimensionalArray(biwords.size());

		for (Biword b : biwords) {
			int[] indexes = discretize(b.getCoords());
			grid.insert(indexes, b);
		}

		//grid = new SparseGrid();
		//grid = new GridSearch(sizes, diffs);
		//grid.buildGrid(biwords);
	}

	private int[] discretize(double[] x) {
		int[] indexes = new int[x.length];
		for (int i = 0; i < x.length; i++) {
			double v = x[i];
			if (v < globalMin[i]) {
				v = globalMin[i];
			}
			if (v > globalMax[i]) {
				v = globalMax[i];
			}
			indexes[i] = (int) Math.floor((v - globalMin[i]) / (globalMax[i] - globalMin[i]) * bins);
		}
		return indexes;
	}

	private int disc(double d, double min, double max) {
		int i = (int) Math.floor((d - min) / (max - min) * bins);
		if (i >= bins) {
			i = bins; // in case it is highest possible double within range
		}
		return i;
	}

	// TODO write a test on fake biwords
	// TODO implement filter, passed to getRange 
	public void search(Biword b, Buffer<Biword> out) {
		double[] min = new double[dim];
		double[] max = new double[dim];
		double[] x = b.getCoords();
		for (int i = 0; i < dim; i++) {
			min[i] = x[i] - ranges[i];
			max[i] = x[i] + ranges[i];
			grid.getRange(discretize(min), discretize(max), out);
		}
	}

	public static void main(String[] args) {
		UniversalBiwordGrid g = new UniversalBiwordGrid();
		System.out.println(g.disc(4.95, 1, 5));
	}
}
