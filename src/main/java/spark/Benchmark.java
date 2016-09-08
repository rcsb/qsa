package spark;

import alignment.AlignmentQuality;
import fragments.FragmentsAligner;
import fragments.FragmentsFactory;
import fragments.Parameters;
import io.Directories;
import java.io.BufferedWriter;
import org.rcsb.project10.WritableSegment;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.vecmath.Point3d;

import org.apache.hadoop.io.Text;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import pdb.CompactStructure;
import pdb.PdbChain;
import scala.Tuple2;

/**
 * This class ... add documentation here
 *
 * @author Emilia Copic
 * @author Varkey Alumootil
 * @author Peter Rose
 * @author Antonin Pavelka
 *
 */
public class Benchmark {

	private File home = new File("/Users/antonin/data/qsa/");
	private File out;
	private File datasetFile;
	private File structuresFile = new File(home + "/x-rayChains_20160625_122400.seq");
	private File objectFile;
	private File resultFile;

	private Directories dirs_;
	private Parameters params_;
	private AlignerFunction aligner_;
	private FragmentsFactory ff_;
	private Dataset dataset_;
	private boolean easy_;

	public Benchmark(boolean easy) {
		easy_ = easy;
		if (easy) {
			datasetFile = new File(home + "/TM_L90_111111.csv");
			objectFile = new File(home + "/pairs_easy");
			out = new File(home + "/spark_easy.out");
			Printer.setFile(out);
			Printer.println("EASY");
			resultFile = new File(home + "/result_easy.csv");
		} else {
			datasetFile = new File(home + "/TM_L50-60_111111.csv");
			objectFile = new File(home + "/pairs_hard");
			out = new File(home + "/spark_hard.out");
			Printer.setFile(out);
			Printer.println("HARD");
			resultFile = new File(home + "/result_hard.csv");
		}
		dirs_ = new Directories(home);
		params_ = new Parameters();
		ff_ = new FragmentsFactory(params_);
		aligner_ = new AlignerFunction(new FragmentsAligner(params_, dirs_), ff_);
		dataset_ = new Dataset(datasetFile);
	}

	public static void main(String[] args) throws Exception {
		Benchmark b = new Benchmark(args[0].equals("easy"));
		//b.prepare();
		b.run();
	}

	private JavaSparkContext getSc() {
		SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("ArchLibGenerator")
				.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
				.set("spark.executor.memory", "6g");
		JavaSparkContext sc = new JavaSparkContext(conf);
		return sc;
	}

	private void prepare() throws Exception {
		Printer.println("mem " + Runtime.getRuntime().freeMemory());
		JavaSparkContext sc = getSc();
		JavaPairRDD<Text, Point3d[]> chains = sc
				.sequenceFile(structuresFile.getPath(), Text.class, WritableSegment.class)
				// .sample(false, 1.0 / 100)
				.mapToPair(t -> new Tuple2<>(t._1, t._2.getCoordinates()));
		Printer.println("sequence file loaded");
		JavaRDD<CompactStructure> ss = chains.map(new CoordinateLoader());
		Printer.println("ss");
		JavaPairRDD<PdbChain, CompactStructure> idToStr = ss.mapToPair(t -> new Tuple2<>(t.getId(), t));
		Printer.println("x1");
		JavaPairRDD<PdbChain, PdbChain> pairs = sc.parallelizePairs(dataset_.getPairs());
		Printer.println("x2");
		Printer.println("a " + pairs.count());
		Pairing<PdbChain, CompactStructure> pairing = new Pairing<>();
		Printer.println("x3");
		JavaPairRDD<CompactStructure, CompactStructure> strPairs = pairing.create(idToStr, pairs);// .sample(false,
																									// 1.0
																									// /
																									// 100000);
		Printer.println("x4");
		strPairs.coalesce(1).saveAsObjectFile(objectFile.getPath());
		Printer.println("x5");
		sc.stop();
		sc.close();
	}

	private void run() {
		JavaSparkContext sc = getSc();
		JavaPairRDD<CompactStructure, CompactStructure> strPairs = JavaPairRDD
				.fromJavaRDD(sc.objectFile(objectFile.getPath()));
		strPairs = strPairs.sample(false, 0.1, 1);
		// strPairs = strPairs.filter(new SpecificPairFilter());
		Printer.println(strPairs.count());
		Printer.println("stage 5 " + strPairs.count());
		JavaRDD<AlignmentQuality> results = strPairs.map(aligner_);
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(resultFile));
			for (AlignmentQuality q : results.collect()) {
				ReferenceMetrics rq = dataset_.getReferenceMetrics(q.getA(), q.getB());
				bw.write(q.getLine() + "," + rq.getLine() + "\n");
			}
			bw.close();
		} catch (IOException ex) {
			Logger.getLogger(Benchmark.class.getName()).log(Level.SEVERE, null, ex);
		}
		sc.stop();
		sc.close();
	}

}
