package structure.visual;

import geometry.primitives.Point;
import javax.vecmath.Point3d;

/**
 *
 * @author Antonin Pavelka
 */
public class Graphics {

	public static String pointToPdb(Point3d point, int residueNumber, String element, int serial) {
		return pointToPdb(new Point(point), residueNumber, element, serial);
	}

	public static String pointToPdb(Point point, int residueNumber, String element, int serial) {
		PdbLine pl = new PdbLine(serial, element, element, residueNumber + "",
			Integer.toString(residueNumber), 'A', point.x, point.y, point.z);
		return pl.toString() + "\n";
	}
}
