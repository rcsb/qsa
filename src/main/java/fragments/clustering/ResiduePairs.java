package fragments.clustering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pdb.ResidueId;

public class ResiduePairs {
	private Map<ResiduePair, RankedResiduePair> set = new HashMap<>();

	public void add(ResidueId x, ResidueId y, double rmsd) {
		ResiduePair rp = new ResiduePair(x, y);
		if (!set.containsKey(rp)) {
			RankedResiduePair rrp = new RankedResiduePair(x, y);
			rrp.add(rmsd);
			set.put(rp, rrp);
		} else {
			RankedResiduePair rrp = set.get(rp);
			rrp.add(rmsd);
		}
	}
	
	public int size() {
		return set.size();
	}
	
	public List<RankedResiduePair> values() {
		List<RankedResiduePair> l = new ArrayList<>(set.values());
		Collections.sort(l);
		return l;
	}
}
