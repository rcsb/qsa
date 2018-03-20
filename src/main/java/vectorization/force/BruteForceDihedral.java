package vectorization.force;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonin Pavelka
 */
public class BruteForceDihedral {

	private int n = 100;
	private RandomTriangles generator = new RandomTriangles();
	private List<RigidBodyPair> bodies = new ArrayList<>();

	// try to use two closest for initial optimization?
	public BruteForceDihedral() {
	}

	private void run() {
		bodies.add(generator.generate());
		for (int i = 0; i < n; i++) {
			RigidBodyPair x = generator.generate();
			add(x);
		}
	}

	private void add(RigidBodyPair x) {
		double[] rmsds = computeRmsds(x);
		double[] vds = computeVectorDistances(x);
		double force = computeForce(rmsds, vds);

	}

	private double[] computeRmsds(RigidBodyPair x) {
		double[] rmsds = new double[bodies.size()];
		for (int i = 0; i < bodies.size(); i++) {
			RigidBodyPair y = bodies.get(i);
			rmsds[i] = x.rmsdDistance(y);
		}
		return rmsds;
	}

	private double computeForce(double[] real, double[] approximated) {
		double sum = 0;
		int counter = 0;
		for (int i = 0; i < real.length; i++) {
			sum += real[i] - approximated[i];
			counter++;
		}
		return sum / counter;
	}

	public double[] computeVectorDistances(RigidBodyPair x) {
		double[] vds = new double[bodies.size()];
		for (int i = 0; i < bodies.size(); i++) {
			RigidBodyPair y = bodies.get(i);
			vds[i] = x.vectorDistance(y);
		}
		return vds;
	}

	public static void main(String[] args) {
		BruteForceDihedral m = new BruteForceDihedral();
		m.run();
	}
}
