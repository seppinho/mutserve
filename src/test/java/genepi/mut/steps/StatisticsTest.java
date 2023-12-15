package genepi.mut.steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import genepi.mut.commands.StatisticsCommand;
import genepi.mut.util.report.CloudgeneReport;



public class StatisticsTest {
	
	private static final String CLOUDGENE_LOG = "cloudgene.report.json";
	
	@Test
	public void testReadStatistics() throws Exception {

		String inputFolder = "test-data/statistics/sample_statistics.txt";


		StatisticsCommand command = new StatisticsCommand();
		command.setInput(inputFolder);
		command.setOutput("excluded_samples.txt");

		assertEquals(0, (int) command.call());
		
		CloudgeneReport CloudgeneLog = new CloudgeneReport(CLOUDGENE_LOG);

		assertTrue(CloudgeneLog.hasInMemory("Passed Samples: 2"));
		assertTrue(CloudgeneLog.hasInMemory("[OK]"));


	}
	
	@Test
	public void testWithTwoContigs() throws Exception {

		String inputFolder = "test-data/statistics/sample_statistics_two_contigs.txt";


		StatisticsCommand command = new StatisticsCommand();
		command.setInput(inputFolder);
		command.setOutput("excluded_samples.txt");

		assertEquals(-1, (int) command.call());
		
		CloudgeneReport CloudgeneLog = new CloudgeneReport(CLOUDGENE_LOG);

		assertTrue(CloudgeneLog.hasInMemory("Different contigs have been detected"));
		assertTrue(CloudgeneLog.hasInMemory("[ERROR]"));


	}
	
	@Test
	public void testWithBadQualityDepth() throws Exception {

		String inputFolder = "test-data/statistics/sample_statistics_bad_quality_depth.txt";


		StatisticsCommand command = new StatisticsCommand();
		command.setInput(inputFolder);
		command.setOutput("excluded_samples.txt");

		assertEquals(0, (int) command.call());
		
		CloudgeneReport CloudgeneLog = new CloudgeneReport(CLOUDGENE_LOG);
		assertTrue(CloudgeneLog.hasInMemory("1 sample(s) with mean depth of < 50 have been excluded"));


	}
	
	@Test
	public void testWithBadQualityDepthAndPercentage() throws Exception {

		String inputFolder = "test-data/statistics/sample_statistics_bad_quality_depth_percentage.txt";


		StatisticsCommand command = new StatisticsCommand();
		command.setInput(inputFolder);
		command.setOutput("excluded_samples.txt");

		assertEquals(-1, (int) command.call());
		
		CloudgeneReport CloudgeneLog = new CloudgeneReport(CLOUDGENE_LOG);
		assertTrue(CloudgeneLog.hasInMemory("No input samples passed the QC step"));


	}
	
	@Test
	public void testWithNoContig() throws Exception {

		String inputFolder = "test-data/statistics/sample_statistics_no_contig.txt";


		StatisticsCommand command = new StatisticsCommand();
		command.setInput(inputFolder);
		command.setOutput("excluded_samples.txt");

		assertEquals(0, (int) command.call());
		
		CloudgeneReport CloudgeneLog = new CloudgeneReport(CLOUDGENE_LOG);
		assertTrue(CloudgeneLog.hasInMemory("Passed Samples: 1"));


	}
	

}
