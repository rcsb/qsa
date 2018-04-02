package fragment.cluster;

import geometry.superposition.Superposer;
import java.io.BufferedWriter;
import java.io.IOException;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import structure.visual.Graphics;
import structure.visual.PdbLine;
import util.Counter;

/**
 *
 * @author Antonin Pavelka
 */
public class Fragment {

	Point3d[] points;
	private double hash;

	public Fragment() {

	}

	public Fragment(Point3d[] points) {
		this.points = new Point3d[points.length];
		for (int i = 0; i < points.length; i++) {
			this.points[i] = new Point3d(points[i]);
		}
		this.hash = getHash();
	}

	private double getHash() {
		double hash = 0;
		for (Point3d x : points) {
			for (Point3d y : points) {
				hash += x.distance(y);
			}
		}
		return hash;
	}

	public boolean check() {
		boolean b = Math.abs(hash - getHash()) / hash < 0.0001;
		assert b : hash + " " + getHash();
		return b;
	}

	public void center() {
		check();
		Point3d center = new Point3d();
		for (Point3d point : points) {
			center.add(point);
		}
		center.scale(1.0 / points.length);
		center.negate();
		for (Point3d point : points) {
			point.add(center);
		}
		check();
	}

	public void transform(Matrix4d matrix) {
		for (Point3d point : points) {
			matrix.transform(point);
		}
	}

	public Point3d[] getPoints() {
		return points;
	}

	public void saveAsPdb(int residueNumber, Counter serial, BufferedWriter bw) throws IOException {
		check();
		for (int i = 0; i < points.length; i++) {
			bw.write(Graphics.pointToPdb(points[i], residueNumber, "C", serial.value()));
			if (i > 0 && i != points.length / 2) {
				bw.write(PdbLine.getConnectString(serial.value(), serial.value() - 1));
			}
			serial.inc();
		}
	}

	public double rmsd(Fragment other) {
		Superposer superposer = new Superposer();
		superposer.set(getPoints(), other.getPoints());
		return superposer.getRmsd();
	}
}
