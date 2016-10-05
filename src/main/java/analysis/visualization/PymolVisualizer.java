package analysis.visualization;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PymolVisualizer {

	private List<Chain> chains = new ArrayList<>();

	public void add(Chain c) {
		chains.add(c);
	}	
	
	public void save(File pdb, File py) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(pdb));
			for (Chain c : chains) {
				c.save(bw);
			}
			bw.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
