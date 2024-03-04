package genepi.mut.steps;

import org.junit.Test;

import genepi.io.FileUtil;
import genepi.io.table.reader.CsvTableReader;
import genepi.io.text.LineReader;
import genepi.mut.App;
import genepi.mut.objects.VariantLine;
import genepi.mut.pileup.VcfWriter;
import genepi.mut.tasks.MergeTask;
import genepi.mut.tasks.VariantCallingTask;
import genepi.mut.util.QCMetric;
import genepi.mut.util.RawFileAnalysermtDNA;
import htsjdk.samtools.reference.FastaSequenceFile;
import htsjdk.samtools.reference.ReferenceSequence;
import lukfor.progress.TaskService;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class MutserveTest {

	@Test
	public void testMinorBasesForEqualLevels() {
		
		List<VariantCallingTask> tasks = new Vector<VariantCallingTask>();
		String input = "test-data/mtdna/bam-equal-level/test.bam";
		String ref = "test-data/mtdna/reference/rCRS.fasta";
		String out = "test-data/mtdna/bam-equal-level/here.txt.0";
		String outRaw = "test-data/mtdna/bam-equal-level/here.raw.txt.0";
		String outFinal = "test-data/mtdna/bam-equal-level/here.txt";
		String outRawFinal = "test-data/mtdna/bam-equal-level/here.raw.txt";
		
		VariantCallingTask task = new VariantCallingTask();
		task.setInput(input);
		task.setReference(ref);
		task.setLevel(0.01);
		task.setVarName(out);
		task.setRawName(outRaw);
		tasks.add(task);
		TaskService.setAnsiSupport(false);
		TaskService.monitor(null).run(tasks);
		
		MergeTask mergeTask = new MergeTask();
		mergeTask.setRawPath(outRawFinal);
		mergeTask.setVariantPath(outFinal);
		mergeTask.setInputs(tasks);
		
		TaskService.run(mergeTask);

		CsvTableReader cloudgeneReader = new CsvTableReader(outRawFinal, '\t');

		while (cloudgeneReader.next()) {

			VariantLine line = new VariantLine();

			line.parseLineFromFile(cloudgeneReader);

			if (line.getPosition() == 15607) {
				assertEquals('G', line.getMinorBaseFWD());
				assertEquals('G', line.getMinorBaseREV());
				assertEquals('A', line.getTopBaseFWD());
				assertEquals('A', line.getTopBaseREV());
			}
		}
		FileUtil.deleteFile(out);
		FileUtil.deleteFile("test-data/mtdna/equal-level/here_raw.txt");
	}

	@Test
	public void test1000GIndel() throws IOException {

		String input = "test-data/mtdna/bam/input/HG00096.mapped.ILLUMINA.bwa.GBR.low_coverage.20101123.bam";
		String ref = "test-data/mtdna/reference/rCRS.fasta";
		String out = "test-data/1000g.indel.txt.0";
		String raw = "test-data/1000g.raw.indel.txt.0";
		String outFinal = "test-data/1000g.indel.txt";
		String outRawFinal = "test-data/1000g.raw.indel.txt";
		
		List<VariantCallingTask> tasks = new Vector<VariantCallingTask>();
		VariantCallingTask task = new VariantCallingTask();
		task.setInput(input);
		task.setReference(ref);
		task.setLevel(0.01);
        task.setStrandBias(1.2);
		task.setVarName(out);
		task.setRawName(raw);
		task.setDeletions(true);
		task.setBaq(true);
		TaskService.setAnsiSupport(false);
		tasks.add(task);
		TaskService.monitor(null).run(tasks);
		
		MergeTask mergeTask = new MergeTask();
		mergeTask.setRawPath(outRawFinal);
		mergeTask.setVariantPath(outFinal);
		mergeTask.setInputs(tasks);
		TaskService.run(mergeTask);

		Set<String> expected = new HashSet<String>(
				Arrays.asList("1456", "2746", "3200", "12410", "14071", "14569", "15463", "16093", "16360", "10394",
						"1438", "152", "15326", "15340", "16519", "263", "4769", "750", "8592", "8860"));

		LineReader reader = new LineReader(outFinal);
		HashSet<String> results = new HashSet<String>();

		// header
		reader.next();
		while (reader.next()) {
			String[] splits = reader.get().split("\t");
			if(splits[1].equals("PASS")) {
			results.add(splits[2]);
			}
		}

		assertEquals(true, results.equals(expected));

	}

	@Test
	public void test1000G() throws IOException {

		String input = "test-data/mtdna/bam/input/HG00096.mapped.ILLUMINA.bwa.GBR.low_coverage.20101123.bam";
		String ref = "test-data/mtdna/reference/rCRS.fasta";
		String out = "test-data/1000g.txt.0";
		String raw = "test-data/1000g_raw.txt.0";
		String outFinal = "test-data/1000g.txt";
		String outRawFinal = "test-data/1000g_raw.txt";

		Set<String> expected = new HashSet<String>(
				Arrays.asList("1456", "2746", "3200", "12410", "14071", "14569", "15463", "16093", "16360", "10394",
						"1438", "152", "15326", "15340", "16519", "263", "4769", "750", "8592", "8860"));

		List<VariantCallingTask> tasks = new Vector<VariantCallingTask>();
		VariantCallingTask task = new VariantCallingTask();
		task.setInput(input);
		task.setReference(ref);
		task.setLevel(0.01);
		task.setStrandBias(1.2);
		task.setVarName(out);
		task.setRawName(raw);
		TaskService.setAnsiSupport(false);
		tasks.add(task);
		TaskService.monitor(null).run(tasks);
		
		MergeTask mergeTask = new MergeTask();
		mergeTask.setRawPath(outRawFinal);
		mergeTask.setVariantPath(outFinal);
		mergeTask.setInputs(tasks);
		TaskService.run(mergeTask);

		LineReader reader = new LineReader(outFinal);
		HashSet<String> results = new HashSet<String>();

		reader.next();
		while (reader.next()) {
			String[] splits = reader.get().split("\t");
			if(splits[1].equals("PASS")) {
			results.add(splits[2]);
			}
		}

		assertEquals(true, results.equals(expected));

		FastaSequenceFile fastaFile = new FastaSequenceFile(new File("files/rCRS.fasta"), false);
		final ReferenceSequence referenceSequence = fastaFile.nextSequence();

		if (referenceSequence == null) {
			System.out.println("Can not reference fasta file");
			System.exit(-1);
		}
		
		VcfWriter vcfWriter = new VcfWriter();
		vcfWriter.createVCF(outFinal, "test-data/out.vcf", "files/rCRS.fasta", referenceSequence.getName(),
				referenceSequence.getBaseString().length(), App.VERSION + ";" + App.COMMAND);
		fastaFile.close();
		
	}

	@Test
	public void test2Samples1000G() throws IOException {

		String input = "test-data/mtdna/bam/input/HG00096.mapped.ILLUMINA.bwa.GBR.low_coverage.20101123.bam";
		String ref = "test-data/mtdna/reference/rCRS.fasta";
		String out = "test-data/1000g.txt.0";
		String raw = "test-data/1000g_raw.txt.0";
		String outFinal = "test-data/1000g.multi.txt";
		String outRawFinal = "test-data/1000g_raw.multi.txt";

		List<VariantCallingTask> tasks = new Vector<VariantCallingTask>();
		VariantCallingTask task = new VariantCallingTask();
		task.setInput(input);
		task.setReference(ref);
		task.setLevel(0.01);
		task.setStrandBias(1.2);
		task.setVarName(out);
		task.setRawName(raw);
		TaskService.setAnsiSupport(false);
		tasks.add(task);
		
		task = new VariantCallingTask();
		input = "test-data/mtdna/bam/input/HG00096.mapped.ILLUMINA.bwa.GBR.low_coverage.20101123_2.bam";
		out = "test-data/1000g.txt.1";
		raw = "test-data/1000g_raw.txt.1";
		task.setInput(input);
		task.setReference(ref);
		task.setLevel(0.01);
		task.setStrandBias(1.2);
		task.setVarName(out);
		task.setRawName(raw);
		TaskService.setAnsiSupport(false);
		tasks.add(task);
		
		TaskService.monitor(null).run(tasks);
		
		MergeTask mergeTask = new MergeTask();
		mergeTask.setRawPath(outRawFinal);
		mergeTask.setVariantPath(outFinal);
		mergeTask.setInputs(tasks);
		TaskService.run(mergeTask);

		LineReader reader = new LineReader(outFinal);

		reader.next();
		int count = 0;
		while (reader.next()) {
			String[] splits = reader.get().split("\t");
			if(splits[1].equals("PASS")) {
			count++;
			}
		}

		assertEquals(20*2, count);
		
		
		FastaSequenceFile fastaFile = new FastaSequenceFile(new File("files/rCRS.fasta"), false);
		final ReferenceSequence referenceSequence = fastaFile.nextSequence();

		if (referenceSequence == null) {
			System.out.println("Can not reference fasta file");
			System.exit(-1);
		}
		
		VcfWriter vcfWriter = new VcfWriter();
		vcfWriter.createVCF(outFinal, "test-data/out.vcf", "files/rCRS.fasta", referenceSequence.getName(),
				referenceSequence.getBaseString().length(), App.VERSION + ";" + App.COMMAND);
		fastaFile.close();

	}
	
	@Test
	public void test3Samples1000G() throws IOException {

		String input = "test-data/mtdna/bam/input/HG00096.mapped.ILLUMINA.bwa.GBR.low_coverage.20101123.bam";
		String ref = "test-data/mtdna/reference/rCRS.fasta";
		String out = "test-data/1000g.txt.0";
		String raw = "test-data/1000g_raw.txt.0";
		String outFinal = "test-data/1000g.multi.txt";
		String outRawFinal = "test-data/1000g_raw.multi.txt";

		List<VariantCallingTask> tasks = new Vector<VariantCallingTask>();
		VariantCallingTask task = new VariantCallingTask();
		task.setInput(input);
		task.setReference(ref);
		task.setLevel(0.01);
		task.setStrandBias(1.2);
		task.setVarName(out);
		task.setRawName(raw);
		TaskService.setAnsiSupport(false);
		tasks.add(task);
		
		task = new VariantCallingTask();
		input = "test-data/mtdna/bam/input/HG00096.mapped.ILLUMINA.bwa.GBR.low_coverage.20101123_2.bam";
		out = "test-data/1000g.txt.1";
		raw = "test-data/1000g_raw.txt.1";
		task.setInput(input);
		task.setReference(ref);
		task.setLevel(0.01);
		task.setStrandBias(1.2);
		task.setVarName(out);
		task.setRawName(raw);
		TaskService.setAnsiSupport(false);
		tasks.add(task);
		
		task = new VariantCallingTask();
		input = "test-data/mtdna/bam/input/HG00096.mapped.ILLUMINA.bwa.GBR.low_coverage.20101123_3.bam";
		out = "test-data/1000g.txt.2";
		raw = "test-data/1000g_raw.txt.2";
		task.setInput(input);
		task.setReference(ref);
		task.setLevel(0.01);
		task.setStrandBias(1.2);
		task.setVarName(out);
		task.setRawName(raw);
		TaskService.setAnsiSupport(false);
		tasks.add(task);
		
		TaskService.monitor(null).run(tasks);
		
		MergeTask mergeTask = new MergeTask();
		mergeTask.setRawPath(outRawFinal);
		mergeTask.setVariantPath(outFinal);
		mergeTask.setInputs(tasks);
		TaskService.run(mergeTask);

		LineReader reader = new LineReader(outFinal);

		reader.next();
		int count = 0;
		while (reader.next()) {
			String[] splits = reader.get().split("\t");
			System.out.println(reader.get());
			if(splits[1].equals("PASS")) {
				count++;
			}
		}

		assertEquals(20*3, count);

	}

	
	@Test
	public void testmtDNAMixtures() throws IOException {

		String input = "test-data/mtdna/mixtures/input/s4.bam";
		String ref = "test-data/mtdna/reference/rCRS.fasta";
		String out = "test-data/s4.txt.0";
		String outRaw = "test-data/s4.raw.txt.0";
		String outFinal = "test-data/s4.txt";
		String outRawFinal = "test-data/s4.raw.txt";
		
		List<VariantCallingTask> tasks = new Vector<VariantCallingTask>();
		VariantCallingTask task = new VariantCallingTask();
		task.setInput(input);
		task.setReference(ref);
		task.setVarName(out);
		task.setRawName(outRaw);
		task.setLevel(0.01);
		task.setStrandBias(1.2);
		TaskService.setAnsiSupport(false);
		tasks.add(task);
		TaskService.monitor(null).run(tasks);
		
		MergeTask mergeTask = new MergeTask();
		mergeTask.setRawPath(outRawFinal);
		mergeTask.setVariantPath(outFinal);
		mergeTask.setInputs(tasks);
		TaskService.run(mergeTask);

		RawFileAnalysermtDNA analyser = new RawFileAnalysermtDNA();
		analyser.setCallDel(false);

		String refPath = "test-data/mtdna/reference/rCRS.fasta";
		String sanger = "test-data/mtdna/mixtures/expected/sanger.txt";

		ArrayList<QCMetric> list = analyser.calculateLowLevelForTest(outRawFinal, refPath, sanger,
				Double.valueOf(0.01), Double.valueOf(1.2));

		assertTrue(list.size() == 1);

		for (QCMetric metric : list) {

			assertEquals(100, metric.getPrecision(), 0);
			assertEquals(64.0, metric.getSensitivity(), 0.1);
			assertEquals(100, metric.getSpecificity(), 0);
		}

	}
	
	@Test
	public void testSampleWithSnpsAtEnd() throws IOException {

		String input = "test-data/mtdna/bam-complex/m5.bam";
		String ref = "test-data/mtdna/reference/rCRS.fasta";
		String out = "test-data/complex.txt.0";
		String outRaw = "test-data/complex.raw.txt.0";
		String outFinal = "test-data/complex.txt";
		String outRawFinal = "test-data/complex.raw.txt";
		
		List<VariantCallingTask> tasks = new Vector<VariantCallingTask>();
		VariantCallingTask task = new VariantCallingTask();
		task.setInput(input);
		task.setReference(ref);
		task.setVarName(out);
		task.setRawName(outRaw);
		task.setStrandBias(1.2);
		task.setLevel(0.004);
		task.setBaq(false);
		TaskService.setAnsiSupport(false);
		tasks.add(task);
		TaskService.monitor(null).run(tasks);
		
		MergeTask mergeTask = new MergeTask();
		mergeTask.setRawPath(outRawFinal);
		mergeTask.setVariantPath(outFinal);
		mergeTask.setInputs(tasks);
		TaskService.run(mergeTask);
		
		
		LineReader reader = new LineReader(outFinal);
		HashSet<String> results = new HashSet<String>();

		reader.next();
		while (reader.next()) {
			String[] splits = reader.get().split("\t");
			results.add(splits[2]);
		}
		
			assertTrue(results.contains("16541"));
			assertTrue(results.contains("16544"));
			

	}

	/*
	 * @Test public void testLPAData() throws IOException {
	 * 
	 * String input = "test-data/dna/lpa-sample/bam"; String ref =
	 * "test-data/dna/lpa-sample/reference/kiv2_6.fasta"; String out =
	 * "test-data/lpa.txt";
	 * 
	 * PileupToolLocal pileup = new PileupToolLocal( new String[] { "--input",
	 * input, "--reference", ref, "--output", out, "--level",
	 * "0.01","--insertions","--deletions","--noBaq"});
	 * 
	 * pileup.start();
	 * 
	 * LineReader reader = new LineReader(out);
	 * 
	 * // header reader.next(); int i = 0; int deletions = 0; while (reader.next())
	 * { i++; String[] splits = reader.get().split("\t"); if
	 * (splits[1].equals("35")) { assertEquals(new Double(18190),
	 * Double.valueOf(splits[9])); assertEquals(new Double(0.999),
	 * Double.valueOf(splits[4]));
	 * 
	 * }
	 * 
	 * if (splits[3].contains("D")) { deletions++; } }
	 * 
	 * reader.close(); assertEquals(94, i); assertEquals(33, deletions);
	 * 
	 * }
	 */

}
