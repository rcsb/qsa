package profiling;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.biojava.nbio.structure.io.mmtf.MmtfStructureReader;
import org.rcsb.mmtf.api.StructureDataInterface;
import org.rcsb.mmtf.dataholders.MmtfStructure;
import org.rcsb.mmtf.decoder.GenericDecoder;
import org.rcsb.mmtf.decoder.ReaderUtils;
import org.rcsb.mmtf.decoder.StructureDataToAdapter;

import io.Directories;
import io.HadoopReader;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.biojava.nbio.structure.HetatomImpl;
import org.biojava.nbio.structure.HetatomImpl.PerformanceBehavior;
import org.biojava.nbio.structure.StructureIO;
import org.biojava.nbio.structure.io.PDBFileReader;
import org.rcsb.mmtf.benchmark.Downloader;
import structure.StructureLite;
import util.Timer;
import structure.StructureLiteFactory;

public class Measurement {

	private boolean profile = true;
	private final int iterations = 100;
	private final int cycles = 10;
	//private String[] subset = {/*"1cv2", "1iz7", */"1ajv"};
	private List<String> subset;
	private List<String> codes;
	int sublistN = 10000;
	AtomCache cache;
	Directories dirs;
	PDBFileReader pdbreader = new PDBFileReader();
	StructureLiteFactory slf = new StructureLiteFactory();

	public Measurement() {
		dirs = new Directories(new File("c:/kepler/data/optimization"));
		StructureIO.setPdbPath(dirs.getPdb().toString());
		//Entries entries = new Entries(dirs.getPdbEntries());
		//codes = entries.getCodes();

		codes = new ArrayList<>(dirs.getMmtfAndPdb());

		resetSubset();
		cache = new AtomCache();

		cache.setPath(dirs.getPdbCache());

	}

	private static int seed = 12;

	public void resetSubset() {
		Collections.shuffle(codes, new Random(seed));
		int p = codes.size() - sublistN;
		subset = codes.subList(p, p + sublistN);
	}

