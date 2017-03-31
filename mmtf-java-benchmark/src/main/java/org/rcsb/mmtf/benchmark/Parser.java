package org.rcsb.mmtf.benchmark;

import org.rcsb.mmtf.benchmark.io.Directories;
import org.rcsb.mmtf.benchmark.io.HadoopReader;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.io.FileParsingParameters;
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
		FileParsingParameters fpp = new FileParsingParameters();
		fpp.setAlignSeqRes(false);
		fpp.setCreateAtomBonds(false);
		fpp.setParseSecStruc(false);
		pdbReader.setFileParsingParameters(fpp);
		cifReader.setFileParsingParameters(fpp);
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
	
	public Structure parseMmtfReducedToBiojava(String pdbCode) {
		try {
			MmtfStructureReader mmtfStructureReader = new MmtfStructureReader();
			byte[] array = Files.readAllBytes(dirs.getMmtfReducedPath(pdbCode));
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
			Path f = dirs.getPdbPath(code);
			if (!Files.exists(f)) {
				// large files are not available in PDB file format
				return null;
			}			
			Structure s = pdbReader.getStructure(f.toString());
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

	public List<String> parseHadoop(File hsfDir) {
		List<String> fails = new ArrayList<>();
		try {
			Counter c = new Counter("hadoop sequence file parsing ", 10, 0);
			for (File f : hsfDir.listFiles()) {
				if (f.getName().startsWith(".") || f.getName().startsWith("_")) {
					continue;
				}
				HadoopReader hr = new HadoopReader(f.toString());
				while (hr.next()) {
					String code = "empty";
					try {
						StructureDataInterface sdi = parse(hr.getBytes());
						code = hr.getKey();
						MmtfStructureReader mmtfStructureReader = new MmtfStructureReader();
						new StructureDataToAdapter(sdi, mmtfStructureReader);
						Structure sb = mmtfStructureReader.getStructure();
						c.next();
					} catch (Exception ex) {
						fails.add(code);
						System.err.println("Error in parsing HSF, last PDB code " + code);
						ex.printStackTrace();
					}
				}
				hr.close();
			}
			return fails;
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private StructureDataInterface parse(byte[] bytes) throws IOException, ParseException {
		MmtfStructure mmtf = ReaderUtils.getDataFromInputStream(
			new ByteArrayInputStream(bytes));
		GenericDecoder gd = new GenericDecoder(mmtf);
		return gd;
	}

}
