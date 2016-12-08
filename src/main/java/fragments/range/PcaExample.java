package fragments.range;

import java.util.LinkedList;
import java.util.Random;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.linalg.Matrix;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.linalg.distributed.RowMatrix;

public class PcaExample {
	public static void main(String[] args) {
		System.out.println(Runtime.getRuntime().freeMemory() + " mem");
		SparkConf conf; // = new SparkConf().setAppName("PCA Example");

		conf = new SparkConf().setMaster("local[*]").setAppName("asdjkfhas");

		SparkContext sc = new SparkContext(conf);
		Random random = new Random(1);
		double[][] array = new double[1000000][400];
		for (int x = 0; x < array.length; x++) {
			for (int y = 0; y < array[x].length; y++) {
				array[x][y] = random.nextDouble();
			}
		}
		LinkedList<Vector> rowsList = new LinkedList<Vector>();
		for (int i = 0; i < array.length; i++) {
			Vector currentRow = Vectors.dense(array[i]);
			rowsList.add(currentRow);
		}
		JavaRDD<Vector> rows = JavaSparkContext.fromSparkContext(sc).parallelize(rowsList);
System.out.println("aaa");
		// Create a RowMatrix from JavaRDD<Vector>.
		RowMatrix mat = new RowMatrix(rows.rdd());

		// Compute the top 3 principal components.
		Matrix pc = mat.computePrincipalComponents(3);
		RowMatrix projected = mat.multiply(pc);
		System.out.println("ccc " + projected.numCols() + " " + projected.numRows());
	}
}