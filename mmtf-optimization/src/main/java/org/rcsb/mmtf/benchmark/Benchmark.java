package org.rcsb.mmtf.benchmark;

import io.Directories;
import io.HadoopReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.biojava.nbio.structure.HetatomImpl;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.io.mmtf.MmtfStructureReader;
import org.rcsb.mmtf.api.StructureDataInterface;
import org.rcsb.mmtf.dataholders.MmtfStructure;
import org.rcsb.mmtf.decoder.GenericDecoder;
import org.rcsb.mmtf.decoder.ReaderUtils;
import org.rcsb.mmtf.decoder.StructureDataToAdapter;
import profiling.Measurement;
import util.Timer;

/**
 *
 * @author Antonin Pavelka
 */
public class Benchmark {

	private Directories dirs;

	public Benchmark() {
		dirs = new Directories(new File("e:/data/mmtf-benchmark"));
	}

	public void smallSample() {

	}

	public void parseHadoop() throws IOException {
		//HetatomImpl.performanceBehavior = HetatomImpl.PerformanceBehavior.LESS_MEMORY_SLOWER_PERFORMANCE;
		Counter c = new Counter();
		Timer.start("mmtf-all");
		for (File f : dirs.getHadoopTest().listFiles()) {
			if (f.getName().startsWith(".") || f.getName().startsWith("_")) {
				continue;
			}
			HadoopReader hr = new HadoopReader(f.toString());
			while (hr.next()) {
				StructureDataInterface sdi = parse(hr.getBytes());
				String code = hr.getKey();
				MmtfStructureReader mmtfStructureReader = new MmtfStructureReader();
				new StructureDataToAdapter(sdi, mmtfStructureReader);
				Structure sb = mmtfStructureReader.getStructure();
				c.next();
			}
			hr.close();
		}
		Timer.stop("mmtf-all");
		System.out.println("Structures parsed  " + count);
		Timer.print();
	}

	private StructureDataInterface parse(byte[] bytes) {
		MmtfStructure mmtf = ReaderUtils.getDataFromInputStream(
			new ByteArrayInputStream(bytes));
		GenericDecoder gd = new GenericDecoder(mmtf);
		return gd;
	}

	public void download() {

	}

	public void run() throws Exception {
		//unzipMmtf();
		/*for (int i = 0; i < cycles; i++) {
            profileMmtfLight();
        }*/
		//test();
		//fullM();
		//hadoopUnzippedBiojavaTest();
		parseHadoop();
	}

	public static void main(String[] args) throws Exception {
		Benchmark b = new Benchmark();
		b.run();
	}

}
