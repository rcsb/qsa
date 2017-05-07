package fragments;

import alignment.score.ResidueAlignment;
import fragments.alignment.Alignment;
import fragments.alignment.Alignments;
import alignment.score.EquivalenceOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import geometry.Transformer;
import io.Directories;
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
public class BiwordAlignmentAlgorithm {

	private final transient Directories dirs;
	private final FragmentsFactory ff;
	private final boolean visualize;

	public BiwordAlignmentAlgorithm(Directories dirs, boolean visualize) {
		this.dirs = dirs;
		this.visualize = visualize;
		ff = new FragmentsFactory();
	}

	public void align(AlignablePair pair, EquivalenceOutput eo, int alignmentNumber) {
		Parameters pars = Parameters.create();
		SimpleStructure a = pair.getA();
		SimpleStructure b = pair.getB();
		Biwords ba = ff.create(a, pars.getWordLength(), pars.skipX());
		Biwords bb = ff.create(b, pars.getWordLength(), pars.skipY());
		int minStrSize = Math.min(a.size(), b.size());

		AwpGraph graph = createGraph(ba, bb);
		Alignments all = assembleAlignments(graph, minStrSize);
		List<ResidueAlignmentFactory> filtered = filterAlignments(a, b, all);
		refineAlignments(filtered);
		saveAlignments(a, b, filtered, eo, alignmentNumber);
	}

	private AwpGraph createGraph(Biwords a, Biwords b) {
		Parameters par = Parameters.create();
		Transformer tr = new Transformer();
		AwpGraph graph = new AwpGraph();
		WordMatcher wm = new WordMatcher(a.getWords(), b.getWords(), false,
			par.getMaxWordRmsd());
		Timer.stop();
		BiwordGrid bg = new BiwordGrid(Arrays.asList(b.getFragments()));
		for (int xi = 0; xi < a.size(); xi++) {
			Biword x = a.get(xi);
			List<Biword> near = bg.search(x);
			for (Biword y : near) {
				if (x.isSimilar(y, wm)) {
					tr.set(x.getPoints3d(), y.getPoints3d());
					double rmsd = tr.getRmsd();
					if (rmsd <= par.getMaxFragmentRmsd()) {
						AwpNode[] ps = {new AwpNode(x.getWords()[0], y.getWords()[0]),
							new AwpNode(x.getWords()[1], y.getWords()[1])};
						graph.connect(ps, rmsd);
					}
				}
			}
		}
		return graph;
	}

	private Alignments assembleAlignments(AwpGraph graph, int minStrSize) {
		Alignments alignments;
		if (Parameters.create().doClustering()) {
			alignments = graph.cluster(minStrSize);
		} else {
			alignments = graph.assembleAlignmentByExpansions(minStrSize);
		}
		return alignments;
	}

	private List<ResidueAlignmentFactory> filterAlignments(SimpleStructure a, SimpleStructure b, Alignments alignments) {
		Collection<Alignment> clusters = alignments.getAlignments();
		ResidueAlignmentFactory[] as = new ResidueAlignmentFactory[clusters.size()];
		int i = 0;
		double bestTmScore = 0;
		for (Alignment aln : clusters) {
			//InitialAlignment ia = new InitialAlignment(aln.getNodes()); // TODO move to residue alignment? rather move func to factories, keep just pairing in res.aln.
			//Residue[][] superpositionAlignment = ia.getPairing();
			ResidueAlignmentFactory ac = new ResidueAlignmentFactory(a, b, aln.getNodes(), null);
			as[i] = ac;
			ac.alignBiwords();
			if (bestTmScore < ac.getTmScore()) {
				bestTmScore = ac.getTmScore();
			}
			i++;
		}
		List<ResidueAlignmentFactory> selected = new ArrayList<>();
		for (ResidueAlignmentFactory ac : as) {
			double tm = ac.getTmScore();
			if (tm >= 0.4 || (tm >= bestTmScore * 0.5 && tm > 0.3)) {
				selected.add(ac);
			}
		}
		return selected;
	}

	private void refineAlignments(List<ResidueAlignmentFactory> alignemnts) {
		for (ResidueAlignmentFactory ac : alignemnts) {
			ac.refine();
		}
	}

	private void saveAlignments(SimpleStructure a, SimpleStructure b, List<ResidueAlignmentFactory> alignments,
		EquivalenceOutput eo, int alignmentNumber) {
		Collections.sort(alignments);
		boolean first = true;
		int alignmentVersion = 1;
		if (alignments.isEmpty()) {
			ResidueAlignment eq = new ResidueAlignment(a, b, new Residue[2][0]);
			eo.saveResults(eq);
		} else {
			for (ResidueAlignmentFactory ac : alignments) {
				if (first) {
					ResidueAlignment eq = ac.getEquivalence();
					eo.saveResults(eq);
					first = false;
					if (visualize) {
						eo.setDebugger(ac.getDebugger());
						eo.visualize(eq, ac.getSuperpositionAlignment(), alignmentNumber,
							alignmentVersion);
					}
					alignmentVersion++;
				}
			}
		}
	}

}
