/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fragments;

import alignment.FragmentsAlignment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import analysis.Table;
import fragments.clustering.DeprecatedCluster;
import geometry.Transformer;
import io.Directories;
import io.LineFile;
import pdb.ResidueId;
import pdb.SimpleStructure;
import spark.Printer;
import spark.interfaces.AlignablePair;
import spark.interfaces.Alignment;
import spark.interfaces.StructureAlignmentAlgorithm;
import statistics.Distribution;
import test.MatrixTest;
import util.MapUtil;
import util.Timer;
import util.pymol.PymolVisualizer;

/**
 *
 * @author Antonin Pavelka
 */
public class FragmentsAligner implements StructureAlignmentAlgorithm {

	private transient Directories dirs_;
	private FragmentsFactory ff;
	private boolean visualize = true;
	private AlignablePair alignablePair;
	private MatrixTest matrixTest;

	private LineFile pyFile;

	public FragmentsAligner(Directories dirs) {
		dirs_ = dirs;
		pyFile = new LineFile(dirs.getPyFile());
		ff = new FragmentsFactory();
	}

	public void doMatrixTest(String name) {
		matrixTest = new MatrixTest(name);
	}

	public Alignment align(AlignablePair sp) {
		this.alignablePair = sp;
		Fragments a = ff.create(sp.getA(), Parameters.create().skipX());
		Fragments b = ff.create(sp.getB(), Parameters.create().skipY());
		if (visualize) {
			a.visualize(dirs_.getTemp(a.getStructure().getPdbCode() + "_" + "frags_A.py"));
			a.visualize(dirs_.getTemp(b.getStructure().getPdbCode() + "_" + "frags_B.py"));
		}
		Alignment al = align(a, b);
		return al;
	}

	private FragmentsAlignment align(Fragments a, Fragments b) {
		Parameters par = Parameters.create();
		Printer.println("i: " + a.getStructure().getPdbCode() + " " + b.getStructure().getPdbCode());
		double[] result = {0, 0, 0};
		Distribution ds = new Distribution();
		List<FragmentPair> hsp = new ArrayList<>();
		long start = System.nanoTime();

		Transformer tr = new Transformer();
		AwpGraph wg = new AwpGraph();

		boolean biwords = true;
		if (biwords) {
			System.out.println("Matching pairs of words, fragment numbers: " + a.size() + " " + b.size() + " ...");
			Timer.start();
			WordMatcher wm = new WordMatcher(a.getWords(), b.getWords(), false, par.getMaxWordRmsd());
			Timer.stop();
			System.out.println("... word similarity computed in: " + Timer.get());

			BiwordGrid bg = new BiwordGrid(Arrays.asList(b.getFragments()));
			double[] max = {0, 0, 0, 0, 0, 0};
			Timer.start();
			int similar = 0;
			int total = 0;
			for (int xi = 0; xi < a.size(); xi++) {
				Fragment x = a.get(xi);

				List<Fragment> near = bg.search(x);
				//System.out.println(near.size());

				for (Fragment y : near) {
					//for (int yi = 0; yi < b.size(); yi++) {
					//Fragment y = b.get(yi);
					total++;
					if (x.isSimilar(y, wm)) {
						similar++;
						tr.set(x.getPoints3d(), y.getPoints3d());
						double rmsd = tr.getRmsd();
						if (rmsd <= par.getMaxFragmentRmsd()) {
							hsp.add(new FragmentPair(x, y, rmsd));
							AwpNode[] ps = {new AwpNode(x.getWords()[0], y.getWords()[0]),
								new AwpNode(x.getWords()[1], y.getWords()[1])};
							wg.connect(ps, rmsd);
							double[] diff = x.coordDiff(y);
							for (int i = 0; i < diff.length; i++) {
								if (max[i] < diff[i]) {
									max[i] = diff[i];
								}
							}
						}
					}
				}
			}
			Timer.stop();
			System.out.println("DIFF " + max[0] + " " + max[1] + " " + max[2]);
			System.out.println("... fragment matching finished in: " + Timer.get());
			System.out.println("similar / total " + similar + " / " + total);
			if (false) {
				return null;
			}
		} else {
			Timer.start();
			WordMatcher wm = new WordMatcher(a.getWords(), b.getWords(), true, par.getMaxWordRmsd());
			List<Awp> alignedWords = wm.getAlignedWords();
			System.out.println("Awps: " + alignedWords.size());
			EulerGrid eg = new EulerGrid(alignedWords);
			int count = 0;
			for (Awp x : alignedWords) {
				List<Awp> near = eg.search(x);
				for (Awp y : near) {
					if (!x.equals(y)) {
						count++;
					}

				}
			}
			System.out.println("count: " + count);
			Timer.stop();
			System.out.println("... word transformations computed in: " + Timer.get());
			Timer.start();
			return null;
		}
		System.out.println("options " + a.size() * b.size());
		double operation = ((double) Timer.getNano() / (a.size() * b.size()));
		System.out.println("per operation " + operation);
		double cycle = ((double) 1000 * 1000 * 1000 / 3 / 1000 / 1000 / 1000);
		System.out.println("cpu cycle takes " + cycle);
		System.out.println("cycles per operation: " + (operation / cycle));

		System.out.println("HSPs: " + hsp.size());
		Timer.start();
		AwpClustering clustering = wg.cluster();
		System.out.println("Clusters: " + clustering.size());
		Timer.stop();
		System.out.println("Clustered in: " + Timer.get());

		align(a.getStructure(), b.getStructure(), clustering);

		// wg.getClusters();
		if (true) { // totally dead old code below?
			return null;
		}

		FragmentPair[] hspa = new FragmentPair[hsp.size()];
		hsp.toArray(hspa);

		System.out.println("hsp " + hsp.size());
		result[0] = (double) hsp.size() / Math.min(a.size(), b.size());
		FragmentsAlignment fa = new FragmentsAlignment(a.getStructure(), b.getStructure());
		fa.setHsp(hsp.size());
		long end = System.nanoTime();
		System.out.println("time " + (end - start) / 1000000);

		if (hspa.length > 0) {
			Arrays.sort(hspa);
			System.out.println("AFP best " + hspa[0].getRmsd());
			System.out.println("AFP worst " + hspa[hsp.size() - 1].getRmsd());
			System.out.println("AFP 1000 " + hsp.get(hsp.size() - 1).getRmsd());
			System.out.println("assembling...");
			List<DeprecatedCluster> clusters = cluster(hspa);
			int maxSize = 0;
			for (DeprecatedCluster c : clusters) {
				if (c.size() > maxSize) {
					maxSize = c.size();
				}
			}
			for (int i = clusters.size() - 1; i >= 0; i--) {
				DeprecatedCluster c = clusters.get(i);
				if (c.size() == 1 && maxSize > 1) {
					clusters.remove(i);
				} else {
					c.computeScore(alignablePair.getA(), alignablePair.getB());
				}
			}
			// List<Cluster> clusters = assemble(hsp);
			System.out.println("...assembled");
			System.out.println("evaluating blocks...");
			double score = evaluateBlocks(a.getStructure(), b.getStructure(), clusters);
			fa.setScore(score);
			System.out.println("...finished");
			int diff = clusters.get(0).size();
			if (clusters.size() >= 2) {
				diff -= clusters.get(1).size();
			}
			System.out.print("CLUSTER DIFF " + diff);
			DeprecatedCluster c = clusters.get(0);
			result[1] = (double) clusters.get(0).size() / Math.min(a.size(), b.size());
			fa.setClusters(clusters);
			// fa.setTmScore(evaluate(a, b, transformation, clusters));
			// Printer.println("r: " + result[1] + " " + result[2]);

			// writer.close();
			// File pf = new File("c:/tonik/rozbal/pairs.pdb");
			// Visualization.visualize(hsp, pf);
		} else {
			System.out.println("NO MATCH");
		}
		//pymolFragments.save(Directories.createDefault().getFragmentPairSelections());
		/*
		 * } catch (Exception ex) { throw new RuntimeException(ex); }
		 */
		return fa;
	}

