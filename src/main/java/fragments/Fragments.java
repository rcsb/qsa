package fragments;

import java.io.File;
import java.io.Serializable;

import pdb.SimpleStructure;
import util.pymol.PymolVisualizer;

/**
 *
 * @author Antonin Pavelka
 */
public final class Fragments implements Serializable {

    private static final long serialVersionUID = 1L;
    private Fragment[] fragments;
    private SimpleStructure structure;
    private Word[] words;

    protected Fragments(SimpleStructure structure, Fragment[] fragments, Word[] words) {
        this.structure = structure;
        this.fragments = fragments;
        this.words = words;
    }
    
    public Fragment get(int i) {
        return fragments[i];
    }

    public Word[] getWords() {
        return words;
    }

    public int size() {
        return fragments.length;
    }

    public SimpleStructure getStructure() {
        return structure;
    }

    /*public void sample(int max) {
        fragments = new ArrayList<>(fragments.subList(0, max));
    }*/

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
