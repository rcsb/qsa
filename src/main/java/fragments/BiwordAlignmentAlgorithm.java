package fragments;

import alignment.score.ResidueAlignment;
import fragments.alignment.Alignment;
import fragments.alignment.Alignments;
import alignment.score.EquivalenceOutput;
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
import util.Mayhem;
import util.Time;
import util.Timer;

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
		ff = new BiwordsFactory();
	}
	
	public void search(SimpleStructure queryStructure, StructureProvider sp, Index index,
		EquivalenceOutput eo, int alignmentNumber) {
		Time.start("search");
		BiwordPairWriter bpf = new BiwordPairWriter(sp.size());
		Parameters par = Parameters.create();
		Transformer tr = new Transformer();
		Biwords queryBiwords = ff.create(queryStructure, pars.getWordLength(), pars.skipX(), false);
		for (int xi = 0; xi < queryBiwords.size(); xi++) {
			Biword x = queryBiwords.get(xi);
			Buffer<Biword> buffer = index.query(x);   // jak tady pracovat se souborama, filtrovat rmsd az pri individ. aln
			// Buffer do grid cells
			// first save them? how many items in each cell?
			// or store it in mem, 3 ids?
			// is this phase even issue? when it crashes?
			
			
			
			for (int i = 0; i < buffer.size(); i++) {
				Biword y = buffer.get(i);
				tr.set(x.getPoints3d(), y.getPoints3d());
				double rmsd = tr.getRmsd();
				if (rmsd <= par.getMaxFragmentRmsd()) {
					int targetStructureId = y.getStructureId();
					bpf.add(x.getIdWithingStructure(), targetStructureId, y.getIdWithingStructure(), rmsd);
					index.getBiword(queryStructure.getId(), x.getIdWithingStructure());
					index.getBiword(targetStructureId, y.getIdWithingStructure());
				}
			}
		}
		bpf.close();
		Time.stop("search");
		Time.print();
		
		System.exit(1);
		
		Time.start("align");
		BiwordPairReader bpr = new BiwordPairReader();
		for (int i = 0; i < bpr.size(); i++) {
			bpr.open(i);
			int targetStructureId = bpr.getTargetStructureId();
			int qwn = queryBiwords.getWords().length;
			int twn = index.getBiwords(targetStructureId).getWords().length;
			GraphPrecursor g = new GraphPrecursor(qwn, twn);
			while (bpr.loadNext(i)) {
				int queryBiwordId = bpr.getQueryBiwordId();
				int targetBiwordId = bpr.getTargetBiwordId();
				double rmsd = bpr.getRmsd();
				Biword x = queryBiwords.get(queryBiwordId);
				Biword y = index.getBiword(targetStructureId, targetBiwordId);
				
				AwpNode[] ns = {new AwpNode(x.getWords()[0], y.getWords()[0]),
					new AwpNode(x.getWords()[1], y.getWords()[1])};
				//if (ns[1].before(ns[0])) { // now solved by BirwordFactory assymetric permute
				//	continue; // if good match, will be added from the other direction/order or nodes
				//}
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
			SimpleStructure targetStructure = index.getStructure(targetStructureId);
			AwpGraph graph = new AwpGraph(g.getNodes(), g.getEdges());
			findComponents(graph, queryStructure.size(), targetStructure.size());
			int minStrSize = Math.min(queryStructure.size(), targetStructure.size());
			Alignments all = assembleAlignments(graph, minStrSize);
			List<ResidueAlignmentFactory> filtered = filterAlignments(queryStructure, targetStructure, all);
			refineAlignments(filtered);
			saveAlignments(queryStructure, targetStructure, filtered, eo, alignmentNumber++); //++ !
		}
		Time.stop("align");
		Time.print();
	}
	
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
	
	private Alignments assembleAlignments(AwpGraph graph, int minStrSize) {
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

		// why is this 0????????????????????????????????????????????????????????????????
		//System.out.println("Expansion alignments: " + as.getAlignments().size());
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
				selected.add(ac);
			}
		}
		
		return selected;
	}
	static int ii;
	
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
			eo.saveResults(eq, 0, 0);
		} else {
			for (ResidueAlignmentFactory ac : alignments) {
				if (first) {
					ResidueAlignment eq = ac.getEquivalence();
					eo.saveResults(eq, bestInitialTmScore, maxComponentSize);
					refined += eq.tmScore();
					rc++;
					if (Parameters.create().displayFirstOnly()) {
						first = false;
					}
					if (visualize) {
						eo.setDebugger(ac.getDebugger());
						eo.visualize(eq, ac.getSuperpositionAlignment(), bestInitialTmScore, alignmentNumber, alignmentVersion);
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
