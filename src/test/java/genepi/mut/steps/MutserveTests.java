package genepi.mut.steps;

import org.junit.Test;

import genepi.io.FileUtil;
import genepi.io.table.reader.CsvTableReader;
import genepi.io.text.LineReader;
import genepi.mut.objects.VariantLine;
import genepi.mut.pileup.PileupToolLocal;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
			
			if(line.getPosition() == 15607) { 
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
		
		Set<String> expected = new HashSet<String>(Arrays.asList("1456", "2746", "3200", "12410", "14071", "14569", "15463",
				"16093", "16360", "10394", "1438", "152", "15326", "15340", "16519", "263", "4769", "750", "8592", "8860", "3107","302.1","310.1"));
	

		PileupToolLocal pileup = new PileupToolLocal(
				new String[] { "--input", input, "--reference", ref, "--output", out, "--level", "0.01","--deletions", "--insertions"});

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

}
