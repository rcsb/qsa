package spark;

import org.apache.spark.api.java.JavaPairRDD;
import scala.Tuple2;

/**
 *
 * @author Antonin Pavelka
 * @param <K>
 * @param <V>
 */
public class Pairing<K, V> {

    public JavaPairRDD<V, V> create(JavaPairRDD<K, V> data, JavaPairRDD<K, K> pairs) {
        Printer.println("begin");
        JavaPairRDD<K, Tuple2<V, K>> j = data.join(pairs);
        Printer.println(j.count());
        //Printer.println(j.collect().size());
        //List<Tuple2<K, Tuple2<V, K>>> m = j.collect();
        //Printer.println(j.collectAsMap().size());
        /*for (Tuple2<K, Tuple2<V, K>> t : m) {
            Printer.println(t._1 + " " + t._2._1 + " " + t._2._2);
        }*/
        JavaPairRDD<K, V> half = j.mapToPair(t -> new Tuple2<K, V>(t._2._2, t._2._1));
        Printer.println(half.count());
        JavaPairRDD<V, V> r = half.join(data).mapToPair(t -> new Tuple2<V, V>(t._2._1, t._2._2));
        Printer.println(r.count());
        return r;
    }
}
