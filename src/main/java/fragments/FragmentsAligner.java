/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fragments;

import alignment.score.Equivalence;
import alignment.score.EquivalenceOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import geometry.Transformer;
import io.Directories;
import io.LineFile;
import java.util.Collection;
import pdb.Residue;
import pdb.SimpleStructure;
import spark.interfaces.AlignablePair;
import util.Timer;

/**
 *
 * @author Antonin Pavelka
 */
public class FragmentsAligner {

	private final transient Directories dirs;
	private final FragmentsFactory ff;
	private AlignablePair alignablePair;
	private final LineFile resultsFile;
	private final boolean visualize;

	public FragmentsAligner(Directories dirs, boolean visualize) {
		this.dirs = dirs;
		this.visualize = visualize;
		resultsFile = new LineFile(dirs.getResultsFile());
		ff = new FragmentsFactory();
	}

	public void align(AlignablePair sp, EquivalenceOutput eo, int alignmentNumber) {
		this.alignablePair = sp;
		Parameters pars = Parameters.create();
		Fragments a = ff.create(sp.getA(), pars.getWordLength(), pars.skipX());
		Fragments b = ff.create(sp.getB(), pars.getWordLength(), pars.skipY());
		align(a, b, eo, alignmentNumber);
	}

	private void align(Fragments a, Fragments b, EquivalenceOutput eo, int alignmentNumber) {
		Parameters par = Parameters.create();
		double[] result = {0, 0, 0};
		List<FragmentPair> hsp = new ArrayList<>();
		long start = System.nanoTime();

		Transformer tr = new Transformer();
		AwpGraph wg = new AwpGraph();

		boolean biwords = true;
		if (biwords) {
			//System.out.println("Matching pairs of words, fragment numbers: "
			//+ a.size() + " " + b.size() + " ...");
			Timer.start();
			WordMatcher wm = new WordMatcher(a.getWords(), b.getWords(), false,
				par.getMaxWordRmsd());
			Timer.stop();
			//System.out.println("... word similarity computed in: " + Timer.get());

			BiwordGrid bg = new BiwordGrid(Arrays.asList(b.getFragments()));
			double[] max = {0, 0, 0, 0, 0, 0};
			Timer.start();
			int similar = 0;
			int total = 0;
			for (int xi = 0; xi < a.size(); xi++) {
				Fragment x = a.get(xi);

				List<Fragment> near = bg.search(x);
				//System.out.println(near.size());

				for (Fragment y : near) {
					//for (int yi = 0; yi < b.size(); yi++) {
					//Fragment y = b.get(yi);
					total++;
					if (x.isSimilar(y, wm)) { // TODO check here best W-W alignment is similar to F-F
						similar++;
						tr.set(x.getPoints3d(), y.getPoints3d());
						double rmsd = tr.getRmsd();
						if (rmsd <= par.getMaxFragmentRmsd()) {
							hsp.add(new FragmentPair(x, y, rmsd));
							AwpNode[] ps = {new AwpNode(x.getWords()[0], y.getWords()[0]),
								new AwpNode(x.getWords()[1], y.getWords()[1])};
							wg.connect(ps, rmsd);
							double[] diff = x.coordDiff(y);
							for (int i = 0; i < diff.length; i++) {
								if (max[i] < diff[i]) {
									max[i] = diff[i];
								}
							}
						}
					}
				}
			}
			Timer.stop();
			//System.out.println("DIFF " + max[0] + " " + max[1] + " " + max[2]);
			//System.out.println("... fragment matching finished in: " + Timer.get());
			//System.out.println("similar / total " + similar + " / " + total);
		} else {
			WordMatcher wm = new WordMatcher(a.getWords(), b.getWords(), true,
				par.getMaxWordRmsd());
			List<Awp> alignedWords = wm.getAlignedWords();
			//System.out.println("Awps: " + alignedWords.size());
			EulerGrid eg = new EulerGrid(alignedWords);
			int count = 0;
			for (Awp x : alignedWords) {
				List<Awp> near = eg.search(x);
				for (Awp y : near) {
					if (!x.equals(y)) {
						count++;
					}

				}
			}
		}
		AwpClustering clustering = wg.cluster();
		align(a.getStructure(), b.getStructure(), clustering, eo, alignmentNumber);
	}

	private static double mr;
	private static double tmscore;
	private static int counter;

	private void align(SimpleStructure a, SimpleStructure b, AwpClustering clustering,
		EquivalenceOutput eo, int alignmentNumber) {
		
		Collection<AwpCluster> clusters = clustering.getGoodClusters(Math.min(a.size(), b.size()));
		AlignmentCore[] as = new AlignmentCore[clusters.size()];
		int i = 0;
		for (AwpCluster c : clusters) {
			Residue[][] superpositionAlignment = c.computeAlignment();
			as[i] = new AlignmentCore(a, b, superpositionAlignment, c.getDebugger());
			i++;
		}
		Arrays.sort(as);
		boolean first = true;
		int alignmentVersion = 1;
		if (as.length == 0) {
			Equivalence eq = new Equivalence(a, b, new Residue[2][0]);
			eo.saveResults(eq);
		} else {
			for (AlignmentCore ac : as) {
				if (first) {
					Equivalence eq = ac.getEquivalence();
					mr += eq.matchingResiduesRelative();
					tmscore += eq.tmScore();
					counter++;
					System.out.println("* " + (tmscore / counter) + " " + (mr / counter));
					eo.saveResults(eq);
					first = false;
					if (visualize) {
						eo.setDebugger(ac.getDebugger());
						eo.visualize(eq, ac.getSuperpositionAlignment(), alignmentNumber, alignmentVersion);
					}
					alignmentVersion++;
				}
			}
		}
	}

}
