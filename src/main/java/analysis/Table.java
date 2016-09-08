package analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class Table {

    private List<List<Double>> table = new ArrayList<>();
    private Map<Integer, Double> hi_ = new HashMap<>();
    private Map<Integer, Double> lo_ = new HashMap<>();
    private List<String> lines = new ArrayList<>();

    public Table() {
    }

    public Table(File f) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            while ((line = br.readLine()) != null) {
                add(line);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setFilter(int high, int row, double threshold) {
        if (high == 1) {
            hi_.put(row, threshold);
        } else {
            lo_.put(row, threshold);
        }
    }

    public void resetFilters() {
        hi_.clear();
        lo_.clear();
    }

    public List<String> getFiltered() {
        List<String> filtered = new ArrayList<>();
        for (int i = 0; i < table.size(); i++) {
            List<Double> line = table.get(i);
            boolean include = true;
            for (int row = 0; row < line.size(); row++) {
                double l = Double.NEGATIVE_INFINITY;
                double h = Double.POSITIVE_INFINITY;
                if (lo_.containsKey(row)) {
                    l = lo_.get(row);
                }
                if (hi_.containsKey(row)) {
                    h = hi_.get(row);
                }
                double value = line.get(row);
                include &= l <= value && value <= h;
            }
            if (include) {
                filtered.add(lines.get(i));
            }
        }
        return filtered;
    }

    public void add(String sLine) {
        StringTokenizer st = new StringTokenizer(sLine, " \t;,");
        List<Double> line = new ArrayList<>();
        //st.nextToken();
        //st.nextToken();
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            double d = 8888;
            try {
                d = Double.parseDouble(s);
            } catch (Exception ex) {
            }
            line.add(d);
        }
        table.add(line);
        lines.add(sLine);
    }

    public void sort(int i) {
        Collections.sort(table, new RowComparator(i));
    }

    public List<List<Double>> get() {
        return table;

    }
}

class RowComparator implements Comparator<List<Double>> {

    int col;

    public RowComparator(int c) {
        col = c;
    }

    public int compare(List<Double> a, List<Double> b) {
        return -a.get(col).compareTo(b.get(col));
    }
}
