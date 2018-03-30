package algorithm.scoring;

import fragment.word.Word;
import fragment.word.WordsFactory;
import geometry.primitives.Point;
import geometry.superposition.Superposer;
import global.Parameters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import structure.Residue;
import structure.SimpleStructure;

/**
 * Creates residue - residue 1 : 1 mapping from superimposed structures. Whole words are matched to prevent matching
 * isolated residues.
 *
 * Hungry approach. Match words with smallest RMSD (no superposition done, relying on structures being aligned) first.
 * Do not match any word that defines matching inconsistent with already matched residues. Since words are overlapping
 * (sliding windows), no unmatched regions should occur in very similar structures.
 */
public class WordAlignmentFactory {

	private final Parameters parameters;
	private static final Superposer tr = new Superposer();

	public WordAlignmentFactory(Parameters parameters) {
		this.parameters = parameters;
	}

	public ResidueAlignment create(SimpleStructure strA, SimpleStructure strB) {
		ScoreParameters scoreParameters = parameters.getScorePars();
		Word[] wa = getWords(strA);
		Word[] wb = getWords(strB);
		Map<Residue, Residue> sa = new HashMap<>(); // mapping strA -> strB
		Map<Residue, Residue> sb = new HashMap<>(); // mapping strB -> strA
		List<WordPair> cs = new ArrayList<>();
		//int id = 0;
		for (Word a : wa) {
			for (Word b : wb) {
				if (a.getCenter().distance(b.getCenter()) < scoreParameters.initCenterDist) {
					//for (int i = 0; i < 2; i++) {
					//if (i == 1) {
					//	b = b.invert(id++);
					//}
					if (allClose(a, b, scoreParameters.all)) {
						double d = dist(a, b);
						if (d < scoreParameters.dist) {
							tr.set(a.getPoints3d(), b.getPoints3d());
							double rmsd = tr.getRmsd();
							if (rmsd <= scoreParameters.rmsd) {
								double sum = rmsd + d;
								if (sum <= scoreParameters.sum) {
									cs.add(new WordPair(a, b, sum));
								}
							}
						}
					}
					//}
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
		int normalizationLength = parameters.getReferenceLength(strA.size(), strB.size());
		ResidueAlignment eq = new ResidueAlignment(strA, strB, normalizationLength, mapping);
		return eq;
	}

	private static boolean compatible(Word a, Word b, Map<Residue, Residue> map) {
		Residue[] ras = a.getResidues();
		Residue[] rbs = b.getResidues();
		for (int i = 0; i < ras.length; i++) {
			Residue ra = ras[i];
			Residue rb = map.get(ra);
			if (rb != null && !rb.equals(rbs[i])) {
				return false;
			}
		}
		return true;
	}

	private static boolean allClose(Word a, Word b, double limit) {
		Point[] ap = a.getPoints();
		Point[] bp = b.getPoints();
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

	private Word[] getWords(SimpleStructure ss) {
		WordsFactory wf = new WordsFactory(parameters, ss, 1);
		return wf.create().toArray();
	}

}
