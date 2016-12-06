package alignment;

import java.util.List;

import org.biojava.nbio.structure.Calc;

import fragments.clustering.DeprecatedCluster;
import pdb.PdbChainId;
import pdb.SimpleStructure;
import spark.interfaces.Alignment;

/**
 *
 * @author Antonin Pavelka
 */
public class FragmentsAlignment extends Alignment {

	private static final long serialVersionUID = 1L;
	private String a_, b_;
	private double tmScore;
	private List<DeprecatedCluster> clusters;
	private int hsp;
	private double score;

	public FragmentsAlignment(SimpleStructure a, SimpleStructure b) {
		this.a_ = a.getPdbCode();
		this.b_ = b.getPdbCode();
	}

	public static String getHeader() {
		StringBuilder sb = new StringBuilder("a__id").append(SEP);
		sb.append("b__id").append(SEP);
		sb.append("tmScore").append(SEP);
		sb.append("1_cluster").append(SEP);
		sb.append("hsp").append(SEP);
		sb.append("euler__1").append(SEP);
		sb.append("euler__2").append(SEP);
		sb.append("euler__3").append(SEP);
		return sb.toString();
	}

	public String getLine() {
		StringBuilder sb = new StringBuilder(a_.toString()).append(SEP);
		sb.append(b_.toString()).append(SEP);
		sb.append(tmScore).append(SEP);
		if (clusters != null && !clusters.isEmpty()) {
			sb.append(clusters.get(0).size()).append(SEP);
		} else {
			sb.append("-").append(SEP);
		}
		sb.append(hsp).append(SEP);
		//double[] euler = Calc.getXYZEuler(t.getSuperimposer().getRotation());
		//sb.append(t.getSuperimposer());

		return sb.toString();
	}

	public void setClusters(List<DeprecatedCluster> clusters) {
		this.clusters = clusters;
	}

	public void setTmScore(double tmScore) {
		this.tmScore = tmScore;
	}

	public void setHsp(int hsp) {
		this.hsp = hsp;
	}

	public String getA() {
		return a_;
	}

	public String getB() {
		return b_;
	}

	public void setScore(double s) {
		this.score = s;
	}

	public double getScore() {
		return score;
	}

}
