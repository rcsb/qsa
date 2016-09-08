package benchmark;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.biojava.nbio.structure.align.model.AFPChain;

import alignment.Fatcat;
import analysis.MySerializer;
import pdb.MmtfStructureProvider;
import pdb.SimpleChain;
import pdb.SimpleStructure;
import spark.interfaces.StructurePairsProvider;

public class TmAlignBenchmark implements StructurePairsProvider {

	private MmtfStructureProvider provider;
	private List<String> chainIds = new ArrayList<>();

	public TmAlignBenchmark(File codesFile, File data) {
		provider = new MmtfStructureProvider(data.toPath());
		readPdbCodes(codesFile.toPath());
	}

	private void readPdbCodes(Path ids) {
		try (Stream<String> stream = Files.lines(ids)) {
			chainIds = stream.map(String::toUpperCase).collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public SimpleChain[] getStructures() {
		int count = 0;
		SimpleChain[] structures = new SimpleChain[chainIds.size()];
		for (int i = 0; i < chainIds.size(); i++) {
			String id = chainIds.get(i);
			try {
				String code = id.substring(0, 4);
				char chain = id.charAt(4);
				SimpleStructure ss = provider.getStructure(code);
				structures[i] = ss.getChainByName(chain);
				System.out.println(code + " " + chain + " " + structures[i].size());
				count++;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println(count + " chains loaded.");
		return structures;
	}		

	public static void main(String args[]) {
		String home = "/Users/antonin/data/qsa/";
		Path ids = Paths.get(home + "/tm_benchmark.txt");
		Path data = Paths.get(home + "/mmtf");
		File fatcat = new File(home + "/tm_benchmark_fatcat");
		MySerializer s = new MySerializer(fatcat);
		TmAlignBenchmark b = new TmAlignBenchmark(ids.toFile(), data.toFile());
		SimpleChain[] structures = b.getStructures();
		for (int xi = 0; xi < 10; xi++) {
		//for (int xi = 0; xi < structures.length; xi++) {
			//for (int yi = 0; yi < xi; yi++) {
			for (int yi = 0; yi < 1; yi++) {  // !!!!!!!!!!!!!!!!!
				Fatcat fc = new Fatcat(structures[xi], structures[yi]);
				AFPChain ac = fc.align();
				s.serialize(ac);
			}
		}

		/*
		 * System.out.println("now to biojava:");
		 * 
		 * try {
		 * 
		 * long timeS = System.currentTimeMillis();
		 * 
		 * AtomCache cache = new AtomCache();
		 * 
		 * Structure s = cache.getStructure("4hhb");
		 * 
		 * long timeE = System.currentTimeMillis(); System.out.println("took " +
		 * ( timeE - timeS ) + " ms. to load structure");
		 * 
		 * System.out.println(s); } catch (Exception e){ e.printStackTrace(); }
		 */

		System.out.println("end.");
	}

}
