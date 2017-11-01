package fragments;

import alignment.score.ResidueAlignment;
import alignment.score.EquivalenceOutput;
import biword.BiwordId;
import biword.BiwordPairReader;
import biword.BiwordPairWriter;
import biword.Index;
import fragments.alignment.ExpansionAlignment;
import fragments.alignment.ExpansionAlignments;
import java.util.ArrayList;
import java.util.List;
import geometry.Transformer;
import grid.sparse.Buffer;
import io.Directories;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import pdb.Residue;
import pdb.SimpleStructure;
import pdb.StructureProvider;
import util.Time;

/**
 *
 * @author Antonin Pavelka
 */
public class BiwordAlignmentAlgorithm {

	private final transient Directories dirs;
	private final BiwordsFactory ff;
	private final boolean visualize;
	private double bestInitialTmScore = 0;
	private final Parameters pars = Parameters.create();
	private static int maxComponentSize;

	public BiwordAlignmentAlgorithm(Directories dirs, boolean visualize) {
		this.dirs = dirs;
		this.visualize = visualize;
		ff = new BiwordsFactory(dirs);
	}

	public void search(SimpleStructure queryStructure, StructureProvider sp, Index index,
		EquivalenceOutput eo, int alignmentNumber) {
		Time.start("search");
		BiwordPairWriter bpf = new BiwordPairWriter(dirs, sp.size());
		Parameters par = Parameters.create();
		Transformer tr = new Transformer();
		Biwords queryBiwords = ff.create(queryStructure, pars.getWordLength(), pars.skipX(), false);
		for (int xi = 0; xi < queryBiwords.size(); xi++) {
			System.out.println("Searching with biword " + xi + " / " + queryBiwords.size());
			Biword x = queryBiwords.get(xi);
			Buffer<BiwordId> buffer = index.query(x);   // jak tady pracovat se souborama, filtrovat rmsd az pri individ. aln
			for (int i = 0; i < buffer.size(); i++) {
				BiwordId y = buffer.get(i);
				bpf.add(x.getIdWithingStructure(), y.getStructureId(), y.getIdWithinStructure());
			}
		}
		bpf.close();
		Time.stop("search");
		Time.print();

		// REALLY just half structures has some bpr?
		Time.start("align");
		BiwordPairReader bpr = new BiwordPairReader(dirs);
		// process matching biwords, now organized by structure, matches for each structure in a different file
		for (int i = 0; i < bpr.size(); i++) {
			System.out.println("Constructing alignment " + i + " / " + bpr.size());
			bpr.open(i);

			int targetStructureId = bpr.getTargetStructureId();
			Biwords targetBiwords = index.getStorage().load(targetStructureId);

			int qwn = queryBiwords.getWords().length;
			int twn = targetBiwords.getWords().length;

			GraphPrecursor g = new GraphPrecursor(qwn, twn);
			while (bpr.loadNext(i)) {
				int queryBiwordId = bpr.getQueryBiwordId();
				int targetBiwordId = bpr.getTargetBiwordId();
				Biword x = queryBiwords.get(queryBiwordId);
				Biword y = targetBiwords.get(targetBiwordId);
				tr.set(x.getPoints3d(), y.getPoints3d());
				double rmsd = tr.getRmsd();
				if (rmsd <= par.getMaxFragmentRmsd()) {
					AwpNode[] ns = {new AwpNode(x.getWords()[0], y.getWords()[0]),
						new AwpNode(x.getWords()[1], y.getWords()[1])};
					for (int j = 0; j < 2; j++) {
						ns[j] = g.addNode(ns[j]);
						if (ns[j] == null) {
							throw new RuntimeException();
						}
					}
					ns[0].connect(); // increase total number of undirected connections 
					ns[1].connect();
					Edge e = new Edge(ns[0], ns[1], rmsd);
					g.addEdge(e);
				}
			}
			SimpleStructure targetStructure = targetBiwords.getStructure();
			AwpGraph graph = new AwpGraph(g.getNodes(), g.getEdges());
			//System.out.println(g.getNodes().length);
			//System.out.println(g.getEdges().size());

			/*for (Edge e : g.getEdges()) {
				AwpNode x, y;
				x = e.getX();
				y = e.getY();
				if (x.getWords()[0].getCentralResidue().getId().getChain().getId().equals("B")&&
					y.getWords()[0].getCentralResidue().getId().getChain().getId().equals("A")) {
					System.out.println("jksjfksdj");
				}
			}*/
			findComponents(graph, queryStructure.size(), targetStructure.size());
			int minStrSize = Math.min(queryStructure.size(), targetStructure.size());
			ExpansionAlignments expansion = assembleAlignments(graph, minStrSize);
			List<FinalAlignment> filtered = filterAlignments(queryStructure, targetStructure, expansion);
			refineAlignments(filtered);
			saveAlignments(queryStructure, targetStructure, filtered, eo, alignmentNumber++); //++ !
		}
		Time.stop("align");
		Time.print();
	}

