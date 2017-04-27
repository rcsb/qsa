package benchmark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pdb.ChainId;
import pdb.PdbChainId;

public class PdbListParser {
	/*public static PdbChainId[] readPdbCodes(File listFile, int first, int last) {
		try (Stream<String> stream = Files.lines(listFile.toPath())) {
			List<PdbChainId> list = stream.map(s -> s.substring(first, last)).map(String::toUpperCase)
					.map(s -> new PdbChainId(s)).collect(Collectors.toList());
			PdbChainId[] a = new PdbChainId[list.size()];
			list.toArray(a);
			return a;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}*/

	public static PdbChainId[] readPdbCodesFromFasta(File fasta) {
		List<PdbChainId> list = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(fasta))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith(">")) {
					PdbChainId c = new PdbChainId(line.substring(1, 5), new ChainId(line.charAt(6)));
					list.add(c);					
				}
			}
			PdbChainId[] a = new PdbChainId[list.size()];
			return list.toArray(a);			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
