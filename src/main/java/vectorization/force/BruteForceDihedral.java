package vectorization.force;

import analysis.statistics.Statistics;
import java.util.ArrayList;
import java.util.List;
import language.MathUtil;
import vectorization.dimension.DimensionCyclic;

/**
 *
 * @author Antonin Pavelka
 */
public class BruteForceDihedral {

	private int n = 100;
	private RandomTriangles generator = new RandomTriangles();
	private List<RigidBodyPair> bodies = new ArrayList<>();
	private double min, max;
	private double factor = 2;

	// try to use two closest for initial optimization?
	public BruteForceDihedral() {
	}

	private void run() {
		bodies.add(generator.generate());
		DimensionCyclic dc = (DimensionCyclic) bodies.get(0).getDimensions().getLastDimension();
		min = dc.getMin();
		max = dc.getMax();
		for (int i = 0; i < n; i++) {
			RigidBodyPair x = generator.generate();
			add(x);
		}
	}

	private void add(RigidBodyPair x) {
		double moved;
		double rmsd = computeAverageRmsd(x);
		double vd = computeAverageVectorDistance(x);

		System.out.println(rmsd - vd);
		//System.out.println(vd);
		//System.out.println("----");
		double changeSqr = rmsd * rmsd - vd * vd; // euclid only ...
		//System.out.println(changeSqr);
		if (changeSqr >= 0) {
			moved = Math.sqrt(changeSqr);
		} else {
			moved = x.getDihedral();
		}
		double wrapped = MathUtil.wrap(moved, min, max);

		System.out.println("");
		System.out.println("b " + x.getDihedral());
		System.out.println("change " + wrapped);
		x.setDihedral((float) (wrapped));
		System.out.println("aa " + x.getDihedral());

		System.out.println((rmsd - vd) + " " + (rmsd - computeAverageVectorDistance(x)));
		//bodies.add(x);
	}

	private double comuteAbsoluteError(RigidBodyPair x) {
		return Math.abs(computeAverageRmsd(x) - computeAverageVectorDistance(x));
	}

	private double computeAverageRmsd(RigidBodyPair x) {
		return Statistics.average(bodies, y -> (x.rmsdDistance(y) * factor));
	}

	private double computeAverageVectorDistance(RigidBodyPair x) {
		return Statistics.average(bodies, y -> x.vectorDistance(y));
	}

	public static void main(String[] args) {
		BruteForceDihedral m = new BruteForceDihedral();
		m.run();
	}
}
