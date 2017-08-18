package genepi.mut.steps;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import genepi.hadoop.HdfsUtil;
import genepi.hadoop.common.WorkflowStep;
import genepi.io.FileUtil;
import genepi.mut.align.AlignTool;
import genepi.mut.sort.SortTool;
import genepi.mut.util.TestCluster;
import genepi.mut.util.WorkflowTestContext;
import htsjdk.samtools.DefaultSAMRecordFactory;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;

public class SortStepTest {

	public static final boolean VERBOSE = true;

	@BeforeClass
	public static void setUp() throws Exception {
		TestCluster.getInstance().start();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		TestCluster.getInstance().stop();
	}

	@Test
	public void SortTestSE() throws IOException {

		String inputFolder = "test-data/mtdna/fastqse/input";
		String archive = "test-data/mtdna/fastqse/reference/rcrs.tar.gz";
		String hdfsFolder = "input";
		String type = "se";

		importInputdata(inputFolder, hdfsFolder);

		// create workflow context
		WorkflowTestContext context = buildContext(inputFolder, archive, type);

		// create step instance
		AlignTool align = new AlignnMock("files");
		context.setOutput("bwaOut", "cloudgene-bwaOutSe");
		context.setOutput("outputBam", "outputBam1");

		boolean result = align.run(context);

		assertTrue(result);

		SortTool sort = new SortMock("files");
		result = sort.run(context);
		assertTrue(result);

		assertTrue(HdfsUtil.exists("outputBam1"));

		List<String> files = HdfsUtil.getFiles("outputBam1");
		String out = "test-data/tmp/out.bam";

		for (String file : files) {
			HdfsUtil.get(file, out);
		}

		final SamReader reader = SamReaderFactory.make().validationStringency(ValidationStringency.SILENT)
				.samRecordFactory(DefaultSAMRecordFactory.getInstance()).open(new File(out));

		SAMRecordIterator s = reader.iterator();
		int i = 0;
		while (s.hasNext()) {
			SAMRecord rec = s.next();
			if(rec.getReadName().equals("QS6LK:01421:01280")){
				assertEquals("rCRS",rec.getContig());
			}
			i++;
		}

		assertEquals(317, i);

		FileUtil.deleteDirectory("test-data/tmp");
	}
	
	@Test
	public void SortTestPE() throws IOException {

		String inputFolder = "test-data/mtdna/fastqpe/input";
		String archive = "test-data/mtdna/fastqpe/reference/rcrs.tar.gz";
		String hdfsFolder = "input";
		String type = "pe";

		importInputdata(inputFolder, hdfsFolder);

		// create workflow context
		WorkflowTestContext context = buildContext(inputFolder, archive, type);

		// create step instance
		AlignTool align = new AlignnMock("files");
		context.setInput("chunkLength", "0");
		context.setOutput("bwaOut", "cloudgene-bwaOutPe");
		context.setOutput("outputBam", "outputBam2");

		boolean result = align.run(context);

		assertTrue(result);

		SortTool sort = new SortMock("files");
		result = sort.run(context);
		assertTrue(result);

		assertTrue(HdfsUtil.exists("outputBam2"));

		List<String> files = HdfsUtil.getFiles("outputBam2");
		String out = "test-data/tmp/out.bam";

		for (String file : files) {
			HdfsUtil.get(file, out);
		}

		final SamReader reader = SamReaderFactory.make().validationStringency(ValidationStringency.SILENT)
				.samRecordFactory(DefaultSAMRecordFactory.getInstance()).open(new File(out));

		SAMRecordIterator s = reader.iterator();

		int i = 0;
		while (s.hasNext()) {
			SAMRecord rec = s.next();
			i++;
			System.out.println("sorted " + rec.getSAMString());
		}
		System.out.println("AMOUNT " + i);
		
		FileUtil.deleteDirectory("test-data/tmp");
		

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

	protected boolean run(WorkflowTestContext context, WorkflowStep step) {
		step.setup(context);
		return step.run(context);
	}

	protected WorkflowTestContext buildContext(String input, String archive, String type) {

		File file = new File("test-data/tmp");
		if (file.exists()) {
			FileUtil.deleteDirectory(file);
		}
		file.mkdirs();

		WorkflowTestContext context = new WorkflowTestContext();

		context.setInput("input", "input");
		context.setInput("inType", type);
		context.setVerbose(VERBOSE);
		context.setInput("archive", archive);

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
