package analysis.visualization;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fragments.Cluster;
import fragments.Fragment;
import fragments.FragmentPair;
import pdb.Residue;
import scala.tools.nsc.GenericRunnerCommand.HowToRun;

public class PymolVisualizer {

	private List<Chain> chains = new ArrayList<>();
	private List<Cluster> clusters = new ArrayList<>();

	public void add(Chain c) {
		chains.add(c);
	}

	public void add(Cluster c) {
		clusters.add(c);
	}

	public void save(File pdb, File py) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(pdb));
			for (Chain c : chains) {
				c.save(bw);
			}
			bw.close();
			bw = new BufferedWriter(new FileWriter(py));
			for (Cluster c : clusters) {
				saveCluster(c, bw);
			}
			bw.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String getSelection(List<Residue> rs, char c) {
		StringBuilder sb = new StringBuilder("sele " + c + ", ");
		for (Residue r : rs) {
			sb.append("(resi " + r.getIndex() + " and chain " + c + ") + ");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	private void saveCluster(Cluster c, BufferedWriter bw) throws IOException {
		FragmentPair p = c.getCore();
		
		// for (FragmentPair p : c.getFragmentPairs()) {
		Fragment[] fs = p.getFragments();
		System.out.println(" d " + fs[0].getCenter().distance(fs[1].getCenter()));
		bw.write(getSelection(fs[0].getResidues(), 'A'));
		
		bw.newLine();
		bw.write(getSelection(fs[1].getResidues(), 'B'));
		bw.newLine();
		bw.newLine();
		// }
	}
}
