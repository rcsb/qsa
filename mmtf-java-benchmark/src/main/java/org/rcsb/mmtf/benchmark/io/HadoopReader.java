package org.rcsb.mmtf.benchmark.io;

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

/**
 *
 * @author Antonin Pavelka
 *
 * Convenience class for reading a single Hadoop sequence file.
 *
 */
public class HadoopReader {

	Text key = new Text();
	BytesWritable value = new BytesWritable();
	SequenceFile.Reader reader;

	public HadoopReader(String file) throws IOException {
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.getLocal(conf);
		reader = new SequenceFile.Reader(fs,
			new org.apache.hadoop.fs.Path(file), conf);
	}

	public boolean next() throws IOException {
		return reader.next(key, value);
	}

	public String getKey() {
		return key.toString();
	}

	/**
	 * Returns more than copyBytes, but it is faster and zip and JSON parsers
	 * seems to be able to deals with it.
	 */
	public byte[] getBytes() {
		return value.getBytes();
	}

	public void close() throws IOException {
		reader.close();
	}

}
