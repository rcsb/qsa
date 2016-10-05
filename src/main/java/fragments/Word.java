package fragments;

import geometry.Point;
import java.io.Serializable;
import statistics.Statistics;
import java.util.ArrayList;
import java.util.List;
import pdb.Residue;

/**
 *
 * @author Antonin Pavelka
 */
public class Word implements Serializable {

	private List<Residue> residues_;
	private List<Double> intDist_ = new ArrayList<>();
	private Point center;

	public Word(List<Residue> residues) {
		residues_ = new ArrayList<>(residues);
		computeInternalDistances();
	}

	private void computeInternalDistances() {
		for (int x = 0; x < residues_.size(); x++) {
			for (int y = 0; y < x; y++) {
				Point a = residues_.get(x).getPosition();
				Point b = residues_.get(y).getPosition();
				intDist_.add(a.distance(b));
			}
		}
	}

	public int seqDist(Word other) {
		int d = Integer.MAX_VALUE;
		for (Residue x : residues_) {
			for (Residue y : other.residues_) {
				int diff = Math.abs(x.getIndex() - y.getIndex());
				if (diff < d) {
					d = diff;
				}
			}
		}
		return d;
	}

	public boolean isInContact(Word other, double threshold) {
		Residue a1 = residues_.get(0);
		Residue a2 = residues_.get(residues_.size() - 1);
		Residue b1 = other.residues_.get(0);
		Residue b2 = other.residues_.get(residues_.size() - 1);
		return a1.distance(b1) <= threshold && a1.distance(b2) <= threshold && a2.distance(b1) <= threshold
				&& a2.distance(b2) <= threshold;
	}

	public double shapeDifference(Word other) {
		return Statistics.difference(intDist_, other.intDist_);
	}

	public Point[] getPoints() {
		Point[] points = new Point[residues_.size()];
		for (int i = 0; i < residues_.size(); i++) {
			points[i] = residues_.get(i).getPosition();
		}
		return points;
	}

	public List<Residue> getResidues() {
		return residues_;
	}

	public Point getCenter() {
		if (center == null) {
			Point sum = new Point(0, 0, 0);
			Point[] ps = getPoints();
			for (Point p : ps) {
				sum = sum.plus(p);
			}
			center = sum.divide(ps.length);
		}
		return center;
	}
}
