package pdb;

import geometry.Point;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Locale;

/*
 * Represents a line of a PDB file. For information about PDB files, see PDB
 * file format guide. Contains data from ATOM or HETATM line of PDB file.
 *
 * The class provides parsing of PDB line from String and then saving the
 * information into String.
 *
 * COLUMNS DATA TYPE FIELD DEFINITION
 * --------------------------------------------------------------------------------
 * 1 - 6 Record name "HETATM"
 *
 * 7 - 11 Integer serial Atom serial number.
 *
 * 13 - 16 Atom name Atom name.
 *
 * 17 Character altLoc Alternate location indicator.
 *
 * 18 - 20 Residue name resName Residue name.
 *
 * 22 Character chainID Chain identifier.
 *
 * 23 - 26 Integer resSeq Residue sequence number.
 *
 * 27 AChar iCode Code for insertion of residues.
 *
 * 31 - 38 Real(8.3) x Orthogonal coordinates for X.
 *
 * 39 - 46 Real(8.3) y Orthogonal coordinates for Y.
 *
 * 47 - 54 Real(8.3) z Orthogonal coordinates for Z.
 *
 * 55 - 60 Real(6.2) occupancy Occupancy.
 *
 * 61 - 66 Real(6.2) tempFactor Temperature factor.
 *
 * 73 - 76 LString(4) segID Segment identifier; left-justified.
 *
 * 77 - 78 LString(2) element Element symbol; right-justified.
 *
 * 79 - 80 LString(2) charge Charge on the atom.
 *
 *
 */
public class PdbLine {

    private boolean heteroAtom_;
    private int atomSerialNumber_;
    // atom name described in PDB file format guide
    private String atomName_;
    String afterChemicalSymbol_; // can be further parsed decomposed and
    // used to identify specific atoms in amino acids or nucleotides
    private char alternateLocationIndicator_;
    private String residueName_;
    private char chainId_;
    private String residueSequenceNumber_; // String becaus of Gromacs
    // 9999 is not sufficient for water boxes in simulations, so non-digit
    // characters are also used
    private char insertionCode_;
    private double x_, y_, z_;
    private double r_; // for PQR export
    private Double occupancy_;
    private Double temperatureFactor_;
    private String segmentId_;
    private String charge_;
    String chemicalSymbol_; // derived from atom name
    String elementSymbol_; // from dedicated columns 77-78
    // end of line properties
    private String line_;
    // derived
    private String element_; // final chemical element symbol
    private ResidueId residueId_;
    private String atomNamePrefix_;
    private static Locale locale_ = Locale.ENGLISH;

    public static String getConnectString(int a, int b) {
    	return String.format("CONECT%5d%5d", a, b);    	
    }
    
    private PdbLine() {
    }

    public PdbLine(
            int atomSerialNumber, String atomName, String element,
            String residueName, String residueSequenceNumber, char chainId,
            double x, double y, double z) {

        if (99999 < atomSerialNumber) {
            atomSerialNumber = 99999;
        }

        switch (atomName.length()) {
            case 1:
                atomName = " " + atomName + "  ";
                break;
            case 2:
                atomName = " " + atomName + " ";
                break;
            case 3:
                atomName = " " + atomName;
                break;
            default:
                break;
        }

        heteroAtom_ = false;
        atomSerialNumber_ = atomSerialNumber;

        atomName_ = atomName;
        element_ = element;
        //setAtomName(atomName);

        alternateLocationIndicator_ = ' ';
        residueName_ = residueName;
        chainId_ = chainId;

        residueSequenceNumber_ = residueSequenceNumber;
        insertionCode_ = ' ';
        x_ = x;
        y_ = y;
        z_ = z;
        occupancy_ = null;
        temperatureFactor_ = null;
        segmentId_ = null;
        charge_ = null;

        residueId_ = new ResidueId(chainId_, residueSequenceNumber_,
                insertionCode_);

        //initElement();
    }

