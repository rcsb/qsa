package fragment;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import embedding.lipschitz.object.AlternativePointTuples;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Antonin Pavelka
 */
public class ObjectSample implements Iterable<AlternativePointTuples> {

	private List<AlternativePointTuples> fragments = new ArrayList<>();
	private final Kryo kryo = new Kryo();

	public ObjectSample() {

	}

	public ObjectSample(List<AlternativePointTuples> fragments) {
		this.fragments.addAll(fragments);
	}

	public List<AlternativePointTuples> getList() {
		return new ArrayList<>(fragments);
	}

	public AlternativePointTuples[] getArray() {
		AlternativePointTuples[] array = new AlternativePointTuples[fragments.size()];
		fragments.toArray(array);
		return array;
	}

	public void add(AlternativePointTuples fragment) {
		fragments.add(fragment);
	}

	public void save(File file) {
		try (Output output = new Output(new FileOutputStream(file))) {
			kryo.writeObject(output, fragments);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void load(File file) {
		try (Input input = new Input(new FileInputStream(file))) {
			this.fragments = kryo.readObject(input, fragments.getClass());
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public Iterator<AlternativePointTuples> iterator() {
		return fragments.iterator();
	}

	public int size() {
		return fragments.size();
	}

	public AlternativePointTuples get(int i) {
		return fragments.get(i);
	}

	public void subsample(Random random, int sampleSize) {
		Collections.shuffle(fragments, random);
		fragments = fragments.subList(0, Math.min(fragments.size(), sampleSize));
	}
}
