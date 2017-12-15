package alignment;

import global.io.Directories;
import global.io.PairOfAlignedFiles;
import javax.vecmath.Matrix4d;

/**
 *
 * @author Antonin Pavelka
 */
public class Alignment implements Comparable<Alignment> {

	private final Directories dirs;
	private int matchingResiduesAbsolute;
	private double matchingResidues;
	private double tmScore;
	private double rmsd;
	private double identity;
	private Matrix4d matrix;
	private final PairOfAlignedFiles pairOfAlignedFiles;
	private final StructureSourcePair sources;

	public Alignment(Directories dirs, StructureSourcePair sources) {
		this.dirs = dirs;
		this.sources = sources;
		pairOfAlignedFiles = new PairOfAlignedFiles(dirs, sources);
	}

	public PairOfAlignedFiles getPairOfAlignedFiles() {
		return pairOfAlignedFiles;
	}

	public double getTmScore() {
		return tmScore;
	}

	public double getMatchingResiduesAbsolute() {
		return matchingResiduesAbsolute;
	}

	public double getMatchingResidues() {
		return matchingResidues;
	}

	public double getRmsd() {
		return rmsd;
	}

	public double getIdentity() {
		return identity;
	}

	public Matrix4d getMatrix() {
		return matrix;
	}

	public void setMatchingResiduesAbsolute(int value) {
		matchingResiduesAbsolute = value;
	}

	public void setMatchingResidues(double value) {
		matchingResidues = value;
	}

	public void setTmScore(double value) {
		tmScore = value;
	}

	public void setRmsd(double value) {
		rmsd = value;
	}

	public void setIdentity(double value) {
		this.identity = value;
	}

	public StructureSourcePair getStructureSourcePair() {
		return sources;
	}

	public void setMatrix(Matrix4d matrix) {
		this.matrix = matrix;
	}

	@Override
	public int compareTo(Alignment other) {
		return Double.compare(other.getTmScore(), getTmScore());
	}
}