    public PdbLine(String line) {
        line_ = line;
        String recordName = sub(1, 6).trim();
        if (recordName.startsWith("HETATM")) {
            heteroAtom_ = true;
        } else if (recordName.startsWith("ATOM")) {
            heteroAtom_ = false;
        } else {
            throw new RuntimeException("PdbLine can be used only on ATOM or"
                    + " HETATM lines.");
        }
        String serial = sub(7, 11).trim();
        try {
            atomSerialNumber_ = new Integer(serial);
        } catch (NumberFormatException e) {
            try {
                BigInteger bi = new BigInteger(serial, Character.MAX_RADIX);
                atomSerialNumber_ = bi.intValue();
            } catch (NumberFormatException ex) {
                throw new RuntimeException(line, ex);
            }
        }
        setAtomName(sub(13, 16));
        alternateLocationIndicator_ = sub(17);
        if (true) { // use long names
            residueName_ = sub(18, 21).trim();
        } else {
            residueName_ = sub(18, 20).trim();
        }
        chainId_ = sub(22);
        residueSequenceNumber_ = sub(23, 26).trim();
        insertionCode_ = sub(27);
        x_ = new Double(sub(31, 38).trim());
        y_ = new Double(sub(39, 46).trim());
        z_ = new Double(sub(47, 54).trim());
        if (60 <= line.length()) {
            occupancy_ = getDouble(sub(55, 60));
            if (66 <= line.length()) {
                temperatureFactor_ = getDouble(sub(61, 66));
                if (76 <= line.length()) {
                    segmentId_ = sub(73, 76);
                    // LString, spacing is important
                    if (79 <= line.length()) {
                        elementSymbol_ = sub(77, 78);
                        // LString, spacing is important
                        if (80 <= line.length()) {
                            charge_ = sub(79, 80);
                            // LString, spacing is important
                        }
                    }
                }
            }
        }
        this.line_ = null;

        initElement();

        residueId_
                = new ResidueId(chainId_, residueSequenceNumber_, insertionCode_);

    }

    public static boolean isCoordinateLine(String line) {
        return line.startsWith("ATOM") || line.startsWith("HETATM");
    }

    public static boolean isModel(String line) {
        return line.startsWith("MODEL");
    }

    public static String getModelString(int model) {
        return "MODEL        " + model;
    }

    public static String getEndmdlString() {
        return "ENDMDL";
    }

    public static boolean isEndmdl(String line) {
        return line.startsWith(getEndmdlString());
    }

    public final void setAtomName(String name) {
        atomName_ = name;
        String s;
        if (Character.isDigit(atomName_.charAt(0))) { // 1HD1 LEU
            s = atomName_.substring(1);
            atomNamePrefix_ = atomName_.substring(0, 1);
        } else {
            s = atomName_;
        }
        atomName_ = atomName_.trim();

        if ('H' == s.charAt(0)) {
            chemicalSymbol_ = "H";
            afterChemicalSymbol_ = s.substring(1).trim();
        } else if (1 == s.length()) {
            chemicalSymbol_ = s;
            afterChemicalSymbol_ = "";
        } else {
            chemicalSymbol_ = s.substring(0, 2).trim();
            afterChemicalSymbol_ = s.substring(2);
        }

    }

    public final void setInsertionCode(char c) {
        insertionCode_ = c;
    }

    private Double getDouble(String s) {
        if (0 == s.trim().length()) {
            return null;
        } else {
            return new Double(s);
        }

    }

    public String getAtomNamePrefix() {
        return atomNamePrefix_;
    }

    private void initElement() {
        if (null == elementSymbol_ || 0 == elementSymbol_.trim().length()) {
            element_ = chemicalSymbol_;
        } else {
            element_ = elementSymbol_.trim();
        }
    }

    @Override
    public String toString() {
        return getPdbString();
    }

