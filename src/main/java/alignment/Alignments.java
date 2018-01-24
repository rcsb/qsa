package alignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Antonin Pavelka
 * 
 * Result of a search - among others, aligned structure ids, transformation matrix and score.
 * 
 */
public class Alignments {

	private final Map<StructureSourcePair, AlternativeAlignments> map = new HashMap<>();

	public Alignments() {
	}

	public synchronized void add(Alignment alignmentSummary) {
		StructureSourcePair key = alignmentSummary.getStructureSourcePair();
		AlternativeAlignments alternativeAlignments = map.get(key);
		if (alternativeAlignments == null) {
			alternativeAlignments = new AlternativeAlignments(key);
			alternativeAlignments.add(alignmentSummary);
			map.put(key, alternativeAlignments);
		} else {
			alternativeAlignments.add(alignmentSummary);
		}
	}

	public void merge(Alignments additional) {
		map.putAll(additional.map);
	}

	public List<Alignment> getBestSummariesSorted(double tmThreshold) {
		ArrayList<Alignment> list = new ArrayList<>();
		for (AlternativeAlignments alternativeAlignments : map.values()) {
			Alignment best = alternativeAlignments.getBest();
			if (best.getTmScore() >= tmThreshold) {
				list.add(best);
			}
		}
		Collections.sort(list);
		return list;
	}

	public Alignment getBest() {
		Alignment best = null;
		double bestScore = 0;
		for (AlternativeAlignments alternativeAlignments : map.values()) {
			Alignment alternative = alternativeAlignments.getBest();
			if (alternative.getTmScore() >= bestScore) {
				bestScore = alternative.getTmScore();
				best = alternative;
			}
		}
		return best;
	}

}
