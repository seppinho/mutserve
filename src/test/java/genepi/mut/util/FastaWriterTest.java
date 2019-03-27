package genepi.mut.util;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;

import genepi.io.FileUtil;
import genepi.io.text.LineReader;
import genepi.mut.pileup.PileupToolLocal;

public class FastaWriterTest {

	@Test
	public void testNA20851() {

		String input = "test-data/mtdna/fasta/input/NA20851.mapped.ILLUMINA.bwa.GIH.low_coverage.20120522.bam";
		String ref = "test-data/mtdna/reference/rCRS.fasta";
		String out = "test-data/mtdna/fasta/output/NA20851.txt";
		String fasta = "test-data/mtdna/fasta/output/NA20851.fasta";

		PileupToolLocal pileup = new PileupToolLocal(
				new String[] { "--input", input, "--reference", ref, "--output", out, "--level","0.01","--minCoverage", "30", "--insertions", "--deletions"});

		pileup.start();

		FastaWriter writer = new FastaWriter();

		writer.createFasta(out, fasta, ref);

		try {
			String expected = new String(Files.readAllBytes(Paths.get("test-data/mtdna/fasta/input/NA20851.fasta")));
			LineReader reader = new LineReader(fasta);
			reader.next();
			reader.next();
			String actual = reader.get();
			assertEquals(expected, actual);
		} catch (IOException e) {
			e.printStackTrace();
		}

		FileUtil.deleteFile(out);
	}
	
	@Test
	public void testNA20886() {

		String input = "test-data/mtdna/fasta/input/NA20886.mapped.ILLUMINA.bwa.GIH.low_coverage.20120522.bam";
		String ref = "test-data/mtdna/reference/rCRS.fasta";
		String out = "test-data/mtdna/fasta/output/NA20886.txt";
		String fasta = "test-data/mtdna/fasta/output/NA20886.fasta";

		PileupToolLocal pileup = new PileupToolLocal(
				new String[] { "--input", input, "--reference", ref, "--output", out, "--level","0.01","--minCoverage", "30", "--insertions", "--deletions"});

		pileup.start();

		FastaWriter writer = new FastaWriter();

		writer.createFasta(out, fasta, ref);

		try {
			String expected = new String(Files.readAllBytes(Paths.get("test-data/mtdna/fasta/input/NA20886.fasta")));
			LineReader reader = new LineReader(fasta);
			reader.next();
			reader.next();
			String actual = reader.get();
			assertEquals(expected, actual);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void testNA20895() {

		String input = "test-data/mtdna/fasta/input/NA20895.mapped.ILLUMINA.bwa.GIH.low_coverage.20120522.bam";
		String ref = "test-data/mtdna/reference/rCRS.fasta";
		String out = "test-data/mtdna/fasta/output/NA20895.txt";
		String fasta = "test-data/mtdna/fasta/output/NA20895.fasta";

		PileupToolLocal pileup = new PileupToolLocal(
				new String[] { "--input", input, "--reference", ref, "--output", out, "--level","0.01","--minCoverage", "30", "--insertions", "--deletions"});

		pileup.start();

		FastaWriter writer = new FastaWriter();

		writer.createFasta(out, fasta, ref);

		try {
			String expected = new String(Files.readAllBytes(Paths.get("test-data/mtdna/fasta/input/NA20895.fasta")));
			LineReader reader = new LineReader(fasta);
			reader.next();
			reader.next();
			String actual = reader.get();
			assertEquals(expected, actual);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void testHG03706() {

		String input = "test-data/mtdna/fasta/input/HG03706.mapped.ILLUMINA.bwa.PJL.low_coverage.20130415.bam";
		String ref = "test-data/mtdna/reference/rCRS.fasta";
		String out = "test-data/mtdna/fasta/output/HG03706.txt";
		String fasta = "test-data/mtdna/fasta/output/HG03706.fasta";

		PileupToolLocal pileup = new PileupToolLocal(
				new String[] { "--input", input, "--reference", ref, "--output", out, "--level","0.01","--minCoverage", "30", "--insertions", "--deletions"});

		pileup.start();

		FastaWriter writer = new FastaWriter();

		writer.createFasta(out, fasta, ref);

		try {
			String expected = new String(Files.readAllBytes(Paths.get("test-data/mtdna/fasta/input/HG03706.fasta")));
			LineReader reader = new LineReader(fasta);
			reader.next();
			reader.next();
			String actual = reader.get();
			assertEquals(expected, actual);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void testNA19785() {

		String input = "test-data/mtdna/fasta/input/NA19785.mapped.ILLUMINA.bwa.MXL.low_coverage.20120522.bam";
		String ref = "test-data/mtdna/reference/rCRS.fasta";
		String out = "test-data/mtdna/fasta/output/NA19785.txt";
		String fasta = "test-data/mtdna/fasta/output/NA19785.fasta";

		PileupToolLocal pileup = new PileupToolLocal(
				new String[] { "--input", input, "--reference", ref, "--output", out, "--level","0.01","--minCoverage", "30", "--insertions", "--deletions"});

		pileup.start();

		FastaWriter writer = new FastaWriter();

		writer.createFasta(out, fasta, ref);

		try {
			String expected = new String(Files.readAllBytes(Paths.get("test-data/mtdna/fasta/input/NA19785.fasta")));
			LineReader reader = new LineReader(fasta);
			reader.next();
			reader.next();
			String actual = reader.get();
			assertEquals(expected, actual);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


}
