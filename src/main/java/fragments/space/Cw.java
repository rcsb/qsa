package fragments.space;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.vecmath.Point3d;
import pdb.PdbLine;
import superposition.SuperPositionQCP;

/**
 *
 * @author Antonin Pavelka
 */
public class Cw {

	private Point3d[] representant;
	//private Point3d[] points;
	private List<Point3d[]> words = new ArrayList<>();

	public Cw(Point3d[] w) {
		this.representant = w;
		add(w);
	}

	public void add(Point3d[] w) {
		words.add(w);
	}

	public Point3d[] getRepresentant() {
		return representant;
	}

	public void visualize(File f) {
		SuperPositionQCP qcp = new SuperPositionQCP(false);

		List<Point3d[]> sample = new ArrayList<>(words);
		Collections.shuffle(sample);

		Point3d[][] aligned = new Point3d[Math.min(sample.size(), 1000)][];
		Point3d[] a = representant;
		aligned[0] = representant;
		for (int i = 1; i < aligned.length; i++) {
			Point3d[] b = sample.get(i);
			qcp.set(a, b);
			Point3d[] c = qcp.getTransformedCoordinates();
			aligned[i] = c;
		}

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
			int serial = 1;
			for (int i = 0; i < aligned.length; i++) {
				int count = 0;
				for (Point3d p : aligned[i]) {
					PdbLine pl = new PdbLine(serial, "CA", "C", "GLY",
						Integer.toString(i), 'A', p.x, p.y, p.z);
					bw.write(pl.getPdbString());
					bw.newLine();
					if (count > 0) {
						bw.write(PdbLine.getConnectString(serial - 1, serial));
						bw.newLine();
					}
					count++;
					serial++;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
