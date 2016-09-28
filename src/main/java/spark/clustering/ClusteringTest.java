package spark.clustering;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import scala.Option;

public class ClusteringTest implements Serializable {

	public void run() {
		SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("ArchLibGenerator").set("spark.serializer",
				"org.apache.spark.serializer.KryoSerializer");
		JavaSparkContext sc = new JavaSparkContext(conf);
		List<Multidimensional> list = new ArrayList<>();
		for (int i = 0; i < 1000000; i++) {
			list.add(new Multidimensional());
		}
		JavaRDD<Multidimensional> data = sc.parallelize(list);
		ClusterAccumulator acc = new ClusterAccumulator();
		Option<String> option = Option.empty();
		acc.register(sc.sc(), option, false);
		data.foreach(t -> acc.add(t));
		Clustering result = acc.value();
		System.out.println(result.size());
		result.print();
		
	}

	public static void main(String[] args) {
		long time1 = System.nanoTime();
		System.out.println("started...");
		ClusteringTest m = new ClusteringTest();
		m.run();
		long time2 = System.nanoTime();
		long time = (time2 - time1) / 1000000;
		System.out.println("...finished in " + time);
	}
}
