/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fragments;

import alignment.score.Equivalence;
import fragments.alignment.Alignment;
import fragments.alignment.Alignments;
import alignment.score.EquivalenceOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import geometry.Transformer;
import io.Directories;
import io.LineFile;
import java.util.Collection;
import java.util.Collections;
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
		Alignments alignments;
		int minStrSize = Math.min(a.getStructure().size(), b.getStructure().size());
		if (Parameters.create().doClustering()) {
			alignments = wg.cluster(minStrSize);
		} else {
			alignments = wg.assembleAlignmentByExpansions(minStrSize);
		}
		align(a, b, alignments, eo, alignmentNumber);
	}

	private static double mr;
	private static double tmscore;
	private static int counter;

	private void align(Fragments fa, Fragments fb, Alignments alignments,
		EquivalenceOutput eo, int alignmentNumber) {

		SimpleStructure a = fa.getStructure();
		SimpleStructure b = fb.getStructure();

		Collection<Alignment> clusters = alignments.getAlignments();
		AlignmentCore[] as = new AlignmentCore[clusters.size()];
		int i = 0;
		//Distribution2d dist = new Distribution2d();
		double bestTmScore = 0;
		
		for (Alignment aln : clusters) {
			//System.out.println("aln " + aln.getNodes().size());
			InitialAlignment ia = new InitialAlignment(aln.getNodes());
			Residue[][] superpositionAlignment = ia.getPairing();
			AlignmentCore ac = new AlignmentCore(a, b, superpositionAlignment, aln.getScore(), null);
			as[i] = ac;
			ac.alignBiwords();
			if (bestTmScore < ac.getTmScore()) {
				bestTmScore = ac.getTmScore();
			}
			//dist.add(aln.getScore(), as[i].getEquivalence().tmScore());
			i++;
		}
		List<AlignmentCore> good = new ArrayList<>();
		for (AlignmentCore ac : as) {
			if (ac.getTmScore() >= bestTmScore * 0.5 && ac.getTmScore() > 0.4) {
				ac.refine();
				good.add(ac);
			}
		}
		
		//dist.approximation(0.9);
		Collections.sort(good);
		boolean first = true;
		int alignmentVersion = 1;
		if (good.isEmpty()) {
			Equivalence eq = new Equivalence(a, b, new Residue[2][0]);
			eo.saveResults(eq);
		} else {
			for (AlignmentCore ac : good) {
				if (first) {
					Equivalence eq = ac.getEquivalence();
					mr += eq.matchingResiduesRelative();
					tmscore += eq.tmScore();
					counter++;
					//System.out.println("* " + (tmscore / counter) + " " + (mr / counter));
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
