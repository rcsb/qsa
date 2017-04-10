package fragments.clustering;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.vecmath.Matrix4d;

import fragments.FragmentPair;
import pdb.Residue;
import pdb.ResidueId;

/**
 *
 * @author Antonin Pavelka
 */
@Deprecated
public class DeprecatedCluster implements Comparable<DeprecatedCluster> {

	private List<FragmentPair> list = new ArrayList<>();
	private FragmentPair core;
	private int coverage = -1;
	private double score;
	private Matrix4d matrix;
	private ResidueId[][] aln;
	private double rmsd;
	private int id;
	private static int idG;
	private String loadA;
	private String loadB;
	private File fileA;
	private File fileB;

	public DeprecatedCluster(FragmentPair p) {
		list.add(p);
		core = p;
		id = idG++;
	}

	private void setMatrix(Matrix4d m) {
		this.matrix = new Matrix4d(m);
	}

	public Matrix4d getMatrix() {
		return matrix;
	}

	public int getCoverage() {
		return getAlignment()[0].length;
	}

	public ResidueId[][] getAlignment() {
		return aln;
	}

	public void setRmsd(double d) {
		rmsd = d;
	}

	public double getRmsd() {
		return rmsd;
	}

	public String getLoadA() {
		return loadA;
	}

	public String getLoadB() {
		return loadB;
	}

	public File getFileA() {
		return fileA;
	}

	public File getFileB() {
		return fileB;
	}

	public double getScore() {
		return score;
	}

	public FragmentPair getCore() {
		return core;
	}

	public List<FragmentPair> getFragmentPairs() {
		return list;
	}

	public void add(FragmentPair p) {
		list.add(p);
		p.capture();
	}

	public void add(FragmentPair p, double rmsd) {
		list.add(p);

	}

	public int size() {
		return list.size();
	}

	@Override
	public int compareTo(DeprecatedCluster other) {
		return Double.compare(score, other.score);
	}
}
