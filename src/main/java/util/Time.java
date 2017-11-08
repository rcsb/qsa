package util;

import java.util.HashMap;
import java.util.Map;

public class Time {

	private long timeA, timeB = -1;

	private static Map<String, Time> timers = new HashMap<>();

	public static void start(String name) {
		Time timer = new Time();
		timers.put(name, timer);
		timer.start();
	}

	public static void stop(String name) {
		Time timer = timers.get(name);
		timer.stop();
	}

	public static void print() {
		for (String s : timers.keySet()) {
			Time t = timers.get(s);
			if (t.timeB != -1) {
				System.out.println(s + " " + t.get() + " ms");
			}
		}
	}

	public void start() {
		timeA = System.nanoTime();
	}

	public void stop() {
		timeB = System.nanoTime();
	}

	private long get() {
		long time = (timeB - timeA) / 1000000;
		return time;
	}

	private long getNano() {
		long time = (timeB - timeA);
		return time;
	}

	private long getMicro() {
		long time = (timeB - timeA) / 1000;
		return time;
	}

	private double seconds() {
		double t = (double) get();
		return t / 1000;
	}

}
