package fragments.alignment;

import fragments.AwpNode;
import java.util.ArrayList;
import java.util.List;

public class ExpansionAlignments {

	private final List<ExpansionAlignment> as = new ArrayList<>();
	private final int minStrSize;
	private final boolean[] covered;

	public ExpansionAlignments(int nodeN, int minStrSize) {
		covered = new boolean[nodeN];
		this.minStrSize = minStrSize;
	}

	public void add(ExpansionAlignment a) {
		as.add(a);
		for (AwpNode n : a.getNodes()) {
			covered[n.getId()] = true;
		}
	}

	public List<ExpansionAlignment> getAlignments() {
		int max = 0;
		for (ExpansionAlignment c : as) {
			int r = c.sizeInResidues();
			if (max < r) {
				max = r;
			}
		}
		List<ExpansionAlignment> good = new ArrayList<>();
		for (ExpansionAlignment c : as) {
			int n = c.sizeInResidues();
			if (n >= 15 && (n >= minStrSize / 5) && n >= (max / 5)) {
				good.add(c);
			}
		}
		return good;
	}

	public boolean covers(AwpNode node) {
		return covered[node.getId()];
	}

}
