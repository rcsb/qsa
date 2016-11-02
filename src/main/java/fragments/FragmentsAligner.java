/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fragments;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.AtomImpl;
import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.geometry.SuperPosition;
import org.biojava.nbio.structure.jama.Matrix;

import alignment.FragmentsAlignment;
import alignment.PointMatcher;
import fragments.clustering.Cluster;
import geometry.Transformation;
import geometry.Transformer;
import io.Directories;
import pdb.Residue;
import pdb.ResidueId;
import pdb.SimpleStructure;
import spark.Printer;
import spark.interfaces.AlignablePair;
import spark.interfaces.Alignment;
import spark.interfaces.StructureAlignmentAlgorithm;
import statistics.Distribution;
import superposition.SuperPositionQCP;
import util.MapUtil;
import util.Timer;
import util.pymol.Chain;
import util.pymol.PymolFragments;
import util.pymol.PymolVisualizer;

/**
 *
 * @author Antonin Pavelka
 */
public class FragmentsAligner implements StructureAlignmentAlgorithm {

	private transient Directories dirs_;
	private FragmentsFactory ff;
	private boolean visualize;

	public FragmentsAligner(Directories dirs) {
		dirs_ = dirs;
		ff = new FragmentsFactory();
	}

	public void setVisualize(boolean b) {
		visualize = b;
	}

	public Alignment align(AlignablePair sp) {
		Fragments a = ff.create(sp.getA(), 1);
		Fragments b = ff.create(sp.getB(), 5); // !!!!
		Alignment al = align(a, b);
		return al;
	}

	public FragmentsAlignment align(Fragments a, Fragments b) {
		Parameters par = Parameters.create();
		Transformation transformation = null;
		Printer.println("i: " + a.getStructure().getPdbCode() + " " + b.getStructure().getPdbCode());
		double[] result = { 0, 0, 0 };
		Distribution ds = new Distribution();
		List<FragmentPair> hsp = new ArrayList<>();
		long start = System.nanoTime();
		System.out.println("fragments " + a.size() + " " + b.size());
		if (par.findAfpBySuperposing()) {
			Transformer tr = new Transformer();
			for (int xi = 0; xi < a.size(); xi++) {
				for (int yi = 0; yi < b.size(); yi++) {
					Fragment x = a.get(xi);
					Fragment y = b.get(yi);
					tr.set(x.getPoints3d(), y.getPoints3d());
					double rmsd = tr.getRmsd();
					if (rmsd <= par.getMaxFragmentRmsd()) {
						hsp.add(new FragmentPair(x, y, rmsd));
					}
				}
			}
		} else { // dead code
			for (int xi = 0; xi < a.size(); xi++) {
				for (int yi = 0; yi < b.size(); yi++) {
					Fragment x = a.get(xi);
					Fragment y = b.get(yi);
					double d = x.distance(y);
					if (d <= par.getMaxFragmentSimilarity()) {
						hsp.add(new FragmentPair(x, y, d));
					}
				}
			}
		}
		System.out.println("hsp " + hsp.size());
		result[0] = (double) hsp.size() / Math.min(a.size(), b.size());
		FragmentsAlignment fa = new FragmentsAlignment(a.getStructure(), b.getStructure());
		fa.setTransformation(transformation);
		fa.setHsp(hsp.size());
		long end = System.nanoTime();
		System.out.println("time " + (end - start) / 1000000);
		PymolFragments pymolFragments = new PymolFragments(a.getStructure().getPdbCode(),
				b.getStructure().getPdbCode());
		if (!hsp.isEmpty()) {
			for (int i = 0; i < hsp.size(); i++) {
				FragmentPair p = hsp.get(i);
				p.computeSuperposition();
				pymolFragments.add(p.get());
			}
			Collections.sort(hsp);

			System.out.println("AFP best " + hsp.get(0).getRmsd());
			System.out.println("AFP worst " + hsp.get(hsp.size() - 1).getRmsd());
			// if (hsp.size() >= 1000) {
			// hsp = hsp.subList(0, 1000); //
			// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			// }
			System.out.println("AFP 1000 " + hsp.get(hsp.size() - 1).getRmsd());

			System.out.println("clustering...");

			// List<Cluster> clusters = cluster(hsp);
			List<Cluster> clusters = assemble(hsp);

			evaluateBlocks(a.getStructure(), b.getStructure(), clusters);

			System.out.println("...clustered");
			int diff = clusters.get(0).size();
			if (clusters.size() >= 2) {
				diff -= clusters.get(1).size();
			}
			for (Cluster c : clusters) {
				System.out.print(c.size() + " ");
			}
			System.out.print("CLUSTER DIFF " + diff);
			if (diff > 3) {
				System.out.println(" ***");
			} else {
				System.out.println(" ###");
			}
			Cluster c = clusters.get(0);

			result[1] = (double) clusters.get(0).size() / Math.min(a.size(), b.size());
			fa.setClusters(clusters);
			transformation = c.getTransformation();
			// fa.setTmScore(evaluate(a, b, transformation, clusters));
			Printer.println("r: " + result[1] + " " + result[2]);

			// writer.close();
			// File pf = new File("c:/tonik/rozbal/pairs.pdb");
			// Visualization.visualize(hsp, pf);
		} else {
			System.out.println("NO MATCH");
		}
		pymolFragments.save(Directories.createDefault().getFragmentPairSelections());
		/*
		 * } catch (Exception ex) { throw new RuntimeException(ex); }
		 */
		fa.setTransformation(transformation);
		return fa;
	}

