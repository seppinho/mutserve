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
	public void testNA20895() {

		String input = "test-data/mtdna/fasta-files/input/NA20895.mapped.ILLUMINA.bwa.GIH.low_coverage.20120522.bam";
		String ref = "test-data/mtdna/reference/rCRS.fasta";
		String out = "test-data/NA20895.txt";
		String fasta = "test-data/NA20895.fasta";

		PileupToolLocal pileup = new PileupToolLocal(new String[] { "--input", input, "--reference", ref, "--output",
				out, "--level", "0.01", "--minCoverage", "30", "--insertions", "--deletions" });

		pileup.start();

		FastaWriter writer = new FastaWriter();

		writer.createFasta(out, fasta, ref);

		try {
			String expected = new String(Files.readAllBytes(Paths.get("test-data/mtdna/fasta-files/expected/NA20895.fasta")));
			LineReader reader = new LineReader(fasta);
			reader.next();
			reader.next();
			String actual = reader.get();
			assertEquals(expected, actual);
		} catch (IOException e) {
			e.printStackTrace();
		}

		FileUtil.deleteFile(out);
		FileUtil.deleteFile(fasta);

	}

/*	
	@Test
	public void testNA18544() {

		String input = "test-data/mtdna/fasta-files/input/NA18544.mapped.ILLUMINA.bwa.CHB.low_coverage.20130415.bam";
		String ref = "test-data/mtdna/reference/rCRS.fasta";
		String out = "test-data/tmp/NA18544.txt";
		String fasta = "test-data/tmp/NA18544.fasta";

		PileupToolLocal pileup = new PileupToolLocal(new String[] { "--input", input, "--reference", ref, "--output",
				out, "--level", "0.01", "--minCoverage", "30", "--deletions", "--insertions" });

		pileup.start();

		FastaWriter writer = new FastaWriter();
		writer.createFasta(out, fasta, ref);

		try {
			String expected = new String(Files.readAllBytes(Paths.get("test-data/mtdna/fasta-files/expected/NA18544.fasta")));
			LineReader reader = new LineReader(fasta);
			reader.next();
			reader.next();
			String actual = reader.get();
			assertEquals(expected, actual);
		} catch (IOException e) {
			e.printStackTrace();
		}

		FileUtil.deleteFile(out);
		FileUtil.deleteFile(fasta);

	}*/

}
