package genepi.mut.steps;

import org.junit.Test;

import genepi.io.FileUtil;
import genepi.io.table.reader.CsvTableReader;
import genepi.io.text.LineReader;
import genepi.mut.objects.VariantLine;
import genepi.mut.pileup.PileupToolLocal;
import genepi.mut.util.QCMetric;
import genepi.mut.util.RawFileAnalysermtDNA;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MutserveTests {

	@Test
	public void testMinorBasesForEqualLevels() {

		String input = "test-data/mtdna/bam-equal-level/test.bam";
		String ref = "test-data/mtdna/reference/rCRS.fasta";
		String out = "test-data/mtdna/bam-equal-level/here.txt";

		PileupToolLocal pileup = new PileupToolLocal(
				new String[] { "--input", input, "--reference", ref, "--output", out, "--level", "0.01", "--noBaq" });

		pileup.start();

		CsvTableReader cloudgeneReader = new CsvTableReader("test-data/mtdna/bam-equal-level/here_raw.txt", '\t');

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

		File file = new File("test-data/tmp");
		if (file.exists()) {
			FileUtil.deleteDirectory(file);
		}

		String input = "test-data/mtdna/bam/input";
		String ref = "test-data/mtdna/reference/rCRS.fasta";
		String out = "test-data/1000g.txt";

		Set<String> expected = new HashSet<String>(Arrays.asList("1456", "2746", "3200", "12410", "14071", "14569",
				"15463", "16093", "16360", "10394", "1438", "152", "15326", "15340", "16519", "263", "4769", "750",
				"8592", "8860", "3107", "302.1", "310.1"));

		PileupToolLocal pileup = new PileupToolLocal(new String[] { "--input", input, "--reference", ref, "--output",
				out, "--level", "0.01", "--deletions", "--insertions" });

		pileup.start();

		LineReader reader = new LineReader(out);
		HashSet<String> results = new HashSet<String>();

		// header
		reader.next();
		while (reader.next()) {
			String[] splits = reader.get().split("\t");
			results.add(splits[1]);
		}

		assertEquals(true, results.equals(expected));

	}

	@Test
	public void test1000G() throws IOException {

		File file = new File("test-data/tmp");
		if (file.exists()) {
			FileUtil.deleteDirectory(file);
		}

		String input = "test-data/mtdna/bam/input";
		String ref = "test-data/mtdna/reference/rCRS.fasta";
		String out = "test-data/1000g.txt";

		Set<String> expected = new HashSet<String>(Arrays.asList("1456", "2746", "3200", "12410", "14071", "14569",
				"15463", "16093", "16360", "10394", "1438", "152", "15326", "15340", "16519", "263", "4769", "750",
				"8592", "8860", "3107", "302.1", "310.1"));

		PileupToolLocal pileup = new PileupToolLocal(new String[] { "--input", input, "--reference", ref, "--output",
				out, "--level", "0.01", "--insertions" });

		pileup.start();

		LineReader reader = new LineReader(out);
		HashSet<String> results = new HashSet<String>();

		// header
		reader.next();
		while (reader.next()) {
			String[] splits = reader.get().split("\t");
			results.add(splits[1]);
		}

		assertEquals(true, results.equals(expected));

	}

	@Test
	public void testmtDNAMixtures() throws IOException {

		String input = "test-data/mtdna/mixtures/input";
		String ref = "test-data/mtdna/reference/rCRS.fasta";
		String out = "test-data/1000g.txt";

		PileupToolLocal pileup = new PileupToolLocal(
				new String[] { "--input", input, "--reference", ref, "--output", out, "--level", "0.01" });

		pileup.start();

		RawFileAnalysermtDNA analyser = new RawFileAnalysermtDNA();
		analyser.setCallDel(false);

		String refPath = "test-data/mtdna/reference/rCRS.fasta";
		String sanger = "test-data/mtdna/mixtures/expected/sanger.txt";

		ArrayList<QCMetric> list = analyser.calculateLowLevelForTest(
				"test-data/1000g_raw.txt", refPath, sanger, Double.valueOf(0.01));

		assertTrue(list.size() == 1);

		for (QCMetric metric : list) {

			System.out.println(metric.getPrecision());
			System.out.println(metric.getSensitivity());
			System.out.println(metric.getSpecificity());

			assertEquals(100, metric.getPrecision(), 0);
			assertEquals(64.0, metric.getSensitivity(), 0.1);
			assertEquals(100, metric.getSpecificity(), 0);
		}

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
