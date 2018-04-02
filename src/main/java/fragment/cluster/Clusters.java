package fragment.cluster;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import geometry.superposition.Superposer;
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
public class Clusters implements Iterable<Cluster> {

	private List<Cluster> clusters = new ArrayList<>();
	private Superposer superposer = new Superposer();
	private Kryo kryo = new Kryo();

	public void add(Cluster cluster) {
		clusters.add(cluster);
	}

	public List<Fragment> getRepresentants() {
		List<Fragment> list = new ArrayList<>();
		for (Cluster cluster : clusters) {
			list.add(cluster.getCentroid());
		}
		return list;
	}

	@Override
	public Iterator<Cluster> iterator() {
		return clusters.iterator();
	}

	public void shuffle(Random random) {
		Collections.shuffle(clusters, random);
	}

	public int size() {
		return clusters.size();
	}

	public void save(File file) {
		try (Output output = new Output(new FileOutputStream(file))) {
			kryo.writeObject(output, clusters);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void load(File file) {
		try (Input input = new Input(new FileInputStream(file))) {
			this.clusters = kryo.readObject(input, clusters.getClass());
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public Cluster get(int i) {
		return clusters.get(i);
	}
}
