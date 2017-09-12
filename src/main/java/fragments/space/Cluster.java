package fragments.space;

import fragments.Biword;
import javax.vecmath.Point3d;

/**
 *
 * @author Antonin Pavelka
 */
public class Cluster {

	private Biword representant;
	private Point3d[] points;

	public Cluster(Biword bw) {
		this.representant = bw;
		this.points = bw.getPoints3d();
	}

	public void add(Biword bw) {

	}

	public Point3d[] getPoints() {
		return points;
	}
}
