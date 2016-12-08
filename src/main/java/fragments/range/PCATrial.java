package fragments.range;

import Jama.Matrix;
import com.mkobos.pca_transform.PCA;
import fragments.FragmentsFactory;
import fragments.Word;
import geometry.Transformer;
import io.Directories;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import pdb.Entries;
import pdb.MmtfStructureProvider;
import pdb.SimpleStructure;
import vectorization.SmartVectorizer;

public class PCATrial {

    private Directories dir = Directories.createDefault();
    private Random random = new Random(1);
    private double[] rmsds;
    private double[][] positives;
    private double[][] negatives;

    public static void main(String[] args) {

        PCATrial m = new PCATrial();
        m.run();
    }

    public void run() {
        if (false) {
            generateWordPairs(1000 * 1000);
        }
        WordVectorReader r = new WordVectorReader();
        VectorData data = r.read(dir.getDistances(), 2);
        data.optimize();
        //data.accuracy();
        //pca(positives);
        //pca(negatives);
    }

    public void pca(double[][] m) {
        PCATrial p = new PCATrial();
        //    p.generateWordPairs();

        System.out.println("Running a demonstration program on some sample data ...");
        /**
         * Training data matrix with each row corresponding to data point and
         * each column corresponding to dimension.
         */

        Matrix trainingData = new Matrix(m);

        /*Matrix trainingData = new Matrix(new double[][]{
            {1, 2, 3, 4, 5, 6},
            {6, 5, 4, 3, 2, 1},
            {2, 2, 2, 2, 2, 2}});*/
        PCA pca = new PCA(trainingData);
        /**
         * Test data to be transformed. The same convention of representing data
         * points as in the training data matrix is used.
         */

        double[][] tm = new double[m[0].length][];
        for (int i = 0; i < m[0].length; i++) {
            tm[i] = new double[m[0].length];
            tm[i][i] = 1;
        }

        Matrix testData = new Matrix(m);
        /*Matrix testData = new Matrix(new double[][]{
            {1, 2, 3, 4, 5, 6},
            {1, 2, 1, 2, 1, 2}});
        /**
         * The transformed test data.
         */
        Matrix transformedData
                = pca.transform(testData, PCA.TransformationType.WHITENING);
        System.out.println("Transformed data (each row corresponding to transformed data point):");
        for (int r = 0; r < transformedData.getRowDimension(); r++) {
//            System.out.print(values[r] + "|  ");
            for (int c = 0; c < transformedData.getColumnDimension(); c++) {

                System.out.print(transformedData.get(r, c));
                if (c == transformedData.getColumnDimension() - 1) {
                    continue;
                }
                System.out.print(", ");
            }
            System.out.println("");
        }
    }

    public Word[] getRandomWord(String code, int n) {
        Word[] words = new Word[n];
        MmtfStructureProvider provider = new MmtfStructureProvider(dir.getMmtf().toPath());
        SimpleStructure s = provider.getStructure(code);
        FragmentsFactory ff = new FragmentsFactory();
        List<Word> ws = ff.createWords(s, 1);
        if (ws.size() > 10) {
            for (int i = 0; i < n; i++) {
                words[i] = ws.get(random.nextInt(ws.size()));
            }
            return words;
        } else {
            return null;
        }
    }

    public void generateWordPairs(int n) {
        Entries entries = new Entries(dir.getPdbEntryTypes());
        List<String> codes = entries.getCodes();
        Transformer tr = new Transformer();
        int i = 0;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(dir.getDistances()))) {
            while (i <= n) {
                if (i % 1000 == 0) {
                    System.out.println(" i " + i);
                }
                try {
                    Word[][] ws = new Word[2][];
                    for (int w = 0; w < 2; w++) {
                        ws[w] = getRandomWord(codes.get(random.nextInt(codes.size())), 50);
                    }
                    if (ws[0] != null && ws[1] != null) {

                        for (Word x : ws[0]) {
                            for (Word y : ws[1]) {
                                tr.set(x.getPoints3d(), y.getPoints3d());
                                double rmsd = tr.getRmsd();
                                //double euclidean = ws[0].getEuclidean(ws[1]);
                                //double manhattan = ws[0].getManhattan(ws[1]);
                                //double chebyshev = ws[0].getChebyshev(ws[1]);
                                //bw.write(rmsd + "," + euclidean + "," + manhattan + "," + chebyshev + "\n");
                                bw.write(rmsd + " ");
                                double[] a = new SmartVectorizer(x).getVector();
                                double[] b = new SmartVectorizer(y).getVector();
                                for (int k = 0; k < a.length; k++) {
                                    double d = Math.abs(a[k] - b[k]);
                                    bw.write("," + d);
                                }
                                bw.write("\n");
                                i++;
                            }
                        }

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
