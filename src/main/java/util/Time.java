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
				System.out.println(s + " " + t.getMiliseconds() + " ms");
			}
		}
	}

	public static Time get(String name) {
		return timers.get(name);
	}

	public void start() {
		timeA = System.nanoTime();
	}

	public void stop() {
		timeB = System.nanoTime();
	}

	public long getMiliseconds() {
		long time = (timeB - timeA) / 1000000;
		return time;
	}

	private long getNanoseconds() {
		long time = (timeB - timeA);
		return time;
	}

	private long getMicroseconds() {
		long time = (timeB - timeA) / 1000;
		return time;
	}

}
