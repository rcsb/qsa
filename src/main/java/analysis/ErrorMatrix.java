package analysis;

public class ErrorMatrix {

    private int tp, tn, fp, fn;
    private double total;

    public ErrorMatrix(int tp, int tn, int fp, int fn) {
        this.tp = tp;
        this.tn = tn;
        this.fp = fp;
        this.fn = fn;
        this.total = tp + tn + fp + fn;
    }

    public double inside() {
        return (double) (tp + fp) / total;
    }

    public double specificity() {
        return (double) tp / (tp + fp);
    }

    
    public double missing() {
        return (double) fn / (tp + fn);
    }

    public double p() {
        return (tp + fn) / total;
    }
}
