package genepi.cnv.steps;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.math.MathException;
import org.junit.Test;

import genepi.cnv.util.QCMetric;
import genepi.cnv.util.RawFileAnalyser;

public class AnalyseRawFileVariantsTest {

	@Test
	public void HetModelTest() {

		double hetLevel = 1;
		String refPath = "test-data/mtdna/raw-results/rCRS.fasta";
		String sanger = "test-data/mtdna/raw-results/sanger.txt";

		RawFileAnalyser model = new RawFileAnalyser();
		File input = new File("test-data/mtdna/raw-results/raw-s4.txt");

			System.out.println("input file is " + input.getName());
				try {
					ArrayList<QCMetric> metrics = model.analyseFile(input.getPath(), refPath, sanger, hetLevel / 100);
					for(QCMetric  metric : metrics){
						assertEquals(100, metric.getPrecision(), 0);
						assertEquals(59.259, metric.getSensitivity(), 0.1);
						assertEquals(100, metric.getSpecificity(), 0);
					}
					
					
				} catch (MathException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		

	}
}