package fragments.index;

import fragments.Biword;
import geometry.Point;
import io.Directories;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point3d;

/**
 *
 * @author Antonin Pavelka
 */
public class IndexFile {

	private final Directories dirs = Directories.createDefault();

	private List<float[]> vectors = new ArrayList<>();
	private List<float[]> coords = new ArrayList<>();

	public void add(Biword bw, DataOutputStream dos) throws IOException {
		double[] vs = bw.getSmartVector();
		if (vs == null) {
			System.err.println("Smart vector problem.");
			return;
		}
		Point[] points = bw.getPoints();
		double[] cs = new double[points.length * 3];
		for (int i = 0; i < points.length; i++) {
			Point p = points[i];
			cs[i * 3] = p.x;
			cs[i * 3 + 1] = p.y;
			cs[i * 3 + 2] = p.z;
		}

		dos.writeByte(vs.length);
		for (int i = 0; i < vs.length; i++) {
			dos.writeFloat((float) vs[i]);
		}

		dos.writeByte(cs.length);
		for (int i = 0; i < cs.length; i++) {
			dos.writeFloat((float) cs[i]);
		}

	}

	private static float[] readFloatArray(DataInputStream dis) throws IOException {
		int n = dis.readByte();
		float[] a = new float[n];
		for (int i = 0; i < n; i++) {
			a[i] = dis.readFloat();
		}
		return a;
	}

	public void readAll(int max) {
		try (DataInputStream dis = new DataInputStream(
			new BufferedInputStream(new FileInputStream(dirs.getBiwordIndex())))) {
			boolean open = true;
			while (open) {
				try {
					vectors.add(readFloatArray(dis));
					coords.add(readFloatArray(dis));
					if (vectors.size() >= max) {
						return;
					}
				} catch (EOFException ex) {
					open = false;
				}
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public int size() {
		return vectors.size();
	}

	public Point3d[] getPoints(int index) {
		float[] cs = coords.get(index);
		Point3d[] points = new Point3d[cs.length / 3];
		for (int i = 0; i < points.length; i++) {
			points[i] = new Point3d(cs[i * 3], cs[i * 3 + 1], cs[i * 3 + 2]);
		}
		return points;
	}

	public float[] getVector(int index) {
		return vectors.get(index);
	}

}
