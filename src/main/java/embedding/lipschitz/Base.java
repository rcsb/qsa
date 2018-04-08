package embedding.lipschitz;

/**
 *
 * Lipschitz base.
 *
 * @author Antonin Pavelka
 */
public class Base {

	private final Similar[] objects;

	public Base(Similar... objects) {
		this.objects = objects;
	}

	public double getDistance(Similar other) {
		double min = Double.MAX_VALUE;
		for (Similar object : objects) {
			double d = object.getDistance(other);
			if (d < min) {
				min = d;
			}
		}
		return min;
	}

	public double getDistance(Base other) {
		double min = Double.MAX_VALUE;
		for (Similar object : objects) {
			for (Similar otherObject : other.objects) {
				double d = object.getDistance(otherObject);
				if (d < min) {
					min = d;
				}
			}
		}
		return min;
	}
}
