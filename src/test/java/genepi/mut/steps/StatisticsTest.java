package genepi.mut.steps;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import genepi.mut.commands.StatisticsCommand;
import genepi.mut.util.report.OutputReader;


public class StatisticsTest {
	
	private static final String CLOUDGENE_LOG = "cloudgene.report";
	
	@Test
	public void testReadStatistics() throws Exception {

		String inputFolder = "test-data/statistics/sample_statistics.txt";

		StatisticsCommand command = buildCommand(inputFolder);

		assertEquals(0, (int) command.call());
		
		OutputReader log = new OutputReader(CLOUDGENE_LOG);

		assertTrue(log.hasInMemory("Passed Samples: 2"));


	}
	
	@Test
	public void testWithTwoContigs() throws Exception {

		String inputFolder = "test-data/statistics/sample_statistics_two_contigs.txt";

		StatisticsCommand command = buildCommand(inputFolder);

		assertEquals(1, (int) command.call());
		
		OutputReader log = new OutputReader(CLOUDGENE_LOG);

		assertTrue(log.hasInMemory("Different mtDNA contig names have been detected in your input files."));

	}
	
	@Test
	public void testWithBadQualityDepth() throws Exception {

		String inputFolder = "test-data/statistics/sample_statistics_bad_quality_depth.txt";

		StatisticsCommand command = buildCommand(inputFolder);

		assertEquals(0, (int) command.call());
		
		OutputReader log = new OutputReader(CLOUDGENE_LOG);
		assertTrue(log.hasInMemory("1 sample(s) with mean depth of < 50 have been excluded"));


	}
	
	@Test
	public void testWithBadQualityDepthAndPercentage() throws Exception {

		String inputFolder = "test-data/statistics/sample_statistics_bad_quality_depth_percentage.txt";

		StatisticsCommand command = buildCommand(inputFolder);

		assertEquals(1, (int) command.call());
		
		OutputReader log = new OutputReader(CLOUDGENE_LOG);
		assertTrue(log.hasInMemory("No input samples passed the QC step"));


	}
	
	@Test
	public void testWithNoContig() throws Exception {

		String inputFolder = "test-data/statistics/sample_statistics_no_contig.txt";

		StatisticsCommand command = buildCommand(inputFolder);

		assertEquals(0, (int) command.call());
		
		OutputReader log = new OutputReader(CLOUDGENE_LOG);
		assertTrue(log.hasInMemory("Passed Samples: 1"));


	}
	
	@Test
	public void testWithMissingContig() throws Exception {

		String inputFolder = "test-data/statistics/sample_statistics_missing_contig.txt";

		StatisticsCommand command = buildCommand(inputFolder);

		assertEquals(1, (int) command.call());
		



	}
	
	private StatisticsCommand buildCommand(String inputFolder) {
		StatisticsCommand command = new StatisticsCommand();
		command.setInput(inputFolder);
		command.setOutput("excluded_samples.txt");
		command.setReport(CLOUDGENE_LOG);
		return command;
	}
	

}
