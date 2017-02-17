package genepi.cnv.steps;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.commons.math.MathException;
import org.junit.Test;

import genepi.cnv.util.RawFileAnalyser;

public class HeteroplasmyModelTest {

	@Test
	public void HetModelTest() {

		double hetLevel = 1;
		String refPath = "files/rcrs.fasta";
		String sanger = "test-data/mtdna/sanger.txt";

		RawFileAnalyser model = new RawFileAnalyser();
		File input = new File("test-data/mtdna/raw-results/");

		for (File file : input.listFiles()) {
			System.out.println("input file is " + file.getName());
			try {
				boolean result = model.analyseFile(file.getPath(), refPath, sanger, hetLevel / 100);
				assertEquals(true, result);
			} catch (MathException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
}