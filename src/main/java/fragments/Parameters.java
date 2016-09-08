package fragments;

import java.io.Serializable;

/**
 *
 * @author Antonin Pavelka
 */
public class Parameters implements Serializable {

    public int getWordLength() {
        return 10;
    }

    public double getSeqSep() {
        return getWordLength();
    }

    public double getResidueContactDistance() {
        return 20;
    }
}
