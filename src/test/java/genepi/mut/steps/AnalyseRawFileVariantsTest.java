package genepi.mut.steps;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;

import org.junit.Test;

import genepi.mut.util.QCMetric;
import genepi.mut.util.RawFileAnalysermtDNA;
import genepi.mut.util.RawFileAnalyserDNA;

public class AnalyseRawFileVariantsTest {

	@Test
	public void HetModelTest() {

		double hetLevel = 0.01;
		String refPath = "test-data/mtdna/raw-results/rCRS.fasta";
		String sanger = "test-data/mtdna/raw-results/sanger.txt";

		RawFileAnalysermtDNA rawAnalyser = new RawFileAnalysermtDNA();
		rawAnalyser.setCallDel(false);
		File input = new File("test-data/mtdna/raw-results/raw-s4-nodel.txt");

			System.out.println("input file is " + input.getName());
					ArrayList<QCMetric> metrics = rawAnalyser.calculateLowLevelForTest(input.getPath(), refPath, sanger, hetLevel);
					for(QCMetric  metric : metrics){
						assertEquals(100, metric.getPrecision(), 0);
						// paper value, changed since I added checkbases check: assertEquals(59.259, metric.getSensitivity(), 0.1);
						assertEquals(64, metric.getSensitivity(), 0.1);
						assertEquals(100, metric.getSpecificity(), 0);
					}
	}
	
	
	@Test
	public void Plasmid12Test() {

		double hetLevel = 0.01;

		String refPath = "test-data/dna/plasmids/reference/kiv2_6.fasta";
		String sanger = "test-data/dna/plasmids/plasmid12/gold/plasmid12_major.txt";

		RawFileAnalyserDNA rawAnalyser = new RawFileAnalyserDNA();
		File input = new File("test-data/dna/plasmids/plasmid12/raw/plasmid12-raw.txt");

			System.out.println("input file is " + input.getName());
					ArrayList<QCMetric> metrics = rawAnalyser.calculateLowLevelForTest(input.getPath(), refPath, sanger, hetLevel);
	}
	
	@Test
	public void Plasmid13Test() {

		double hetLevel = 0.01;

		String refPath = "test-data/dna/plasmids/reference/kiv2_6.fasta";
		String sanger = "test-data/dna/plasmids/plasmid13/gold/plasmid13_minor.txt";

		RawFileAnalyserDNA rawAnalyser = new RawFileAnalyserDNA();
		File input = new File("test-data/dna/plasmids/plasmid13/raw/plasmid13-raw.txt");

			System.out.println("input file is " + input.getName());
					ArrayList<QCMetric> metrics = rawAnalyser.calculateLowLevelForTest(input.getPath(), refPath, sanger, hetLevel);
					
	}		
				

}