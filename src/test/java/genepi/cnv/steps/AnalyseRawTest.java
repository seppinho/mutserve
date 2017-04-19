package genepi.cnv.steps;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.math.MathException;
import org.junit.Test;

import genepi.cnv.util.QCMetric;
import genepi.cnv.util.RawFileAnalyser;

public class AnalyseRawTest {

	@Test
	public void HetModelTest() {

		double hetLevel = 1;
		String refPath = "files/rcrs.fasta";
		String sanger = "test-data/mtdna/sanger.txt";

		RawFileAnalyser model = new RawFileAnalyser();
		File input = new File("test-data/tmp/raw.txt");

			System.out.println("input file is " + input.getName());
			try {
				ArrayList<QCMetric> metrics = model.analyseFile(input.getPath(), refPath, sanger, hetLevel / 100);
			} catch (MathException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}
}