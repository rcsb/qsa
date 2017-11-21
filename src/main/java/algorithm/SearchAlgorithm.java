package algorithm;

import global.Parameters;
import algorithm.graph.AwpGraph;
import algorithm.graph.AwpNode;
import algorithm.graph.Edge;
import algorithm.graph.GraphPrecursor;
import algorithm.scoring.ResidueAlignment;
import algorithm.scoring.EquivalenceOutput;
import biword.BiwordId;
import biword.BiwordPairFiles;
import biword.BiwordPairReader;
import biword.BiwordPairWriter;
import biword.Index;
import fragments.alignment.ExpansionAlignment;
import fragments.alignment.ExpansionAlignments;
import java.util.ArrayList;
import java.util.List;
import geometry.Transformer;
import global.FlexibleLogger;
import grid.sparse.Buffer;
import global.io.Directories;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import pdb.Residue;
import pdb.SimpleStructure;
import pdb.Structures;
import util.Time;

/**
 *
 * @author Antonin Pavelka
 */
public class SearchAlgorithm {

	private final transient Directories dirs;
	private final boolean visualize;
	private double bestInitialTmScore = 0;
	private final Parameters parameters = Parameters.create();
	private static int maxComponentSize;
	private final SimpleStructure queryStructure;
	private final EquivalenceOutput equivalenceOutput;
	private int alignmentNumber;
	private final Index index;
	private final Structures structures;

	public SearchAlgorithm(SimpleStructure queryStructure, Structures sp, Index index, Directories dirs,
		boolean visualize, EquivalenceOutput eo, int alignmentNumber) {
		this.queryStructure = queryStructure;
		this.structures = sp;
		this.index = index;
		this.dirs = dirs;
		this.visualize = visualize;
		this.equivalenceOutput = eo;
		this.alignmentNumber = alignmentNumber;
	}

	public void search() {
		Time.start("biword search");
		BiwordPairWriter bpf = new BiwordPairWriter(dirs, structures.size());
		BiwordsFactory biwordsFactory = new BiwordsFactory(dirs, queryStructure, parameters.skipX(), true);
		Biwords queryBiwords = biwordsFactory.getBiwords();
		for (int xi = 0; xi < queryBiwords.size(); xi++) {
			//System.out.println("Searching with biword " + xi + " / " + queryBiwords.size());
			Biword x = queryBiwords.get(xi);
			Buffer<BiwordId> buffer = index.query(x);
			for (int i = 0; i < buffer.size(); i++) {
				BiwordId y = buffer.get(i);
				bpf.add(x.getIdWithingStructure(), y.getStructureId(), y.getIdWithinStructure());
			}
		}
		bpf.close();
		Time.stop("biword search");
		Time.print();
		Time.start("alignment assembly");
		BiwordPairFiles biwordPairFiles = new BiwordPairFiles(dirs);
		if (parameters.isParallel()) { // TODO synchronize table.csv and alignment.py, createDirs?
			biwordPairFiles.getReaders().parallelStream().forEach(reader -> assemble(reader, queryBiwords));
		} else {
			for (BiwordPairReader reader : biwordPairFiles.getReaders()) {
				assemble(reader, queryBiwords);
			}
		}
		Time.stop("alignment assembly");
		Time.print();
	}

	private void assemble(BiwordPairReader reader, Biwords queryBiwords) {
		try {
			int targetStructureId = reader.getTargetStructureId();
			Biwords targetBiwords = index.getStorage().load(targetStructureId);
			int qwn = queryBiwords.getWords().length;
			int twn = targetBiwords.getWords().length;
			GraphPrecursor g = new GraphPrecursor(qwn, twn);
			Transformer transformer = new Transformer();
			while (reader.readNextBiwordPair()) {
				int queryBiwordId = reader.getQueryBiwordId();
				int targetBiwordId = reader.getTargetBiwordId();
				Biword x = queryBiwords.get(queryBiwordId);
				Biword y = targetBiwords.get(targetBiwordId);
				transformer.set(x.getPoints3d(), y.getPoints3d());
				double rmsd = transformer.getRmsd();
				if (rmsd <= parameters.getMaxFragmentRmsd()) {
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
			reader.close();
			System.out.println("Nodes: " + g.getNodes().length);
			System.out.println("Edges: " + g.getEdges().size());
			SimpleStructure targetStructure = targetBiwords.getStructure();
			AwpGraph graph = new AwpGraph(g.getNodes(), g.getEdges());
			findComponents(graph, queryStructure.size(), targetStructure.size());
			int minStrSize = Math.min(queryStructure.size(), targetStructure.size());
			ExpansionAlignments expansion = createExpansionAlignments(graph, minStrSize); // TODO hash for starting words, expressing neighborhood, required minimum abount of similar word around
			System.out.println("Expansion alingments: " + expansion.getAlignments().size());
			List<FinalAlignment> filtered = filterAlignments(queryStructure, targetStructure, expansion);
			refineAlignments(filtered);
			saveAlignments(queryStructure, targetStructure, filtered, equivalenceOutput, alignmentNumber++); //++ !*/
		} catch (Exception ex) {
			FlexibleLogger.error(ex);
		}
	}

	int screen = 1;

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

	private ExpansionAlignments createExpansionAlignments(AwpGraph graph, int minStrSize) {
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
					if (visualize && ac.getTmScore() >= 0.15) {
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
