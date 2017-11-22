package fragments.alignment;

import algorithm.scoring.ResidueAlignment;
import algorithm.graph.AwpGraph;
import algorithm.graph.AwpNode;
import algorithm.graph.Edge;
import global.Parameters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import pdb.Residue;
import geometry.SuperPositionQCP;
import algorithm.Fragment;

public class ExpansionAlignment {

	private final AwpGraph graph;
	private final Set<AwpNode> nodes = new HashSet<>();
	private final Map<Residue, Residue> residuesA = new HashMap<>();
	private final Map<Residue, Residue> residuesB = new HashMap<>();
	//private final Map<ResiduePair, Double> rmsds = new HashMap<>();
	private final PriorityQueue<Edge> queue = new PriorityQueue<>();
	private final List<ResiduePair> history = new ArrayList<>();
	private Matrix4d lastMatrix;
	private int bestIndex = -1;
	private double bestTmScore;
	private final int minStrLength;
	private final Parameters parameters;

	public ExpansionAlignment(Parameters parameters, AwpNode origin, AwpGraph graph, int minStrLength) {
		this.parameters = parameters;
		this.graph = graph;
		this.minStrLength = minStrLength;
		add(origin, null);
		expand();
	}

	public ExpansionAlignment(Parameters parameters, AwpNode x, AwpNode y, AwpGraph graph, int minStrLength) {
		this.parameters = parameters;
		this.graph = graph;
		this.minStrLength = minStrLength;
		add(x, null);
		add(y, null);
		expand();
	}

	private void expand() {
		while (!queue.isEmpty()) {
			Edge e = queue.poll();
			AwpNode x = e.getX();
			AwpNode y = e.getY();
			if (nodes.contains(y)) {
				continue;
			}
			assert nodes.contains(x);
			// let's add y
			if (isCompatible(y) && isConsistent(y)/* && isRigid(y)*/) {
				add(y, e.getRmsd());
			}
		}
	}

	private void add(AwpNode node, Double rmsd) {
		nodes.add(node);
		saveResiduePairing(node, rmsd);
		double[] rmsds = graph.getRmsds(node);
		AwpNode[] neighbors = graph.getNeighbors(node);
		for (int i = 0; i < neighbors.length; i++) {
			Edge e = new Edge(node, neighbors[i], rmsds[i]);
			queue.add(e);
		}
		lastMatrix = measureQuality();
	}

	private Matrix4d measureQuality() {
		int n = residuesA.size();
		Point3d[] as = new Point3d[n];
		Point3d[] bs = new Point3d[n];
		int i = 0;
		for (Residue r : residuesA.keySet()) {
			as[i] = r.getPosition3d();
			bs[i] = new Point3d(residuesA.get(r).getPosition3d());
			i++;
		}
		SuperPositionQCP qcp = new SuperPositionQCP();
		qcp.set(as, bs);
		Matrix4d m = qcp.getTransformationMatrix();
		//double rmsd = qcp.getRmsd();
		for (Point3d b : bs) {
			m.transform(b);
		}
		double tm = ResidueAlignment.tmScore(as, bs, minStrLength);
		if (tm > bestTmScore || bestIndex < 0) {
			bestTmScore = tm;
			bestIndex = history.size();
		}
		//System.out.println("tm " + tm + " " + n);
		return m;
	}

	public Residue[][] getBestPairing() {
		Residue[][] pairing = new Residue[2][bestIndex];
		for (int i = 0; i < bestIndex; i++) {
			ResiduePair p = history.get(i);
			pairing[0][i] = p.x;
			pairing[1][i] = p.y;
		}
		return pairing;
	}

	private boolean isCompatible(AwpNode y) {
		Fragment[] ws = y.getWords(); // matching words we want to add
		Point3d[] as = ws[0].getPoints3d(); // word in the first structure
		Point3d[] bs = ws[1].getPoints3d(); // word in the second structure
		double avg = 0;
		for (int i = 0; i < as.length; i++) {
			Point3d a = as[i];
			Point3d b = bs[i];
			Point3d c = new Point3d(b);
			lastMatrix.transform(c);
			double dist = a.distance(c);
			//System.out.println(dist + " dist");
			avg += dist;
			if (dist > parameters.getMaxDeviation()) {
				return false;
			}
		}
		if (avg / as.length > parameters.getAvgDeviation()) {
			return false;
		}
		return true;
	}

	public final void saveResiduePairing(AwpNode node, Double rmsd) {
		Fragment[] ws = node.getWords();
		Residue[] ras = ws[0].getResidues();
		Residue[] rbs = ws[1].getResidues();
		int n = ras.length;
		for (int i = 0; i < n; i++) {
			Residue ra = ras[i];
			Residue rb = rbs[i];
			assert residuesA.size() == residuesB.size();
			residuesA.put(ra, rb);
			residuesB.put(rb, ra);
			assert residuesA.size() == residuesB.size();
			ResiduePair pair = new ResiduePair(ra, rb);
			history.add(pair);
			//if (rmsd != null) { // null only for the first node, values will be added 
			//	rmsds.put(pair, rmsd);
			//}
		}
	}

	/**
	 * Checks if the node does not assign a word differently than some node of the cluster.
	 *
	 * @return true iff the new word pairing defined by node is consistent with pairings defined by nodes already in
	 * this cluster, i.e. Guarantees
	 */
	public final boolean isConsistent(AwpNode node) {
		Fragment[] ws = node.getWords(); // new word pairing
		Residue[] ras = ws[0].getResidues(); // word in protein A
		Residue[] rbs = ws[1].getResidues(); // matching word in protein B
		int n = ras.length;
		for (int i = 0; i < n; i++) {
			Residue ra = ras[i];
			Residue rb = rbs[i];
			Residue rbo = residuesA.get(ra); // existing match for word nwa
			if (rbo != null && !rbo.equals(rb)) { // if it was matched and the match is different
				return false; // one word would be paired with two different words
			} // now let's do the same in oposite direction
			Residue rao = residuesB.get(rb);
			if (rao != null && !rao.equals(ra)) {
				return false;
			}
		}
		return true;
	}

	public Set<AwpNode> getNodes() {
		return nodes;
	}

	public int sizeInWords() {
		return nodes.size();
	}

	public int sizeInResidues() {
		return residuesA.size();
	}

	public double getScore() {
		return bestTmScore;
	}

}
