package fragments;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import pdb.SimpleStructure;

/**
 *
 * @author Antonin Pavelka
 */
public final class Fragments implements Serializable {

	private static final long serialVersionUID = 1L;
	private List<Fragment> fragments_ = new ArrayList<>();
	private SimpleStructure structure_;

	protected Fragments(SimpleStructure structure) {
		structure_ = structure;
	}

	public void add(Fragment f) {
		fragments_.add(f);
	}

	public Fragment get(int i) {
		return fragments_.get(i);
	}

	public int size() {
		return fragments_.size();
	}

	public SimpleStructure getStructure() {
		return structure_;
	}

	public List<Fragment> getList() {
		return fragments_;
	}

	public void sample(int max) {
		fragments_ = new ArrayList<>(fragments_.subList(0, max));
	}
}