    /*
     * Constructs and returns an ATOM or HETATM line of PDB file.
     */
    public String getPdbString() {
        char[] spaces = new char[80];
        Arrays.fill(spaces, ' ');
        StringBuilder sb = new StringBuilder(new String(spaces));

        if (isHeteroAtom()) {
            printLeft("HETATM", 1, 6, sb);
        } else {
            printLeft("ATOM", 1, 6, sb);
        }

        int serial = 99999;
        if (atomSerialNumber_ < serial) {
            serial = atomSerialNumber_;
        }
        printRight(Integer.toString(serial), 7, 11, sb);

        if (("H".equals(getElementSymbol())
                && atomName_.trim().length() == 4)
                || Character.isDigit(atomName_.charAt(0))
                || getElementSymbol().length() == 2) {
            printLeft(atomName_, 13, 16, sb);
        } else {
            printLeft(atomName_, 14, 16, sb);
        }

        printLeft(Character.toString(alternateLocationIndicator_), 17, 17, sb);
        printRight(residueName_, 18, 20, sb);
        printLeft(Character.toString(chainId_), 22, 22, sb);

        String resiString = residueSequenceNumber_;
        try {
            int resi = Integer.parseInt(residueSequenceNumber_);
            if (9999 < resi) {
                resi = 9999;
            }
            resiString = String.valueOf(resi);
        } catch (Exception e) {
        }
        printRight(resiString, 23, 26, sb);
        printLeft(Character.toString(insertionCode_), 27, 27, sb);
        if (x_ < -999.999) {
            x_ = -999.999;
        }
        if (y_ < -999.999) {
            y_ = -999.999;
        }
        if (z_ < -999.999) {
            z_ = -999.999;
        }
        if (9999.999 < x_) {
            x_ = 9999.999;
        }
        if (9999.999 < y_) {
            y_ = 9999.999;
        }
        if (9999.999 < z_) {
            z_ = 9999.999;
        }

        printRight(String.format(locale_, "%8.3f", x_), 31, 38, sb);
        printRight(String.format(locale_, "%8.3f", y_), 39, 46, sb);
        printRight(String.format(locale_, "%8.3f", z_), 47, 54, sb);
        if (null != occupancy_) {
            printRight(String.format(locale_, "%6.2f", occupancy_), 55, 60, sb);
        }
        if (null != temperatureFactor_) {
            if (999.99 < temperatureFactor_) {
                temperatureFactor_ = 999.99;
            }
        }
        if (null != temperatureFactor_) {
            printRight(String.format(locale_, "%6.2f", temperatureFactor_),
                    61, 66, sb);
        }
        if (null != segmentId_) {
            printLeft(segmentId_, 73, 76, sb);
        }
        if (null != elementSymbol_) {
            printRight(elementSymbol_, 77, 78, sb);
        }
        if (null != charge_) {
            printLeft(charge_, 79, 80, sb);
        }

        return sb.toString();

    }

    public String getPqrString() {

        Locale locale = null;
        char[] spaces = new char[80];
        Arrays.fill(spaces, ' ');
        StringBuilder sb = new StringBuilder(new String(spaces));

        if (isHeteroAtom()) {
            printLeft("HETATM", 1, 6, sb);
        } else {
            printLeft("ATOM", 1, 6, sb);
        }

        printRight(Integer.toString(atomSerialNumber_), 7, 11, sb);
        if (("H".equals(getElementSymbol())
                && atomName_.length() == 4)
                || Character.isDigit(atomName_.charAt(0))
                || getElementSymbol().length() == 2) {
            printLeft(atomName_, 13, 16, sb);
        } else {
            printLeft(atomName_, 14, 16, sb);
        }

        printLeft(Character.toString(alternateLocationIndicator_), 17, 17, sb);
        printRight(residueName_, 18, 20, sb);
        printLeft(Character.toString(chainId_), 22, 22, sb);
        printRight(residueSequenceNumber_, 23, 26, sb);
        printLeft(Character.toString(insertionCode_), 27, 27, sb);
        if (x_ < -999.999) {
            x_ = -999.999;
        }
        if (y_ < -999.999) {
            y_ = -999.999;
        }
        if (z_ < -999.999) {
            z_ = -999.999;
        }
        if (9999.999 < x_) {
            x_ = 9999.999;
        }
        if (9999.999 < y_) {
            y_ = 9999.999;
        }
        if (9999.999 < z_) {
            z_ = 9999.999;
        }

        printRight(String.format(locale, "%8.3f", x_), 31, 38, sb);
        printRight(String.format(locale, "%8.3f", y_), 39, 46, sb);
        printRight(String.format(locale, "%8.3f", z_), 47, 54, sb);

        double charge = 0.0;
        if (null != charge_) {
            charge = Double.parseDouble(charge_);
        }
        printRight(String.format(locale, "%1.4f", charge), 56, 62, sb);

        double radius = r_;
        if (radius < 0) {
            radius = 0;
        }
        if (100 <= radius) {
            radius = 99.999;
        }
        if (10 <= radius) {
            printRight(String.format(locale, "%2.3f", radius), 64, 69, sb);
        } else {
            printRight(String.format(locale, "%1.4f", radius), 64, 69, sb);
        }

        return sb.toString();

    }

