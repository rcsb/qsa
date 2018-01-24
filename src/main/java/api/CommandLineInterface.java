package api;

import analysis.Job;
import java.io.File;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author Antonin Pavelka
 */
public class CommandLineInterface {

	private static Job createJob(String[] args) {
		Options options = new Options();
		options.addOption(Option.builder("h")
			.desc("path to home directory, where all the data will be stored")
			.hasArg()
			.build());
		CommandLineParser parser = new DefaultParser();
		try {
			org.apache.commons.cli.CommandLine cl = parser.parse(options, args);
			if (cl.hasOption("h")) {
				File home = new File(cl.getOptionValue("h").trim());
				return new Job(home);
			} else {
				throw new ParseException("No -h parameter, please specify the home directory.");
			}
		} catch (ParseException exp) {
			throw new RuntimeException("Parsing arguments has failed: " + exp.getMessage());
		}
	}

	public static void main(String[] args) {
		Job job = createJob(args);
		job.run();
	}

}
