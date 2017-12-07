package algorithm;

import global.Parameters;
import algorithm.graph.AwpGraph;
import algorithm.graph.AwpNode;
import algorithm.graph.Edge;
import algorithm.graph.GraphPrecursor;
import algorithm.scoring.ResidueAlignment;
import alignment.AlignmentSummaries;
import alignment.AlignmentSummary;
import alignment.StructureSourcePair;
import biword.BiwordId;
import biword.BiwordPairFiles;
import biword.BiwordPairReader;
import biword.BiwordPairWriter;
import biword.Index;
import biword.serialization.BiwordLoader;
import fragments.alignment.ExpansionAlignment;
import fragments.alignment.ExpansionAlignments;
import geometry.Point;
import java.util.ArrayList;
import java.util.List;
import geometry.Transformer;
import global.FlexibleLogger;
import global.io.Directories;
import global.io.PairOfAlignedFiles;
import grid.sparse.BufferOfLong;
import java.io.File;
import java.util.ArrayDeque;
import java.util.Collection;
import pdb.Residue;
import pdb.SimpleStructure;
import pdb.Structures;
import util.Counter;
import util.Time;
import util.pymol.PymolVisualizer;

/**
 *
 * @author Antonin Pavelka
 */
public class SearchAlgorithm {

	private final Directories dirs;
	private final boolean visualize;
	private double bestInitialTmScore = 0;
	private final Parameters parameters;
	private static int maxComponentSize;
	private final SimpleStructure queryStructure;
	private final Index index;
	private final Structures structures;

	public SearchAlgorithm(Parameters parameters, Directories dirs, SimpleStructure queryStructure, Structures sp,
		Index index, boolean visualize) {
		this.parameters = parameters;
		this.dirs = dirs;
		this.queryStructure = queryStructure;
		this.structures = sp;
		this.index = index;
		this.visualize = visualize;
	}

	public void search() {
		Time.start("biword search");
		BiwordPairWriter bpf = new BiwordPairWriter(dirs, structures.size());
		BiwordsFactory biwordsFactory = new BiwordsFactory(parameters, dirs, queryStructure, parameters.getSkipX(), true);
		BiwordedStructure queryBiwords = biwordsFactory.getBiwords();
		for (int xi = 0; xi < queryBiwords.size(); xi++) {
			//System.out.println("Searching with biword " + xi + " / " + queryBiwords.size());
			Biword x = queryBiwords.get(xi);
			BufferOfLong buffer = index.query(x);
			for (int i = 0; i < buffer.size(); i++) {
				long encoded = buffer.get(i);
				BiwordId y = BiwordId.decode(encoded);
				bpf.add(x.getIdWithingStructure(), y.getStructureId(), y.getIdWithinStructure());
			}
		}
		bpf.close();
		Time.stop("biword search");
		Time.print();
		Time.start("alignment assembly");
		BiwordPairFiles biwordPairFiles = new BiwordPairFiles(dirs);
		AlignmentSummaries summaries = new AlignmentSummaries(parameters, dirs);
		Counter pairCounter = new Counter();
		if (parameters.isParallel()) {
			biwordPairFiles.getReaders().parallelStream().forEach(
				reader -> assemble(pairCounter, reader, queryBiwords, summaries));
		} else {
			for (BiwordPairReader reader : biwordPairFiles.getReaders()) {
				assemble(pairCounter, reader, queryBiwords, summaries);
			}
		}
		summaries.finalizeOutput();
		Time.start("clean");
		clean();
		Time.stop("clean");
		Time.stop("alignment assembly");
		Time.print();
	}

	private void clean() {
		File dir = dirs.getBiwordHitsDir().toFile();
		for (File file : dir.listFiles()) {
			file.delete();
		}
		dir.delete();

	}

