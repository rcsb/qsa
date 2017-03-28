package io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.rcsb.mmtf.decoder.ReaderUtils;

public class HadoopSequenceFileConverter {

	public static void convert(String in, String untared, String out) throws IOException {
		File tarDir = new File(untared);
		untar(new File(in), tarDir);
		if (tarDir.listFiles().length != 1) {
			System.err.println("Suspicious number of directories in " + tarDir
				+ ", should be just one, is " + tarDir.listFiles().length);
		}
		convert(tarDir.listFiles()[0].toString(), out);
	}

	private static void untar(File in, File out) throws IOException {
		Tar.unpack(in, out.toPath());
	}

	private static void convert(String source, String destination) throws IOException {
		File src = new File(source);
		File outDir = new File(destination);
		if (!outDir.exists()) {
			outDir.mkdir();
		}
		SequenceFile.Writer w = createWriter(new Path(destination + File.separator + "hsf"));
		int counter = 0;
		for (File f : src.listFiles()) {
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

				Text key = new Text(hr.getKey());
				BytesWritable val = new BytesWritable(bytes);

				val.set(bytes, 0, bytes.length);
				w.append(key, val);

				counter++;
			}
			hr.close();
		}
		w.close();
	}

	private static SequenceFile.Writer createWriter(Path path) throws IOException {
		Configuration conf = new Configuration();
		SequenceFile.Writer.Option[] options = {SequenceFile.Writer.file(path),
			SequenceFile.Writer.keyClass(Text.class),
			SequenceFile.Writer.valueClass(BytesWritable.class),
			SequenceFile.Writer.compression(SequenceFile.CompressionType.NONE)
		};
		SequenceFile.Writer writer = SequenceFile.createWriter(conf, options);
		return writer;
	}

}
