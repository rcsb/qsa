/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rcsb.mmtf.benchmark.io;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Antonin Pavelka
 */
public class Tar {

	public static void unpack(File in, Path out) throws IOException {
		try (TarArchiveInputStream tis = new TarArchiveInputStream(new FileInputStream(in))) {
			TarArchiveEntry entry = null;
			String name;
			int offset;
			if (!Files.exists(out)) {
				Files.createDirectory(out);
			}
			while ((entry = tis.getNextTarEntry()) != null) {
				name = entry.getName();
				if (entry.isDirectory()) {
					Files.createDirectory(out.resolve(name));
				} else {
					byte[] content = new byte[(int) entry.getSize()];
					offset = 0;
					tis.read(content, offset, content.length - offset);
					try (FileOutputStream fos = new FileOutputStream(out.resolve(name).toFile())) {
						IOUtils.write(content, fos);
					}
				}
			}
		}
	}

	public static void main(String[] args) throws IOException {
		unpack(new File("e:/data/mmtf-benchmark/reduced.tar"),
			Paths.get("e:/data/mmtf-benchmark"));
	}
}
