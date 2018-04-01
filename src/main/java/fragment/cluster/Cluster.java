package fragment.cluster;

import geometry.superposition.Superposer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonin Pavelka
 */
public class Cluster {

	private List<FragmentPoints> content = new ArrayList<>();
	private FragmentPoints centroid;
	private double radius;

	public Cluster() {

	}

	public Cluster(FragmentPoints representant) {
		this.centroid = representant;
		this.centroid.center();
		//add(representant);
	}

	public void add(FragmentPoints element) {
		content.add(element);
		align(element);
	}

	private void align(FragmentPoints element) {
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

	public FragmentPoints getCentroid() {
		return centroid;
	}

	public double getRadius() {
		return radius;
	}

	public int size() {
		return content.size();
	}

	public List<FragmentPoints> getContent() {
		return content;
	}

	public boolean validate() {
		boolean ok = true;
		for (FragmentPoints f : content) {

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
