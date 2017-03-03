package io;

import java.io.IOException;
import java.util.Map;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.DefaultCodec;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.JobContext;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import static org.apache.hadoop.mapreduce.lib.output.FileOutputFormat.getCompressOutput;
import static org.apache.hadoop.mapreduce.lib.output.FileOutputFormat.getOutputCompressorClass;
import org.apache.hadoop.util.Progressable;
import org.apache.hadoop.util.ReflectionUtils;

/**
 *
 * @author Antonin Pavelka
 */
public class HadoopMapFile {

	public RecordWriter<WritableComparable, Writable> getRecordWriter(
		FileSystem ignored, Configuration conf, JobConf jobConf,
		JobContext jobContext, String name, Progressable progress)
		throws IOException {
		// get the path of the temporary output file 
		Path file = FileOutputFormat.getTaskOutputPath(jobConf, name);

		FileSystem fs = file.getFileSystem(conf);
		CompressionCodec codec = null;
		CompressionType compressionType = CompressionType.NONE;
		if (getCompressOutput(jobContext)) {
			// find the kind of compression to do
			compressionType = SequenceFileOutputFormat.getOutputCompressionType(jobConf);

			// find the right codec
			Class<? extends CompressionCodec> codecClass
				= getOutputCompressorClass(jobContext,
					DefaultCodec.class);
			codec = ReflectionUtils.newInstance(codecClass, jobConf);
		}

		// ignore the progress parameter, since MapFile is local
		final MapFile.Writer out
			= new MapFile.Writer(jobConf, fs, file.toString(),
				jobConf.getOutputKeyClass().asSubclass(WritableComparable.class),
				jobConf.getOutputValueClass().asSubclass(Writable.class),
				compressionType, codec,
				progress);

		return new RecordWriter<WritableComparable, Writable>() {

			@Override
			public void write(WritableComparable key, Writable value)
				throws IOException {

				out.append(key, value);
			}

			public void close(Reporter reporter) throws IOException {
				out.close();
			}

			@Override
			public void close(TaskAttemptContext tac)
				throws IOException, InterruptedException {
				out.close();
			}
		};
	}

	public void maybe() throws IOException {
		HadoopMapFile m = new HadoopMapFile();

		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.getLocal(conf);

		JobConf jobConf = new JobConf();
		jobConf.setOutputKeyClass(Text.class);
		jobConf.setOutputValueClass(IntWritable.class);

		JobContext jobContext = null;
		String name = "mmtf_map";
		Progressable progress = null;

		/*RecordWriter<WritableComparable, Writable> rw = m.getRecordWriter(
			fs, conf, jobConf,
			jobContext, name, progress);*/
	}

	public static void main(String[] args) throws Exception {

		xxx();
	}

	public static void xxx() throws IOException {

		Configuration conf = new Configuration();
		FileSystem fs;

		try {
			fs = FileSystem.get(conf);

			//Path inputFile = new Path(args[0]);
			Path outputFile = new Path("c:/kepler/rozbal/mmtf_map");

			Text txtKey = new Text();
			Text txtValue = new Text();

			String strLineInInputFile = "";
			String lstKeyValuePair[] = null;
			MapFile.Writer writer = null;

			//FSDataInputStream inputStream = fs.open(inputFile);
			try {
				writer = new MapFile.Writer(conf, fs, outputFile.toString(),
					txtKey.getClass(), txtKey.getClass());
				writer.setIndexInterval(1);
				for (int i = 0; i < 100000; i++) {
					txtKey.set("_key");
					txtValue.set("_value");
					writer.append(txtKey, txtValue);
				}
			} finally {
				IOUtils.closeStream(writer);
				System.out.println("Map file created successfully!!");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
