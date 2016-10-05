package spark;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import javax.vecmath.Point3d;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.rcsb.mmtf.api.StructureDataInterface;
import org.rcsb.mmtf.spark.data.StructureDataRDD;

import fragments.Fragment;
import fragments.FragmentsFactory;
import geometry.PointConversion;
import io.Directories;
import pdb.MmtfStructureProvider;
import pdb.SimpleStructure;
import scala.Tuple2;
import superposition.SuperPositionQCP;
import util.Timer;

public class WholePdb implements Serializable {

	private FragmentsFactory ff = new FragmentsFactory();
	private SuperPositionQCP qcp = new SuperPositionQCP();

	File pdbFull = new File("/Users/antonin/data/qsa/full");
	File pdbSample = new File("/Users/antonin/data/qsa/pdb_sample");

	private List<Fragment> fragment(Tuple2<String, StructureDataInterface> t) {
		MmtfStructureProvider p = new MmtfStructureProvider(Directories.createDefault().getMmtf().toPath());
		SimpleStructure ss = p.getStructure(t._1, t._2);
		return ff.create(ss, 1).getList();
	}

	private static int counter = 0;

	private double superpose(Fragment a, Fragment b) {
		Point3d[] x = PointConversion.getPoints3d(a.getPoints());
		Point3d[] y = PointConversion.getPoints3d(b.getPoints());
		qcp.set(x, y);
		double rmsd = qcp.getRmsd();
		// System.out.println(rmsd);
		counter++;
		return rmsd;
	}

	private void sample() {
		try {
			StructureDataRDD structureData = new StructureDataRDD(pdbFull.getPath());
			structureData = structureData.sample(0.0001);
			structureData.saveToFile(pdbSample.getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void prepare() throws Exception {

		try {
			StructureDataRDD structureData = new StructureDataRDD(pdbSample.getPath());
			JavaPairRDD<String, StructureDataInterface> data = structureData.getJavaRdd().cache();
			FlatMapFunction<Tuple2<String, StructureDataInterface>, Fragment> f = new FlatMapFunction<Tuple2<String, StructureDataInterface>, Fragment>() {
				private static final long serialVersionUID = 1L;

				public Iterable<Fragment> call(Tuple2<String, StructureDataInterface> t) throws Exception {
					List<Fragment> l = fragment(t);
					return l;
				}
			};
			JavaRDD<Fragment> fragments = data.flatMap(f);
			System.out.println("Fragments: " + fragments.count());
			JavaRDD<Fragment> sample = data.flatMap(f).sample(false, 0.0001);
			System.out.println("Fragments sample: " + sample.collect().size());
			JavaPairRDD<Fragment, Fragment> pairs = sample.cartesian(sample);

			List<Tuple2<Fragment, Fragment>> fl = pairs.collect();

			Timer.start();
			for (Tuple2<Fragment, Fragment> t : fl) {
				superpose(t._1, t._2);
			}
			Timer.stop();
			System.out.println(Timer.get() + " ms");
			System.out.println("Counter " + counter);

			System.out.println("Pairs: " + pairs.collect().size());
			Timer.start();
			JavaRDD<Double> rmsd = pairs.map(t -> superpose(t._1, t._2));
			List<Double> rl = rmsd.collect();
			Timer.stop();
			System.out.println(Timer.get() + " ms");
			System.out.println("Counter " + counter);
			for (double d : rl) {
				System.out.print(d + " ");
			}
			System.out.println();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		WholePdb m = new WholePdb();
		m.prepare();
	}
}
