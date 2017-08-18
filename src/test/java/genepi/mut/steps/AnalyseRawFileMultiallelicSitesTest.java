package genepi.mut.steps;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import org.junit.Test;

import genepi.mut.util.MultiallelicAnalyser;
import genepi.mut.util.QCMetric;

public class AnalyseRawFileMultiallelicSitesTest {

	
	@Test
	public void MultiallelicTest() {
		
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
		DecimalFormat df = (DecimalFormat) nf;
		df.setMinimumFractionDigits(2);
		df.setMaximumFractionDigits(3);

		File raw = new File("test-data/lpa/lpa-exome-raw-results/raw.txt");
		File expected = new File("test-data/lpa/lpa-exome-raw-results/stefan.txt");
		File refPath = new File("files/kiv2_6.fasta");
		double hetLevel = 0.01;
		
		MultiallelicAnalyser analyser = new MultiallelicAnalyser();

		ArrayList<QCMetric> metrics = analyser.analyseFile(raw.getPath(), expected.getPath(),
				refPath.getPath(), hetLevel);

		for (QCMetric metric : metrics) {
			String prec = df.format(metric.getPrecision());
			String sens = df.format(metric.getSensitivity());
			String spec = df.format(metric.getSpecificity());
			
			System.out.println("Precision: " + prec + " %");
			System.out.println("Sensitivity: " + sens + " %");
			System.out.println("Specificity: " + spec+ " %");
		}

	}
}
