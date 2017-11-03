package algorithm;

import javax.vecmath.Point3d;

public class Point3dBuffer {

	private final Point3d[] a;
	private int s;

	public Point3dBuffer(int n) {
		a = new Point3d[n];
	}

	public void clear() {
		s = 0;
	}

	public void add(Point3d t) {
		a[s++] = t;
	}

	public int size() {
		return s;
	}

	public Point3d get(int i) {
		return a[i];
	}

	public boolean isEmpty() {
		return s == 0;
	}

	public void addAll(Iterable<Point3d> it) {
		for (Point3d t : it) {
			add(t);
		}
	}

	public void addAll(Point3d[] ts) {
		for (Point3d t : ts) {
			add(t);
		}
	}

	public Point3d[] toArray() {
		Point3d[] b = new Point3d[s];
		System.arraycopy(a, 0, b, 0, s);
		return b;
	}

	public void addAll(Point3dBuffer b) {
		System.arraycopy(b.a, 0, a, s, b.s);
		s += b.s;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s; i++) {
			sb.append(a[i]);
			if (i != s - 1) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}

}
