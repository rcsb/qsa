package geometry;

import javax.vecmath.Point3d;

import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.SVDSuperimposer;
import org.biojava.nbio.structure.StructureException;

import fragments.Fragment;
import fragments.Parameters;

/**
 *
 * @author Antonin Pavelka
 */
public class Transformation {

	private Point translation_;
	private Point eulerAngles;
	private SVDSuperimposer svd;
	private static Parameters par = Parameters.create();

	public Transformation(Fragment a, Fragment b) {
		translation_ = b.getCenter().minus(a.getCenter());
		Point3d[] ap = PointConversion.getPoints3d(a.getCenteredPoints());
		Point3d[] bp = PointConversion.getPoints3d(b.getCenteredPoints());
		try {
			svd = new SVDSuperimposer(ap, bp);
			eulerAngles = new Point(Calc.getXYZEuler(svd.getRotation()));
		} catch (StructureException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * Maybe keep for later, for comparison with BioJava aligners.
	 */
	@Deprecated
	private Transformation(SVDSuperimposer svd) {
		this.svd = svd;
		this.translation_ = new Point(svd.getTranslation().getCoords());
		this.eulerAngles = new Point(Calc.getXYZEuler(svd.getRotation()));
	}

	public SVDSuperimposer getSuperimposer() {
		return svd;
	}

	public boolean close(Transformation other) {
		// double td = other.translation_.minus(translation_).size();
		double rd = other.eulerAngles.minus(eulerAngles).size();
		if (rd < par.getMaxRotation()) {
			return true;
		} else {
			return false;
		}
	}

}
