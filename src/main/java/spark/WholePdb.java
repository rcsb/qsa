package spark;

import java.io.File;
import java.util.List;

import org.apache.hadoop.io.Text;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.rcsb.mmtf.spark.data.StructureDataRDD;

public class WholePdb {

	private JavaSparkContext getSc() {
		SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("PDBloader")
				.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
				.set("spark.executor.memory", "14g");
		JavaSparkContext sc = new JavaSparkContext(conf);
		return sc;
	}

	private void prepare() throws Exception {

		long timeA = System.nanoTime();
		try {
//			 File file = new File("/Users/antonin/data/qsa/full_mmtf");
			File file = new File("/Users/antonin/data/qsa/full");
			JavaSparkContext sc = getSc();

			// Configuration conf = new Configuration();
			// FileSystem fs = FileSystem.getLocal(conf);
			// Path seqFilePath = new Path(file.getPath());
			// SequenceFile.Reader reader = new SequenceFile.Reader(fs,
			// seqFilePath, conf);
			// System.out.println(reader.getKeyClass());
			// System.out.println(reader.getValueClassName());

			// .getKeyClass();
			// SequenceFile.Reader.getValueClass();

			// JavaPairRDD<Text, Object> chains =
			// sc.sequenceFile(file.getPath(), Text.class,
			// Object.class).sample(false, 0.001);
			// System.out.println(chains.getClass().getName());
			
			
	        StructureDataRDD structureData = new StructureDataRDD(file.getPath());
	        structureData.sample(0.00001).getJavaRdd().collect();	        
	        
			
			

			// System.out.println(list.get(0)._1);
			// System.out.println(list.get(0)._2.getClass());

			// .sample(false, 1.0 / 100)
			// .mapToPair(t -> new Tuple2<>(t._1, t._2.getCoordinates()));
			/*
			 * JavaRDD<CompactStructure> ss = chains.map(new
			 * CoordinateLoader()); JavaPairRDD<PdbChain, CompactStructure>
			 * idToStr = ss.mapToPair( t -> new Tuple2<>(t.getId(), t));
			 * JavaPairRDD<PdbChain, PdbChain> pairs =
			 * sc.parallelizePairs(dataset_.getPairs()); Pairing<PdbChain,
			 * CompactStructure> pairing = new Pairing<>();
			 * JavaPairRDD<CompactStructure, CompactStructure> strPairs =
			 * pairing.create(idToStr, pairs);//.sample(false, 1.0 / 100000);
			 * strPairs.coalesce(1).saveAsObjectFile(objectFile.getPath());
			 */
			sc.stop();
			sc.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		long timeB = System.nanoTime();
		long time = (timeB - timeA) / 1000000;
		System.out.println("time  " + time);
	}

	public static void main(String[] args) throws Exception {
		WholePdb m = new WholePdb();
		m.prepare();
	}
}
