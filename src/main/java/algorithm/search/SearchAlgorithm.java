package algorithm.search;

import algorithm.Biword;
import algorithm.BiwordedStructure;
import algorithm.BiwordsFactory;
import algorithm.Component;
import algorithm.AlignmentRefiner;
import global.Parameters;
import algorithm.graph.AwpGraph;
import algorithm.graph.AwpNode;
import algorithm.graph.Edge;
import algorithm.graph.GraphPrecursor;
import algorithm.scoring.ResidueAlignment;
import alignment.Alignments;
import alignment.Alignment;
import alignment.StructureSourcePair;
import fragment.BiwordId;
import fragment.BiwordPairFiles;
import fragment.BiwordPairReader;
import fragment.BiwordPairSaver;
import biword.index.OrthogonalGrid;
import biword.serialization.BiwordLoader;
import fragment.alignment.ExpansionAlignment;
import fragment.alignment.ExpansionAlignments;
import java.util.ArrayList;
import java.util.List;
import geometry.superposition.Transformer;
import global.io.Directories;
import grid.sparse.BufferOfLong;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import structure.SimpleStructure;
import structure.Structures;
import util.Time;

/**
 *
 * Implements the algorithm for identification of structures similar to a query structure.
 * 
 * @author Antonin Pavelka
 * 
 */
public class SearchAlgorithm {

	private final Directories dirs;
	private double bestInitialTmScore = 0;
	private final Parameters parameters;
	private final SimpleStructure queryStructure;
	private final OrthogonalGrid index;
	private final Structures structures;

	public SearchAlgorithm(Parameters parameters, Directories dirs, SimpleStructure queryStructure, Structures sp,
		OrthogonalGrid index) {
		this.parameters = parameters;
		this.dirs = dirs;
		this.queryStructure = queryStructure;
		this.structures = sp;
		this.index = index;
	}

	public Alignments search() {
		Time.start("biword search");
		BiwordPairSaver bpf = new BiwordPairSaver(dirs, structures.size());
		BiwordsFactory biwordsFactory = new BiwordsFactory(parameters, dirs, queryStructure, parameters.getSkipX(),
			true);
		BiwordedStructure queryBiwords = biwordsFactory.getBiwords();
		if (parameters.isParallel()) {
			Arrays.stream(queryBiwords.getBiwords()).parallel().forEach(biword -> findMatchingBiwords(biword, bpf));
		} else {
			for (Biword x : queryBiwords.getBiwords()) {
				findMatchingBiwords(x, bpf);
			}
		}
		bpf.close();
		Time.stop("biword search");
		Time.print();
		Time.start("alignment assembly");
		BiwordPairFiles biwordPairFiles = new BiwordPairFiles(dirs);
		Alignments summaries = new Alignments();
		if (parameters.isParallel()) {
			biwordPairFiles.getReaders().parallelStream().forEach(
				reader -> assemble(reader, queryBiwords, summaries));
		} else {
			for (BiwordPairReader reader : biwordPairFiles.getReaders()) {
				assemble(reader, queryBiwords, summaries);
			}
		}
		Time.start("clean");
		clean();
		Time.stop("clean");
		Time.stop("alignment assembly");
		Time.print();
		return summaries;
	}

	private void clean() {
		File dir = dirs.getBiwordHitsDir().toFile();
		for (File file : dir.listFiles()) {
			file.delete();
		}
		dir.delete();

	}

	private void findMatchingBiwords(Biword x, BiwordPairSaver bpf) {
		BufferOfLong buffer = index.query(x);
		for (int i = 0; i < buffer.size(); i++) {
			long encoded = buffer.get(i);
			BiwordId y = BiwordId.decode(encoded);
			bpf.save(x.getIdWithingStructure(), y.getStructureId(), y.getIdWithinStructure());
		}
	}

