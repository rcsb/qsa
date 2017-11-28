package alignment;

import global.Parameters;
import global.io.Directories;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.pymol.PymolVisualizer;

/**
 *
 * @author Antonin Pavelka
 */
public class AlignmentSummaries {

	private final Parameters parameters;
	private final Directories dirs;
	private Map<StructureSourcePair, AlternativeAlignments> map = new HashMap<>();

	public AlignmentSummaries(Parameters parameters, Directories dirs) {
		this.parameters = parameters;
		this.dirs = dirs;
	}

	public synchronized void add(AlignmentSummary alignmentSummary) {
		StructureSourcePair key = alignmentSummary.getStructureSourcePair();
		AlternativeAlignments alternativeAlignments = map.get(key);
		if (alternativeAlignments == null) {
			alternativeAlignments = new AlternativeAlignments(key);
			alternativeAlignments.add(alignmentSummary);
			map.put(key, alternativeAlignments);
		} else {
			alternativeAlignments.add(alignmentSummary);
		}
	}

	public void finalizeOutput() {
		List<AlignmentSummary> summaries = getFinalList();
		generateTable(summaries);
		generatePymolScript(summaries);
	}

	private void generateTable(List<AlignmentSummary> list) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(dirs.getTableFile()))) {
			print("Query structure", bw);
			print("Matching structure", bw);
			print("TM score", bw);
			print("Structurally matching residues", bw);
			print("Structurally matching residues relative", bw);
			print("RMSD of matching residues", bw);
			print("Sequence identity of structurally matching residues", bw);
			newLine(bw);
			for (AlignmentSummary summary : list) {
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
		bw.write(",");
	}

	private void print(String s, BufferedWriter bw) throws IOException {
		bw.write(s);
		bw.write(",");
	}

	private void newLine(BufferedWriter bw) throws IOException {
		bw.write("\n");
	}

	private void generatePymolScript(List<AlignmentSummary> list) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(dirs.getPyFile()))) {
			if (list.isEmpty()) {
				return;
			}
			String queryPath = list.get(0).getPairOfAlignedFiles().getPdbPath(0).getPath();
			bw.write(PymolVisualizer.load(queryPath, 1));
			bw.write("\n");
			int frame = 2;
			for (AlignmentSummary aln : list) {
				String targetPath = aln.getPairOfAlignedFiles().getPdbPath(1).getPath();
				bw.write(PymolVisualizer.load(targetPath, frame));
				bw.write("\n");
				/*if (parameters.isDebug()) {
				pyFile.writeLine(PymolVisualizer.load(dirs.getFinalLines(name), frame));
				pyFile.writeLine(PymolVisualizer.load(dirs.getInitialLines(name), frame));
				pyFile.writeLine(PymolVisualizer.load(dirs.getWordLines(name), frame));
			    }*/
				frame++;
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private List<AlignmentSummary> getFinalList() {
		ArrayList<AlignmentSummary> list = new ArrayList<>();
		for (AlternativeAlignments alternativeAlignments : map.values()) {
			AlignmentSummary best = alternativeAlignments.getBest();
			list.add(best);
		}
		Collections.sort(list);
		return list;
	}

}
