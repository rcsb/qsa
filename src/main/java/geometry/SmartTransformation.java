package geometry;

import javax.vecmath.Point3d;

import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.SVDSuperimposer;
import org.biojava.nbio.structure.StructureException;

import fragments.Fragment;
import fragments.Parameters;
import superposition.SuperPositionQCP;

public class SmartTransformation {
	
	private SuperPositionQCP qcp = new SuperPositionQCP(true);
	private Point translation_;
	private Point eulerAngles;
	private SVDSuperimposer svd;
	private static Parameters par = Parameters.create();
	private double rmsd;

	
	// TODO copy coordinates whenever needed
	public SmartTransformation(Point3d[] x, Point3d[] y) {

		qcp.set(x, y);
		rmsd = qcp.getRmsd();

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
	private SmartTransformation(SVDSuperimposer svd) {
		this.svd = svd;
		this.translation_ = new Point(svd.getTranslation().getCoords());
		this.eulerAngles = new Point(Calc.getXYZEuler(svd.getRotation()));
	}

	public SVDSuperimposer getSuperimposer() {
		return svd;
	}

}
