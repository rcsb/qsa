package fragment.cluster;

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
public class FragmentPoints {

	Point3d[] points;

	public FragmentPoints() {

	}

	public void center() {
		Point3d center = new Point3d();
		for (Point3d point : points) {
			center.add(point);
		}
		center.scale(1.0 / points.length);
		center.negate();
		for (Point3d point : points) {
			point.add(center);
		}
	}

	public void transform(Matrix4d matrix) {
		for (Point3d point : points) {
			matrix.transform(point);
		}
	}

	public FragmentPoints(Point3d[] points) {
		this.points = points;
	}

	public Point3d[] getPoints() {
		return points;
	}

	public void saveAsPdb(int residueNumber, Counter serial, BufferedWriter bw) throws IOException {
		for (int i = 0; i < points.length; i++) {
			bw.write(Graphics.pointToPdb(points[i], residueNumber, "C", serial.value()));
			bw.write("\n");
			if (i > 0 && i != points.length / 2) {
				bw.write(PdbLine.getConnectString(serial.value(), serial.value() - 1));
				bw.write("\n");
			}
			serial.inc();
		}
	}
}
