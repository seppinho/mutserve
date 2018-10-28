package genepi.mut.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import genepi.mut.objects.Sample;
import genepi.mut.objects.Variant;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypeBuilder;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.variantcontext.writer.Options;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.vcf.VCFConstants;
import htsjdk.variant.vcf.VCFFilterHeaderLine;
import htsjdk.variant.vcf.VCFFormatHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFHeaderVersion;

public class VcfWriter {

	public void createVCF(String in, String out, String reference, String chromosome, int length, String command) {

		MutationServerReader reader = new MutationServerReader(in);

		VCFHeader header = generateHeader(chromosome, length, command);

		String fasta = ReferenceUtil.readInReference(reference);

		HashMap<String, Sample> samples = reader.parse();
		TreeSet<Integer> positions = new TreeSet<Integer>();

		for (Sample sample : samples.values()) {
			header.getGenotypeSamples().add(sample.getId());
			for (Variant var : sample.getVariants()) {
				positions.add(var.getPos());
			}
		}

		VariantContextWriterBuilder builder = new VariantContextWriterBuilder().setOutputFile(out)
				.unsetOption(Options.INDEX_ON_THE_FLY);
		VariantContextWriter vcfWriter = builder.build();
		vcfWriter.writeHeader(header);

		for (Integer pos : positions) {

			VariantContextBuilder vcBuilder = new VariantContextBuilder().start(pos).stop(pos).chr(chromosome);

			final List<Genotype> genotypes = new ArrayList<Genotype>();
			final HashSet<Allele> alleles = new HashSet<Allele>();

			for (Sample sample : samples.values()) {

				Variant variant = sample.getVariant(pos);

				if (variant != null) {

					Allele refAllele = Allele.create(variant.getRef() + "", true);
					Allele varAllele = Allele.create(variant.getVariant() + "", false);
					alleles.add(refAllele);
					alleles.add(varAllele);

					if (variant.getType() == 1) {

						final GenotypeBuilder gb = new GenotypeBuilder(sample.getId(), Arrays.asList(varAllele));
						gb.DP(variant.getCoverage());

						genotypes.add(gb.make());

					} else if (variant.getType() == 2) {

						Allele genotypeAllele1;
						Allele genotypeAllele2;

						// check for multiallelic sites
						if (variant.getMajor() == variant.getRef()) {
							genotypeAllele1 = Allele.create(variant.getMajor() + "", true);
							genotypeAllele2 = Allele.create(variant.getMinor() + "", false);

						} else {
							genotypeAllele1 = Allele.create(variant.getMajor() + "", false);
							// REF: C; MAJOR: A; MINOR:T
							if (variant.getMinor() != variant.getRef()) {
								genotypeAllele2 = Allele.create(variant.getMinor() + "", false);
								// new allele found, add to alleles
								alleles.add(genotypeAllele2);
							} else {
								genotypeAllele2 = Allele.create(variant.getMinor() + "", true);
							}
						}

						final GenotypeBuilder gb = new GenotypeBuilder(sample.getId(),
								Arrays.asList(genotypeAllele1, genotypeAllele2));
						gb.DP(variant.getCoverage());
						gb.attribute("HP", variant.getLevel());

						if (variant.getLevel() == variant.getMajorLevel()) {
							gb.attribute("HP1", variant.getMinorLevel());
						} else {
							gb.attribute("HP1", variant.getMajorLevel());
						}
						genotypes.add(gb.make());

					}
				} else {
					final GenotypeBuilder gb = new GenotypeBuilder(sample.getId(),
							Arrays.asList(Allele.create(fasta.charAt(pos - 1) + "", true)));
					genotypes.add(gb.make());
				}
			}

			if (alleles.size() > 0) {
				vcBuilder.alleles(alleles).genotypes(genotypes);
				vcBuilder.filter("PASS");
				vcfWriter.add(vcBuilder.make());
			}
		}

		vcfWriter.close();
	}

	private VCFHeader generateHeader(String chromosome, int length, String command) {

		Set<VCFHeaderLine> headerLines = new HashSet<VCFHeaderLine>();

		headerLines.add(new VCFHeaderLine(VCFHeaderVersion.VCF4_2.getFormatString(),
				VCFHeaderVersion.VCF4_2.getVersionString()));

		headerLines.add(new VCFFormatHeaderLine(VCFConstants.GENOTYPE_KEY, 1, VCFHeaderLineType.String, "Genotype"));

		SAMSequenceDictionary sequenceDict = generateSequenceDictionary(chromosome, length);

		VCFHeader header = new VCFHeader(headerLines);

		header.setSequenceDictionary(sequenceDict);

		header.addMetaDataLine(new VCFHeaderLine("Mutserve", command));

		header.addMetaDataLine(new VCFFilterHeaderLine("PASS", "Variants passed Mutserve"));

		header.addMetaDataLine(new VCFFormatHeaderLine("HP", 1, VCFHeaderLineType.Float,
				"Inferred Heteroplasmy Frequency of top (non-reference) allele"));

		header.addMetaDataLine(new VCFFormatHeaderLine("HP1", 1, VCFHeaderLineType.Float,
				"Inferred Heteroplasmy Frequency of reference or second allele)"));

		header.addMetaDataLine(new VCFFormatHeaderLine("DP", 1, VCFHeaderLineType.Integer, "Read Depth"));

		return header;

	}

	private SAMSequenceDictionary generateSequenceDictionary(String name, int length) {

		SAMSequenceDictionary sequenceDict = new SAMSequenceDictionary();

		SAMSequenceRecord newSequence = new SAMSequenceRecord(name, length);

		sequenceDict.addSequence(newSequence);

		return sequenceDict;

	}

}
