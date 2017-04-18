package genepi.cnv.steps;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.math.MathException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import genepi.cnv.align.AlignTool;
import genepi.cnv.detect.DetectTool;
import genepi.cnv.pileup.PileupTool;
import genepi.cnv.sort.SortTool;
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
	public void DetectPipelinePETest() throws IOException {

		String inputFolder = "test-data/mtdna/fastqpe/";
		String reference = "rcrs";
		String hdfsFolder = "input";
		String type = "pe";

		importInputdata(inputFolder, hdfsFolder);

		// create workflow context
		WorkflowTestContext context = buildContext(hdfsFolder, reference, type);
		
		context.setInput("chunkLength", "0");

		// create step instance
		AlignTool align = new AlignnMock("files");

		boolean result = align.run(context);

		assertTrue(result);

		SortTool sort = new SortMock("files");
		result = sort.run(context);
		assertTrue(result);

		assertTrue(HdfsUtil.exists("outputBam"));

		PileupTool pileUp = new PileupMock("files");
		result = pileUp.run(context);
		assertTrue(result);
		
		DetectMock detect = new DetectMock("files");
		result = detect.run(context);
		assertTrue(result);
	}
	
	@Test
	public void DetectPipelineBAM() throws IOException {

		String inputFolder = "test-data/mtdna/mixtures/";
		String reference = "rcrs";
		String hdfsFolder = "input";
		String type = "bam";

		importInputdata(inputFolder, hdfsFolder);

		// create workflow context
		WorkflowTestContext context = buildContext(hdfsFolder, reference, type);
		
		PileupTool pileUp = new PileupMock("files");
		boolean result = pileUp.run(context);
		assertTrue(result);
		
		DetectMock detect = new DetectMock("files");
		result = detect.run(context);
		assertTrue(result);
		
		double hetLevel = Double.valueOf(context.get("level"));
		String refPath = "files/rcrs.fasta";
		String sanger = "test-data/mtdna/sanger.txt";

		RawFileAnalyser model = new RawFileAnalyser();
		File file = new File("test-data/tmp");

			try {
				result = model.analyseFile(file.getPath() +"/raw.txt", refPath, sanger, hetLevel / 100);
				assertEquals(true, result);
			} catch (MathException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
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
		context.setInput("reference", ref);
		context.setOutput("bwaOut", "cloudgene-bwaOutSe");
		context.setOutput("outputBam", "outputBam");
		context.setOutput("analyseOut", "analyseOut");
		context.setOutput("mapQuality", "20");
		context.setOutput("baseQuality", "20");
		context.setOutput("alignQuality", "30");
		context.setOutput("statistics", "statistics");
		context.setOutput("baq", "true");
		
		context.setOutput("raw", file.getAbsolutePath()+"/raw");
		context.setOutput("variants", file.getAbsolutePath()+"/variants");
		context.setOutput("uncovered_pos", file.getAbsolutePath()+"/uncovered");
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
