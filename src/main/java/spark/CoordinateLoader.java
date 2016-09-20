package spark;

import javax.vecmath.Point3d;
import org.apache.hadoop.io.Text;
import org.apache.spark.api.java.function.Function;
import pdb.ChainId;
import pdb.CompactStructure;
import pdb.PdbChainId;
import pdb.SimpleChain;
import pdb.SimpleStructure;
import scala.Tuple2;

/**
 *
 * @author Antonin Pavelka
 */
public class CoordinateLoader implements
        Function<Tuple2<Text, Point3d[]>, CompactStructure> {

    @Override
    public CompactStructure call(Tuple2<Text, Point3d[]> t) {
        CompactStructure s = new CompactStructure(new PdbChainId(t._1.toString()), t._2);
        return s;
    }

    public SimpleStructure call_for_precomputation(Tuple2<Text, Point3d[]> t) {
        SimpleStructure s = new SimpleStructure(new PdbChainId(t._1.toString()));
        ChainId cid = ChainId.createEmpty();
        s.addChain(cid, new SimpleChain(cid, t._2));
        return s;
    }
}
