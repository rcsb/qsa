package spark;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Class for controling output to the screen. Should be used instead of
 * System.out and System.err.
 */
public class Printer {

    public static final int IMP = 100;
    public static final int NORMAL = 20;
    public static final int ALL = 0;
    public static final int SOME = 10;
    private static int out = ALL; // print only messages of higher importance
    private static int in = 1; // default importance of incoming messages
    private static Integer oneTimeIn = -1;
    private static File file_;
    private static List<String> suggestions = new ArrayList<>();
    // storage of advises and suggestions for a user

    /*
     * Processes an advise or a suggestion for a user.
     */
    public static void suggests(String what) {
        suggestions.add(what);
    }

    public static void saveSuggestions(File file) throws IOException {
        if (0 < suggestions.size()) {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            for (String s : suggestions) {
                bw.write(s + "\n");
            }
            bw.close();
        }
    }

    public static void setFile(File file) {
        file_ = file;
    }

    public static void listenOnce(int p) {
        oneTimeIn = p;
    }

    public static void listen(int p) {
        out = p;
    }

    public static void print(String s) {
        System.out.print(s);
        if (null != file_) {
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(file_,
                        true));
                bw.write(s);
                bw.close();
            } catch (IOException e) {
                Logger.getLogger("caver").log(
                        Level.WARNING, "Log file error.", e);
            }
        }
    }

    public static void warn(String msg) {
        print(msg);
        Logger.getLogger("caver").log(
                Level.WARNING, msg);
    }

    public static void println(String s) {
        print(s + "\n");
    }

    public static void println(String s, int level) {
        listenOnce(level);
        print(s + "\n");
    }

    public static void printFile(File f, int level) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            while (null != (line = br.readLine())) {
                println(line, level);
            }
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void println() {
        print("\n");
    }

    public static void println(int i) {
        println("" + i);
    }

    public static void println(double d) {
        println("" + d);
    }

    public static void println(boolean b) {
        println("" + b);
    }

    /*
     * For printing that will be deleted from code.
     */
    public static void debug(String s) {
        println(s);
    }

    public static void out(String s) {
        println(s);
    }

    public static void error(String s) {
        println(s);
    }
}
