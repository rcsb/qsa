package fragments.vector;

import geometry.Transformer;
import io.Directories;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.vecmath.Point3d;
import pdb.PdbLine;
import util.Histogram;

public class Points {

	private final long RANDOM_ITERATIONS = 100000000;
	private final int MAX_SAMPLE_N = 10000;
	private final int WALK = 1;
	private final int dim = 3;
	//private final int objectDim = 100;
	private double[][] vectors;
	private Point3d[][] words;
	private int size;
	private Random random = new Random(2);
	private final Directories dirs = Directories.createDefault();
	private final Transformer transformer = new Transformer();
	private double totalError = 0;
	private double worstOfWorst = 0;
	private long totalCounter = 0;
	private double initSpeed;
	private double speedChange = 1.5;
	private double speedChangeDivisor = 1.5;

	private int sampleN;
	private double[][] sampleVectors;
	private double[] rmsds;

	double[] distantSample;
	double maxRmsd;

	double[] vector = new double[dim];

	public Points() throws Exception {
		size = 0;
		words = PointVectorDataset.read(dirs.getWordRepresentants("2"));
		vectors = new double[words.length][dim];
		int counter = 0;
		for (Point3d[] w : words) {
			add(w);
			if (counter++ > 60) {
				break;
			}
		}
		System.out.println("VECTORS");
		double m = 100;
		double[][] errors = new double[size][size];
		Histogram histogram = new Histogram(10);
		try (BufferedWriter bw = new BufferedWriter(new FileWriter("c:/kepler/rozbal/errors.csv"))) {
			for (int x = 0; x < errors.length; x++) {
				for (int y = 0; y < x; y++) {
					errors[x][y] = Math.abs(rmsd(words[x], words[y]) - euclidean(vectors[x], vectors[y]));
					bw.write(errors[x][y] + "\n");
					histogram.add(errors[x][y]);
				}
			}
		}
		histogram.print();

		System.out.println("! " + vectors[7][0]);

		try (BufferedWriter bw = new BufferedWriter(new FileWriter("c:/kepler/rozbal/vectors.pdb"))) {
			int serial = 1;

			double min = Double.POSITIVE_INFINITY;
			double avg = 0;
			for (int i = 0; i < landscapeValues.size(); i++) {
				double v = landscapeValues.get(i);
				avg += v;
				if (v < min) {
					min = v;
				}
			}
			avg /= landscape.size();

			for (int i = 0; i < size; i++) {
				double[] v = vectors[i];
				Point3d[] word = words[i];
				boolean first = true;
				for (int j = 0; j < word.length; j++) {
					Point3d a = word[j];
					PdbLine pl = new PdbLine(serial, "C", "C", "ALA", "" + serial, 'A',
						v[0] * m + a.x, v[1] * m + a.y, v[2] * m + a.z);
					double worst = 0;
					for (double e : errors[i]) {
						if (worst < e) {
							worst = e;
						}
					}
					pl.setTemperatureFactor(avg);
					bw.write(pl.toString() + "\n");
					if (!first) {
						bw.write(PdbLine.getConnectString(serial - 1, serial) + "\n");
					} else {
						first = false;
					}
					serial++;
				}
			}

			for (int i = 0; i < landscape.size(); i++) {
				double[] a = landscape.get(i);
				double v = landscapeValues.get(i);
				if (v < min * 10.3) {

					PdbLine pl = new PdbLine(serial, "C", "C", "ALA", "" + serial, 'B',
						m * a[0], m * a[1], m * a[2]);
					pl.setTemperatureFactor(v);
					bw.write(pl.toString() + "\n");
					if (i != 0) {
						//bw.write(PdbLine.getConnectString(serial - 1, serial) + "\n");
					}
					serial++;
				}
			}
		}
	}

	private double[] center(double[][] points) {
		int n = points.length;
		double[] c = new double[points[0].length];
		for (int x = 0; x < n; x++) {
			for (int y = 0; y < c.length; y++) {
				c[y] += points[x][y];
			}
		}
		for (int i = 0; i < c.length; i++) {
			c[i] /= n;
		}
		return c;
	}

