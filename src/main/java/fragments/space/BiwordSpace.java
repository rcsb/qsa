package fragments.space;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import fragments.vector.*;
import fragments.Biword;
import fragments.Biwords;
import fragments.BiwordsFactory;
import fragments.WordImpl;
import io.Directories;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import pdb.SimpleStructure;
import pdb.StructureFactory;

/**
 *
 * @author Antonin Pavelka
 */
public class BiwordSpace {

	private static Random random = new Random(1);
	private static Directories dirs = Directories.createDefault();

	private List<SimpleStructure> structures = new ArrayList<>();
	private List<WordImpl> a = new ArrayList<>(); // biword
	private List<WordImpl> b = new ArrayList<>(); // biword

	public BiwordSpace() {
	}

	public void readStructures() {
		int pdbCounter = 0;
		PdbDataset pd = new PdbDataset();
		List<String> ids = pd.loadAll();
		System.out.println(ids.size() + " PDB codes");
		for (String id : ids) {
			try {
				StructureFactory provider = new StructureFactory(dirs);
				SimpleStructure ss = StructureFactory.convert(provider.getSingleChain(id), id); // !!! single
				if (ss.size() <= 10000) {
					structures.add(ss);
				} else {
					System.out.println("avoiging large structure " + id + " " + ss.size());
				}
				System.out.println((pdbCounter++) + " / " + ids.size());
				if (pdbCounter > 10) {
					return;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			} catch (Error er) {
				er.printStackTrace();
			}
		}
	}

	public void createBiwords() {
		BiwordsFactory bf = new BiwordsFactory();
		for (int i = 0; i < structures.size(); i++) {
			SimpleStructure s = structures.get(i);
			Biwords bs = bf.create(s, 10, 1);
			for (Biword bw : bs.getBiwords()) {
				WordImpl[] pair = bw.getWords();
				a.add(pair[0]);
				b.add(pair[1]);
			}
			System.out.println("biwords: " + a.size());
		}
	}

	public void save() {
		Kryo kryo = new Kryo();
		try (FileOutputStream fos = new FileOutputStream(dirs.getBiwordSpace())) {
			Output output = new Output(fos);
			kryo.writeObject(output, this);
			output.close();
		} catch (IOException ex) {
			throw new RuntimeException();
		}
	}

	public static BiwordSpace load() {
		Kryo kryo = new Kryo();
		try (FileInputStream fis = new FileInputStream(dirs.getBiwordSpace())) {
			Input input = new Input(fis);
			BiwordSpace o = kryo.readObject(input, BiwordSpace.class);
			input.close();
			return o;
		} catch (IOException ex) {
			throw new RuntimeException();
		}
	}

	public void run() {
		readStructures();
		createBiwords();
		save();
	}

	public static void main(String[] args) {
		BiwordSpace m = new BiwordSpace();		
		m.run();
		load();
	}

}
