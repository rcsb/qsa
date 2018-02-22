package geometry.search;

import geometry.primitives.Coordinates;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * This class implements a structure containing set of points and allows efficient identification of the points in a
 * sphere.
 */
public class GridRangeSearch<T extends Coordinates> {

	private Bucket[][][] cells;
	private double[] origin = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
	private double d;

	public GridRangeSearch(double d) {
		this.d = d;
	}

	private int index(double zero, double value) {
		return (int) Math.round((float) (value - zero) / d);
	}

	public void buildGrid(Collection<T> ts) {
		double[] max = {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
		for (T t : ts) {
			for (int i = 0; i < 3; i++) {
				if (t.getCoords()[i] < this.origin[i]) {
					this.origin[i] = t.getCoords()[i];
				}
				if (max[i] < t.getCoords()[i]) {
					max[i] = t.getCoords()[i];
				}
			}
		}
		cells = new Bucket[index(origin[0], max[0]) + 1][index(origin[1], max[1]) + 1][index(origin[2], max[2]) + 1];
		for (T t : ts) {
			int[] g = new int[3];
			for (int i = 0; i < 3; i++) {
				g[i] = index(this.origin[i], t.getCoords()[i]);
			}
			Bucket bucket;
			if (cells[g[0]][g[1]][g[2]] == null) {
				bucket = new Bucket();
				cells[g[0]][g[1]][g[2]] = bucket;
			} else {
				bucket = cells[g[0]][g[1]][g[2]];
			}
			bucket.add(t);
		}
	}

	/**
	 * @param buffer result of the query are stored here, must be initialized before calling the method
	 * @param q center of the query sphere
	 * @param r radius of the sphere
	 */
	public void nearest(Coordinates q, double r, Collection<T> buffer) {
		double sq_r = r * r;
		double[] low = {q.getCoords()[0] - r, q.getCoords()[1] - r, q.getCoords()[2] - r};
		double[] high = {q.getCoords()[0] + r, q.getCoords()[1] + r, q.getCoords()[2] + r};
		int[] lo = {index(origin[0], low[0]), index(origin[1], low[1]), index(origin[2], low[2])};
		int[] hi = {index(origin[0], high[0]), index(origin[1], high[1]), index(origin[2], high[2])};
		for (int i = 0; i < 3; i++) {
			lo[i] = Math.max(0, lo[i]);
		}
		hi[0] = Math.min(cells.length - 1, hi[0]);
		hi[1] = Math.min(cells[0].length - 1, hi[1]);
		hi[2] = Math.min(cells[0][0].length - 1, hi[2]);
		for (int x = lo[0]; x <= hi[0]; x++) {
			for (int y = lo[1]; y <= hi[1]; y++) {
				for (int z = lo[2]; z <= hi[2]; z++) {
					Bucket bucket = cells[x][y][z];
					if (bucket != null) {
						for (Object o : bucket) {
							T t = (T) o;
							double sq_d = 0;
							double[] a = q.getCoords();
							double[] b = t.getCoords();
							for (int i = 0; i < 3; i++) {
								double diff = a[i] - b[i];
								sq_d += diff * diff;
							}
							if (sq_d <= sq_r) {
								buffer.add(t);
							}
						}
					}
				}
			}
		}
	}
}

class Bucket implements Iterable {

	List list = new ArrayList<>();

	public void add(Object t) {
		list.add(t);
	}

	@Override
	public Iterator iterator() {
		return list.iterator();
	}
}