	public Structure parseMmtfToBiojava(String pdbCode) {
		try {
			MmtfStructureReader mmtfStructureReader = new MmtfStructureReader();
			byte[] array = Files.readAllBytes(dirs.getMmtfPath(pdbCode));
			array = ReaderUtils.deflateGzip(array);
			ByteArrayInputStream bai = new ByteArrayInputStream(array);
			MmtfStructure mmtf = ReaderUtils.getDataFromInputStream(bai);
			GenericDecoder gd = new GenericDecoder(mmtf);
			new StructureDataToAdapter(gd, mmtfStructureReader);
			return mmtfStructureReader.getStructure();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	//public MmtfStructure parseMmtf(String pdbCode) throws Exception {
	//	Path f = dirs.getMmtfPath(pdbCode);
//		MmtfStructure mmtf = ReaderUtils.getDataFromZippedFile(f);
//		return mmtf;
		//JsonTree tree = ReaderUtils.getJsonTreeFromZip(mmtf);
		//StructureDataInterface s = new GenericDecoder(new MmtfStructure(tree));
		//return parseMmtf(pdbCode, s);
//	}

	public MmtfStructure parseMmtfUnzipped(String pdbCode) throws Exception {
		Path f = dirs.getMmtfUnzippedPath(pdbCode);
		MmtfStructure mmtf = ReaderUtils.getDataFromFile(f);
		return mmtf;
	}

	public void fullM() throws IOException {
		int count = 0;
		Timer.start();
		File dir = dirs.getHadoopSequenceFileDir();
		for (File f : dir.listFiles()) {
			if (!f.getName().startsWith("part-")) {
				continue;
			}
			HadoopReader hr = new HadoopReader(f.toString());
			while (hr.next()) {
				byte[] bytes = hr.getBytes();
				MmtfStructure mmtf = ReaderUtils.getDataFromInputStream(
					new ByteArrayInputStream(
						ReaderUtils.deflateGzip(bytes)));
				GenericDecoder gd = new GenericDecoder(mmtf);
				slf.parseMmtf("xxxx", gd);
				count++;
				if (count % 1000 == 0) {
					System.out.println(count);
				}
				//System.err.println(key + "\t" + val.getLength());
			}
			hr.close();
		}
		Timer.stop();
		System.out.println("READ " + count);
		System.out.println("Time " + Timer.get());
	}

	private StructureDataInterface parse(byte[] bytes) {
		MmtfStructure mmtf = ReaderUtils.getDataFromInputStream(
			new ByteArrayInputStream(bytes));
		GenericDecoder gd = new GenericDecoder(mmtf);
		return gd;
	}

	public void full() {

		// SparkConf conf = new SparkConf().setMaster("local[*]").setAppName(SparkUtils.class.getSimpleName());
		// JavaSparkContext sc = new JavaSparkContext(conf);

		/*JavaPairRDD<String, WritableSegment> segments =*/// sc
		//       .sequenceFile(dirs.getHadoopSequenceFile(), Text.class, BytesWritable.class);
		//.mapToPair(t -> new Tuple2<String, BytesWritable> (new String(t._1.toString()), t._2) );
		//segments = segments.filter(t -> t._2.getSequence().contains("HHHHH"));	    
		//System.out.println("Count: " + segments.count());
	}

	public void open(String pdbCode) throws IOException {

		byte[] array = Files.readAllBytes(dirs.getMmtfUnzippedPath(pdbCode));
	}

	public void openChannel(String pdbCode) throws IOException {
		Path f = dirs.getMmtfUnzippedPath(pdbCode);
		final FileChannel channel = new FileInputStream(f.toFile()).getChannel();
		MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
		/*for (int i = 0; i < f.toFile().length(); i++) {
            buffer.get();
        }*/
		channel.close();
	}

	public StructureLite parseMmtfLite(String pdbCode) throws IOException {
		Path f = dirs.getMmtfUnzippedPath(pdbCode);
		MmtfStructure mmtfData = ReaderUtils.getDataFromFile(f);
		StructureDataInterface s = new GenericDecoder(mmtfData);
		StructureLite sl = slf.parseMmtf(pdbCode, s);
		return sl;
	}

	/*    public SimpleStructure parsePdb(String pdbCode) {
        SimpleStructure structure = new SimpleStructure(pdbCode);
        File f = dirs.getPdbPath(pdbCode).toFile();
        int counter = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            while ((line = br.readLine()) != null) {
                if (PdbLine.isCoordinateLine(line)) {
                    PdbLine pl = new PdbLine(line);
                    counter++;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        // System.out.println("atoms " + counter);
        return structure;
    }
	 */
	public Structure parsePdbToBiojava(String code) throws Exception {
		File f = dirs.getPdbPath(code).toFile();
		//Structure s = StructureIO.getStructure(code);        
		Structure s = pdbreader.getStructure(dirs.getPdbPath(code).toString());
		return s;
	}

	/*public void profileMmtfSample() {
		try {
			//for (int i = 0; i < iterations; i++) {
			for (String code : subset) {
				Timer.start();
				for (int i = 0; i < 100 - 1; i++) {
					parseMmtf(code);
				}
				MmtfStructure s = parseMmtf(code);
				Timer.stop();
				int an = s.getNumAtoms();
				System.out.println(code + " " + an + " " + Timer.getMicro() + " microseconds / structure");
			}
			//}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

	public void profileMmtfLight() throws Exception {
		//resetSubset();
		Timer.start();
		for (String code : subset) {
			//MmtfStructure s = parseMmtfUnzipped(code);
			parseMmtfLite(code);
		}
		Timer.stop();
		System.out.println("mmtf light " + (Timer.getMicro() / subset.size()));
		//int an = s.getNumAtoms();
		//System.out.println(code + " " + an + " " + Timer.getMicro());

	}

	public void profileMmtfBiojava() throws Exception {
		//resetSubset();
		Timer.start();
		for (String code : subset) {
			Structure s = parseMmtfToBiojava(code);
		}
		Timer.stop();
		System.out.println("mmtf biojava " + (Timer.getMicro() / subset.size()));
		//int an = s.getNumAtoms();
		//System.out.println(code + " " + an + " " + Timer.getMicro());

	}

	public void profilePdbBiojava() throws Exception {
		//resetSubset();
		Timer.start();
		for (String code : subset) {
			try {
				Structure s = parsePdbToBiojava(code);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		Timer.stop();
		System.out.println("pdb biojava " + (Timer.getMicro() / subset.size()));
		//int an = s.getNumAtoms();
		//System.out.println(code + " " + an + " " + Timer.getMicro());

	}

	/*    public void profilePdb() throws Exception {
        //resetSubset();
        Timer.start();
        for (String code : subset) {
            try {
                parsePdb(code);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        Timer.stop();
        System.out.println("pdb biojava " + (Timer.getMicro() / subset.size()));
        //int an = s.getNumAtoms();
        //System.out.println(code + " " + an + " " + Timer.getMicro());

    }
	 */
	public void test() throws Exception {
		Map<String, String> fails = new HashMap<>();

		//codes.clear();
		//codes.add("4kuk");
		for (int i = 0; i < 3; i++) {

			resetSubset();
			Timer.start();
			for (String code : subset) {
				int counter = 0;
				try {
					//Timer.start();
					//Structure sb = parseMmtfToBiojava(code);
					//Timer.stop();
					//double a = Timer.getMicro();
					//Timer.start();
					StructureLite sl = parseMmtfLite(code);
					//Timer.stop();
					//double b = Timer.getMicro();
					//StructureTest st = new StructureTest(code);
					//st.compareStructures(sb, sl);
					counter++;
					if (counter % 1000 == 0) {
						//System.out.println(code + " " + counter + " " + a + " " + (b / a));
						System.out.println(code + " " + counter);
					}
				} catch (Exception e) {
					e.printStackTrace();
					fails.put(code, e.getMessage());
				}
			}
			Timer.stop();
			System.out.println("FAILS : " + fails.size());
			System.out.println("mmtf structures parsed (CA) in " + Timer.get());
		}
		for (String code : fails.keySet()) {
			String m = fails.get(code);
			System.out.println(code + ": " + m);
		}
	}

	private void measureDownload() {
		Downloader downloader = new Downloader(dirs);

		Timer.start();
		downloader.downloadMmtf();
		Timer.stop();
		System.out.println("mmtf download " + Timer.get());

		Timer.start();
		downloader.downloadPdb();
		Timer.stop();
		System.out.println("pdb download " + Timer.get());

	}

	public void unzipPdb() throws IOException {
		dirs.unzipDb(dirs.getPdb(), dirs.getPdbUnzipped());
	}

	public void unzipMmtf() throws IOException {
		dirs.unzipDb(dirs.getMmtf(), dirs.getMmtfUnzipped());
	}

	public void hadoopUnzippedBiojavaTest() throws IOException {

		HetatomImpl.performanceBehavior = PerformanceBehavior.LESS_MEMORY_SLOWER_PERFORMANCE;

		int count = 0;
		boolean test = true;
		Timer.start();
		HadoopReader hr = new HadoopReader(dirs.getHadoopSequenceFileUnzipped());
		while (hr.next()) {
			count++;
			//if (count > 2000) {
			//    break;
			//}
			StructureDataInterface sdi = parse(hr.getBytes());
			String code = hr.getKey();
			//StructureLite sl = slf.parseMmtf(code, sdi);
			if (test) {
				MmtfStructureReader mmtfStructureReader = new MmtfStructureReader();
				new StructureDataToAdapter(sdi, mmtfStructureReader);
				Structure sb = mmtfStructureReader.getStructure();
				//StructureTest st = new StructureTest(code);
				//st.compareStructures(sb, sl);
			}
			if (count % 1000 == 0) {
				System.out.println(count);
			}
		}
		hr.close();
		Timer.stop();
		System.out.println("Structures parsed  " + count);
		System.out.println("Time " + Timer.seconds());
	}

	public void files() throws IOException {

		HetatomImpl.performanceBehavior = PerformanceBehavior.LESS_MEMORY_SLOWER_PERFORMANCE;

		int count = 0;
		boolean test = true;
		Timer.start();
		for (File f : dirs.getHadoopTest().listFiles()) {
			if (f.getName().startsWith(".") || f.getName().startsWith("_")) {
				continue;
			}
			HadoopReader hr = new HadoopReader(f.toString());
			while (hr.next()) {
				count++;
				//if (count > 2000) {
				//    break;
				//}
				StructureDataInterface sdi = parse(hr.getBytes());
				String code = hr.getKey();
				//StructureLite sl = slf.parseMmtf(code, sdi);
				if (test) {
					MmtfStructureReader mmtfStructureReader = new MmtfStructureReader();
					new StructureDataToAdapter(sdi, mmtfStructureReader);
					Structure sb = mmtfStructureReader.getStructure();
					//StructureTest st = new StructureTest(code);
					//st.compareStructures(sb, sl);
				}
				if (count % 1000 == 0) {
					System.out.println(count);
				}
			}
			hr.close();
		}
		Timer.stop();
		System.out.println("Structures parsed  " + count);
		System.out.println("Time " + Timer.seconds());
	}

	public void run() throws Exception {
		//unzipMmtf();
		/*for (int i = 0; i < cycles; i++) {
            profileMmtfLight();
        }*/
		//test();
		//fullM();
		//hadoopUnzippedBiojavaTest();
		files();
	}

	public static void main(String[] args) throws Exception {
		Measurement m = new Measurement();
		m.run();
	}

}