	private static int state = 1;

	private void align(SimpleStructure a, SimpleStructure b, AwpClustering clustering) {
		AlignmentCore[] as = new AlignmentCore[clustering.size()];
		int i = 0;
		for (AwpCluster c : clustering.getClusters()) {
			ResidueId[][] matching = c.computeAlignment();
			as[i] = new AlignmentCore(a, b, matching, i);
			i++;
		}
		Arrays.sort(as);
		boolean first = true;
		for (AlignmentCore ac : as) {
			System.out.println("alignment score: " + ac.getScore());
			System.out.println("alignment rmsd: " + ac.getRmsd());
			System.out.println("alignment length: " + ac.getLength());
			System.out.println("load " + ac.getA());
			System.out.println("load " + ac.getB());
			if (first) {
				first = false;
				pyFile.writeLine(PymolVisualizer.load(ac.getA(), state));
				pyFile.writeLine(PymolVisualizer.load(ac.getB(), state));
				state++;
			}
			System.out.println();
		}
	}

	/*
	 * private void distribution(Point3d[] x, Point3d[] y, ) {
	 * 
	 * }
	 */
	@Deprecated
	private double evaluateBlocks(SimpleStructure a, SimpleStructure b, List<DeprecatedCluster> clusters) {
		Table table = new Table();
		Collections.sort(clusters);
		Collections.reverse(clusters);
		if (!clusters.isEmpty()) {
			if (matrixTest != null) {
				DeprecatedCluster c = clusters.get(0);
				matrixTest.addTestCase(a.getPdbCode(), b.getPdbCode(), c.getMatrix());
			}
		}
		LineFile lf = new LineFile(Directories.createDefault().getAlignedPdbs());
		if (!clusters.isEmpty()) {
			DeprecatedCluster c = clusters.get(0);
			lf.writeLine("load " + c.getFileA().getPath());
			lf.writeLine("load " + c.getFileB().getPath());
		}
		for (DeprecatedCluster c : clusters) {
			System.out.format("%6.3f %5.3f %4d  %d5 \n", c.getScore(), c.getRmsd(), c.getAlignment()[0].length,
				c.size());
			System.out.println(c.getLoadA());
			System.out.println(c.getLoadB());
			// table.add(c.getScore(a,
			// b)).add(qcp.getRmsd()).add(aln[0].length).add(c.size()).add(c.getCoverage());
			// table.line();
			// File sfa = Directories.createDefault().getVis(a.getPdbCode());
			// File sfb = Directories.createDefault().getVis(b.getPdbCode());
			// PymolVisualizer.save(a, sfa);
			// PymolVisualizer.save(b, sfb);
			// PymolVisualizer.saveLauncher(sfa, sfb);
			/*
			 * AtomicInteger serial = new AtomicInteger(1); PymolVisualizer v =
			 * new PymolVisualizer(); v.add(new Chain(x, serial, 'A'));
			 * v.add(new Chain(y, serial, 'B')); v.add(c);
			 * v.save(Directories.createDefault().getVisPdb(),
			 * Directories.createDefault().getVisPy());
			 */
		}

		table.sortDescending(0).print();
		return clusters.get(0).getScore();
	}

