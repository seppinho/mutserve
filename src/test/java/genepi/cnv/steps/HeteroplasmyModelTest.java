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
		File input = new File("/home/seb/git/cnv-mutation-server/test-data/tmp/raw.txt");

			System.out.println("input file is " + input.getName());
			try {
				boolean result = model.analyseFile(input.getPath(), refPath, sanger, hetLevel / 100);
				assertEquals(true, result);
			} catch (MathException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}
}