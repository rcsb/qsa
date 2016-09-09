/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.geometry.CalcPoint;
import org.biojava.nbio.structure.geometry.SuperPosition;

import alignment.AlignmentQuality;
import alignment.PointMatcher;
import alignment.ReferenceAlignment;
import io.Directories;
import spark.Printer;
import spark.interfaces.AlignablePair;
import spark.interfaces.Alignment;
import spark.interfaces.AlignmentWrapper;
import spark.interfaces.StructureAlignmentAlgorithm;
import statistics.Distribution;

/**
 *
 * @author Antonin Pavelka
 */
public class FragmentsAligner implements StructureAlignmentAlgorithm {

	private Parameters params_;
	private transient Directories dirs_;
	private FragmentsFactory ff;

	public FragmentsAligner(Parameters params, Directories dirs) {
		params_ = params;
		dirs_ = dirs;
		ff = new FragmentsFactory(params);
	}

	public Alignment align(AlignablePair sp) {
		Fragments fa = ff.create(sp.getA(), 1);
		Fragments fb = ff.create(sp.getB(), 1);
		AlignmentQuality aq = align(fa, fb);
		return new AlignmentWrapper(aq);
	}

	public AlignmentQuality align(Fragments a, Fragments b) {
		Matrix4d transformation = null;
		Printer.println("i: " + a.getStructure().getId() + " " + b.getStructure().getId());
		double[] result = { 0, 0, 0 };
		Distribution ds = new Distribution();
		List<Pair> hsp = new ArrayList<>();
		long start = System.nanoTime();
		System.out.println("fragments " + a.size() + " " + b.size());
		for (int xi = 0; xi < a.size(); xi++) {
			for (int yi = 0; yi < b.size(); yi++) {
				Fragment x = a.get(xi);
				Fragment y = b.get(yi);
				double d = x.distance(y);
				if (d < 0.7) {
					hsp.add(new Pair(x, y, d));
				}
			}
		}
		System.out.println("hsp " + hsp.size());
		result[0] = (double) hsp.size() / Math.min(a.size(), b.size());
		long end = System.nanoTime();
		System.out.println("time " + (end - start) / 1000000);
		if (!hsp.isEmpty()) {
			Collections.sort(hsp);
			for (int i = 0; i < hsp.size(); i++) {
				Pair p = hsp.get(i);
				p.computeSuperposition();
			}
			List<Cluster> clusters = cluster(hsp);
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
			transformation = c.getTransformation();
			result[2] = evaluate(a, b, transformation);
			Printer.println("r: " + result[1] + " " + result[2]);

			// writer.close();
			// File pf = new File("c:/tonik/rozbal/pairs.pdb");
			// Visualization.visualize(hsp, pf);
		} else {
			System.out.println("NO MATCH");
		}
		/*
		 * } catch (Exception ex) { throw new RuntimeException(ex); }
		 */
		return new AlignmentQuality(a.getStructure().getId(), b.getStructure().getId(), transformation, result);
	}

	@Deprecated
	public double evaluateRef(Structure a, Structure b) {
		double refTm = ReferenceAlignment.align(a, b);
		System.out.println("Reference TM-score: " + refTm);
		return refTm;
	}

	private double evaluate(Fragments a, Fragments b, Matrix4d m) {
		Point3d[] x = a.getStructure().getPoints();
		Point3d[] y = b.getStructure().getPoints();
		CalcPoint.transform(m, x);
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

	private List<Cluster> cluster(List<Pair> pairs) {
		List<Cluster> clusters = new ArrayList<>();
		for (int xi = 0; xi < pairs.size(); xi++) {
			Pair x = pairs.get(xi);
			if (!x.free()) {
				continue;
			}
			Cluster c = new Cluster(x);
			clusters.add(c);
			for (int yi = 0; yi < pairs.size(); yi++) {
				Pair y = pairs.get(yi);
				if (!y.free()) {
					continue;
				}
				if (xi == yi) {
					continue;
				}
				c.tryToAdd(y);
			}
		}
		Collections.sort(clusters);
		return clusters;
	}

}
