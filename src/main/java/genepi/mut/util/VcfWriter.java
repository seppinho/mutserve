package genepi.mut.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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

	public void createVCF(String in, String out, String reference, String chromosome, int length) {

		MutationServerReader reader = new MutationServerReader(in);
		
		VCFHeader header = generateHeader(chromosome, length);
		
		String fasta = ReferenceUtil.readInReference(reference);

		HashMap<String, Sample> samples = reader.parse();
		TreeSet<Integer> positions = new TreeSet<Integer>();

		for (Sample sample : samples.values()) {
			header.getGenotypeSamples().add(sample.getId());
			for (Variant var : sample.getPositions().values()) {
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

				if (sample.getPositions().containsKey(pos)) {

					Variant var = sample.getPositions().get(pos);

					Allele refAllele = Allele.create(var.getRef() + "", true);
					Allele varAllele = Allele.create(var.getVariant() + "", false);
					alleles.add(refAllele);
					alleles.add(varAllele);

					if (var.getType() == 1) {

						final GenotypeBuilder gb = new GenotypeBuilder(sample.getId(), Arrays.asList(varAllele));
						gb.DP(var.getCoverage());
						genotypes.add(gb.make());

					} else if (var.getType() == 2) {

						char allele2;
						char ref;
						Allele allele1 = null;

						// check for multiallelic sites
						if (var.getMajor() == var.getRef()) {
							ref = var.getMajor();
							allele1 = Allele.create(ref + "", true);
							allele2 = var.getMinor();

						} else {

							allele2 = var.getMajor();

							// third allele found, add to alleles
							if (var.getMinor() != var.getRef()) {
								allele1 = Allele.create(var.getMinor() + "", false);
								alleles.add(allele1);
							} else {
								ref = var.getMinor();
								allele1 = Allele.create(ref + "", true);
							}
						}

						final GenotypeBuilder gb = new GenotypeBuilder(sample.getId(),
								Arrays.asList(allele1, Allele.create(allele2 + "")));
						gb.DP(var.getCoverage());
						gb.attribute("HP", var.getLevel());
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

	private VCFHeader generateHeader(String chromosome, int length) {

		Set<VCFHeaderLine> headerLines = new HashSet<VCFHeaderLine>();

		headerLines.add(new VCFHeaderLine(VCFHeaderVersion.VCF4_2.getFormatString(),
				VCFHeaderVersion.VCF4_2.getVersionString()));

		headerLines.add(new VCFFormatHeaderLine(VCFConstants.GENOTYPE_KEY, 1, VCFHeaderLineType.String, "Genotype"));

		SAMSequenceDictionary sequenceDict = generateSequenceDictionary(chromosome, length);

		VCFHeader header = new VCFHeader(headerLines);

		header.setSequenceDictionary(sequenceDict);

		header.addMetaDataLine(new VCFFilterHeaderLine("PASS", "Variants passed mtDNA-Server"));

		header.addMetaDataLine(
				new VCFFormatHeaderLine("HP", 1, VCFHeaderLineType.Float, "Inferred Heteroplasmy Frequency"));

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
