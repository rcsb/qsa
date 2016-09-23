package spark;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import org.rcsb.mmtf.api.StructureDataInterface;
import org.rcsb.mmtf.spark.data.StructureDataRDD;

import fragments.Fragment;
import fragments.FragmentsFactory;
import io.Directories;
import pdb.MmtfStructureProvider;
import pdb.SimpleStructure;
import scala.Tuple2;

public class WholePdb implements Serializable {

	private FragmentsFactory ff = new FragmentsFactory();

	private List<Fragment> fragment(Tuple2<String, StructureDataInterface> t) {	
		MmtfStructureProvider p = new MmtfStructureProvider(Directories.createDefault().getMmtf().toPath());
		SimpleStructure ss = p.getStructure(t._1, t._2);
		return ff.create(ss, 1).getList();
	}

	private void prepare() throws Exception {

		long timeA = System.nanoTime();
		try {
			File file = new File("/Users/antonin/data/qsa/full");
			StructureDataRDD structureData = new StructureDataRDD(file.getPath());
			System.out.println(structureData.getJavaRdd().sample(false, 0.01).flatMap(t -> fragment(t)).count());
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
