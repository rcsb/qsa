package fragments.vector;

import geometry.Transformer;
import io.Directories;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.vecmath.Point3d;

/**
 *
 * @author Antonin Pavelka
 *
 * Expresses vectorization as linear programming problem.
 *
 */
public class LinearProgramManhattan {

	private final Directories dirs = Directories.createDefault();
	private final Transformer transformer = new Transformer();

	private double dim = 100;
	private static final String eol = "\n";

	public void convert() throws Exception {
		int all = Integer.MAX_VALUE;

		Point3d[][] clustered = PointVectorDataset.read(dirs.getBiwordRepresentants(5.0), all);
		System.out.println("size " + clustered.length);

		int equationNumber = 0;
		List<String> errors = new ArrayList<>();
		List<String> constraints = new ArrayList<>();
		Set<String> coordVars = new HashSet<>();

		for (int x = 0; x < clustered.length; x++) {
			for (int y = 0; y < x; y++) {
				double d = realDistance(clustered[x], clustered[y]);
				String e = "e_" + equationNumber;
				String f = "f_" + equationNumber;
				String s = getVector(x, true) + " " + getVector(y, false)
					+ " + " + e + " - " + f + " = " + d;
				constraints.add(s);
				for (int i = 0; i < dim; i++) {
					coordVars.add(getCoordVar(x, i));
				}
				for (int i = 0; i < dim; i++) {
					coordVars.add(getCoordVar(y, i));
				}
				errors.add(e);
				errors.add(f);
				equationNumber++;
			}

		}

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(dirs.getLogicalProgram()))) {
			bw.write("Minimize" + eol);
			bw.write(" obj");
			for (String e : errors) {
				bw.write(" + " + e);
			}
			bw.write(eol);
			bw.write(eol);
			bw.write("Subject To" + eol);
			for (int i = 0; i < constraints.size(); i++) {
				bw.write(" c" + i + ": ");
				bw.write(constraints.get(i) + eol);
			}
			bw.write("Bounds" + eol);
			for (String e : errors) {
				bw.write(" 0 <= " + e + " <= 5" + eol);
			}
			for (String cv : coordVars) {
				bw.write(" 0 <= " + cv + " <= 50" + eol);
			}
		}
	}

	private String getVector(int index, boolean plus) {
		StringBuilder sb = new StringBuilder();
		for (int d = 0; d < dim; d++) {
			if (plus) {
				sb.append(" + ");
			} else {
				sb.append(" - ");
			}
			sb.append(getCoordVar(index, d));
		}
		return sb.toString();
	}

	private String getCoordVar(int index, int dim) {
		return "x_" + index + "_" + dim;
	}

	private double realDistance(Point3d[] a, Point3d[] b) {
		transformer.set(a, b);
		return transformer.getRmsd();
		//return transformer.getSumOfDifferences();
	}

	public static void main(String[] args) throws Exception {
		LinearProgramManhattan lp = new LinearProgramManhattan();
		lp.convert();
	}
}
