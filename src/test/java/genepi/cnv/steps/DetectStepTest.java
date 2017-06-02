package genepi.cnv.steps;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.MathException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import genepi.cnv.align.AlignTool;
import genepi.cnv.detect.DetectTool;
import genepi.cnv.pileup.PileupTool;
import genepi.cnv.sort.SortTool;
import genepi.cnv.util.MultiallelicAnalyser;
import genepi.cnv.util.QCMetric;
import genepi.cnv.util.RawFileAnalyser;
import genepi.cnv.util.TestCluster;
import genepi.cnv.util.WorkflowTestContext;
import genepi.hadoop.HdfsUtil;
import genepi.hadoop.common.WorkflowStep;
import genepi.io.FileUtil;
import genepi.io.text.LineReader;
import htsjdk.samtools.DefaultSAMRecordFactory;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;

public class DetectStepTest {

	public static final boolean VERBOSE = true;

	@BeforeClass
	public static void setUp() throws Exception {
		TestCluster.getInstance().start();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		// TestCluster.getInstance().stop();
	}

	@Test
	public void DetectPipelinemtDNAMixturePETest() throws IOException {

		String inputFolder = "test-data/mtdna/fastqpe/input";
		String archive = "test-data/mtdna/fastqpe/reference/rcrs.tar.gz";
		String hdfsFolder = "input";
		String type = "pe";

		importInputdata(inputFolder, hdfsFolder);

		// create workflow context
		WorkflowTestContext context = buildContext(hdfsFolder, archive, type);

		context.setInput("chunkLength", "0");
		context.setInput("baq", "true");
		context.setOutput("bwaOut", "cloudgene-bwaOutPe1");
		context.setOutput("outputBam", "outputBam1");
		context.setOutput("analyseOut", "analyseOut1");

		// create step instance
		AlignTool align = new AlignnMock("files");

		boolean result = align.run(context);

		assertTrue(result);

		SortTool sort = new SortMock("files");
		result = sort.run(context);
		assertTrue(result);

		assertTrue(HdfsUtil.exists("outputBam1"));

		PileupTool pileUp = new PileupMock("files");
		result = pileUp.run(context);
		assertTrue(result);

		DetectMock detect = new DetectMock("files");
		result = detect.run(context);
		assertTrue(result);
	}