	/**
	 * Assembles the alignment for a single target structure, using matching biwords loaded from reader.
	 */
	private void assemble(BiwordPairReader reader, BiwordedStructure queryBiwords,
		Alignments alignments) {

		try {
			int targetStructureId = reader.getTargetStructureId();

			BiwordLoader biwordLoader = new BiwordLoader(parameters, dirs, structures.getId());

			BiwordedStructure targetBiwords = biwordLoader.load(targetStructureId);
			SimpleStructure targetStructure = targetBiwords.getStructure();

			GraphPrecursor g = createGraphPrecursor(reader, queryBiwords, targetBiwords);

			AwpGraph graph = new AwpGraph(g.getNodes(), g.getEdges());

			findComponents(graph, queryStructure.size(), targetStructure.size());

			ExpansionAlignments expansion = ExpansionAlignments.createExpansionAlignments(parameters, graph,
				queryStructure.size(), targetStructure.size());

			List<AlignmentRefiner> refiners = filterAlignments(queryStructure, targetStructure, expansion);

			refineAlignments(refiners);

			convertAlignments(refiners, alignments);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private GraphPrecursor createGraphPrecursor(BiwordPairReader reader, BiwordedStructure queryBiwords,
		BiwordedStructure targetBiwords) throws IOException {
		int qwn = queryBiwords.getWords().length;
		int twn = targetBiwords.getWords().length;
		GraphPrecursor graphPrecursor = new GraphPrecursor(qwn, twn);
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
					ns[j] = graphPrecursor.addNode(ns[j]);
					if (ns[j] == null) {
						throw new RuntimeException();
					}
				}
				ns[0].connect(); // increase total number of undirected connections 
				ns[1].connect();
				Edge e = new Edge(ns[0], ns[1], rmsd);
				graphPrecursor.addEdge(e);
			}
		}
		reader.close();
		return graphPrecursor;
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
		int maxComponentSize = -1;
		for (Component c : components) {
			if (c.sizeInResidues() > maxComponentSize) {
				maxComponentSize = c.sizeInResidues();
			}
		}
	}

	private List<AlignmentRefiner> filterAlignments(SimpleStructure a, SimpleStructure b,
		ExpansionAlignments alignments) {

		Collection<ExpansionAlignment> alns = alignments.getAlignments();
		AlignmentRefiner[] as = new AlignmentRefiner[alns.size()];
		int i = 0;
		double bestTmScore = 0;
		bestInitialTmScore = 0;
		for (ExpansionAlignment aln : alns) {
			AlignmentRefiner ac = new AlignmentRefiner(parameters, a, b, aln.getBestPairing(), aln.getScore(), aln);
			as[i] = ac;
			if (bestTmScore < ac.getTmScore()) {
				bestTmScore = ac.getTmScore();
			}
			if (bestInitialTmScore < ac.getInitialTmScore()) {
				bestInitialTmScore = ac.getInitialTmScore();
			}
			i++;
		}
		List<AlignmentRefiner> selected = new ArrayList<>();
		for (AlignmentRefiner ac : as) {
			double tm = ac.getTmScore();
			if (tm > parameters.getInitialTmFilter()) {
				selected.add(ac);
			}
		}
		return selected;
	}

	private void refineAlignments(List<AlignmentRefiner> alignemnts) {
		for (AlignmentRefiner ac : alignemnts) {
			ac.refine();
		}
	}

	private void convertAlignments(List<AlignmentRefiner> finalAlignments,
		Alignments alignments) {

		for (AlignmentRefiner aln : finalAlignments) {
			alignments.add(createAlignment(aln));
		}
	}

	private Alignment createAlignment(AlignmentRefiner finalAlignment) {
		ResidueAlignment alignment = finalAlignment.getResidueAlignment();
		Alignment summary = new Alignment(dirs,
			new StructureSourcePair(alignment.getStructures()));
		summary.setMatchingResiduesAbsolute(alignment.getMatchingResiduesAbsolute());
		summary.setMatchingResidues(alignment.getMatchingResidues());
		summary.setTmScore(alignment.getTmScore());
		summary.setIdentity(alignment.getIdentity());
		summary.setRmsd(alignment.getRmsd());
		summary.setMatrix(finalAlignment.getMatrix());
		return summary;
	}

}
