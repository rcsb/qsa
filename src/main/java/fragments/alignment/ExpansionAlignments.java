package fragments.alignment;

import fragments.AwpNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExpansionAlignments implements Alignments {

	private final List<ExpansionAlignment> as = new ArrayList<>();
	private final int minStrSize;
	private final Set<AwpNode> covered = new HashSet<>();

	public ExpansionAlignments(int minStrSize) {
		this.minStrSize = minStrSize;
	}

	public void add(ExpansionAlignment a) {
		as.add(a);
		covered.addAll(a.getNodes());
	}

	@Override
	public List<Alignment> getAlignments() {
		int max = 0;
		for (ExpansionAlignment c : as) {
			int r = c.sizeInResidues();
			if (max < r) {
				max = r;
			}
		}
		List<Alignment> good = new ArrayList<>();
		for (ExpansionAlignment c : as) {
			int n = c.sizeInResidues();
			if (n >= 15 && (n >= minStrSize / 5) && n >= (max / 5)) {
				good.add(c);
			}
		}
		return good;
	}

	public boolean covers(AwpNode node) {
		return covered.contains(node);
	}

}
