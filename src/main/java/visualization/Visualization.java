package visualization;

import fragments.Biword;
import fragments.FragmentPair;
import geometry.Point;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import javax.vecmath.Point3d;
import pdb.PdbLine;

/**
 *
 * @author Antonin Pavelka
 */
public class Visualization {

    public static void visualize(Collection<FragmentPair> pairs, File file) {
        try {
            int serial = 1;
            int resi = 1;
            int pairName = 1;
            PrintStream ps = new PrintStream(new FileOutputStream(file));
            for (FragmentPair pair : pairs) {
                Biword[] fs = pair.get();
                for (Biword f : fs) {
                    for (Point p : f.getPoints()) {
                        PdbLine pdbLine = new PdbLine(
                                serial, "H", "H", pairName + "", resi + "", 'A',
                                p.x, p.y, p.z);

                        ps.println(pdbLine.getPdbString());
                        serial++;
                    }
                    resi++;
                }
                if (pairName < 999) {
                    pairName++;
                }
            }
            ps.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void visualize(Point3d[] points, char chain, File file) {
        try {
            int serial = 1;
            int resi = 1;
            int pairName = 1;
            PrintStream ps = new PrintStream(new FileOutputStream(file));
            for (Point3d p : points) {
                PdbLine pdbLine = new PdbLine(
                        serial, "H", "H", pairName + "", resi + "", chain,
                        p.x, p.y, p.z);
                ps.println(pdbLine.getPdbString());
                serial++;
                resi++;
            }
            ps.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
