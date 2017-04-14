package genepi.cnv.steps;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import genepi.cnv.align.AlignTool;
import genepi.cnv.sort.SortTool;
import genepi.cnv.util.TestCluster;
import genepi.cnv.util.WorkflowTestContext;
import genepi.hadoop.HdfsUtil;
import genepi.hadoop.common.WorkflowStep;
import genepi.io.FileUtil;
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

		String inputFolder = "test-data/mtdna/fastqse/";
		String reference = "rcrs";
		String hdfsFolder = "input";
		String type = "se";

		importInputdata(inputFolder, hdfsFolder);

		// create workflow context
		WorkflowTestContext context = buildContext(inputFolder, reference,type);

		// create step instance
		AlignTool align = new AlignnMock("files");

		boolean result = align.run(context);

		assertTrue(result);

		SortTool sort = new SortMock("files");
		result = sort.run(context);
		assertTrue(result);

		assertTrue(HdfsUtil.exists("outputBam"));

		List<String> files = HdfsUtil.getFiles("outputBam");
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
		;

	}
	
	@Test
	public void SortTestPE() throws IOException {

		String inputFolder = "test-data/mtdna/fastqpe/";
		String reference = "rcrs";
		String hdfsFolder = "input";
		String type = "pe";

		importInputdata(inputFolder, hdfsFolder);

		// create workflow context
		WorkflowTestContext context = buildContext(inputFolder, reference, type);

		// create step instance
		AlignTool align = new AlignnMock("files");
		context.setInput("chunkLength", "0");

		boolean result = align.run(context);

		assertTrue(result);

		SortTool sort = new SortMock("files");
		result = sort.run(context);
		assertTrue(result);

		assertTrue(HdfsUtil.exists("outputBam"));

		List<String> files = HdfsUtil.getFiles("outputBam");
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

	protected WorkflowTestContext buildContext(String input, String ref, String type) {

		File file = new File("test-data/tmp");
		if (file.exists()) {
			FileUtil.deleteDirectory(file);
		}
		file.mkdirs();

		WorkflowTestContext context = new WorkflowTestContext();

		context.setInput("input", "input");
		context.setInput("inType", type);
		context.setVerbose(VERBOSE);
		context.setInput("reference", ref);
		context.setOutput("bwaOut", "cloudgene-bwaOutSe");
		context.setOutput("outputBam", "outputBam");

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
