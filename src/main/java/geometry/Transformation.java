package geometry;

import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.SVDSuperimposer;

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

	public Transformation(SVDSuperimposer svd) {
		this.svd = svd;
		this.translation_ = new Point(svd.getTranslation().getCoords());
		this.eulerAngles = new Point(Calc.getXYZEuler(svd.getRotation()));
	}

	public SVDSuperimposer getSuperimposer() {
		return svd;
	}

	public boolean close(Transformation other) {
		double td = other.translation_.minus(translation_).size();
		double rd = other.eulerAngles.minus(eulerAngles).size();
		return td < par.getMaxTranslation() && rd < par.getMaxRotation();
	}

}
