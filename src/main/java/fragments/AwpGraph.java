package fragments;

import fragments.alignment.ExpansionAlignment;
import fragments.alignment.Alignments;
import fragments.alignment.ExpansionAlignments;
import geometry.Transformer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Graph of aligned word pairs, connected by edges iff the RMSD of aligned biwords (biword = any two
 * non-overlapping words from single protein) is low.
 *
 * Aligned pair = pair of similar elements from different proteins.
 *
 * @author antonin
 *
 */
public class AwpGraph {

	private final List<Edge> edges = new ArrayList<>();
	private final Map<AwpNode, List<Edge>> connections = new HashMap<>();
	private final Map<AwpNode, AwpNode> nodes = new HashMap<>();
	private final Parameters pars;
	private final Point3dBuffer linkA, linkB; // gathers atoms from 3 - 4 words connecting clusters
	private final Transformer transformer = new Transformer();

	public AwpGraph() {
		pars = Parameters.create();
		linkA = new Point3dBuffer(pars.getWordLength() * 4);
		linkB = new Point3dBuffer(pars.getWordLength() * 4);
	}

	public List<Edge> getConnections(AwpNode n) {
		return connections.get(n);
	}

	public void connect(AwpNode[] ps, double rmsd) {
		for (int i = 0; i < ps.length; i++) {
			AwpNode p = ps[i];
			if (nodes.containsKey(p)) { // guarantees no object is duplicated
				ps[i] = nodes.get(p);
			} else {
				nodes.put(p, p);
			}
		}
		Edge e = new Edge(ps[0], ps[1], rmsd);
		edges.add(e); // just one direction here, because fragments are created with both orderings of words, so this results in both directions eventually
		List<Edge> es = connections.get(ps[0]);
		if (es == null) {
			es = new ArrayList<>();
			connections.put(ps[0], es);
		}
		es.add(e);
	}

	public void printGraph() {
		for (Edge e : edges) {
			System.out.println(e.getX() + " " + e.getY() + " " + e.getRmsd());
		}
	}

	public void print() {
		Collections.sort(edges);
		for (int i = 0; i < Math.min(100, edges.size()); i++) {
			Edge e = edges.get(i);
			System.out.println("rmsd = " + e.getRmsd());
		}
	}

	private void extract(AwpNode n) {
		Word[] ws = n.getWords();
		linkA.addAll(ws[0].getPoints3d());
		linkB.addAll(ws[1].getPoints3d());
	}

	@Deprecated	 //TODO move to AlignmentByExpansion
	public Alignments assembleAlignmentByExpansions(int minStrLength) {
		ExpansionAlignments as = new ExpansionAlignments(minStrLength);
		for (AwpNode origin : nodes.keySet()) {
			if (!as.covers(origin)) {
				ExpansionAlignment aln = new ExpansionAlignment(origin, this, minStrLength);
				as.add(aln);
			}
		}
		//Collections.sort(scores);
		//for (int i = scores.size() - 1; i >= 0; i--) {
		//	System.out.print(scores.get(i) + " ");
		//}
		//System.out.println("");
		return as;
	}
}
