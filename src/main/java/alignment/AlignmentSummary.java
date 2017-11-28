package alignment;

import global.io.Directories;
import global.io.PairOfAlignedFiles;
import pdb.StructureSource;

/**
 *
 * @author Antonin Pavelka
 */
public class AlignmentSummary implements Comparable<AlignmentSummary> {

	private final Directories dirs;
	private int matchingResiduesAbsolute;
	private double matchingResidues;
	private double tmScore;
	private double rmsd;
	private double identity;
	private final PairOfAlignedFiles pairOfAlignedFiles;
	private final StructureSourcePair sources;

	public AlignmentSummary(Directories dirs, StructureSourcePair sources) {
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

	@Override
	public int compareTo(AlignmentSummary other) {
		return Double.compare(other.getTmScore(), getTmScore());
	}
}
