package org.rcsb.mmtf.benchmark;

import io.Directories;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import profiling.ProfilingFileUtils;

public class Downloader {

	private List<String> codes;
	private Directories dirs;

	public Downloader(Directories dirs) {
		this.dirs = dirs;
	}

	public void downloadMmtf() {
		Counter c = new Counter();
		for (String code : codes) {
			Path p = dirs.getMmtfPath(code);
			ProfilingFileUtils.downloadMmtf(code, p);
			c.next();
		}
	}
	
	public void downloadPdb() {
		Counter c = new Counter();
		for (String code : codes) {
			Path p = dirs.getPdbPath(code);
			if (Files.notExists(p)) {
				ProfilingFileUtils.downloadPdb(code, p);
			}
			c.next();
		}
	}

	public void downloadHadoopSequenceFile() {
		try {
			Path p = dirs.getSub("hsf");
			if (Files.notExists(p)) {
				ProfilingFileUtils.download(
					"http://mmtf.rcsb.org/v1.0/hadoopfiles/full.tar", p);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

// url=http://www.example.com/testFile.zip
// localFile=/path/to/testFile.zip
	public static void download(String url, String localFile) throws Exception {

		System.out.println("Downloading " + localFile);

		boolean downloadComplete = false;

		while (!downloadComplete) {
			downloadComplete = transferData(url, localFile);
		}

	}

	public static boolean transferData(String url, String filename) throws Exception {

		long transferedSize = getFileSize(filename);

		URL website = new URL(url);
		URLConnection connection = website.openConnection();
		System.out.println(transferedSize);
		connection.setRequestProperty("Range", "bytes=" + transferedSize + "-");
		ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
		long remainingSize = connection.getContentLength();
		long buffer = remainingSize;
		if (remainingSize > 65536) {
			buffer = 1 << 16;
		}
		System.out.println("Remaining size: " + remainingSize);

		System.out.println(transferedSize + " == " + remainingSize);
		if (transferedSize == remainingSize) {
			System.out.println("File is complete");
			rbc.close();
			return true;
		}

		FileOutputStream fos = new FileOutputStream(filename, true);

		System.out.println("Continue downloading at " + transferedSize);
		while (remainingSize > 0) {
			long delta = fos.getChannel().transferFrom(rbc, transferedSize, buffer);
			transferedSize += delta;
			System.out.println(transferedSize + " bytes received");
			if (delta == 0) {
				break;
			}
		}
		fos.close();
		System.out.println("Download incomplete, retrying");

		return false;

	}

	public static long getFileSize(String file) {
		File f = new File(file);
		System.out.println("Size: " + f.length());
		return f.length();
	}

	public static void main(String[] args) throws Exception {
		Directories dirs = new Directories(new File("c:/kepler/data/download"));
		Downloader d = new Downloader(dirs);
		d.downloadHadoopSequenceFile();
		//Downloader.download("http://mmtf.rcsb.org/v1.0/full/4HHB.mmtf.gz",
		//	dirs.getHome().getAbsolutePath() + "/down");
	}
}
