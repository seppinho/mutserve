package genepi.mut.steps;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import genepi.hadoop.HdfsUtil;
import genepi.hadoop.common.WorkflowStep;
import genepi.io.FileUtil;
import genepi.io.text.LineReader;
import genepi.mut.align.AlignStep;
import genepi.mut.pileup.PileupStep;
import genepi.mut.sort.SortStep;
import genepi.mut.util.QCMetric;
import genepi.mut.util.RawFileAnalysermtDNA;
import genepi.mut.util.TestCluster;
import genepi.mut.util.WorkflowTestContext;
import htsjdk.samtools.DefaultSAMRecordFactory;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;

public class MutationServerTest {

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
	public void AlignmentSETest() throws IOException {

		String inputFolder = "test-data/mtdna/fastqse/input";
		String archive = "test-data/mtdna/fastqse/reference/rcrs.tar.gz";
		String hdfsFolder = "inputSE";

		importInputdata(inputFolder, hdfsFolder);

		String type = "se";

		// create workflow context
		WorkflowTestContext context = buildContext(hdfsFolder, archive, type);

		context.setOutput("bwaOut", "cloudgene-bwaOut");

		// create step instance
		AlignStep align = new AlignnMock("files");

		boolean result = align.run(context);

		assertTrue(result);

		HdfsUtil.merge(new File("test-data/tmp/bwaOut/result.txt").getPath(), "cloudgene-bwaOut/0", false);

		try (BufferedReader br = new BufferedReader(new FileReader(new File("test-data/tmp/bwaOut/result.txt")))) {
			String line;
			int i = 0;
			while ((line = br.readLine()) != null) {
				if (line.length() > 0) {
					if (line.contains("QS6LK:01441:00464")) {
						String splits[] = line.split("\t");
						assertEquals("rCRS", splits[3]);
						assertEquals("1", splits[4]);
						assertEquals("60", splits[5]);
						assertEquals("23S25M1D48M1I93M", splits[6]);
					}
					i++;
				}
			}
			br.close();
			/// bwa mem rcrs.fasta small_small.fastq_| wc -l
			assertEquals(317, i);
			FileUtil.deleteDirectory("test-data/tmp");

		}
	}

	@Test
	public void AlignmentPETest() throws IOException {

		String inputFolder = "test-data/mtdna/fastqpe/input";
		String archive = "test-data/mtdna/fastqpe/reference/rcrs.tar.gz";
		String hdfsFolder = "inputPE";

		importInputdata(inputFolder, hdfsFolder);

		String type = "pe";
		// create workflow context
		WorkflowTestContext context = buildContext(hdfsFolder, archive, type);

		context.setInput("chunkLength", "0");
		context.setOutput("bwaOut", "cloudgene-bwaOut");

		// create step instance
		AlignStep align = new AlignnMock("files");

		boolean result = align.run(context);

		assertTrue(result);

		String tmpFile = "test-data/tmp/bwaOut/result.txt";
		HdfsUtil.merge(new File(tmpFile).getPath(), "cloudgene-bwaOut/0", false);

		try (BufferedReader br = new BufferedReader(new FileReader(new File(tmpFile)))) {

			String line;
			int i = 0;
			while ((line = br.readLine()) != null) {
				if (line.length() > 0) {
					if (line.contains("HWI-ST301L:236:C0EJ5ACXX:3:1101:4808:2302:0")) {
						String splits[] = line.split("\t");
						assertEquals("rCRS", splits[3]);
						assertEquals("101M", splits[6]);
					}
					// if(line.contains("HWI-ST301L:236:C0EJ5ACXX:3:1101:15916:2104")){
					System.out.println("aligned \t " + line);

					// }
					i++;
				}

			}
			/// bwa mem rcrs.fasta small_r1.fastq small_r2.fastq_small | wc -l
			// there is one additional line which is ignored
			// HWI-ST301L:236:C0EJ5ACXX:3:1101:15916:2104 has two reads, flags
			/// 73 and 133 (unmapped), since one AS tag is smaller <30 its
			/// ignored!
			assertEquals(200, i);
			System.out.println(i);
			br.close();
			FileUtil.deleteDirectory("test-data/tmp");

		}
	}

	@Test
	public void SortTestSE() throws IOException {

		String inputFolder = "test-data/mtdna/fastqse/input";
		String archive = "test-data/mtdna/fastqse/reference/rcrs.tar.gz";
		String hdfsFolder = "inputSE";
		String type = "se";

		importInputdata(inputFolder, hdfsFolder);

		// create workflow context
		WorkflowTestContext context = buildContext(hdfsFolder, archive, type);

		// create step instance
		AlignStep align = new AlignnMock("files");
		context.setOutput("bwaOut", "cloudgene-bwaOut");
		context.setOutput("outputBam", "outputBam");

		boolean result = align.run(context);

		assertTrue(result);

		SortStep sort = new SortMock("files");
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
			
			//read having two alignments (first, secondary). 
			if(rec.getReadName().equals("QS6LK:01115:01248")){
				System.out.println("sorted " + rec.getSAMString());
			}
			
			
			if (rec.getReadName().equals("QS6LK:01421:01280")) {
				assertEquals("rCRS", rec.getContig());
			}
			i++;
		}

