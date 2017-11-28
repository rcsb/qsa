package alignment;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonin Pavelka
 */
public class AlternativeAlignments {

	private final StructureSourcePair structureSourcePair;
	private final List<AlignmentSummary> list = new ArrayList<>();

	public AlternativeAlignments(StructureSourcePair structureSourcePair) {
		this.structureSourcePair = structureSourcePair;
	}

	public void add(AlignmentSummary aln) {
		list.add(aln);
	}

	public StructureSourcePair getStructureSourcePair() {
		return structureSourcePair;
	}

	public AlignmentSummary getBest() {
		AlignmentSummary best = list.get(0);
		for (int i = 1; i < list.size(); i++) {
			AlignmentSummary a = list.get(i);
			if (best.compareTo(a) < 0) {
				best = a;
				System.out.println("improving TM score: " + best.getTmScore());
			}
		}
		return best;
	}
}