	@Test
	public void DetectPipelinemtDNAMixtureBAMTest() throws IOException {

		String inputFolder = "test-data/mtdna/mixtures/input";
		String archive = "test-data/mtdna/mixtures/reference/rcrs.tar.gz";
		String hdfsFolder = "input";
		String type = "bam";
		
		String refPath = "test-data/mtdna/mixtures/reference/rCRS.fasta";
		String sanger = "test-data/mtdna/mixtures/expected/sanger.txt";

		importInputdata(inputFolder, hdfsFolder);

		// create workflow context
		WorkflowTestContext context = buildContext(hdfsFolder, archive, type);

		context.setInput("baq", "true");
		context.setOutput("analyseOut", "analyseOut2");

		PileupTool pileUp = new PileupMock("files");
		boolean result = pileUp.run(context);
		assertTrue(result);

		DetectMock detect = new DetectMock("files");
		result = detect.run(context);
		assertTrue(result);

		double hetLevel = Double.valueOf(context.get("level"));

		RawFileAnalyser analyser = new RawFileAnalyser();
		File file = new File("test-data/tmp");

		try {
			ArrayList<QCMetric> list = analyser.analyseFile(file.getPath() + "/raw.txt", refPath, sanger,
					hetLevel / 100);

			assertTrue(list.size() == 1);

			for (QCMetric metric : list) {

				System.out.println(metric.getPrecision());
				System.out.println(metric.getSensitivity());
				System.out.println(metric.getSpecificity());

				assertEquals(100, metric.getPrecision(), 0);
				assertEquals(59.259, metric.getSensitivity(), 0.1);
				assertEquals(100, metric.getSpecificity(), 0);
			}
			assertEquals(true, result);
		} catch (MathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	public void DetectPipelineLPAMultiallelicBAMTestKIV2() throws IOException {

		String inputFolder = "test-data/lpa/lpa-exome-kiv2/input";
		String archive = "test-data/lpa/lpa-exome-kiv2/reference/kiv2_6.tar.gz";
		String hdfsFolder = "input";
		String type = "bam";

		File expected = new File("test-data/lpa/lpa-exome-kiv2/expected/expected.txt");
		File refPath = new File("test-data/lpa/lpa-exome-kiv2/reference/kiv2_6.fasta");

		
		importInputdata(inputFolder, hdfsFolder);

		// create workflow context
		WorkflowTestContext context = buildContext(hdfsFolder, archive, type);

		context.setInput("baq", "false");
		context.setOutput("analyseOut", "analyseOut3");

		PileupTool pileUp = new PileupMock("files");
		boolean result = pileUp.run(context);
		assertTrue(result);

		DetectMock detect = new DetectMock("files");
		result = detect.run(context);
		assertTrue(result);

		MultiallelicAnalyser analyser = new MultiallelicAnalyser();
		File file = new File("test-data/tmp");

		double hetLevel = 0.001;
		ArrayList<QCMetric> list = analyser.analyseFile(file.getPath() + "/raw.txt", expected.getPath(), refPath.getPath(), hetLevel);

		assertTrue(list.size() == 1);

		for (QCMetric metric : list) {

			System.out.println("Precision: " + metric.getPrecision());
			System.out.println("Sensitivity " + metric.getSensitivity());
			System.out.println("Specificity " + metric.getSpecificity());
		}

	}
	
	@Test
	public void DetectPipelineLPAMultiallelicBAMTestKIV7() throws IOException {

		String inputFolder = "test-data/lpa/lpa-exome-kiv7/input";
		String archive = "test-data/lpa/lpa-exome-kiv7/reference/LPA-KIV7.tar.gz";
		String hdfsFolder = "input";
		String type = "bam";

		importInputdata(inputFolder, hdfsFolder);

		// create workflow context
		WorkflowTestContext context = buildContext(hdfsFolder, archive, type);

		context.setInput("baq", "false");
		context.setOutput("analyseOut", "analyseOut4");

		PileupTool pileUp = new PileupMock("files");
		boolean result = pileUp.run(context);
		assertTrue(result);

		DetectMock detect = new DetectMock("files");
		result = detect.run(context);
		assertTrue(result);

	}

	class AlignnMock extends AlignTool {

		private String folder;

		public AlignnMock(String folder) {
			super();
			this.folder = folder;
		}

		@Override
		public String getFolder(Class clazz) {
			// override folder with static folder instead of jar location
			return folder;
		}

	}

	class SortMock extends SortTool {

		private String folder;

		public SortMock(String folder) {
			super();
			this.folder = folder;
		}

		@Override
		public String getFolder(Class clazz) {
			// override folder with static folder instead of jar location
			return folder;
		}

	}

	class PileupMock extends PileupTool {

		private String folder;

		public PileupMock(String folder) {
			super();
			this.folder = folder;
		}

		@Override
		public String getFolder(Class clazz) {
			// override folder with static folder instead of jar location
			return folder;
		}

	}

	class DetectMock extends DetectTool {

		private String folder;

		public DetectMock(String folder) {
			super();
			this.folder = folder;
		}

		@Override
		public String getFolder(Class clazz) {
			// override folder with static folder instead of jar location
			return folder;
		}

	}

	protected boolean run(WorkflowTestContext context, WorkflowStep step) {
		step.setup(context);
		return step.run(context);
	}

	protected WorkflowTestContext buildContext(String input, String ref, String type) {

		File file = new File("test-data/tmp");
		if (file.exists()) {
			FileUtil.deleteDirectory(file);
		}
		file.mkdirs();

		WorkflowTestContext context = new WorkflowTestContext();

		context.setInput("input", input);
		context.setInput("inType", type);
		context.setVerbose(VERBOSE);
		context.setInput("archive", ref);
		context.setOutput("mapQuality", "20");
		context.setOutput("baseQuality", "20");
		context.setOutput("alignQuality", "30");
		context.setOutput("statistics", "statistics");

		context.setOutput("raw", file.getAbsolutePath() + "/raw");
		context.setOutput("variants", file.getAbsolutePath() + "/variants");
		context.setOutput("multiallelic", file.getAbsolutePath() + "/multiallelic");
		context.setOutput("uncovered_pos", file.getAbsolutePath() + "/uncovered");
		context.setOutput("level", "1");

		return context;

	}

	private void importInputdata(String folder, String input) {
		System.out.println("Import Data:");
		String[] files = FileUtil.getFiles(folder, "*.*");
		for (String file : files) {
			String target = HdfsUtil.path(input, FileUtil.getFilename(file));
			System.out.println("  Import " + file + " to " + target);
			HdfsUtil.put(file, target);
		}
	}
}
