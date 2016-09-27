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
		List<Entity> list = new ArrayList<>();
		for (int i = 0; i < 100000; i++) {
			list.add(new Entity());
		}
		JavaRDD<Entity> data = sc.parallelize(list);
		ClusterAccumulator acc = new ClusterAccumulator();
		Option<String> option = Option.empty();
		acc.register(sc.sc(), option, false);
		data.foreach(t -> acc.add(t));
		System.out.println(acc.value().size());
	}

	public static void main(String[] args) {
		System.out.println("start");
		ClusteringTest m = new ClusteringTest();
		m.run();
		System.out.println("end");
	}
}
