package org.rcsb.mmtf.benchmark;

import io.Directories;
import io.HadoopReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.biojava.nbio.structure.HetatomImpl;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.io.MMCIFFileReader;
import org.biojava.nbio.structure.io.PDBFileReader;
import org.biojava.nbio.structure.io.mmtf.MmtfStructureReader;
import org.rcsb.mmtf.api.StructureDataInterface;
import org.rcsb.mmtf.dataholders.MmtfStructure;
import org.rcsb.mmtf.decoder.GenericDecoder;
import org.rcsb.mmtf.decoder.ReaderUtils;
import org.rcsb.mmtf.decoder.StructureDataToAdapter;
import util.Timer;

/**
 *
 * @author Antonin Pavelka
 */
public class Parser {

	private Directories dirs;
	PDBFileReader pdbReader = new PDBFileReader();
	MMCIFFileReader cifReader = new MMCIFFileReader();

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

	public Structure parseCifToBiojava(String code) {
		try {
			Structure s = cifReader.getStructure(dirs.getPdbPath(code).toString());
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
						long timeA = System.nanoTime();
						StructureDataInterface sdi = parse(hr.getBytes());
						code = hr.getKey();
						MmtfStructureReader mmtfStructureReader = new MmtfStructureReader();
						new StructureDataToAdapter(sdi, mmtfStructureReader);
						Structure s = mmtfStructureReader.getStructure();
						long timeB = System.nanoTime();
						r.addStructure(code, timeB - timeA);
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
	
	private StructureDataInterface parse(byte[] bytes) {
		MmtfStructure mmtf = ReaderUtils.getDataFromInputStream(
			new ByteArrayInputStream(bytes));
		GenericDecoder gd = new GenericDecoder(mmtf);
		return gd;
	}

}
