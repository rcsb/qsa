package fragments;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;

import geometry.Point;
import pdb.Residue;
import statistics.Statistics;

/**
 *
 * @author Antonin Pavelka
 */
public class Word implements Serializable, WordInterface {

    private static final long serialVersionUID = 1L;
    private final Residue[] residues_;
    private float[] intDist_;
    private Point center;
    private final int id;
    private final Point3d[] points;

    public Word(int id, List<Residue> residues) {
        residues_ = new Residue[residues.size()];
        residues.toArray(residues_);
        computeInternalDistances();
        this.id = id;
        points = getPoints3d();
    }

    public float[] getInternalDistances() {
        return intDist_;
    }

    public double getEuclidean(Word other) {
        double sum = 0;
        for (int i = 0; i < intDist_.length; i++) {
            double d = intDist_[i] - other.intDist_[i];
            sum += d * d;
        }
        return Math.sqrt(sum / intDist_.length);
    }

    public double getManhattan(Word other) {
        double sum = 0;
        for (int i = 0; i < intDist_.length; i++) {
            double d = intDist_[i] - other.intDist_[i];
            sum += Math.abs(d);
        }
        return sum / intDist_.length;
    }

    public double getChebyshev(Word other) {
        double max = 0;
        for (int i = 0; i < intDist_.length; i++) {
            max = Math.max(max, Math.abs(intDist_[i] - other.intDist_[i]));
        }
        return max;
    }

    public int getId() {
        return id;
    }

    private void computeInternalDistances() {
        intDist_ = new float[residues_.length * (residues_.length - 1) / 2];
        int i = 0;
        for (int x = 0; x < residues_.length; x++) {
            for (int y = 0; y < x; y++) {
                Point a = residues_[x].getPosition();
                Point b = residues_[y].getPosition();
                intDist_[i++] = (float) a.distance(b);
            }
        }
    }

    public int seqDist(Word other) {
        int d = Integer.MAX_VALUE;
        for (Residue x : residues_) {
            for (Residue y : other.residues_) {
                int diff = Math.abs(x.getIndex().getSequenceNumber() - y.getIndex().getSequenceNumber());
                if (diff < d) {
                    d = diff;
                }
            }
        }
        return d;
    }

    public boolean isInContact(Word other, double threshold) {
        Residue a1 = residues_[0];
        Residue a2 = residues_[residues_.length - 1];
        Residue b1 = other.residues_[0];
        Residue b2 = other.residues_[residues_.length - 1];

        int n1 = a1.getId().getSequenceNumber();
        int n2 = a2.getId().getSequenceNumber();
        int m1 = b1.getId().getSequenceNumber();
        int m2 = b2.getId().getSequenceNumber();
        if ((n1 <= m1 && m1 <= n2) || (n1 <= m2 && m2 <= n2) || (m1 <= n1 && n1 <= m2) || (m1 <= n2 && n2 <= m2)) {
            return false;
        } else {
            for (int x = 0; x < residues_.length; x++) {
                for (int y = 0; y < other.residues_.length; y++) {
                    double d = residues_[x].distance(other.residues_[y]);
                    if (d <= threshold) {
                        return true;
                    }
                }
            }
            // return (a1.distance(b1) <= threshold && a2.distance(b2) <=
            // threshold)
            // || (a1.distance(b2) <= threshold && a2.distance(b1) <=
            // threshold);
        }
        return false;
    }

    public final Point[] getPoints() {
        Point[] points = new Point[residues_.length];
        for (int i = 0; i < residues_.length; i++) {
            points[i] = residues_[i].getPosition();
        }
        return points;
    }

    public final Point3d[] getPoints3d() {
        Point3d[] points = new Point3d[residues_.length];
        for (int i = 0; i < residues_.length; i++) {
            points[i] = residues_[i].getPosition3d();
        }
        return points;
    }

    public Residue[] getResidues() {
        return residues_;
    }

    public Point getCenter() {
        if (center == null) {
            Point sum = new Point(0, 0, 0);
            Point[] ps = getPoints();
            for (Point p : ps) {
                sum = sum.plus(p);
            }
            center = sum.divide(ps.length);
        }
        return center;
    }

    public void print() {
        for (Residue r : residues_) {
            System.out.print(r.getId().toString() + " ");
        }
        System.out.println();
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        Word other = (Word) o;
        return id == other.id;
    }

    public String toString() {
        return Integer.toString(id);
    }
}
