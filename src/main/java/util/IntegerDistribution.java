package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Antonin Pavelka
 */
public class IntegerDistribution {

	private Map<Integer, Integer> frequenciesBySize = new TreeMap<>();

	public void add(int itemSize) {
		Integer value = frequenciesBySize.get(itemSize);
		if (value == null) {
			frequenciesBySize.put(itemSize, 1);
		} else {
			frequenciesBySize.put(itemSize, value + 1);
		}
	}

	public void print() {
		List<Item> items = new ArrayList<>();
		for (Integer size : frequenciesBySize.keySet()) {
			int frequency = frequenciesBySize.get(size);
			Item item = new Item(size, frequency);
			items.add(item);
		}
		Collections.sort(items);
		System.out.println("Frequencies:");
		for (Item item : items) {
			System.out.println(item);
		}
		System.out.println("---");
	}
}

class Item implements Comparable<Item> {

	private int size;
	private int frequency;

	public Item(int size, int frequency) {
		this.size = size;
		this.frequency = frequency;
	}

	public int compareTo(Item other) {
		int c = Integer.compare(frequency, other.frequency);
		if (c == 0) {
			return Integer.compare(size, other.size);
		} else {
			return c;
		}
	}

	public String toString() {
		return size + ":" + frequency;
	}
}