	int screen = 2;

	private void findComponents(AwpGraph graph, int queryStructureN, int targetStructureN) {
		AwpNode[] nodes = graph.getNodes();
		boolean[] visited = new boolean[nodes.length];
		List<Component> components = new ArrayList<>();
		for (int i = 0; i < nodes.length; i++) {
			if (visited[i]) {
				continue;
			}
			Component c = new Component(queryStructureN, targetStructureN);
			components.add(c);
			ArrayDeque<Integer> q = new ArrayDeque<>();
			q.offer(i);
			visited[i] = true;
			while (!q.isEmpty()) {
				int x = q.poll();
				AwpNode n = nodes[x];
				n.setComponent(c);
				c.add(n);
				for (AwpNode m : graph.getNeighbors(n)) {
					int y = m.getId();
					if (visited[y]) {
						continue;
					}
					q.offer(y);
					visited[y] = true;
				}
			}
		}
		maxComponentSize = -1;
		for (Component c : components) {
			if (c.sizeInResidues() > maxComponentSize) {
				maxComponentSize = c.sizeInResidues();
			}
		}
	}

	private ExpansionAlignments assembleAlignments(AwpGraph graph, int minStrSize) {
		ExpansionAlignments as = new ExpansionAlignments(graph.getNodes().length, minStrSize);
		for (AwpNode origin : graph.getNodes()) {
			if ((double) origin.getComponent().sizeInResidues() / minStrSize < 0.5) {
				//	continue;
			}
			if (!as.covers(origin)) {
				ExpansionAlignment aln = new ExpansionAlignment(origin, graph, minStrSize);
				as.add(aln);
			}
		}
		// why is this 0 so often, is it wasteful?
		//System.out.println("Expansion alignments: " + as.getAlignments().size());
		return as;
	}

	private List<FinalAlignment> filterAlignments(SimpleStructure a, SimpleStructure b, ExpansionAlignments alignments) {
		Collection<ExpansionAlignment> alns = alignments.getAlignments();
		FinalAlignment[] as = new FinalAlignment[alns.size()];
		int i = 0;
		double bestTmScore = 0;
		bestInitialTmScore = 0;
		for (ExpansionAlignment aln : alns) {
			FinalAlignment ac = new FinalAlignment(a, b, aln.getBestPairing(), aln.getScore(), aln);
			as[i] = ac;
			if (bestTmScore < ac.getTmScore()) {
				bestTmScore = ac.getTmScore();
			}
			if (bestInitialTmScore < ac.getInitialTmScore()) {
				bestInitialTmScore = ac.getInitialTmScore();
			}
			i++;
		}
		List<FinalAlignment> selected = new ArrayList<>();
		for (FinalAlignment ac : as) {
			double tm = ac.getTmScore();
			//if (/*tm >= 0.4 || */(tm >= bestTmScore * 0.1 && tm > 0.1)) {

			if (tm > Parameters.create().tmFilter()) {
				selected.add(ac);
			}
		}

		return selected;
	}
	static int ii;

	private void refineAlignments(List<FinalAlignment> alignemnts) {
		for (FinalAlignment ac : alignemnts) {
			ac.refine();
		}
	}

	private static double sum;
	private static int count;
	private static double refined;
	private static int rc;

	private void saveAlignments(SimpleStructure a, SimpleStructure b, List<FinalAlignment> alignments,
		EquivalenceOutput eo, int alignmentNumber) {
		Collections.sort(alignments);
		boolean first = true;
		int alignmentVersion = 1;
		if (alignments.isEmpty()) {
			ResidueAlignment eq = new ResidueAlignment(a, b, new Residue[2][0]);
			eo.saveResults(eq, 0, 0);
		} else {
			for (FinalAlignment ac : alignments) {
				if (first) {
					ResidueAlignment eq = ac.getEquivalence();
					eo.saveResults(eq, bestInitialTmScore, maxComponentSize);
					refined += eq.tmScore();
					rc++;
					if (Parameters.create().displayFirstOnly()) {
						first = false;
					}
					if (visualize && ac.getTmScore() >= 0.5) {
						System.out.println("Vis TM: " + ac.getTmScore());
						eo.visualize(ac.getExpansionAlignemnt().getNodes(), eq, ac.getInitialPairing(), bestInitialTmScore, /*alignmentNumber*/ screen++, alignmentVersion);
						//eo.visualize(eq, ac.getSuperpositionAlignment(), bestInitialTmScore, alignmentVersion, alignmentVersion);
					}
					alignmentVersion++;
				}
			}
		}
		sum += bestInitialTmScore;
		count++;
	}
}
