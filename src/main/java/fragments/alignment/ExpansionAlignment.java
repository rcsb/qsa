package fragments.alignment;

import alignment.score.ResidueAlignment;
import fragments.AwpGraph;
import fragments.AwpNode;
import fragments.Edge;
import fragments.Parameters;
import fragments.Word;
import fragments.clustering.ResiduePair;
import geometry.Point;
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
import superposition.SuperPositionQCP;

public class ExpansionAlignment implements Alignment {

	private final AwpGraph graph;
	private final Set<AwpNode> nodes = new HashSet<>();
	private final Map<Residue, Residue> residuesA = new HashMap<>();
	private final Map<Residue, Residue> residuesB = new HashMap<>();
	//private final Map<ResiduePair, Double> rmsds = new HashMap<>();
	private final PriorityQueue<Edge> queue = new PriorityQueue<>();
	private final List<ResiduePair> history = new ArrayList<>();
	private int bestIndex = -1;
	private double bestTmScore;
	private int minStrLength;

	public ExpansionAlignment(AwpNode origin, AwpGraph graph, int minStrLength) {
		//System.out.println("----");
		this.graph = graph;
		this.minStrLength = minStrLength;
		add(origin, null);
		expand();
		
	}

	private void add(AwpNode node, Double rmsd) {
		nodes.add(node);
		saveResiduePairing(node, rmsd);
		List<Edge> edges = graph.getConnections(node);
		if (edges != null) {
			queue.addAll(edges);
		} else { // does it happen for some good reason or is it a bug?
		}
		quality();
	}

	private void quality() {
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
	}

	@Override
	public Residue[][] getBestPairing() {
		Residue[][] pairing = new Residue[2][bestIndex];
		for (int i = 0; i < bestIndex; i++) {
			ResiduePair p = history.get(i);
			pairing[0][i] = p.x;
			pairing[1][i] = p.y;
		}
		return pairing;
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
			if (isConsistent(y)/* && isRigid(y)*/) {
				add(y, e.getRmsd());
			}
		}
	}

	private boolean isRigid(AwpNode x) {
		Point ax = x.getWords()[0].getCenter();
		Point bx = x.getWords()[1].getCenter();
		for (AwpNode y : nodes) {
			Point ay = y.getWords()[0].getCenter();
			Point by = y.getWords()[1].getCenter();
			double da = ax.distance(ay);
			double db = bx.distance(by);
			if (Math.abs(da - db) > Parameters.create().rigid()) {
				return false;
			}
		}
		return true;
	}

	public final void saveResiduePairing(AwpNode node, Double rmsd) {
		Word[] ws = node.getWords();
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
		Word[] ws = node.getWords(); // new word pairing
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

	@Override
	public double getScore() {
		return bestTmScore;
	}

}
