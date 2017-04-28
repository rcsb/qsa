package fragments;

import geometry.Transformer;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.vecmath.Point3d;
import util.Timer;
import util.pymol.PymolVisualizer;

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
	private final Map<AwpNode, AwpNode> nodes = new HashMap<>();
	private final Parameters pars;
	private final Point3dBuffer linkA, linkB; // gathers atoms from 3 - 4 words connecting clusters
	private final Transformer transformer = new Transformer();

	public AwpGraph() {
		pars = Parameters.create();
		linkA = new Point3dBuffer(pars.getWordLength() * 4);
		linkB = new Point3dBuffer(pars.getWordLength() * 4);
	}

	public void connect(AwpNode[] ps, double rmsd) {
		for (int i = 0; i < ps.length; i++) {
			AwpNode p = ps[i];
			if (nodes.containsKey(p)) {
				ps[i] = nodes.get(p);
			} else {
				nodes.put(p, p);
			}
		}
		edges.add(new Edge(ps[0], ps[1], rmsd));
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

	private void extract(AwpCluster c, AwpNode n) {
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

	public AwpClustering cluster() {
		double mergeRmsd = Parameters.create().getMergeRmsd();
		AwpClustering clustering = new AwpClustering();
		int id = 0;
		for (AwpNode p : nodes.keySet()) {
			p.setClusterId(id);
			AwpCluster cluster = new AwpCluster(id++, p, clustering);
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
				AwpCluster cx = clustering.getCluster(nodeX.getClusterId());
				AwpCluster cy = clustering.getCluster(nodeY.getClusterId());
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

	public static void test_main(String[] args) {
		AwpGraph wg = new AwpGraph();
		/*
		 * int an = 10; int bn = 10; WordInterface[] wa = new WordInterface[an];
		 * for (int i = 0; i < an; i++) { wa[i] = new DummyWord(i); }
		 * WordInterface[] wb = new WordInterface[bn]; for (int i = 0; i < bn;
		 * i++) { wb[i] = new DummyWord(i + 2); }
		 */
		for (int i = 0; i < 4; i++) {
			AwpNode[] ps = {new AwpNode(new DummyWord(0 + i), new DummyWord(2 + i)),
				new AwpNode(new DummyWord(1 + i), new DummyWord(3 + i))};
			wg.connect(ps, 10 + i);

			AwpNode[] pss = {new AwpNode(new DummyWord(0 + i), new DummyWord(2 + i)),
				new AwpNode(new DummyWord(1 + i + 1), new DummyWord(3 + i + 1))};
			wg.connect(pss, 10 - i + 3);

		}

		// wg.printGraph();
		wg.cluster();

	}

}