	private void init(double[] x, double[] distantSample, double maxRmsd) {
		for (int i = 0; i < dim; i++) {
			x[i] = distantSample[i] + (random.nextDouble() - 0.5) * 2 * maxRmsd * 1.1;
		}
	}

	private void first(double[] x) {
		for (int i = 0; i < dim; i++) {
			x[i] = 0;
		}
	}

	public void plus(double[] x, final double[] y) {
		for (int i = 0; i < x.length; i++) {
			x[i] += y[i];
		}
	}

	public void invert(double[] x) {
		for (int i = 0; i < x.length; i++) {
			x[i] = -x[i];
		}
	}

	public void minus(double[] x, final double[] y) {
		for (int i = 0; i < x.length; i++) {
			x[i] -= y[i];
		}
	}

	public double[] vector(final double[] x, final double[] y) {
		double[] v = new double[dim];
		for (int i = 0; i < x.length; i++) {
			v[i] = y[i] - x[i];
		}
		return v;
	}

	public void divide(double[] x, double d) {
		for (int i = 0; i < x.length; i++) {
			x[i] /= d;
		}
	}

	public void multiply(double[] x, double d) {
		for (int i = 0; i < x.length; i++) {
			x[i] *= d;
		}
	}

	public double[] copy(double[] v) {
		double[] w = new double[v.length];
		System.arraycopy(v, 0, w, 0, v.length);
		return w;
	}

	public double size(double[] v) {
		double sum = 0;
		for (int i = 0; i < v.length; i++) {
			sum += v[i] * v[i];

		}
		return (double) Math.sqrt(sum);
	}

	public void finite(double[] fs) {
		for (double f : fs) {
			assert Double.isFinite(f);
		}
	}

