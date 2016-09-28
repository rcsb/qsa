package spark.clustering;

import java.util.Random;

public class Entity implements Clusterable<Entity> {

	private static final long serialVersionUID = 1L;
	private static Random random = new Random(1);
	private static int dimensions = 100;
	private double[] coords = new double[dimensions];

	public Entity() {
		for (int i = 0; i < dimensions; i++) {
			coords[i] = random.nextDouble();
		}
	}

	public double distance(Entity e) {
		double sum = 0;
		for (int i = 0; i < dimensions; i++) {
			sum += coords[i] - e.coords[i];
		}
		return sum;
	}

}
