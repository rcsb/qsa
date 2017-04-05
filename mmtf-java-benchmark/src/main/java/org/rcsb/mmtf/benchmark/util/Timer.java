package org.rcsb.mmtf.benchmark.util;

import java.util.HashMap;
import java.util.Map;

public class Timer {

	private long timeA, timeB;

	private static Map<String, Timer> timers = new HashMap<>();

	public static void start(String name) {
		Timer timer = new Timer();
		timers.put(name, timer);
		timer.start();
	}

	public static void stop(String name) {
		Timer timer = timers.get(name);
		timer.stop();
	}

	public static void print() {
		for (String s : timers.keySet()) {
			Timer t = timers.get(s);
			System.out.println(s + " " + t.seconds() + " s " + t.get() + " ms");
		}
	}

	public void start() {
		timeA = System.nanoTime();
	}

	public void stop() {
		timeB = System.nanoTime();
	}

	public long get() {
		long time = (timeB - timeA) / 1000000;
		return time;
	}

	private long getNano() {
		long time = (timeB - timeA);
		return time;
	}

	public long getMicro() {
		long time = (timeB - timeA) / 1000;
		return time;
	}

	private double seconds() {
		double t = (double) get();
		return t / 1000;
	}

}
