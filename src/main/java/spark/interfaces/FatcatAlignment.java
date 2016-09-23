package spark.interfaces;

import javax.vecmath.Matrix4d;

import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.align.model.AFPChain;
import org.biojava.nbio.structure.align.util.AlignmentTools;
import org.biojava.nbio.structure.jama.Matrix;

public class FatcatAlignment extends Alignment {

	private static final long serialVersionUID = 1L;
	private AFPChain o;

	public FatcatAlignment(AFPChain o) {
		this.o = o;
	}

	public AFPChain get() {
		return o;
	}

	public static String getHeader() {
		StringBuilder sb = new StringBuilder("a_id").append(SEP);
		sb.append("b_id").append(SEP);
		sb.append("fatcat_tm_score").append(SEP);
		sb.append("probability").append(SEP);
		sb.append("align_score").append(SEP);
		sb.append("norm_align_score").append(SEP);
		sb.append("afp_chain_l").append(SEP);
		sb.append("euler_1").append(SEP);
		sb.append("euler_2").append(SEP);
		sb.append("euler_3").append(SEP);
		return sb.toString();
	}

	public String getLine() {
		StringBuilder sb = new StringBuilder(o.getName1()).append(SEP);
		sb.append(o.getName2()).append(SEP);
		sb.append(o.getTMScore()).append(SEP);
		sb.append(o.getProbability()).append(SEP);
		sb.append(o.getAlignScore()).append(SEP);
		sb.append(o.getNormAlignScore()).append(SEP);
		sb.append(o.getAfpChainLen()).append(SEP);
		Matrix m = o.getBlockRotationMatrix()[0];
		double[] euler = Calc.getXYZEuler(m);
		sb.append(euler[0]).append(SEP);
		sb.append(euler[1]).append(SEP);
		sb.append(euler[2]).append(SEP);
		// Matrix4d m4 = new Matrix4d(m.getArray());
		// Calc.getTranslationVector(m4);
		// AlignmentTools.getSuperimposer(arg0, arg1, arg2, arg3, arg4);
		// o.getBlockRotationMatrix();
		// sb.append(o.getBlockRotationMatrix()).append(SEP);
		return sb.toString();
	}

}