		assertEquals(317, i);

		//FileUtil.deleteDirectory("test-data/tmp");
	}

	@Test
	public void SortTestPE() throws IOException {

		String inputFolder = "test-data/mtdna/fastqpe/input";
		String archive = "test-data/mtdna/fastqpe/reference/rcrs.tar.gz";
		String hdfsFolder = "inputPE";
		String type = "pe";

		importInputdata(inputFolder, hdfsFolder);

		// create workflow context
		WorkflowTestContext context = buildContext(hdfsFolder, archive, type);

		// create step instance
		AlignStep align = new AlignnMock("files");
		context.setInput("chunkLength", "0");
		context.setOutput("bwaOut", "cloudgene-bwaOut");
		context.setOutput("outputBam", "outputBam");

		boolean result = align.run(context);

		assertTrue(result);

		SortStep sort = new SortMock("files");
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

		FileUtil.deleteDirectory("test-data/tmp");

	}
	
	@Test
	public void PileupFromFastqTest() throws IOException {

		String inputFolder = "test-data/mtdna/fastqse/input";
		String archive = "test-data/mtdna/fastqse/reference/rcrs.tar.gz";
		String hdfsFolder = "inputSE";
		String type = "se";

		importInputdata(inputFolder, hdfsFolder);

		// create workflow context
		WorkflowTestContext context = buildContext(hdfsFolder, archive, type);

		// create step instance
		AlignStep align = new AlignnMock("files");
		context.setOutput("bwaOut", "cloudgene-bwaOut");
		context.setOutput("outputBam", "outputBam");

		boolean result = align.run(context);

		assertTrue(result);

		SortStep sort = new SortMock("files");
		result = sort.run(context);

		
		PileupStep pileUp = new PileupMock("files");
		context.setOutput("rawHdfs", "rawHdfs");
		context.setOutput("rawLocal", "test-data/tmp/rawLocal1000G");
		context.setOutput("variantsHdfs", "variantsHdfs");
		context.setOutput("variantsLocal", "test-data/tmp/variantsLocal1000G");
		context.setOutput("baq", "true");
		context.setOutput("callDel", "true");
		context.setOutput("level", "0.01");

		result = pileUp.run(context);
		assertTrue(result);

		FileUtil.deleteDirectory("test-data/tmp");
	}


	@Test
	public void Pileup1000GBamTest() throws IOException {

		String inputFolder = "test-data/mtdna/bam/input";
		String archive = "test-data/mtdna/bam/reference/rcrs.tar.gz";
		String hdfsFolder = "input";
		String type = "bam";

		Set<Integer> expected = new HashSet<Integer>(Arrays.asList(1456, 2746, 3200, 12410, 14071, 14569, 15463,
				16093, 16360, 10394, 1438, 152, 15326, 15340, 16519, 263, 4769, 750, 8592, 8860));

		importInputdata(inputFolder, hdfsFolder);

		// create workflow context
		WorkflowTestContext context = buildContext(hdfsFolder, archive, type);

		PileupStep pileUp = new PileupMock("files");
		context.setOutput("rawHdfs", "rawHdfs");
		context.setOutput("rawLocal", "test-data/tmp/rawLocal1000G");
		context.setOutput("variantsHdfs", "variantsHdfs");
		context.setOutput("variantsLocal", "test-data/tmp/variantsLocal1000G");
		context.setOutput("baq", "true");
		context.setOutput("callDel", "false");
		context.setOutput("level", "0.01");

		boolean result = pileUp.run(context);
		assertTrue(result);

		LineReader reader = new LineReader("test-data/tmp/variantsLocal1000G");
		HashSet<Integer> results = new HashSet<Integer>();

		// header
		reader.next();
		while (reader.next()) {
			String[] splits = reader.get().split("\t");
			int pos = Integer.valueOf(splits[1]);
			results.add(pos);
			System.out.println(pos);
		}

		assertEquals(true, results.equals(expected));

		reader = new LineReader("test-data/tmp/rawLocal1000G");

		int i = 0;
		while (reader.next()) {
			if (i < 10) {
				System.out.println(reader.get());
			}
			i++;
		}

	}
	
	@Test
	public void Pileup1000GBamAndIndelTest() throws IOException {

		String inputFolder = "test-data/mtdna/bam/input";
		String archive = "test-data/mtdna/bam/reference/rcrs.tar.gz";
		String hdfsFolder = "input";
		String type = "bam";

		Set<Integer> expected = new HashSet<Integer>(Arrays.asList(1456, 2746, 3200, 12410, 14071, 14569, 15463,
				16093, 16360, 10394, 1438, 152, 15326, 15340, 16519, 263, 4769, 750, 8592, 8860, 3107));
		
		importInputdata(inputFolder, hdfsFolder);

		// create workflow context
		WorkflowTestContext context = buildContext(hdfsFolder, archive, type);

		PileupStep pileUp = new PileupMock("files");
		context.setOutput("rawHdfs", "rawHdfs");
		context.setOutput("rawLocal", "test-data/tmp/rawLocal1000G");
		context.setOutput("variantsHdfs", "variantsHdfs");
		context.setOutput("variantsLocal", "test-data/tmp/variantsLocal1000G");
		context.setOutput("baq", "true");
		context.setOutput("callDel", "true");
		context.setOutput("level", "0.01");

		boolean result = pileUp.run(context);
		assertTrue(result);

		LineReader reader = new LineReader("test-data/tmp/variantsLocal1000G");
		HashSet<Integer> results = new HashSet<Integer>();

		// header
		reader.next();
		while (reader.next()) {
			String[] splits = reader.get().split("\t");
			int pos = Integer.valueOf(splits[1]);
			results.add(pos);
			System.out.println(pos);
		}

		assertEquals(true, results.equals(expected));

		reader = new LineReader("test-data/tmp/rawLocal1000G");

		int i = 0;
		while (reader.next()) {
			if (i < 10) {
				System.out.println(reader.get());
			}
			i++;
		}

	}

	@Test
	public void DetectPipelinemtDNAMixtureBAMTest() throws IOException {

		String inputFolder = "test-data/mtdna/mixtures/input";
		String archive = "test-data/mtdna/mixtures/reference/rcrs.tar.gz";
		String hdfsFolder = "input2";
		String type = "bam";

		String level = "0.01";

		String refPath = "test-data/mtdna/mixtures/reference/rCRS.fasta";
		String sanger = "test-data/mtdna/mixtures/expected/sanger.txt";

		importInputdata(inputFolder, hdfsFolder);

		// create workflow context
		WorkflowTestContext context = buildContext(hdfsFolder, archive, type);

		PileupStep pileUp = new PileupMock("files");
		context.setOutput("rawHdfs", "rawHdfs");
		context.setOutput("rawLocal", "test-data/tmp/rawLocalMixture");
		context.setOutput("variantsHdfs", "variantsHdfs");
		context.setOutput("variantsLocal", "test-data/tmp/variantsLocalMixture");
		context.setOutput("baq", "true");
		context.setOutput("callDel", "false");
		context.setOutput("level", level);

		boolean result = pileUp.run(context);
		assertTrue(result);

		RawFileAnalysermtDNA analyser = new RawFileAnalysermtDNA();
		analyser.setCallDel(false);

		ArrayList<QCMetric> list = analyser.calculateLowLevelForTest("test-data/tmp/rawLocalMixture", refPath, sanger,
				Double.valueOf(level));

		assertTrue(list.size() == 1);

		for (QCMetric metric : list) {

			System.out.println(metric.getPrecision());
			System.out.println(metric.getSensitivity());
			System.out.println(metric.getSpecificity());

			assertEquals(100, metric.getPrecision(), 0);
			//PAPER: assertEquals(59.259, metric.getSensitivity(), 0.1);
			assertEquals(66.667, metric.getSensitivity(), 0.1);
			assertEquals(100, metric.getSpecificity(), 0);
		}
		assertEquals(true, result);

	}

	class AlignnMock extends AlignStep {

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

	class SortMock extends SortStep {

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

	class PileupMock extends PileupStep {

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

		HdfsUtil.delete("rawHdfs");
		HdfsUtil.delete("variantsHdfs");
		HdfsUtil.delete("cloudgene-bwaOut");
		HdfsUtil.delete("outputBam");
		HdfsUtil.delete("outputBam-temp");
		
		
		WorkflowTestContext context = new WorkflowTestContext();

		context.setInput("input", input);
		context.setInput("inType", type);
		context.setVerbose(VERBOSE);
		context.setInput("archive", archive);
		context.setOutput("mapQuality", "20");
		context.setOutput("baseQuality", "20");
		context.setOutput("alignQuality", "30");
		context.setOutput("statistics", "statistics");
		context.setOutput("baq", "true");

		FileUtil.createDirectory(file.getAbsolutePath() + "/bwaOut");

		return context;

	}

	private void importInputdata(String folder, String input) {
		
		HdfsUtil.delete(input);
		
		System.out.println("Import Data:");
		String[] files = FileUtil.getFiles(folder, "*.*");
		for (String file : files) {
			String target = HdfsUtil.makeAbsolute(HdfsUtil.path(input, FileUtil.getFilename(file)));
			System.out.println("  Import " + file + " to " + target);
			HdfsUtil.put(file, target);
		}
	}
}
