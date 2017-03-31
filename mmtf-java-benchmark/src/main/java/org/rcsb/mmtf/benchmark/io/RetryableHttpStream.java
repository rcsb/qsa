/*
 * The MIT License
 *
 * Copyright (c) 2009, Sun Microsystems, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.rcsb.mmtf.benchmark.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

/**
 * {@link InputStream} implementation around {@link HttpURLConnection} that automatically reconnects
 * if the connection fails in the middle.
 *
 * The original Kohsuke Kawaguchi version was modified to support files over 2 GB (Integer.MAX_VALUE
 * limitation).
 *
 * @author Kohsuke Kawaguchi
 * @author Antonin Pavelka
 */
public class RetryableHttpStream extends InputStream {

	private final static int MB = (int) Math.round(Math.pow(2d, 20d));

	/**
	 * Where are we downloading from?
	 */
	public final URL url;

	/**
	 * Proxy, or null none is explicitly given (Java runtime may still decide to use a proxy,
	 * though.)
	 */
	protected final Proxy proxy;

	/**
	 * Total bytes of the entity.
	 */
	public final long totalLength;

	/**
	 * Number of bytes read so far.
	 */
	protected long read;

	/**
	 * Current underlying InputStream.
	 */
	private InputStream in;

	/**
	 * Bytes read when report was produced the last time.
	 */
	private long previousRead;

	/**
	 * The report is printed each time when this amount of megabytes is newly downloaded.
	 */
	private Integer reportMegabytes = 100;
	private final int numberOfRetries = 100;

	/**
	 * {@link HttpURLConnection} to allow the caller to access HTTP resposne headers. Do not use
	 * {@link HttpURLConnection#getInputStream()}, however.
	 */
	public final HttpURLConnection connection;

	/**
	 * Connects to the given HTTP/HTTPS URL, by using the proxy auto-configured by the Java runtime.
	 */
	public RetryableHttpStream(URL url) throws IOException {
		this(url, null);
	}

	public void setVerbosity(Integer megabytes) {
		this.reportMegabytes = megabytes;
	}

	/**
	 * Connects to the given HTTP/HTTPS URL, by using the specified proxy.
	 *
	 * @param proxy To force a direct connection, pass in {@link Proxy#NO_PROXY}.
	 */
	public RetryableHttpStream(URL url, Proxy proxy) throws IOException {
		this.url = url;
		if (!url.getProtocol().startsWith("http")) {
			throw new IllegalArgumentException(url + " is not an HTTP URL");
		}
		this.proxy = proxy;

		connection = connect();
		totalLength = connection.getContentLengthLong();
		in = getStream(connection);
	}

	/**
	 * Hook for tests.
	 */
	/*package*/ InputStream getStream(HttpURLConnection con) throws IOException {
		return con.getInputStream();
	}

	/**
	 * Opens the URL and makes a connection.
	 */
	protected HttpURLConnection connect() throws IOException {
		return (HttpURLConnection) (proxy != null ? url.openConnection(proxy) : url.openConnection());
	}

	/**
	 * Reconnect and fast-forward until a desired position.
	 */
	private void reconnect() throws IOException {
		while (true) {
			shallWeRetry();

			HttpURLConnection con = connect();
			con.setRequestProperty("Range", "bytes=" + read + "-");
			con.connect();

			String cr = con.getHeaderField("Content-Range");
			in = getStream(con);
			if (cr != null && cr.startsWith("bytes " + read + "-")) {
				// server responded with a range
				return;
			} else {
				if (reportMegabytes != null) {
					System.out.println("finding position");
				}
				// server sent us the whole thing again. fast-forward till where we want
				long bytesToSkip = read;
				while (true) {
					long l = in.skip(bytesToSkip);
					if (l == 0) {
						break; // hit EOF. do it all over again
					}
					bytesToSkip -= l;
					if (bytesToSkip == 0) {
						return; // fast forward complete
					}
				}
			}
		}
	}

	/**
	 * Subclass can override this method to determine if we should continue to retry, or abort.
	 *
	 * <p>
	 * If this method returns normally, we'll retry. By default, this method retries 100 times then
	 * quits.
	 *
	 * @throws IOException to abort the processing.
	 */
	protected void shallWeRetry() throws IOException {
		if (nRetry++ > numberOfRetries) {
			throw new IOException("Too many failures. Aborting.");
		}
		if (reportMegabytes != null) {
			System.out.println("reconnecting");
		}
	}
	private int nRetry;

	@Override
	public int read() throws IOException {
		while (true) {
			int ch = in.read();
			if (ch >= 0) {
				read++;
				return ch;
			}
			if (read >= totalLength) {
				return -1;  // EOF expected
			}
			reconnect();
		}
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		while (true) {
			int r = in.read(b, off, len);
			if (r >= 0) {
				read += r;
				if (read - previousRead > reportMegabytes * MB) {
					System.out.println("downloaded " + getMb(read) + " / "
						+ getMb(totalLength) + " MB");
					previousRead = read;
				}
				return r;
			}
			if (read >= totalLength) {
				return -1;  // EOF expected
			}
			reconnect();
		}
	}

	private String getMb(long bytes) {
		return Long.toString(bytes / MB);
	}
}
