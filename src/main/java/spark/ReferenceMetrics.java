package spark;

/**
 *
 * @author Antonin Pavelka
 */
public class ReferenceMetrics {

    public final double tmScore;
    public final double rmsd;
    public final double probability;
    public final double alignmentLength;
    public final double coverage1;
    public final double coverage2;
    private static String s = ",";

    public ReferenceMetrics(double tmScore, double rmsd, double probability,
            double alignmentLength, double coverage1, double coverage2) {
        this.tmScore = tmScore;
        this.rmsd = rmsd;
        this.probability = probability;
        this.alignmentLength = alignmentLength;
        this.coverage1 = coverage1;
        this.coverage2 = coverage2;
    }

    public String getLine() {
        StringBuilder sb = new StringBuilder();
        sb.append(tmScore).append(s);
        sb.append(rmsd).append(s);
        sb.append(probability).append(s);
        sb.append(alignmentLength).append(s);
        sb.append(coverage1).append(s);
        sb.append(coverage2).append(s);
        return sb.toString();
    }
}