	/*
	 * private void distribution(Point3d[] x, Point3d[] y, ) {
	 * 
	 * }
	 */

	private void evaluateBlocks(SimpleStructure a, SimpleStructure b, List<Cluster> clusters) {
		SuperPositionQCP qcp = new SuperPositionQCP();
		for (Cluster c : clusters) {

			// TODO better sorting of clusters, using coverage? rmsd?

			// Cluster c = clusters.get(0);
			ResidueId[][] aln = c.getAlignment();
			Point3d[] x = a.getPoints(aln[0]);
			Point3d[] y = b.getPoints(aln[1]);
			qcp.set(x, y);
			Matrix4d m = qcp.getTransformationMatrix();
			// System.out.println();
			// System.out.println(c.size() + " " + x.length + " RMSD " +
			// qcp.getRmsd());
			// System.out.println();
			if (false) {
				x = a.getPoints();
				y = b.getPoints();

				qcp.transform(m, y);

				File sfa = Directories.createDefault().getVis(a.getPdbCode());
				File sfb = Directories.createDefault().getVis(b.getPdbCode());
				PymolVisualizer.save(a, sfa);
				PymolVisualizer.save(b, sfb);
				PymolVisualizer.saveLauncher(sfa, sfb);

				AtomicInteger serial = new AtomicInteger(1);
				PymolVisualizer v = new PymolVisualizer();

				v.add(new Chain(x, serial, 'A'));
				v.add(new Chain(y, serial, 'B'));
				v.add(clusters.get(0));
				v.save(Directories.createDefault().getVisPdb(), Directories.createDefault().getVisPy());

				b.transform(m);
				PymolVisualizer.save(a, Directories.createDefault().getAlignedA());
				PymolVisualizer.save(b, Directories.createDefault().getAlignedB());
			}
			if (c.size() == 1) {
				break; // !!!!!!!!!!!!
			}

		}

	}

