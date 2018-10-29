package genepi.mut.util;

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
	public void compareOneCramSampleTxtVsVcfTest() {
		String in = "test-data/results/variantsLocal1000G";
		String out = "test-data/results/variantsLocal1000G.vcf";
		VcfWriter writer = new VcfWriter();
		writer.createVCF(in, out, "test-data/mtdna/mixtures/reference/rCRS.fasta", "chrM", 16569, "testcommand");

		VCFFileReader reader = new VCFFileReader(new File(out), false);
		MutationServerReader readerServer = new MutationServerReader(in);

		HashMap<String, Sample> samples = readerServer.parse();

		String sampleString = "test.cram";

		Sample sample = samples.get(sampleString);
		int countHomoplasmies = 0;
		int countHeteroplasmies = 0;

		for (Variant variant : sample.getVariants()) {
			int pos = variant.getPos();
			char var = variant.getVariant();

			if (variant.getType() == 1) {
				countHomoplasmies++;
				for (final VariantContext vc : reader) {
					if (vc.getStart() == pos) {
						Genotype genotype = vc.getGenotype(sampleString);
						assertEquals(genotype.getGenotypeString(false), String.valueOf(var));
					}

				}

			}

			if (variant.getType() == 2) {
				for (final VariantContext vc : reader) {
					if (vc.getStart() == pos) {
						countHeteroplasmies++;
						Genotype genotype = vc.getGenotype(sampleString);
						genotype.getAlleles();
						String txtGenotype = variant.getMajor() + "/" + variant.getMinor();
						assertEquals(txtGenotype, genotype.getGenotypeString(true));
					}

				}

			}
		}

		assertEquals(7, countHeteroplasmies);
		assertEquals(38, countHomoplasmies);

		reader.close();
	}

	@Test
	public void compareTwoCramSampleTxtVsVcfTest() {
		String in = "test-data/mtdna/cram/output/variants.txt";
		String vcf = "test-data/mtdna/cram/output/variants.vcf";
		VcfWriter writer = new VcfWriter();
		writer.createVCF(in, vcf, "test-data/mtdna/mixtures/reference/rCRS.fasta", "chrM", 16569, "testcommand");

		VCFFileReader reader = new VCFFileReader(new File(vcf), false);
		MutationServerReader readerServer = new MutationServerReader(in);

		HashMap<String, Sample> samples = readerServer.parse();

		for (Sample sample : samples.values()) {
			int countHomoplasmies = 0;
			int countHeteroplasmies = 0;

			for (Variant variant : sample.getVariants()) {
				int pos = variant.getPos();
				char var = variant.getVariant();

				if (variant.getType() == 1) {
					countHomoplasmies++;
					for (final VariantContext vc : reader) {
						if (vc.getStart() == pos) {
							Genotype genotype = vc.getGenotype(sample.getId());
							assertEquals(genotype.getGenotypeString(false), String.valueOf(var));
						}

					}

				}

				if (variant.getType() == 2) {
					for (final VariantContext vc : reader) {
						if (vc.getStart() == pos) {
							countHeteroplasmies++;
							Genotype genotype = vc.getGenotype(sample.getId());
							String txtGenotype = variant.getMajor() + "/" + variant.getMinor();
							assertEquals(txtGenotype, genotype.getGenotypeString(true));
						}

					}

				}
			}

			if (sample.getId().equals("test.cram")) {
				assertEquals(7, countHeteroplasmies);
				assertEquals(38, countHomoplasmies);
			} else if (sample.getId().equals("test1.cram")) {
				assertEquals(21, countHeteroplasmies);
				assertEquals(19, countHomoplasmies);
			}
		}

		reader.close();
	}

}
