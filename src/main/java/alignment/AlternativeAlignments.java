package alignment;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonin Pavelka
 */
public class AlternativeAlignments {

	private final StructureSourcePair structureSourcePair;
	private final List<Alignment> list = new ArrayList<>();

	public AlternativeAlignments(StructureSourcePair structureSourcePair) {
		this.structureSourcePair = structureSourcePair;
	}

	public void add(Alignment aln) {
		list.add(aln);
	}

	public StructureSourcePair getStructureSourcePair() {
		return structureSourcePair;
	}

	public Alignment getBest() {
		Alignment best = list.get(0);
		for (int i = 1; i < list.size(); i++) {
			Alignment a = list.get(i);
			if (a.compareTo(best) < 0) {
				best = a;
			}
		}
		return best;
	}
}
