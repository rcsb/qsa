/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fragments;

import alignment.score.Equivalence;
import geometry.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import geometry.Transformer;
import io.Directories;
import io.LineFile;
import java.io.File;
import pdb.ResidueId;
import pdb.SimpleStructure;
import spark.Printer;
import spark.interfaces.AlignablePair;
import spark.interfaces.Alignment;
import spark.interfaces.StructureAlignmentAlgorithm;
import statistics.Distribution;
import util.Timer;
import util.pymol.PymolVisualizer;

/**
 *
 * @author Antonin Pavelka
 */
public class FragmentsAligner implements StructureAlignmentAlgorithm {

	private transient Directories dirs_;
	private FragmentsFactory ff;
	private AlignablePair alignablePair;

	private LineFile pyFile;
	private LineFile resultsFile;
	private LineFile tableFile;

	public FragmentsAligner(Directories dirs) {
		dirs_ = dirs;
		pyFile = new LineFile(dirs.getPyFile());
		resultsFile = new LineFile(dirs.getResultsFile());
		tableFile = new LineFile(dirs.getTableFile());
		ff = new FragmentsFactory();
	}

	public Alignment align(AlignablePair sp) {
		this.alignablePair = sp;
		Parameters pars = Parameters.create();
		Fragments a = ff.create(sp.getA(), pars.getWordLength(), pars.skipX());
		Fragments b = ff.create(sp.getB(), pars.getWordLength(), pars.skipY());
		/*if (visualize) {
			a.visualize(dirs_.getTemp(a.getStructure().getPdbCode() + "_" + "frags_A.py"));
			b.visualize(dirs_.getTemp(b.getStructure().getPdbCode() + "_" + "frags_B.py"));
			// was a. ????
		}*/
		align(a, b);
		return null;
	}

	private void align(Fragments a, Fragments b) {
		Parameters par = Parameters.create();
		Printer.println("i: " + a.getStructure().getPdbCode() + " "
			+ b.getStructure().getPdbCode());
		double[] result = {0, 0, 0};
		Distribution ds = new Distribution();
		List<FragmentPair> hsp = new ArrayList<>();
		long start = System.nanoTime();

		Transformer tr = new Transformer();
		AwpGraph wg = new AwpGraph();

		boolean biwords = true;
		if (biwords) {
			System.out.println("Matching pairs of words, fragment numbers: "
				+ a.size() + " " + b.size() + " ...");
			Timer.start();
			WordMatcher wm = new WordMatcher(a.getWords(), b.getWords(), false,
				par.getMaxWordRmsd());
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
		} else {
			Timer.start();
			WordMatcher wm = new WordMatcher(a.getWords(), b.getWords(), true,
				par.getMaxWordRmsd());
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
		int id = 0;
		for (AlignmentCore ac : as) {
			if (first) {
				Equivalence eq = ac.getEquivalence();
				saveResults(eq);
				first = false;
				visualize(eq, id++);
				state++;
			}
		}
	}

	private void saveResults(Equivalence eq) {
		StringBuilder sb = new StringBuilder();
		char s = ',';
		sb.append(eq.get(0).getPdbCode()).append(s);
		sb.append(eq.get(1).getPdbCode()).append(s);
		sb.append(eq.matchingResidues()).append(s);
		sb.append(eq.matchingResiduesRelative()).append(s);
		sb.append(eq.tmScore()).append(s);
		tableFile.writeLine(sb.toString());
	}

	private void visualize(Equivalence eq, int id) {
		String name = eq.get(0).getPdbCode() + "_" + eq.get(1).getPdbCode() + "_" + id;

		String na = dirs_.getAligned(name + "_A.pdb");
		String nb = dirs_.getAligned(name + "_B.pdb");

		Point shift = eq.center().negative();
		
		System.out.println(shift);

		PymolVisualizer.save(eq.get(0), shift, new File(na));
		PymolVisualizer.save(eq.get(1), shift, new File(nb));
		eq.save(shift, new File(dirs_.getMatchLines(name)));

		pyFile.writeLine(PymolVisualizer.load(na, state));
		pyFile.writeLine(PymolVisualizer.load(nb, state));
		pyFile.writeLine(PymolVisualizer.load(dirs_.getMatchLines(name), state));
	}

}
