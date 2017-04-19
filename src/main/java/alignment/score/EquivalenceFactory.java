package alignment.score;

import fragments.Word;
import fragments.WordsFactory;
import geometry.Point;
import geometry.Transformer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pdb.Residue;
import pdb.SimpleStructure;
import pdb.StructureFactory;
import util.Timer;

/**
 * Creates residue - residue 1 : 1 mapping. Whole words are matched to prevent matching isolated
 * residues. Structures must be rotated and translated.
 *
 * Hungry approach. Match words with smallest RMSD (no superposition done, relying on structures
 * being aligned) first. Do not match any word that defines matching inconsistent with already
 * matched residues. Since words are overlapping (sliding windows), no unmatched regions should
 * occur in very similar structures.
 */
public class EquivalenceFactory {

	private static ScorePars pars = new ScorePars();
	private static Transformer tr = new Transformer();

	public static Equivalence create(SimpleStructure strA, SimpleStructure strB) {
		Timer.start();
		Transformer tr = new Transformer();
		Word[] wa = getWords(strA);
		Word[] wb = getWords(strB);
		Map<Residue, Residue> sa = new HashMap<>(); // mapping strA -> strB
		Map<Residue, Residue> sb = new HashMap<>(); // mapping strB -> strA
		List<WordPair> cs = new ArrayList<>();
		for (Word a : wa) {
			for (Word b : wb) {
				if (a.getCenter().distance(b.getCenter()) < pars.initCenterDist) {
					for (int i = 0; i < 2; i++) {
						if (i == 1) {
							b = b.invert();
						}
						if (allClose(a, b, pars.all)) {
							double d = dist(a, b);
							if (d < pars.dist) {
								tr.set(a.getPoints3d(), b.getPoints3d());
								double rmsd = tr.getRmsd();
								if (rmsd <= pars.rmsd) {
									double sum = rmsd + d;
									if (sum <= pars.sum) {
										cs.add(new WordPair(a, b, sum));
									}
								}
							}
						}
					}
				}
			}
		}
		WordPair[] a = new WordPair[cs.size()];
		cs.toArray(a);
		Arrays.sort(a);
		for (WordPair p : a) {
			if (compatible(p.a, p.b, sa) && compatible(p.b, p.a, sb)) {
				Residue[] ra = p.a.getResidues();
				Residue[] rb = p.b.getResidues();
				for (int i = 0; i < ra.length; i++) {
					sa.put(ra[i], rb[i]);
					sb.put(rb[i], ra[i]);
				}
			}
		}
		Residue[][] mapping = new Residue[2][sa.size()];
		int i = 0;
		for (Residue r : sa.keySet()) {
			mapping[0][i] = r;
			mapping[1][i] = sa.get(r);
			i++;
		}
		Equivalence eq = new Equivalence(strA, strB, mapping);
		Timer.stop();
		return eq;
	}

	private static boolean compatible(Word a, Word b, Map<Residue, Residue> map) {
		Residue[] ra = a.getResidues();
		Residue[] rb = b.getResidues();
		for (int i = 0; i < ra.length; i++) {
			Residue r = ra[i];
			if (map.containsKey(r)) {
				if (!map.get(r).equals(rb[i])) {
					return false;
				}
			}
		}
		return true;
	}

	private static boolean allClose(Word a, Word b, double limit) {
		Point[] ap = a.getPoints();
		Point[] bp = b.getPoints();
		double d = 0;
		for (int i = 0; i < ap.length; i++) {
			if (ap[i].minus(bp[i]).size() > limit) {
				return false;
			}
		}
		return true;
	}

	private static double dist(Word a, Word b) {
		Point[] ap = a.getPoints();
		Point[] bp = b.getPoints();
		double d = 0;
		for (int i = 0; i < ap.length; i++) {
			d += ap[i].minus(bp[i]).size();
		}
		d /= ap.length;
		return d;
	}

	private static Word[] getWords(SimpleStructure ss) {
		WordsFactory wf = new WordsFactory(ss, pars.wordLength);
		return wf.create().toArray();
	}

	public static void main(String[] args) throws IOException {
		File fa = new File("c:/kepler/data/qsa/equivalence/aln_1a3y_1obp_0_a.pdb");
		File fb = new File("c:/kepler/data/qsa/equivalence/aln_1a3y_1obp_0_b.pdb");
		SimpleStructure[] sss = {
			StructureFactory.parsePdb(fa),
			StructureFactory.parsePdb(fb),};
		EquivalenceFactory.create(sss[0], sss[1]);
	}
}
