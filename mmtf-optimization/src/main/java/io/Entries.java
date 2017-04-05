package org.rcsb.mmtf.db;

import io.LineFile;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Entries {

    private List<String> codes = new ArrayList<>();
    private List<String> types = new ArrayList<>();
    private List<String> experiments = new ArrayList<>();

    public Entries(File f) {
        LineFile lf = new LineFile(f);
        List<String> lines = lf.readLines();
        for (String line : lines) {
            StringTokenizer st = new StringTokenizer(line, " \t");
            String code = st.nextToken();
            String type = st.nextToken();
            String experiment = st.nextToken();
            if (type.equals("prot")) {
                codes.add(code);
                types.add(type);
                experiments.add(experiment);
            }
        }
    }

    public List<String> getCodes() {
        return codes;
    }

}
