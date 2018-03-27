package embedding.lipschitz;

import vectorization.force.RigidBodyPair;

/**
 *
 * @author Antonin Pavelka
 */
public class Base {

	private RigidBodyPair[] objects;

	public Base(RigidBodyPair... objects) {
		this.objects = objects;
	}

	public double distance(RigidBodyPair body) {
		double min = Double.MAX_VALUE;
		for (RigidBodyPair object : objects) {
			double d = object.rmsd(body);
			if (d < min) {
				min = d;
			}
		}
		return min;
	}

	public double distance(Base other) {
		double min = Double.MAX_VALUE;
		for (RigidBodyPair object : objects) {
			for (RigidBodyPair otherObject : other.objects) {
				double d = object.rmsd(otherObject);
				if (d < min) {
					min = d;
				}
			}
		}
		return min;
	}
}
