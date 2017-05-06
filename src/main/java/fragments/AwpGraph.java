package fragments;

import fragments.alignment.ClusterAlignment;
import fragments.alignment.ClusterAlignments;
import fragments.alignment.ExpansionAlignment;
import fragments.alignment.Alignments;
import fragments.alignment.ExpansionAlignments;
import geometry.Transformer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.vecmath.Point3d;
import util.Timer;

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

	public void getClusters() {
		/*
		 * Map<Integer, AwpCluster> cs = new TreeMap<>(); for (AwpNode n :
		 * nodes.keySet()) { cs.put(n.getCluster().getId(), n.getCluster()); }
		 * 
		 * for (int id : cs.keySet()) { AwpCluster c = cs.get(id); if (c.size()
		 * > 1) { System.out.println(cs.get(id)); } }
		 */
	}

	private void extract(ClusterAlignment c, AwpNode n) {
		AwpNode m = c.getLinked(n);
		extract(n);
		if (m != null) { // cluster of size 1 does not have any link
			extract(m);
		}
	}

	private void extract(AwpNode n) {
		Word[] ws = n.getWords();
		linkA.addAll(ws[0].getPoints3d());
		linkB.addAll(ws[1].getPoints3d());
	}

	@Deprecated	 //TODO move to AlignmentByExpansion
	public Alignments assembleAlignmentByExpansions(int minStrSize) {
		ExpansionAlignments as = new ExpansionAlignments(minStrSize);
		for (AwpNode origin : nodes.keySet()) {
			if (!as.covers(origin)) {
				ExpansionAlignment aln = new ExpansionAlignment(origin, this);
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

	public ClusterAlignments cluster(int minStrSize) {
		double mergeRmsd = Parameters.create().getMergeRmsd();
		ClusterAlignments clustering = new ClusterAlignments(minStrSize);
		int id = 0;
		for (AwpNode p : nodes.keySet()) {
			p.setClusterId(id);
			ClusterAlignment cluster = new ClusterAlignment(id++, p, clustering);
			clustering.add(cluster);
			id++;
		}

		//System.out.println("nodes: " + clustering.size());
		//System.out.println("edges: " + edges.size());
		Timer.start();
		Collections.sort(edges);
		// TODO stop verifying when both size() == 1
		int index = 0;
		for (Edge e : edges) {
			if (e.getX().getClusterId() != e.getY().getClusterId()) {
				AwpNode nodeX = e.getX();
				AwpNode nodeY = e.getY();
				ClusterAlignment cx = clustering.getCluster(nodeX.getClusterId());
				ClusterAlignment cy = clustering.getCluster(nodeY.getClusterId());
				if (cx.isConsistent(nodeX) && cy.isConsistent(nodeY)) {
					linkA.clear();
					linkB.clear();
					extract(cx, nodeX);
					extract(cy, nodeY);
					Point3d[] a = linkA.toArray();
					Point3d[] b = linkB.toArray();
					//PymolVisualizer.save(a, new File("c:/kepler/rozbal/exp_a.pdb"), index);
					//PymolVisualizer.save(b, new File("c:/kepler/rozbal/exp_b.pdb"), index);
					index++;
					assert a.length >= 10 && a.length == b.length : a.length;
					transformer.set(a, b);
					double rmsd = transformer.getRmsd();
					if (rmsd <= mergeRmsd) {
						//System.out.println("rmsd " + rmsd + "   " + a.length + " " + b.length);
						if (a.length == 30) {
							assert cx.sizeInWords() == 1 || cy.sizeInWords() == 1;
						}
						clustering.merge(e);
						e.getX().updateRmsd(e.getRmsd());
						e.getY().updateRmsd(e.getRmsd());
					}
				}
			}
		}
		Timer.stop();
		//System.out.println("time: " + Timer.get());
		return clustering;
	}

}
