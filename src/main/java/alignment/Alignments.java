package alignment;

import global.Parameters;
import global.io.Directories;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Antonin Pavelka
 */
public class Alignments {

	private final Parameters parameters;
	private final Directories dirs;
	private final Map<StructureSourcePair, AlternativeAlignments> map = new HashMap<>();
	private double tmThreshold;

	public Alignments(Parameters parameters, Directories dirs) {
		this.parameters = parameters;
		this.dirs = dirs;
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

	public void setTmFilter(double tmThreshold) {
		this.tmThreshold = tmThreshold;
	}

	public List<Alignment> getBestSummariesSorted() {
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

}