	private double evaluate(Fragments a, Fragments b, Transformation m, List<Cluster> clusters) {
		Point3d[] x = a.getStructure().getPoints();
		Point3d[] y = b.getStructure().getPoints();

		// CalcPoint.transform(m.getSuperimposer().getTransformation(), x);

		Matrix rotMatrix = m.getSuperimposer().getRotation();
		Atom tranMatrix = m.getSuperimposer().getTranslation();

		// now we have all the info to perform the rotations ...

		for (int i = 0; i < x.length; i++) {
			Atom at = new AtomImpl();
			at.setX(y[i].x);
			at.setY(y[i].y);
			at.setZ(y[i].z);

			Calc.rotate(at, rotMatrix);
			Calc.shift(at, tranMatrix);

			y[i] = new Point3d(at.getCoords());
		}

		if (visualize) {
			AtomicInteger serial = new AtomicInteger(1);
			PymolVisualizer v = new PymolVisualizer();
			v.add(new Chain(x, serial, 'A'));
			v.add(new Chain(y, serial, 'B'));
			v.add(clusters.get(0));
			v.save(Directories.createDefault().getVisPdb(), Directories.createDefault().getVisPy());
		}

		// Visualization.visualize(x, 'X', dirs_.x());
		// Visualization.visualize(y, 'Y', dirs_.y());
		// !!!!!!!!!!!!!!!!!!!! y, y
		PointMatcher pm = new PointMatcher(x, y);
		Point3d[][] aligned = pm.match();
		double tm = SuperPosition.TMScore(aligned[0], aligned[1], a.getStructure().size()); // is
																							// the
																							// length
																							// correct?
		System.out.println("TM-score " + tm);
		return tm;
	}

	/**
	 * Alternative to clustering, add pairs iterativelly and find best moment to
	 * stop.
	 */
	private List<Cluster> assemble(List<FragmentPair> pairs) {
		Timer.start();
		List<Cluster> clusters = new ArrayList<>();
		int max = 0; // TODO formulate score based on size and RMSD, TM-score like
		for (int xi = 0; xi < pairs.size(); xi++) {
			// for (int xi = 0; xi < 1; xi++) {
			// System.out.println(xi);
			FragmentPair x = pairs.get(xi);
			if (!x.free()) {
				continue;
			}
			Cluster c = new Cluster(x);
			clusters.add(c);
			Map<Integer, Double> map = new HashMap<>();
			for (int yi = 0; yi < pairs.size(); yi++) {
				// no free check, allowing cluster intersections
				if (xi == yi) {
					continue;
				}
				FragmentPair y = pairs.get(yi);
				double rmsd = x.getRmsd(y);
				if (rmsd <= 6) {
					map.put(yi, rmsd);
				}
			}
			map = MapUtil.sortByValue(map);
			Set<Residue> covered = new HashSet<>(); // residues of alignment						
			if (!map.isEmpty()) {
				double initial = map.get(map.keySet().iterator().next());
				for (int i : map.keySet()) { // add greedy and store total scores (over residues, which are more meaningful because of frament overaps
					if (map.get(i) > 2 * initial) {
						break;
					}
					FragmentPair fp = pairs.get(i);
					covered.addAll(fp.getResidues());
					// System.out.println(i + " " + map.get(i) + " " +
					// covered.size());
				}
			}
			// System.out.println("------- " + covered.size());
			if (max < covered.size()) {
				max = covered.size();
			}
		}
		System.out.println("max " + max);
		Collections.sort(clusters);
		Timer.stop();
		System.out.println("Clustering took: " + Timer.get());
		return clusters;
	}

	/**
	 * Not a good idea.
	 */
	private List<Cluster> cluster(List<FragmentPair> pairs) {
		Timer.start();
		List<Cluster> clusters = new ArrayList<>();
		for (int xi = 0; xi < pairs.size(); xi++) {
			FragmentPair x = pairs.get(xi);
			if (!x.free()) {
				continue;
			}
			Cluster c = new Cluster(x);
			clusters.add(c);
			for (int yi = 0; yi < pairs.size(); yi++) {
				// no free check, allowing cluster intersections
				if (xi == yi) {
					continue;
				}
				FragmentPair y = pairs.get(yi);
				if (c.getCore().isRoughlyCompatible(y)) {
					if (c.getCore().isCompatible(y)) {
						c.add(y);
					}
				}
			}
		}
		Collections.sort(clusters);
		Timer.stop();
		System.out.println("Clustering took: " + Timer.get());
		return clusters;
	}

}
