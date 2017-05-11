package fragments;

import alignment.score.ResidueAlignment;
import fragments.alignment.Alignment;
import fragments.alignment.Alignments;
import alignment.score.EquivalenceOutput;
import fragments.alignment.ExpansionAlignment;
import fragments.alignment.ExpansionAlignments;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import geometry.Transformer;
import io.Directories;
import java.util.ArrayDeque;
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
	private final BiwordsFactory ff;
	private final boolean visualize;
	private double bestInitialTmScore = 0; // TODO remove

	public BiwordAlignmentAlgorithm(Directories dirs, boolean visualize) {
		this.dirs = dirs;
		this.visualize = visualize;
		ff = new BiwordsFactory();
	}

	public void align(AlignablePair pair, EquivalenceOutput eo, int alignmentNumber) {
		Parameters pars = Parameters.create();
		SimpleStructure a = pair.getA();
		SimpleStructure b = pair.getB();
		Biwords ba = ff.create(a, pars.getWordLength(), pars.skipX());
		Biwords bb = ff.create(b, pars.getWordLength(), pars.skipY());
		int minStrSize = Math.min(a.size(), b.size());

		AwpGraph graph = createGraph(ba, bb);
		findComponents(graph);
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
		for (AwpNode n : graph.getNodes()) {
			assert graph.getConnections(n) != null && graph.getConnections(n).size() >= 1;
		}
		return graph;
	}

	private void findComponents(AwpGraph graph) {
		System.out.println("edges: " + graph.getEdges().size());
		System.out.println("nodes: " + graph.getNodes().size());
		System.out.println("// " + (double) graph.getEdges().size() / graph.getNodes().size());
		long small = 0;
		for (AwpNode n : graph.getNodes()) {
			if (n.getConnectivity() <= 2) {
				small++;
			}
		}

		AwpNode[] nodes = new AwpNode[graph.getNodes().size()];
		graph.getNodes().toArray(nodes);
		for (int i = 0; i < nodes.length; i++) {
			nodes[i].id = i;
		}
		boolean[] visited = new boolean[nodes.length];
		int componentId = 1;
		for (int i = 0; i < nodes.length; i++) {
			if (visited[i]) {
				continue;
			}
			ArrayDeque<Integer> q = new ArrayDeque<>();
			q.offer(i);
			visited[i] = true;
			while (!q.isEmpty()) {
				int x = q.poll();
				AwpNode n = nodes[x];
				n.setComponent(componentId);
				for (AwpNode m : graph.getNeighbors(n)) {
					int y = m.id;
					if (visited[y]) {
						continue;
					}
					q.offer(y);
					visited[y] = true;
				}
			}
			componentId++;
		}
		System.out.println("components " + (componentId - 1));

	}

	private Alignments assembleAlignments(AwpGraph graph, int minStrSize) {
		ExpansionAlignments as = new ExpansionAlignments(minStrSize);
		for (AwpNode origin : graph.getNodes()) {
			if (!as.covers(origin)) {
				ExpansionAlignment aln = new ExpansionAlignment(origin, graph, minStrSize);
				as.add(aln);
			}
		}
		return as;
	}

	private List<ResidueAlignmentFactory> filterAlignments(SimpleStructure a, SimpleStructure b, Alignments alignments) {
		Collection<Alignment> clusters = alignments.getAlignments();
		ResidueAlignmentFactory[] as = new ResidueAlignmentFactory[clusters.size()];
		int i = 0;
		double bestTmScore = 0;
		bestInitialTmScore = 0;
		for (Alignment aln : clusters) {
			ResidueAlignmentFactory ac = new ResidueAlignmentFactory(a, b, aln.getBestPairing(), aln.getScore(), null);
			as[i] = ac;
			ac.alignBiwords();
			if (bestTmScore < ac.getTmScore()) {
				bestTmScore = ac.getTmScore();
			}
			if (bestInitialTmScore < ac.getInitialTmScore()) {
				bestInitialTmScore = ac.getInitialTmScore();
			}
			i++;
		}
		List<ResidueAlignmentFactory> selected = new ArrayList<>();
		for (ResidueAlignmentFactory ac : as) {
			double tm = ac.getTmScore();
			//if (/*tm >= 0.4 || */(tm >= bestTmScore * 0.1 && tm > 0.1)) {

			if (tm > Parameters.create().tmFilter()) {
				//if (tm >= bestTmScore * 0.3) {
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

	private static double sum;
	private static int count;
	private static double refined;
	private static int rc;

	private void saveAlignments(SimpleStructure a, SimpleStructure b, List<ResidueAlignmentFactory> alignments,
		EquivalenceOutput eo, int alignmentNumber) {
		Collections.sort(alignments);
		boolean first = true;
		int alignmentVersion = 1;
		if (alignments.isEmpty()) {
			ResidueAlignment eq = new ResidueAlignment(a, b, new Residue[2][0]);
			eo.saveResults(eq, 0);
		} else {
			for (ResidueAlignmentFactory ac : alignments) {
				if (first) {
					ResidueAlignment eq = ac.getEquivalence();
					eo.saveResults(eq, bestInitialTmScore);
					refined += eq.tmScore();
					rc++;
					if (Parameters.create().displayFirstOnly()) {
						first = false;
					}
					if (visualize) {
						eo.setDebugger(ac.getDebugger());
						eo.visualize(eq, ac.getSuperpositionAlignment(), bestInitialTmScore, alignmentNumber, alignmentVersion);
					}
					alignmentVersion++;
				}
			}
		}
		sum += bestInitialTmScore;
		count++;

		System.out.println("CHECK " + (sum / count));
		System.out.println("REFI " + (refined / rc));
	}
}
