package spark.interfaces;

import org.biojava.nbio.structure.align.model.AFPChain;

public class FatcatAlignmentWrapper extends Alignment {

	private static final long serialVersionUID = 1L;
	private AFPChain o;

	public FatcatAlignmentWrapper(AFPChain o) {
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
		return sb.toString();
	}

}
