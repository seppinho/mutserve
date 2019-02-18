package genepi.mut.steps;

import org.junit.Test;

import genepi.io.table.reader.CsvTableReader;
import genepi.mut.objects.VariantLine;
import genepi.mut.pileup.PileupToolLocal;
import static org.junit.Assert.*;

public class LocalToolTest {

	@Test
	public void testMinorBasesForEqualLevels() {

		String input = "test-data/mtdna/equal-level/test.bam";
		String ref = "test-data/mtdna/reference/rCRS.fasta";
		String out = "test-data/mtdna/equal-level/here.txt";

		PileupToolLocal pileup = new PileupToolLocal(
				new String[] { "--input", input, "--reference", ref, "--output", out, "--level", "0.01", "--noBaq" });

		pileup.start();

		CsvTableReader cloudgeneReader = new CsvTableReader("test-data/mtdna/equal-level/here_raw.txt", '\t');

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

	}

}
