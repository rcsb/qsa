package test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.vecmath.Matrix4d;

import io.Directories;
import io.LineFile;

class PdbCodePair {
	private String a, b;

	public PdbCodePair(String a, String b) {
		if (a.compareToIgnoreCase(b) < 0) {
			this.a = a.toLowerCase();
			this.b = b.toLowerCase();
		} else {
			this.a = b.toLowerCase();
			this.b = a.toLowerCase();
		}
	}

	public boolean equals(Object o) {
		PdbCodePair other = (PdbCodePair) o;
		return a.equals(other.a) && b.equals(other.b);
	}

	@Override
	public int hashCode() {
		return a.hashCode() * 100001 + b.hashCode();
	}
}

class TestableOutput {
	double[] values = new double[16];

	public TestableOutput(Matrix4d m) {
		int i = 0;
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				values[i++] = m.getElement(x, y);
			}
		}
	}

	public TestableOutput(double[] vs) {
		values = vs;
	}

	public boolean similar(TestableOutput other) {
		for (int i = 0; i < 16; i++) {
			double d = Math.abs(values[i] - other.values[i]);
			if (d > 0.01) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (double d : values) {
			sb.append(d).append(" ");
		}
		return sb.toString();
	}
}

public class MatrixTest {

	private String name;
	private LineFile lf;
	private boolean test = false;
	private Map<PdbCodePair, TestableOutput> cases = new HashMap<>();

	public MatrixTest(String name) {
		this.name = name;
		File f = getFile();
		lf = new LineFile(f);
		if (f.exists()) {
			for (String line : lf.readLines()) {
				StringTokenizer st = new StringTokenizer(line);
				String a = st.nextToken();
				String b = st.nextToken();
				PdbCodePair p = new PdbCodePair(a, b);
				double[] values = new double[16];
				for (int i = 0; i < 16; i++) {
					values[i] = Double.parseDouble(st.nextToken());
				}
				TestableOutput to = new TestableOutput(values);
				cases.put(p, to);
			}
		}
	}

	/**
	 * Tests iff test == true. Saves test case otherwise.
	 */
	public void addTestCase(String a, String b, Matrix4d m) {
		if (test) {
			PdbCodePair p = new PdbCodePair(a, b);
			TestableOutput t = new TestableOutput(m);
			TestableOutput other = cases.get(p);
			if (!t.similar(other)) {
				System.out.println("TEST FAILED for " + a + " " + b);
				System.out.println(t);
				System.out.println(other);
			}
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append(a).append(" ").append(b).append(" ");
			for (int x = 0; x < 4; x++) {
				for (int y = 0; y < 4; y++) {
					sb.append(m.getElement(x, y)).append(" ");
				}
			}
			lf.writeLine(sb.toString());
		}
	}

	private File getFile() {
		return Directories.createDefault().getMatrixTest(name);
	}

	public boolean test() {
		return test;
	}

}
