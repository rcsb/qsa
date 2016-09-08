package statistics;


import java.util.List;

/**
 *
 * @author Antonin Pavelka
 */
public class Statistics {

    public static double difference(List<Double> as, List<Double> bs) {
        if (as.size() != bs.size()) {
            throw new IllegalArgumentException();
        }
        double sum = 0;
        for (int i = 0; i < as.size(); i++) {
            sum += Math.abs(as.get(i) - bs.get(i));
        }
        return sum / as.size();
    }
}
