package alignment;

import geometry.GridRangeSearch;
import geometry.NumberedPoint;
import geometry.PointConversion;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.vecmath.Point3d;

// unclear use case
@Deprecated
public class PointMatcher {

	private NumberedPoint[] xs_, ys_;
	private Point3d[] x3, y3;
	private GridRangeSearch<NumberedPoint> grid_;

	public PointMatcher(Point3d[] x, Point3d[] y) {
		x3 = x;
		y3 = y;
		xs_ = PointConversion.getNumberedPoints(x);
		ys_ = PointConversion.getNumberedPoints(y);
		grid_ = new GridRangeSearch<>(5);
		grid_.buildGrid(Arrays.asList(ys_));
	}

	private List<PointPair> getOrderedPairs() {
		List<PointPair> pps = new ArrayList<>();
		for (int xi = 0; xi < xs_.length; xi++) {
			NumberedPoint x = xs_[xi];
			assert x != null;
			assert grid_ != null;
			List<NumberedPoint> ys = grid_.nearest(x, 3.5);
			assert ys != null;
			for (int yi = 0; yi < ys.size(); yi++) {
				NumberedPoint y = ys.get(yi);
				PointPair pp = new PointPair(x.getNumber(), y.getNumber(), x.distance(y));
				pps.add(pp);
			}
		}
		Collections.sort(pps);
		return pps;
	}

	public Point3d[][] match() {
		List<PointPair> pps = getOrderedPairs();
		boolean[] yUsed = new boolean[ys_.length];
		List<PointPair> list = new ArrayList<>();
		for (PointPair pp : pps) { // x is unique, but y may repeat
			// System.out.println("trying " + pp.getX() + " " + pp.getY() + " "
			// + pp.getD());
			int yi = pp.getY();
			if (yUsed[yi]) {
				continue;
			}
			yUsed[yi] = true;
			list.add(pp);
		}

		Point3d[][] aligned = new Point3d[2][list.size()];
		for (int i = 0; i < list.size(); i++) {
			PointPair pp = list.get(i);
			aligned[0][i] = x3[pp.getX()];
			aligned[1][i] = y3[pp.getY()];
		}
		return aligned;
	}
}
