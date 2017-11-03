package global.io;

import java.io.PrintStream;

/**
 *
 * @author Antonin Pavelka
 */
public class Printer {

    private static PrintStream out = System.out;

    public static void println(String line) {
        out.println(line);
    }

    public static void print(String s) {
        out.print(s);
    }

    public static void println() {
        out.println();
    }

}
