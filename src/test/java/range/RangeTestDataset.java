package range;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author Antonin Pavelka
 */
public class RangeTestDataset {

	Random random = new Random(1);
	int bins;
	int size;
	byte[] keys;
	int[] values;

	public void setSize(int size) {
		this.size = size;
	}

	public void setBins(int bins) {
		this.bins = bins;
	}

	public void init() {
		keys = new byte[size];
		values = new int[size];
		for (int i = 0; i < size; i++) {
			byte b = (byte) random.nextInt(bins);
			if (b < bins) {
				keys[i] = b;
				values[i] = b;
			}
		}
		assert keys.length == values.length;
	}

	public int getValue(int i) {
		return values[i];
	}

	public byte getKey(int i) {
		return keys[i];
	}

	public int size() {
		return size;
	}

	public void print() {
		assert keys.length == values.length;
		for (int i = 0; i < keys.length; i++) {
			System.out.print(keys[i] + ":" + values[i] + " ");
		}
		System.out.println();
	}

	public Set<Byte> getUniqueKeys() {
		Set<Byte> set = new HashSet<>();
		for (byte b : keys) {
			set.add(b);
		}
		return set;
	}

	public List<Integer> getRangeOpen(int[] range) {
		Set<Byte> old = new HashSet<>();
		int min = range[0];
		int max = range[1];
		List<Integer> result = new ArrayList<>();
		for (int i = 0; i < keys.length; i++) {
			if (min <= keys[i] && keys[i] <= max && !old.contains(keys[i])) {
				result.add(values[i]);
				old.add(keys[i]);
			}
		}
		return result;
	}

}
