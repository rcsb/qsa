package org.rcsb.mmtf.benchmark;

import io.Directories;
import io.HadoopReader;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.io.MMCIFFileReader;
import org.biojava.nbio.structure.io.PDBFileReader;
import org.biojava.nbio.structure.io.mmcif.MMcifParser;
import org.biojava.nbio.structure.io.mmcif.SimpleMMcifConsumer;
import org.biojava.nbio.structure.io.mmcif.SimpleMMcifParser;
import org.biojava.nbio.structure.io.mmtf.MmtfStructureReader;
import org.rcsb.mmtf.api.StructureDataInterface;
import org.rcsb.mmtf.dataholders.MmtfStructure;
import org.rcsb.mmtf.decoder.GenericDecoder;
import org.rcsb.mmtf.decoder.ReaderUtils;
import org.rcsb.mmtf.decoder.StructureDataToAdapter;

/**
 *
 * @author Antonin Pavelka
 */
public class Parser {

	private Directories dirs;
	private PDBFileReader pdbReader = new PDBFileReader();
	private MMCIFFileReader cifReader = new MMCIFFileReader();

	public Parser(Directories dirs) {
		this.dirs = dirs;
	}

	public Structure parseMmtfToBiojava(String pdbCode) {
		try {
			MmtfStructureReader mmtfStructureReader = new MmtfStructureReader();
			byte[] array = Files.readAllBytes(dirs.getMmtfPath(pdbCode));
			array = ReaderUtils.deflateGzip(array);
			ByteArrayInputStream bai = new ByteArrayInputStream(array);
			MmtfStructure mmtf = ReaderUtils.getDataFromInputStream(bai);
			GenericDecoder gd = new GenericDecoder(mmtf);
			new StructureDataToAdapter(gd, mmtfStructureReader);
			return mmtfStructureReader.getStructure();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Structure parsePdbToBiojava(String code) {
		try {
			Structure s = pdbReader.getStructure(dirs.getPdbPath(code).toString());
			return s;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Structure parseUnzippedCifToBiojava(String code) {
		try {
			Structure s = cifReader.getStructure(dirs.getPdbPath(code).toString());
			System.out.println(s.size());
			return s;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Structure parseCifToBiojava(String code) {
		try {
			InputStream inStream = new GZIPInputStream(new FileInputStream(
				dirs.getCifPath(code).toString()));
			MMcifParser parser = new SimpleMMcifParser();
			SimpleMMcifConsumer consumer = new SimpleMMcifConsumer();
			parser.addMMcifConsumer(consumer);
			parser.parse(new BufferedReader(new InputStreamReader(inStream)));
			Structure s = consumer.getStructure();
			return s;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void parseHadoop() {
		try {
			Counter c = new Counter();
			for (File f : dirs.getHadoopSequenceFileDir().listFiles()) {
				if (f.getName().startsWith(".") || f.getName().startsWith("_")) {
					continue;
				}
				HadoopReader hr = new HadoopReader(f.toString());
				String code = "";
				while (hr.next()) {
					try {
						StructureDataInterface sdi = parse(hr.getBytes());
						code = hr.getKey();
						MmtfStructureReader mmtfStructureReader = new MmtfStructureReader();
						new StructureDataToAdapter(sdi, mmtfStructureReader);
						Structure sb = mmtfStructureReader.getStructure();
						c.next();
					} catch (Exception ex) {
						System.err.println("Error in parsing HSF, last PDB code " + code);
						ex.printStackTrace();
					}
				}
				hr.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void timesPerStructure(Results r) {
		Set<String> codes = new HashSet<>();
		try {
			Counter c = new Counter();
			for (File f : dirs.getHadoopSequenceFileDir().listFiles()) {
				if (f.getName().startsWith(".") || f.getName().startsWith("_")) {
					continue;
				}
				HadoopReader hr = new HadoopReader(f.toString());
				String code = "";
				while (hr.next()) {
					try {
						code = hr.getKey();
						if (codes.contains(code)) {
							System.err.println("DUPLICATED CODE " + code);
						} else {
							long timeA = System.nanoTime();
							StructureDataInterface sdi = parse(hr.getBytes());
							MmtfStructureReader mmtfStructureReader = new MmtfStructureReader();
							new StructureDataToAdapter(sdi, mmtfStructureReader);
							Structure s = mmtfStructureReader.getStructure();
							long timeB = System.nanoTime();
							r.addStructure(code, timeB - timeA);
							c.next();
							codes.add(code);
						}
					} catch (Exception ex) {
						System.err.println("Error in parsing HSF, last PDB code " + code);
						ex.printStackTrace();
					}
				}
				hr.close();
			}
			r.save(dirs.getAverages());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private StructureDataInterface parse(byte[] bytes) {
		MmtfStructure mmtf = ReaderUtils.getDataFromInputStream(
			new ByteArrayInputStream(bytes));
		GenericDecoder gd = new GenericDecoder(mmtf);
		return gd;
	}

}
