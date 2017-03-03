package profiling;

import io.Directories;
import io.HadoopReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import org.rcsb.mmtf.api.StructureDataInterface;
import org.rcsb.mmtf.dataholders.MmtfStructure;
import org.rcsb.mmtf.decoder.GenericDecoder;
import org.rcsb.mmtf.decoder.ReaderUtils;
import org.rcsb.mmtf.serialization.MessagePackSerialization;

import org.unitils.reflectionassert.ReflectionAssert;
import util.Timer;

public class MessagePackTest extends TestCase {

    Directories dirs = new Directories(new File("c:/kepler/data/optimization"));

    private StructureDataInterface parse(byte[] bytes) throws IOException {
        MmtfStructure mmtf = ReaderUtils.getDataFromInputStream(
                new ByteArrayInputStream(bytes));
        GenericDecoder gd = new GenericDecoder(mmtf);
        return gd;
    }

    public void testByComparisonWithJackson() throws IOException {
        boolean test = true;
        if (!test) { // too slow to do with every build            
            return;
        }        
        int count = 0;
        Timer.start();
        HadoopReader hr = new HadoopReader(dirs.getHadoopSequenceFileUnzipped());
        while (hr.next()) {
			String code = hr.getKey();
            count++;
            byte[] bytes = hr.getBytes();

            MessagePackSerialization.setJackson(false);
            StructureDataInterface sdiJmol = parse(bytes);
            MessagePackSerialization.setJackson(true);
            StructureDataInterface sdiJackson = parse(bytes);
             
            ReflectionAssert.assertReflectionEquals(sdiJackson, sdiJmol);
            
            if (count % 1000 == 0) {
                System.out.println(count);
            }
        }
        hr.close();
        Timer.stop();
        System.out.println("Structures parsed  " + count);
        System.out.println("Time " + Timer.get());
    }
	
}
