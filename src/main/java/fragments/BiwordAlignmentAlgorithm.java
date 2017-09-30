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
import grid.sparse.Buffer;
import io.Directories;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
	private Parameters pars = Parameters.create();
	private List<Biword> biwordDatabase = new ArrayList<>();
	private Map<String, SimpleStructure> structures = new HashMap<>();

	public BiwordAlignmentAlgorithm(Directories dirs, boolean visualize) {
		this.dirs = dirs;
		this.visualize = visualize;
		ff = new BiwordsFactory();
	}

	public void prepareBiwordDatabase(SimpleStructure dbItem) {
		structures.put(dbItem.getId(), dbItem);
		
		
		Biwords bws = ff.create(dbItem, pars.getWordLength(), pars.skipX());
		for (Biword bw : bws.getBiwords()) {
			biwordDatabase.add(bw);
			if (biwordDatabase.size() % 100000 == 0) {
				System.out.println("biwords " + biwordDatabase.size());
			}
		}
	}

	public UniversalBiwordGrid build() {
		System.out.println("building grid...");
		Timer.start();
		UniversalBiwordGrid grid = new UniversalBiwordGrid(biwordDatabase, -1);
		biwordDatabase = null;
		Timer.stop();
		System.out.println("...done in " + Timer.get());
		return grid;
	}

	private boolean similar(Biword bx, Biword by) {
		double[] x = bx.getCoords();
		double[] y = by.getCoords();
		for (int i = 0; i < x.length; i++) {
			double diff = Math.abs(x[i] - y[i]);
			if (diff > pars.getRanges()[i]) {
				return false;
			}
		}
		return true;
	}

	public void search(SimpleStructure query, UniversalBiwordGrid grid, EquivalenceOutput eo, int alignmentNumber) {
		Timer.start();

		Parameters par = Parameters.create();
		Transformer tr = new Transformer();
		//WordMatcher wm = new WordMatcher(a.getWords(), b.getWords(), false, par.getMaxWordRmsd());
		Biwords queryBiwords = ff.create(query, pars.getWordLength(), pars.skipX());
		Buffer<Biword> buffer = new Buffer(grid.size());
		Map<String, GraphPrecursor> gps = new HashMap<>();
		long timeA = System.nanoTime();
		for (int xi = 0; xi < queryBiwords.size(); xi++) {
			Biword x = queryBiwords.get(xi);
			BiwordFilter filter = new BiwordFilter(x, pars.getRanges());
			grid.search(x, buffer);
			System.out.println("retrieved " + buffer.size() + " / " + grid.size());
			for (int i = 0; i < buffer.size(); i++) {
				Biword y = buffer.get(i);
				//if (!filter.include(y)) {
				//	continue;
				//}
				//for (Biword y : buffer) {
				String id = y.getPdbCode();
				GraphPrecursor g = gps.get(id);
				if (g == null) {
					g = new GraphPrecursor(id);
					gps.put(id, g);
				}
				//tr.set(x.getPoints3d(), y.getPoints3d());
				double rmsd = 0;
				//double rmsd = tr.getRmsd();
				//if (rmsd <= par.getMaxFragmentRmsd()) {
				AwpNode[] ns = {new AwpNode(x.getWords()[0], y.getWords()[0]),
					new AwpNode(x.getWords()[1], y.getWords()[1])};
				if (ns[1].before(ns[0])) {
					continue; // if good match, will be added from the other direction/order or nodes
				}
				for (int j = 0; j < 2; j++) {
					ns[j] = g.addNode(ns[j]);

				}
				ns[0].connect(); // increase total number of undirected connections 
				ns[1].connect();
				Edge e = new Edge(ns[0], ns[1], rmsd);
				g.addEdge(e);
				//}
				//}
			}
		}
		long timeB = System.nanoTime();
		System.out.println("fragment search " + ((timeB - timeA) / 1000 / 1000 / 1000));
		Timer.stop();
		System.out.println("search took " + Timer.get());
		System.out.println("biwords " + Biword.count);
		System.out.println("nodes " + GraphPrecursor.nodeCounter);
		System.out.println("edges " + GraphPrecursor.edgeCounter);

		AwpGraph[] graphs = new AwpGraph[gps.size()];
		int i = 0;
		for (String structureId : gps.keySet()) {
			GraphPrecursor gp = gps.get(structureId);
			graphs[i++] = new AwpGraph(structures.get(gp.getPdbCode()), gp.nodes.keySet(), gp.edges);
		}

		for (AwpGraph graph : graphs) {
			findComponents(graph);
			int minStrSize = Math.min(query.size(), graph.structure.size());
			Alignments all = assembleAlignments(graph, minStrSize);
			List<ResidueAlignmentFactory> filtered = filterAlignments(query, graph.structure, all);
			refineAlignments(filtered);
			saveAlignments(query, graph.getStructure(), filtered, eo, alignmentNumber++); //++ !!!!!!!!!!!!!!!!!!!!!!!!!!!
		}
	}

	public void align(AlignablePair pair, EquivalenceOutput eo, int alignmentNumber) {
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
		WordMatcher wm = new WordMatcher(a.getWords(), b.getWords(), false,
			par.getMaxWordRmsd());
		Timer.stop();
		SimpleBiwordGrid bg = new SimpleBiwordGrid(Arrays.asList(b.getBiwords()));

		/*long maxNodes = (long) a.getWords().length * b.getWords().length;
		if (maxNodes > Integer.MAX_VALUE) {
			throw new RuntimeException("OVERFLOW");
		}
		 */
		Map<AwpNode, AwpNode> nodes = new HashMap<>();
		ArrayList<Edge> edges = new ArrayList<>(100000);
		System.out.println("queries " + a.size());
		for (int xi = 0; xi < a.size(); xi++) {
			Biword x = a.get(xi);
			List<Biword> near = bg.search(x);
			for (Biword y : near) {
				if (x.isSimilar(y, wm)) {
					tr.set(x.getPoints3d(), y.getPoints3d());
					double rmsd = tr.getRmsd();
					if (rmsd <= par.getMaxFragmentRmsd()) {
						AwpNode[] ns = {new AwpNode(x.getWords()[0], y.getWords()[0]),
							new AwpNode(x.getWords()[1], y.getWords()[1])};
						if (ns[1].before(ns[0])) {
							continue; // if good match, will be added from the other direction/order or nodes
						}
						for (int i = 0; i < 2; i++) {
							AwpNode n = nodes.get(ns[i]);
							if (n != null) {
								ns[i] = n; // use existing object
							} else {
								nodes.put(ns[i], ns[i]);
							}
						}
						ns[0].connect(); // increase total number of undirected connections 
						ns[1].connect();
						Edge e = new Edge(ns[0], ns[1], rmsd);
						edges.add(e);
					}
				}
			}
		}
		System.out.println("nodes " + nodes.size());
		System.out.println("edges " + edges.size());
		System.out.println("-----------------------");
		AwpGraph graph = new AwpGraph(b.getStructure(), nodes.keySet(), edges);

		return graph;
	}
	private static int maxComponentSize;

	private void findComponents(AwpGraph graph) {
		AwpNode[] nodes = graph.getNodes();
		boolean[] visited = new boolean[nodes.length];
		List<Component> components = new ArrayList<>();
		for (int i = 0; i < nodes.length; i++) {
			if (visited[i]) {
				continue;
			}
			Component c = new Component();
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
					int y = m.id;
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
		ExpansionAlignments as = new ExpansionAlignments(minStrSize);
		for (AwpNode origin : graph.getNodes()) {
			if ((double) origin.getComponent().sizeInResidues() / minStrSize < 0.5) {
				//	continue;
			}
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
				System.out.println("tm = " + tm);
				//if (tm >= bestTmScore * 0.3) {
				selected.add(ac);
			}
		}

		return selected;
	}
	static int ii;

	private void refineAlignments(List<ResidueAlignmentFactory> alignemnts) {
		for (ResidueAlignmentFactory ac : alignemnts) {
			ac.refine();
			System.out.println(ii++);
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

		System.out.println("CHECK " + (sum / count));
		System.out.println("REFI " + (refined / rc));
	}
}
