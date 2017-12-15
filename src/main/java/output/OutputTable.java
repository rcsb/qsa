package output;

import alignment.Alignments;
import alignment.Alignment;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Antonin Pavelka
 */
public class OutputTable {

	private File file;

	public OutputTable(File file) {
		this.file = file;
	}

	public void generateTable(Alignments alignmentSummaries) {
		List<Alignment> list = alignmentSummaries.getBestSummariesSorted();
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			print("Query structure", bw);
			print("Matching structure", bw);
			print("TM score", bw);
			print("Structurally matching residues", bw);
			print("Structurally matching residues relative", bw);
			print("RMSD of matching residues", bw);
			print("Sequence identity of structurally matching residues", bw);
			newLine(bw);
			for (Alignment summary : list) {
				print(summary.getStructureSourcePair().getFirst().toString(), bw);
				print(summary.getStructureSourcePair().getSecond().toString(), bw);
				print(summary.getTmScore(), bw);
				print(summary.getMatchingResiduesAbsolute(), bw);
				print(summary.getMatchingResidues(), bw);
				print(summary.getRmsd(), bw);
				print(summary.getIdentity(), bw);
				newLine(bw);
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void print(double s, BufferedWriter bw) throws IOException {
		print(Double.toString(s), bw);
	}

	private void print(String s, BufferedWriter bw) throws IOException {
		bw.write(s);
		bw.write(",");
	}

	private void newLine(BufferedWriter bw) throws IOException {
		bw.write("\n");
	}
}
