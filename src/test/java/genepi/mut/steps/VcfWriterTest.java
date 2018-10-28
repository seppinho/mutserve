package genepi.mut.steps;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashMap;

import org.junit.Test;

import genepi.mut.objects.Sample;
import genepi.mut.objects.Variant;
import genepi.mut.util.MutationServerReader;
import genepi.mut.util.VcfWriter;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;

public class VcfWriterTest {

	@Test
	public void compareMutationWithVcfFileTest() {
		String in = "test-data/results/variantsLocal1000G";
		String out = "test-data/results/variantsLocal1000G.vcf";
		VcfWriter writer = new VcfWriter();
		writer.createVCF(in, out, "test-data/mtdna/mixtures/reference/rCRS.fasta", "chrM", 16569, "testcommand");

		VCFFileReader reader = new VCFFileReader(new File(out), false);
		MutationServerReader readerServer = new MutationServerReader(in);

		HashMap<String, Sample> samples = readerServer.parse();

		String sampleString = "test.cram";

		Sample sample = samples.get(sampleString);

		for (Variant variant : sample.getVariants()) {
			int pos = variant.getPos();
			char var = variant.getVariant();

			if (variant.getType() == 1) {

				for (final VariantContext vc : reader) {
					if (vc.getStart() == pos) {
						Genotype genotype = vc.getGenotype(sampleString);
						assertEquals(genotype.getGenotypeString(false), String.valueOf(var));
					}

				}

			}
		}

		reader.close();
	}

}
