package util;

public class Timer {

    private static long timeA, timeB;

    public static void start() {
        timeA = System.nanoTime();
    }

    public static void stop() {
        timeB = System.nanoTime();
    }

    public static long get() {
        long time = (timeB - timeA) / 1000000;
        return time;
    }

    public static long getNano() {
        long time = (timeB - timeA);
        return time;
    }

}