	/**
	 * Alternative to clustering, add pairs iterativelly and find best moment to stop.
	 */
	private List<DeprecatedCluster> assemble(List<FragmentPair> pairs) {
		Timer.start();
		List<DeprecatedCluster> clusters = new ArrayList<>();
		DeprecatedCluster best = null;
		int max = 0; // TODO formulate score based on size and RMSD, TM-score //
		// like
		for (int xi = 0; xi < pairs.size(); xi++) {
			// for (int xi = 0; xi < 1; xi++) {
			// System.out.println(xi);
			FragmentPair x = pairs.get(xi);
			if (!x.free()) {
				continue;
			}
			DeprecatedCluster c = new DeprecatedCluster(x);
			clusters.add(c);
			Map<Integer, Double> map = new HashMap<>();
			for (int yi = 0; yi < pairs.size(); yi++) {
				// no free check, allowing cluster intersections
				if (xi == yi) {
					continue;
				}
				FragmentPair y = pairs.get(yi);
				double rmsd = x.getRmsd(y);
				if (rmsd <= 8) {
					map.put(yi, rmsd);
				}
			}
			// System.out.println("potentially compatible: " + map.size());
			map = MapUtil.sortByValue(map);

			if (!map.isEmpty()) {
				double initial = map.get(map.keySet().iterator().next());
				for (int i : map.keySet()) {
					if (map.get(i) > 4 * initial) {
						break;
					}
					FragmentPair fp = pairs.get(i);
					c.add(fp);
				}
			}
		}
		/*
		 * Table table = new Table(); for (Cluster c : clusters) { double score
		 * = c.getScore(alignablePair.getA(), alignablePair.getB());
		 * table.add(score); table.line(); } System.out.println("+++");
		 * table.sortDescending(0).getFirst(5).print();
		 * System.out.println("---"); System.out.println("clusters " +
		 * clusters.size()); System.out.println("max " + max);
		 * Collections.sort(clusters); Timer.stop();
		 * System.out.println("Clustering took: " + Timer.get());
		 */
		// System.out.println("BEST " + best.getCoverage());
		return clusters;
	}

	@Deprecated
	private List<DeprecatedCluster> cluster(FragmentPair[] pairs) {
		Timer.start();
		List<DeprecatedCluster> clusters = new ArrayList<>();
		for (int xi = 0; xi < pairs.length; xi++) {
			// System.out.println(xi + " / " + pairs.length);
			FragmentPair x = pairs[xi];
			/*
			 * if (!x.free()) { continue; }
			 */
			DeprecatedCluster c = new DeprecatedCluster(x);
			clusters.add(c);
			for (int yi = 0; yi < pairs.length; yi++) {
				// no free check, allowing cluster intersections
				if (xi == yi) {
					continue;
				}
				FragmentPair y = pairs[yi];
				// if (c.getCore().isRoughlyCompatible(y)) {
				if (c.getCore().isCompatible(y)) {
					c.add(y);
				}
				// }
			}
			if (c.size() > 1) {
				System.out.println(c.size() + " big");
			}
		}
		Collections.sort(clusters);
		Timer.stop();
		System.out.println("Clustering took: " + Timer.get());

		/*
		 * Table table = new Table(); for (Cluster c : clusters) { double score
		 * = c.getScore(alignablePair.getA(), alignablePair.getB());
		 * table.add(score); table.line(); } System.out.println("+++");
		 * table.sortDescending(0).getFirst(5).print();
		 * System.out.println("---"); System.out.println("clusters " +
		 * clusters.size()); Collections.sort(clusters); Timer.stop();
		 * System.out.println("Clustering took: " + Timer.get());
		 */
		return clusters;
	}

}
