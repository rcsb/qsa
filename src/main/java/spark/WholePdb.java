package spark;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import javax.vecmath.Point3d;

import org.apache.spark.api.java.JavaRDD;
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

	private double superpose(Fragment a, Fragment b) {
		Point3d[] x = PointConversion.getPoints3d(a.getPoints());
		Point3d[] y = PointConversion.getPoints3d(b.getPoints());
		qcp.set(x, y);
		double rmsd = qcp.getRmsd();
		// System.out.println(rmsd);
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
		long timeA = System.nanoTime();
		try {
			StructureDataRDD structureData = new StructureDataRDD(pdbSample.getPath());
			JavaRDD<Fragment> fragments = structureData.getJavaRdd()
					./* sample(false, 0.000001). */flatMap(t -> fragment(t));
			System.out.println("Fragments: " + fragments.count());
			System.out.println(fragments.cartesian(fragments).map(t -> superpose(t._1, t._2)).count());
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
