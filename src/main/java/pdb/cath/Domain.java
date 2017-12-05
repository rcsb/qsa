package pdb.cath;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonin Pavelka
 */
public class Domain {

	private final String id;
	private final List<Segment> segments = new ArrayList<>();

	public Domain(String prefix, int index) {
		String suffix = Integer.toString(index);
		if (suffix.length() == 1) {
			suffix = "0" + suffix;
		}
		this.id = prefix + suffix;
	}

	public String getId() {
		return id;
	}

	public void addSegment(Segment segment) {
		segments.add(segment);
	}
	
	public boolean doesResidueBelong(String chain, int residueNumber, Character insertionCode) {
		for (Segment segment : segments) {
			if (segment.contains(chain, residueNumber, insertionCode)) {
				return true;
			}
		}
		return false;
	}
}