	/**
	 * Assembles the alignment for a single target structure, using matching biwords loaded from reader.
	 */
	private void assemble(Counter pairCounter, BiwordPairReader reader, BiwordedStructure queryBiwords, AlignmentSummaries alignmentSummaries) {
		try {
			int targetStructureId = reader.getTargetStructureId();
			BiwordLoader biwordLoader = new BiwordLoader(parameters, dirs);
			BiwordedStructure targetBiwords = biwordLoader.load(targetStructureId);
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
			int minStrSize = queryStructure.size();
			ExpansionAlignments expansion = createExpansionAlignments(graph, minStrSize); // TODO hash for starting words, expressing neighborhood, required minimum abount of similar word around
			System.out.println("Expansion alingments: " + expansion.getAlignments().size());
			List<FinalAlignment> filtered = filterAlignments(queryStructure, targetStructure, expansion);
			refineAlignments(filtered);

			SimpleStructure[] structures = {queryStructure, targetStructure};
			generateOutputs(structures, filtered, alignmentSummaries);
			//saveAlignments(queryStructure, targetStructure, filtered, equivalenceOutput, alignmentNumber++); //++ !*/
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
		System.out.println("Max component size: " + maxComponentSize);
		
	}

	private ExpansionAlignments createExpansionAlignments(AwpGraph graph, int minStrSize) {
		ExpansionAlignments as = new ExpansionAlignments(graph.getNodes().length, minStrSize);
		
		System.out.println("Min structure size: " + minStrSize);
		
		for (AwpNode origin : graph.getNodes()) {
			double componentSize = ((double) origin.getComponent().sizeInResidues()) / minStrSize;
			
			//System.out.println("jjjjjjjjjjj " + componentSize + " " + parameters.getMinComponentSize());
			if (componentSize < parameters.getMinComponentSize()) {
				continue;
			}
			
			if (!as.covers(origin)) {				
				ExpansionAlignment aln = new ExpansionAlignment(parameters, origin, graph, minStrSize);
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
			FinalAlignment ac = new FinalAlignment(parameters, a, b, aln.getBestPairing(), aln.getScore(), aln);
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
			if (tm > parameters.getTmFilter()) {
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

	/*private static double sum;
	private static int count;
	private static double refined;
	private static int rc;

	@Deprecated
	private void saveAlignments(SimpleStructure a, SimpleStructure b, List<FinalAlignment> finalAlignments,
		AlignmentOutput alignmentOutput, int alignmentNumber) {
		Collections.sort(finalAlignments);
		boolean first = true;
		int alignmentVersion = 1;
		if (finalAlignments.isEmpty()) {
			ResidueAlignment eq = new ResidueAlignment(a, b, new Residue[2][0]);
			alignmentOutput.saveResults(eq, 0, 0);
		} else {
			for (FinalAlignment finalAlignment : finalAlignments) {
				if (first) {
					ResidueAlignment eq = finalAlignment.getResidueAlignment();
					alignmentOutput.saveResults(eq, bestInitialTmScore, maxComponentSize);
					refined += eq.getTmScore();
					rc++;
					if (parameters.isDisplayFirstOnly()) {
						first = false;
					}
					if (visualize && finalAlignment.getTmScore() >= 0.15) {
						System.out.println("Vis TM: " + finalAlignment.getTmScore());
						alignmentOutput.visualize(finalAlignment.getExpansionAlignemnt().getNodes(), eq, finalAlignment.getInitialPairing(), bestInitialTmScore, screen++, alignmentVersion);
						//eo.visualize(eq, ac.getSuperpositionAlignment(), bestInitialTmScore, alignmentVersion, alignmentVersion);
					}
					alignmentVersion++;
				}
			}
		}
		sum += bestInitialTmScore;
		count++;
	}*/
	private void generateOutputs(SimpleStructure[] structures, List<FinalAlignment> finalAlignments,
		AlignmentSummaries alignmentSummaries) {

		addAlignmentsToSummaries(finalAlignments, alignmentSummaries);
		savePdbs(finalAlignments);
	}

	private void savePdbs(List<FinalAlignment> alns) {
		FinalAlignment best = getBest(alns);
		if (best != null) {

			SimpleStructure[] superposed = {best.getFirst(), best.getSecondTransformedStructure()};
			System.out.println("TM-score: " + best.getTmScore()
				+ " " + superposed[0].getSource().toString()
				+ " " + superposed[1].getSource().toString());
			visualize(superposed, best.getExpansionAlignemnt().getNodes(), best.getResidueAlignment(),
				best.getInitialPairing(), bestInitialTmScore);
		}
	}

	private FinalAlignment getBest(List<FinalAlignment> alns) {
		double bestScore = -1;
		FinalAlignment bestAln = null;
		for (FinalAlignment aln : alns) {
			double tmScore = aln.getTmScore();
			if (tmScore > bestScore) {
				bestScore = tmScore;
				bestAln = aln;
			}
		}
		return bestAln;
	}

	private void addAlignmentsToSummaries(List<FinalAlignment> finalAlignments,
		AlignmentSummaries alignmentSummaries) {

		for (FinalAlignment aln : finalAlignments) {
			alignmentSummaries.add(createSummary(aln.getResidueAlignment()));
		}
	}

	private AlignmentSummary createSummary(ResidueAlignment alignment) {

		AlignmentSummary summary = new AlignmentSummary(dirs,
			new StructureSourcePair(alignment.getStructures()));
		summary.setMatchingResiduesAbsolute(alignment.getMatchingResiduesAbsolute());
		summary.setMatchingResidues(alignment.getMatchingResidues());
		summary.setTmScore(alignment.getTmScore());
		summary.setIdentity(alignment.getIdentity());
		summary.setRmsd(alignment.getRmsd());
		return summary;
	}

	/**
	 * Uses residue ids to create similar array, but with residues received from a SimpleStructure object. Serves to
	 * create a pairing with new orientation.
	 */
	private Residue[][] orient(Residue[][] in, SimpleStructure[] structures) {
		Residue[][] out = new Residue[in.length][in[0].length];
		for (int k = 0; k < in.length; k++) {
			for (int i = 0; i < in[0].length; i++) {
				out[k][i] = structures[k].getResidue(in[k][i].getId());
			}
		}
		return out;
	}

	public void visualize(SimpleStructure[] structures, Collection<AwpNode> nodes, ResidueAlignment eq,
		Residue[][] initialPairing, double bestInitialTmScore) {

		StructureSourcePair ssp = new StructureSourcePair(structures);
		PairOfAlignedFiles paf = new PairOfAlignedFiles(dirs, ssp);
		Point shift = null; // no shift, all is aligned to query
		/*if (eq.size() > 0) {
			shift = eq.center().negative();
		}*/
		for (int i = 0; i < 2; i++) {
			PymolVisualizer.save(structures[i], shift, paf.getPdbPath(i).getFile());
		}
		PymolVisualizer.save(eq.getResidueParing(), shift, paf.getFinalLines().getFile());
		PymolVisualizer.save(orient(initialPairing, structures), shift, paf.getInitialLines().getFile());
		PymolVisualizer.saveAwpNodes(nodes, structures, shift, paf.getWordLines().getFile());
	}
}
