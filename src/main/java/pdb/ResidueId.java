package pdb;

import java.io.Serializable;
import java.util.Objects;

/**
 * Identifies residue in context of PDB file. Defines alphabetical ordering
 * based on chain, serial number and insertion code. Thus it is possible to
 * create polymer (mostly protein, DNA and RNA) sequences, however those might
 * be shortened, as ATOM and HETATM records do not cover the whole sequence, but
 * only its part with solved structure.
 */
public class ResidueId implements Comparable<ResidueId>, Serializable {

    private char chain_;
    private String number_; // String allows more IDs in 4 digits of PDB files
    private Character insertion_;
    private final char EMPTY = ' ';

    /**
     * Serial number is a string (maybe hexa number) in Gromacs outputs. This is
     * againstPDB file format guide, but we need to read PDB files produced by
     * Gromacs.
     *
     * @param chain chain ID
     * @param number serial number
     * @param insertionCode insertion code
     */
    public ResidueId(char chain, String number, char insertionCode) {
        chain_ = Character.toUpperCase(chain);
        number_ = number;
        insertion_ = insertionCode;
    }

    public ResidueId(char chain, String number) {
        chain_ = Character.toUpperCase(chain);
        number_ = number;
        insertion_ = EMPTY;
    }

    public char getChain() {
        return chain_;
    }

    public void setChain(char c) {
        chain_ = c;
    }

    /*
     * See PDB file format guide, ATOM
     * http://www.bmsc.washington.edu/CrystaLinks/man/pdb/part_62.html
     */
    public String getSequenceNumber() {
        return number_;
    }

    /*
     * See PDB file format guide, ATOM
     * http://www.bmsc.washington.edu/CrystaLinks/man/pdb/part_62.html
     */
    public char getInsertionCode() {
        return insertion_;
    }

    @Override
    public String toString() {
        if (Character.isWhitespace(chain_)) {
            return number_ + "" + (insertion_ == EMPTY ? "" : insertion_);
        } else {
            return chain_ + ":" + number_ + "" + (insertion_ == EMPTY ? ""
                    : insertion_);
        }
    }

    public String getPdbString() {
        return number_ + "" + (insertion_ == EMPTY ? "" : insertion_);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ResidueId)) {
            return false;
        }
        ResidueId id = (ResidueId) o;
        if (chain_ != id.chain_) {
            return false;
        }
        boolean b = Objects.equals(insertion_, id.insertion_)
                && number_.equals(id.number_);
        return b;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + this.chain_;
        hash = 37 * hash + (this.number_ != null ? this.number_.hashCode() : 0);
        hash = 37 * hash + (this.insertion_ != null
                ? this.insertion_.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(ResidueId id) {
        int chainComparison = new Character(chain_).compareTo(id.chain_);
        if (0 != chainComparison) {
            return chainComparison;
        } else { // same chains
            // shorter number is smaller
            int numberComparison = new Integer(number_.length()).compareTo(
                    id.number_.length());
            if (0 == numberComparison) { // numbers have same lengths,
                // lexicographic comparison should equal to integer comparison
                numberComparison = number_.compareTo(id.number_);
            }
            if (0 != numberComparison) {
                return numberComparison;
            } else { // same chains and numbers
                return insertion_.compareTo(id.insertion_);
            }
        }
    }
}
