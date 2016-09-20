package spark.interfaces;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import alignment.FragmentsAlignment;
import analysis.MySerializer;
import benchmark.PairsProvider;
import pdb.SimpleStructure;

public class MassAligner implements Serializable {

	private static final long serialVersionUID = 1L;
	private List<StructureAlignmentAlgorithm> saas = new ArrayList<>();
	private SimpleStructure[] structures;
	private PairsProvider pairsProvider;
	private MySerializer serializer;
	private File textFile;

	public MassAligner(PairsProvider pairsProvider, MySerializer serializer, File textFile) {
		this.pairsProvider = pairsProvider;
		this.serializer = serializer;
		this.textFile = textFile;
	}

	public void addAlgorithm(StructureAlignmentAlgorithm saa) {
		this.saas.add(saa);
	}

	private void task(int i) {
		try {
			AlignablePair p = pairsProvider.get(i);
			System.out.println("Picked: " + p.getA().getId() + " " + p.getB().getId());
			StringBuilder sb = new StringBuilder();
			for (StructureAlignmentAlgorithm saa : saas) {
				Alignment a = saa.align(p);
				// synchronized (this) {
				// serializer.serialize(a);
				// }
				sb.append(a.getLine()).append(Alignment.SEP);
			}
			synchronized (this) {
				try (BufferedWriter bw = new BufferedWriter(new FileWriter(textFile, true))) {
					bw.write(sb.toString());
					bw.write(Alignment.NEW_LINE);
					bw.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void paralell(boolean parallel) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(textFile));
			bw.write(FatcatAlignmentWrapper.getHeader() + Alignment.SEP + FragmentsAlignment.getHeader());
			bw.write(Alignment.NEW_LINE);
			bw.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		if (parallel)
			IntStream.range(0, pairsProvider.size()).parallel().forEach(i -> task(i));
		else
			IntStream.range(0, pairsProvider.size()).forEach(i -> task(i));
	}

	@Deprecated
	public void run() {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(textFile));
			bw.write(FatcatAlignmentWrapper.getHeader() + Alignment.SEP + FragmentsAlignment.getHeader());
			bw.write(Alignment.NEW_LINE);
			bw.close();
			for (int i = 0; i < pairsProvider.size(); i++) {
				// try {
				AlignablePair p = pairsProvider.get(i);
				// task(p);
				/*
				 * System.out.println("Picked: " + p.getA().getId() + " " +
				 * p.getB().getId()); bw = new BufferedWriter(new
				 * FileWriter(textFile, true)); for (StructureAlignmentAlgorithm
				 * saa : saas) { Alignment a = saa.align(p);
				 * serializer.serialize(a); bw.write(a.getLine() +
				 * Alignment.SEP); } bw.write(Alignment.NEW_LINE); bw.close();
				 */
				// } catch (Exception e) {
				// e.printStackTrace();
				// }
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
