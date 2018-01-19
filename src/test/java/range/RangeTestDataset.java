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
			keys[i] = b;
			values[i] = b;
		}
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

	public Set<Byte> getUniqueKeys() {
		Set<Byte> set = new HashSet<>();
		for (byte b : keys) {
			set.add(b);
		}
		return set;
	}

	public List<Integer> getRangeAcyclic(int[] range) {
		int min = range[0];
		int max = range[1];
		List<Integer> result = new ArrayList<>();
		for (int i = 0; i < keys.length; i++) {
			if (min <= keys[i] && keys[i] <= max) {
				result.add(values[i]);
			}
		}
		return result;
	}

}
