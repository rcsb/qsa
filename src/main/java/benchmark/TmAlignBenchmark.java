package benchmark;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import pdb.MmtfStructureProvider;
import pdb.SimpleChain;
import pdb.SimpleStructure;
import spark.interfaces.StructureSetProvider;

public class TmAlignBenchmark implements StructureSetProvider {

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

	public SimpleStructure[] get() {
		int count = 0;
		SimpleStructure[] structures = new SimpleStructure[chainIds.size()];
		for (int i = 0; i < chainIds.size(); i++) {
			String id = chainIds.get(i);
			try {
				String code = id.substring(0, 4);
				char chain = id.charAt(4);
				SimpleStructure ss = provider.getStructure(code);
				ss.removeChainsExcept(ss.getChainIdWithName(chain));
				structures[i] = ss;
				System.out.println(code + " " + chain + " " + structures[i].size());
				count++;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println(count + " chains loaded.");
		return structures;
	}
}
