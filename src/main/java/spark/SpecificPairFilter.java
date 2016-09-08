package spark;

import java.util.HashSet;
import java.util.Set;
import org.apache.spark.api.java.function.Function;
import pdb.CompactStructure;
import scala.Tuple2;

/**
 *
 * @author Antonin Pavelka
 */
public class SpecificPairFilter implements
        Function<Tuple2<CompactStructure, CompactStructure>, Boolean> {

    private static Set<String> set = new HashSet<>();

    static {
        set.add("2X89");
        set.add("3VXO");
    }

    @Override
    public Boolean call(Tuple2<CompactStructure, CompactStructure> t) {
        return set.contains(t._1.getId().toString().substring(0, 4).toUpperCase())
                && set.contains(t._2.getId().toString().substring(0, 4).toUpperCase());
    }

}
