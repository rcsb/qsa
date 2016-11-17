package fragments;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import pdb.SimpleStructure;
import util.pymol.PymolVisualizer;

/**
 *
 * @author Antonin Pavelka
 */
public final class Fragments implements Serializable {

	private static final long serialVersionUID = 1L;
	private List<Fragment> fragments = new ArrayList<>();
	private SimpleStructure structure;
	private Word[] words;

	protected Fragments(SimpleStructure structure, Word[] words) {
		this.structure = structure;
		this.words = words;
	}

	public void add(Fragment f) {
		fragments.add(f);
	}

	public Fragment get(int i) {
		return fragments.get(i);
	}

	public Word[] getWords() {
		return words;
	}

	public int size() {
		return fragments.size();
	}

	public SimpleStructure getStructure() {
		return structure;
	}

	public List<Fragment> getList() {
		return fragments;
	}

	public void sample(int max) {
		fragments = new ArrayList<>(fragments.subList(0, max));
	}

	public void visualize(File py) {
		PymolVisualizer v = new PymolVisualizer();
		int i = 0;
		for (Fragment f : fragments) {
			v.addSelection(Integer.toString(i), f.getResidues());
			i++;
		}
		v.saveSelections(py);
	}
}
