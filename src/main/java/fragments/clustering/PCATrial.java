package fragments.clustering;

import Jama.Matrix;
import com.mkobos.pca_transform.PCA;
import fragments.FragmentsFactory;
import fragments.Word;
import io.Directories;
import java.util.ArrayList;
import java.util.List;
import pdb.Entries;
import pdb.MmtfStructureProvider;
import pdb.SimpleStructure;

public class PCATrial {

    private Directories dir = Directories.createDefault();

    public static void main(String[] args) {

        PCATrial p = new PCATrial();
        p.generateWords();

        System.out.println("Running a demonstration program on some sample data ...");
        /**
         * Training data matrix with each row corresponding to data point and
         * each column corresponding to dimension.
         */
        Matrix trainingData = new Matrix(new double[][]{
            {1, 2, 3, 4, 5, 6},
            {6, 5, 4, 3, 2, 1},
            {2, 2, 2, 2, 2, 2}});
        PCA pca = new PCA(trainingData);
        /**
         * Test data to be transformed. The same convention of representing data
         * points as in the training data matrix is used.
         */
        Matrix testData = new Matrix(new double[][]{
            {1, 2, 3, 4, 5, 6},
            {1, 2, 1, 2, 1, 2}});
        /**
         * The transformed test data.
         */
        Matrix transformedData
                = pca.transform(testData, PCA.TransformationType.WHITENING);
        System.out.println("Transformed data (each row corresponding to transformed data point):");
        for (int r = 0; r < transformedData.getRowDimension(); r++) {
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

    public void generateWords() {
        //String[] cases = {"1fxi", "1ubq", "1ten", "3hhr", "3hla", "2rhe", "2aza", "1paz", "1cew", "1mol", "1cid", "2rhe", "1crl", "1ede", "2sim", "1nsb", "1bge", "2gmf", "1tie", "4fgf"};
        Entries entries = new Entries(dir.getPdbEntryTypes());
        List<String> cases = entries.getCodes();
        List<Word> words = new ArrayList<>();
        Directories dir = Directories.createDefault();
        int i = 0;
        int fail = 0;
        for (String code : cases) {
            try {
                
                MmtfStructureProvider provider = new MmtfStructureProvider(dir.getMmtf().toPath());
                SimpleStructure s = provider.getStructure(code);
                FragmentsFactory ff = new FragmentsFactory();
                List<Word> ws=  ff.createWords(s,1);
                words.addAll(ws);
                System.out.println((i++) + " / " + cases.size() + " " + code + " " + words.size());
            } catch (Exception ex) {
                fail++;                
                ex.printStackTrace();
            }
        }
        System.out.println("   " + words.size());
        System.out.println("fails " + fail);
    }
}
