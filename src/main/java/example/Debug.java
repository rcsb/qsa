package example;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.SortedSet;
import java.util.stream.Stream;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.rcsb.GetRepresentatives;
import org.rcsb.mmtf.api.StructureDataInterface;
import org.rcsb.mmtf.dataholders.MmtfStructure;
import org.rcsb.mmtf.decoder.GenericDecoder;
import org.rcsb.mmtf.decoder.ReaderUtils;
import org.rcsb.mmtf.serialization.MessagePackSerialization;

public class Debug {

	public static void main(String[] args) throws IOException, StructureException {
		SortedSet<String> pdbIds = GetRepresentatives.getAll();
		long origSize = pdbIds.size();
		long startTime = System.currentTimeMillis();
		Stream<String> stream = pdbIds.parallelStream().limit(1000);
		long counted = stream.map(t -> readStruct(t)).filter(t -> !t.equals("NULL")).count();
		long endTime = System.currentTimeMillis();
		long totTime = endTime - startTime;
		System.out.println(origSize + " looked for.");
		System.out.println(counted + " parsed.");
		System.out.println("Process took: " + totTime);
	}

	private static StructureDataInterface readStruct(String pdbId) {
		try {
			byte[] inputBytes = Files
					.readAllBytes(Paths.get("/Users/antonin/data/qsa/full_mmtf/full" + pdbId.toLowerCase() + ".mmtf.gz"));
			MessagePackSerialization mmtfBeanSeDeMessagePackImpl = new MessagePackSerialization();
			MmtfStructure deserie = mmtfBeanSeDeMessagePackImpl
					.deserialize(new ByteArrayInputStream(ReaderUtils.deflateGzip(inputBytes)));
			return new GenericDecoder(deserie);
		} catch (IOException e) {
			return null;
		}
	}
}