	private String s(double[] fs) {
		StringBuilder sb = new StringBuilder("[");
		for (double f : fs) {
			sb.append(f).append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("]");
		return sb.toString();
	}

	public String nice(double d) {
		return "" + (double) Math.round(d * 100) / 100;
	}

	public double rmsd(Point3d[] a, Point3d[] b) {
		transformer.set(a, b);

		//System.out.println(transformer.getRmsd() + " <> " + transformer.getSumOfDifferences());
		//return transformer.getRmsd();
		return transformer.getSumOfDifferences();
	}

	public double euclidean(double[] x, double[] y) {
		double f = 0;
		for (int d = 0; d < x.length; d++) {
			double dif = x[d] - y[d];
			f += dif * dif;
		}
		f = Math.sqrt(f);
		return f;
	}

	private double[] randomDirection() {
		double[] v = new double[dim];
		for (int i = 0; i < dim; i++) {
			v[i] = (random.nextDouble() - 0.5);
			divide(v, size(v));
			multiply(v, initSpeed);
		}
		return v;
	}

	List<double[]> landscape = new ArrayList<>();
	List<Double> landscapeValues = new ArrayList<>();

	private double measure() {
		double worst = 0;
		for (int i = 0; i < sampleN; i++) {
			double e = Math.abs(euclidean(vector, sampleVectors[i]) - rmsds[i]);
			if (e > worst) {
				worst = e;
			}
		}
		return worst;
	}

	private void randomSampling() {
		long iterations = 100000;
		for (int iteration = 0; iteration < 1 * iterations; iteration++) {
			if (size == 0) {
				first(vector);
			} else {
				init(vector, distantSample, maxRmsd);
			}
			double error = measure();
		}
	}

	public void add(Point3d[] word) {
		double globalMin = Double.MAX_VALUE;
		double[] winner = null;
		sampleN = Math.min(size, MAX_SAMPLE_N);
		sampleVectors = new double[sampleN][];
		rmsds = new double[sampleN];
		distantSample = null;
		maxRmsd = Double.NEGATIVE_INFINITY;

		List<Integer> candidates = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			candidates.add(i);
		}
		for (int i = 0; i < sampleN; i++) {
			int r = candidates.remove(random.nextInt(candidates.size()));
			sampleVectors[i] = vectors[r];
			rmsds[i] = rmsd(word, words[r]);
			if (maxRmsd < rmsds[i]) {
				maxRmsd = rmsds[i];
				distantSample = vectors[r];
			}
		}
		System.out.println();
		//System.out.println("maxRmsd " + maxRmsd);
		//System.out.println("---");
		double bestDescentError = Double.POSITIVE_INFINITY;
		double speedSize = 888;
		Random globalRandom = new Random(100);

		int example = 52;

		long iterations;
		iterations = RANDOM_ITERATIONS;

		randomSampling();

		for (int iteration = 0; iteration < iterations; iteration++) {
			initSpeed = 0.05;
			//random = new Random(globalRandom.nextInt());
			if (size == 0) {
				first(vector);
			} else {
				init(vector, distantSample, maxRmsd);
			}

			int moving = 0;
			double[] direction = null;
			boolean progress = false;
			int stall = 0;

			if (size > 1) {
				//	System.out.println("--- " + bestDescentError);
			}
			long restarts = 0;
			long oscilation = 0;

			bestDescentError = Double.POSITIVE_INFINITY;
			long walk = WALK;
			//long walk = 1000;
			if (size == 0) {
				walk = 1;
			}
			boolean inverted = false;
			for (int o = 0; o < walk; o++) { // WHY is avg worse when more reps?????
				// does it start at the beginning?
				// is avg correct?
				double[] oldVector = copy(vector);
				if (o != 0) {
					if (direction == null) {
						direction = randomDirection();
					} else {
						if (!progress) {
							if (!inverted) {
								invert(direction);
								inverted = true;
								if (size > 0) {
									//System.out.println("INVERSION");
								}
							}
						}
					}
					plus(vector, direction);
				}
				double worst = measure();
				if (size == example && worst < 2) {
					double[] log = copy(vector);
					landscape.add(log);
					landscapeValues.add(worst);
				}
				if (direction != null) {
					speedSize = size(direction);
				}
				if (worst < bestDescentError) {
					restarts = 0;
					oscilation = 0;
					if (o != 0) {
						progress = true;
						stall = 0;
						moving++;
						if (size > 1) {
							//	System.out.println("-> (" + nice(worst) + ") " + speedSize + " : " + o);
						}
						multiply(direction, speedChange);
					}
					bestDescentError = worst;
				} else {
					//if (o != 0 && size > 1) {
					//	System.out.println("|  (" + nice(worst) + ") " + speedSize);
					//}
					progress = false;
					stall++;
					vector = oldVector; // do not move if solution is not better
					if (stall > 2) {
						divide(direction, speedChangeDivisor);
					}
					if (direction != null) {
						if (speedSize < 0.01) {
							direction = randomDirection();
							inverted = false;
							restarts++;
							oscilation++;
							stall = 0;
							if (size > 0) {
								//System.out.println("RESTART");
							}
						}
					}
				}
				/*if (oscilation > 200) {
					if (initSpeed > 0.001) {
						initSpeed /= 1.3;
					}
					oscilation = 0;
				}*/
				if (restarts > 10000) {
					break;
				}
				//System.out.println(s(vector));
			}
			//System.out.println("end of walk error: " + bestDescentError);
			if (bestDescentError < globalMin) {
				globalMin = bestDescentError;
				winner = copy(vector);
			}
			if (size > 3) {
				//	System.out.println("moving " + ((double) moving / walk));
			}
		}
		if (worstOfWorst < globalMin) {
			worstOfWorst = globalMin;
		}
		if (size > 0) {
			totalError += globalMin;
			totalCounter++;
		}
		vectors[size] = winner;
		words[size] = word;
		size++;
		System.out.println("avg " + nice(totalError / totalCounter) + " current "
			+ nice(globalMin) + " historical " + worstOfWorst + " (" + size + ")");
	}

	public static void main(String[] args) throws Exception {
		new Points();
	}

}