    private void printLeft(String s, int first, int last, StringBuilder sb) {
        first -= 1;
        last -= 1;
        s = s.trim();
        if (last - first + 1 < s.length()) {
            throw new RuntimeException(s + " " + first + " " + last);
        }
        last = Math.min(last, first + s.length() - 1);
        if (s.length() > last - first + 1) {
            throw new RuntimeException(s + " " + first + " " + last);
        }
        for (int i = 0; i < s.length(); i++) {
            sb.setCharAt(first + i, s.charAt(i));
        }
    }

    private void printRight(String s, int first, int last, StringBuilder sb) {
        first -= 1;
        last -= 1;
        s = s.trim();
        if (last - first + 1 < s.length()) {
            throw new RuntimeException(s + " " + first + " " + last);
        }
        for (int i = s.length() - 1; 0 <= i; i--) {
            sb.setCharAt(last - s.length() + 1 + i, s.charAt(i));
        }

    }

    private String sub(int first, int last) {
        return line_.substring(first - 1, last);

    }

    private char sub(int index) {
        return line_.charAt(index - 1);

    }

    public int getAtomSerialNumber() {
        return atomSerialNumber_;
    }

    public String getAtomName() {
        return atomName_;
    }

    public char getAlternateLocationIndicator() {
        return alternateLocationIndicator_;
    }

    public String getResidueName() {
        return residueName_;
    }

    public double getX() {
        return x_;
    }

    public double getY() {
        return y_;
    }

    public double getZ() {
        return z_;
    }

    public void setX(double x) {
        x_ = x;
    }

    public void setY(double y) {
        y_ = y;
    }

    public void setZ(double z) {
        z_ = z;
    }

    public void setR(double r) {
        r_ = r;
    }

    public boolean isHeteroAtom() {
        return heteroAtom_;
    }

    public Double getOccupancy() {
        return occupancy_;
    }

    public Double getTemperatureFactor() {
        return temperatureFactor_;
    }

    public String getSegmentId() {
        return segmentId_;
    }

    public String getCharge() {
        return charge_;
    }

    public String getElementSymbol() {
        return element_;
    }

    public ResidueId getResidueId() {
        return residueId_;
    }

    public char getChainId() {
        return chainId_;
    }

    public void setOccupancy(Double occupancy) {
        this.occupancy_ = occupancy;
    }

    public void setTemperatureFactor(Double temperatureFactor) {
        this.temperatureFactor_ = temperatureFactor;
    }

    public void setSegmentId_(String segmentId) {
        this.segmentId_ = segmentId;
    }

    public void setCharge_(String charge) {
        this.charge_ = charge;
    }

    public void setAtomSerialNumber(int n) {
        atomSerialNumber_ = n;
    }

    public Point getPoint() {
        return new Point(getX(), getY(), getZ());
    }

    public void setResidueId(ResidueId ri) {
        this.residueId_ = ri;
        this.chainId_ = ri.getChain();
        this.residueSequenceNumber_ = ri.getSequenceNumber();
        this.insertionCode_ = ri.getInsertionCode();
    }
}
