package fragment.cluster;

import fragment.ObjectSample;
import geometry.superposition.Superposer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonin Pavelka
 */
public class Cluster {

	private List<Fragment> content = new ArrayList<>();
	private Fragment centroid;
	private double radius;

	public Cluster() {

	}

	public Cluster(Fragment representant) {
		this.centroid = representant;
		this.centroid.center();
		//add(representant);
	}

	public void add(Fragment element) {
		content.add(element);
		align(element);
	}

	private void align(Fragment element) {
		element.center();
		Superposer superposer = new Superposer();
		superposer.set(centroid.getPoints(), element.getPoints());
		element.transform(superposer.getMatrix());
	}

	public void updateRadius(double elementCenterDistance) {
		if (elementCenterDistance > radius) {
			radius = elementCenterDistance;
		}
	}

	public Fragment getCentroid() {
		return centroid;
	}

	public double getRadius() {
		return radius;
	}

	public int size() {
		return content.size();
	}

	public ObjectSample getContent() {
		return new ObjectSample(content);
	}

	public boolean validate() {
		boolean ok = true;
		for (Fragment f : content) {

			Superposer superposer = new Superposer();
			superposer.set(centroid.getPoints(), f.getPoints());
			double rrr = superposer.getRmsd();

			if (rrr > 10) {
				ok = false;
				System.out.println("############ " + rrr);
			}
		}
		return ok;
	}

}
