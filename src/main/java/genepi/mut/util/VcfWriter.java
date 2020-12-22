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
import genepi.mut.util.VariantCaller.Filter;
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
			for (Integer key : sample.getKeys()) {
				positions.add(key);
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

				ArrayList<Variant> variants = sample.getVariants(pos);

				if (variants != null) {

					int type1 = 0;
					int type5 = 0;
					boolean multiInsertion = false;
					boolean complex = false;
					
					for (Variant var : variants) {

						if ((var.getType() == 1)) {
							type1++;
						}
						if (var.getType() == 5) {
							type5++;
						}
					}

					if (type1 >= 1 && type5 >= 1) {
					} else if (type5 > 1) {
						multiInsertion = true;
					}
					
					String base = null;
					char ref = '-';

					Variant variant = variants.get(0);
					
					// Write only PASS to VCF
					if(variant.getFilter() != Filter.PASS) {
						continue;
					}
					
					if (variant.getVariant() == 'D') {
						variant.setVariantBase('*');
					}

					if (variant.getMajor() == 'D') {
						variant.setMajor('*');
					}

					if (variant.getMinor() == 'D') {
						variant.setMinor('*');
					}
					
					//default case: a position does not consist of variants in combination with insertions
					if (!multiInsertion && !complex) {

						// deletions handling
						base = String.valueOf(variant.getVariant());
						ref = variant.getRef();

						if (variant.getType() == 5) {
							ref = fasta.charAt(pos - 1);
							base = ref + "" + variant.getVariant();
						}

					} else {

						if (multiInsertion) {

							ref = fasta.charAt(pos - 1);

							StringBuilder insertionBuilder = new StringBuilder();

							//TODO sort!
							for (Variant var : variants) {
								if (var.getType() == 5) {
									insertionBuilder.append(var.getVariant());
								}
							}
							String insertion = insertionBuilder.toString();
							base = ref + insertion;
							
						}
					}
					
					//TOOD: heteroplasmy and insertion!
					if(ref == '-') {
						continue;
					}
					
					Allele refAllele = Allele.create(ref + "", true);
					Allele varAllele = Allele.create(base + "", false);
					alleles.add(refAllele);
					alleles.add(varAllele);

					if (variant.getType() == 1 || variant.getType() == 4 || variant.getType() == 5) {
						final GenotypeBuilder gb = new GenotypeBuilder(sample.getId(), Arrays.asList(varAllele));
						gb.DP(variant.getCoverage());
						gb.attribute("AF", variant.getLevel());

						genotypes.add(gb.make());

					} else if (variant.getType() == 2) {

						Allele genotypeAllele1;
						Allele genotypeAllele2;

						String major = String.valueOf(variant.getMajor());
						String minor = String.valueOf(variant.getMinor());

						// check for multiallelic sites
						if (variant.getMajor() == variant.getRef()) {
							genotypeAllele1 = Allele.create(major + "", true);
							genotypeAllele2 = Allele.create(minor + "", false);

						} else {
							genotypeAllele1 = Allele.create(major + "", false);
							// REF: C; MAJOR: A; MINOR:T
							if (variant.getMinor() != variant.getRef()) {
								genotypeAllele2 = Allele.create(minor + "", false);
								// new allele found, add to alleles
								alleles.add(genotypeAllele2);
							} else {
								genotypeAllele2 = Allele.create(minor + "", true);
							}
						}

						final GenotypeBuilder gb = new GenotypeBuilder(sample.getId(),
								Arrays.asList(genotypeAllele1, genotypeAllele2));
						gb.DP(variant.getCoverage());

						String alleleFreq = variant.getLevel() + "";

						if (variant.getLevel() == variant.getMajorLevel() && variant.getMinor() != variant.getRef()) {
							alleleFreq += "," + variant.getMinorLevel();
						} else if (variant.getLevel() == variant.getMinorLevel()
								&& variant.getMajor() != variant.getRef()) {
							alleleFreq += "," + variant.getMajorLevel();
						}

						gb.attribute("AF", alleleFreq);

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

		header.addMetaDataLine(new VCFFilterHeaderLine("PASS", "Variants passed mtDNA-Server"));

		header.addMetaDataLine(new VCFFormatHeaderLine("AF", 1, VCFHeaderLineType.String,
				"Inferred Allele Frequency of top (non-reference) allele"));

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
