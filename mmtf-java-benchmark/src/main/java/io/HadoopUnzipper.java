package io;

import java.io.File;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.rcsb.mmtf.decoder.ReaderUtils;

public class HadoopUnzipper {

	public SequenceFile.Writer createWriter(Path path) throws IOException {
		Configuration conf = new Configuration();
		SequenceFile.Writer.Option[] options = {SequenceFile.Writer.file(path),
			SequenceFile.Writer.keyClass(Text.class),
			SequenceFile.Writer.valueClass(BytesWritable.class),
			SequenceFile.Writer.compression(SequenceFile.CompressionType.NONE)
		};
		SequenceFile.Writer writer = SequenceFile.createWriter(conf, options);
		return writer;
		//org.apache.hadoop.io.SequenceFile.Writer.Option... opts);

		//throw new UnsupportedOperationException();
	}

	public void convert() throws IOException {
		//throw new UnsupportedOperationException();
		Directories dirs = new Directories(new File("c:/kepler/data/optimization"));
		File dir = dirs.getHadoopSequenceFileDir();
		SequenceFile.Writer w = createWriter(new Path(dirs.getHadoopSequenceFileUnzipped()));
		int counter = 0;
		for (File f : dir.listFiles()) {
			if (!f.getName().startsWith("part-")) {
				continue;
			}
			HadoopReader hr = new HadoopReader(f.getAbsolutePath());
			while (hr.next()) {
				if (counter % 1000 == 0) {
					System.out.println(counter);
				}
				byte[] bytes = hr.getBytes();
				bytes = ReaderUtils.deflateGzip(bytes);
				throw new RuntimeException("uncomment");
				/*val.set(bytes, 0, bytes.length);
				w.append(key, val);
				
				counter++;*/
			}

			//reader.close();
		}
		w.close();
	}

	public static void main(String[] args) throws IOException {
		HadoopUnzipper hu = new HadoopUnzipper();
		hu.convert();
	}
}
