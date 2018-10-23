package genepi.mut.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.reference.FastaSequenceIndex;
import htsjdk.samtools.reference.FastaSequenceIndexCreator;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import htsjdk.samtools.reference.ReferenceSequence;
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
import htsjdk.variant.vcf.VCFInfoHeaderLine;

public class VcfWriter {

	public static void createVCF(String in, String out) throws FileNotFoundException {

		MutationServerReader reader = new MutationServerReader(in);
		String chromosome = "chrM";
		VCFHeader header = generateHeader(chromosome, 16569);

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
		File fasta = new File("test-data/mtdna/mixtures/reference/rCRS.fasta");

		try {
			FastaSequenceIndex fg = FastaSequenceIndexCreator.buildFromFasta(fasta.toPath());
			fg.write(new File(fasta + ".fai").toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		IndexedFastaSequenceFile seq = new IndexedFastaSequenceFile(fasta,
				new FastaSequenceIndex(new File(fasta + ".fai")));

		for (Integer pos : positions) {

			VariantContextBuilder vcBuilder = new VariantContextBuilder().start(pos).stop(pos).chr("chrM");

			Genotype gt = null;
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

						gt = GenotypeBuilder.create(sample.getId(),
								Arrays.asList(Allele.create(var.getVariant() + "")));

						genotypes.add(gt);

					} else if (var.getType() == 2) {

						char allele2;
						char ref;
						Allele allele1 = null;

						//check for multiallelic sites
						if (var.getMajor() == var.getRef()) {
							
							ref = var.getMajor();
							allele1 = Allele.create(ref + "", true);
							allele2 = var.getMinor();
							
						} else {
							
							allele2 = var.getMajor();
							
							// third allele found!
							if (var.getMinor() != var.getRef()) {
								allele1 = Allele.create(var.getMinor() + "", false);
								alleles.add(allele1);
							} else {
								ref = var.getMinor();
								allele1 = Allele.create(ref + "", true);
							}
						}
						
						gt = GenotypeBuilder.create(sample.getId(),
								Arrays.asList(allele1, Allele.create(allele2 + "")));

						genotypes.add(gt);

						vcBuilder.attribute("LFF", var.getLevel());

					}
				} else {
					ReferenceSequence base = seq.getSubsequenceAt("rCRS", pos, pos);
					gt = GenotypeBuilder.create(sample.getId(),
							Arrays.asList(Allele.create(base.getBaseString() + "", true)));
					genotypes.add(gt);
				}
			}

			if (alleles.size() > 0) {
				vcBuilder.alleles(alleles).genotypes(genotypes);
				vcBuilder.filter("PASS");
				vcfWriter.add(vcBuilder.make());
			}
		}

		vcfWriter.close();
		
		try {
			seq.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {

		String input = "test-data/results/file.txt";
		String output = "test-data/results/variantsLocal1000G.vcf";

		try {
			createVCF(input, output);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static VCFHeader generateHeader(String chromosome, int length) {

		Set<VCFHeaderLine> headerLines = new HashSet<VCFHeaderLine>();

		headerLines.add(new VCFHeaderLine(VCFHeaderVersion.VCF4_2.getFormatString(),
				VCFHeaderVersion.VCF4_2.getVersionString()));

		headerLines.add(new VCFFormatHeaderLine(VCFConstants.GENOTYPE_KEY, 1, VCFHeaderLineType.String, "Genotype"));

		SAMSequenceDictionary sequenceDict = generateSequenceDictionary(chromosome, length);

		VCFHeader header = new VCFHeader(headerLines);

		header.setSequenceDictionary(sequenceDict);

		header.addMetaDataLine(new VCFFilterHeaderLine("PASS", "Variants passed mtDNA-Server"));

		header.addMetaDataLine(new VCFInfoHeaderLine("LFF", 1, VCFHeaderLineType.Float, "Low Frequency Fraction"));

		return header;

	}

	private static SAMSequenceDictionary generateSequenceDictionary(String name, int length) {

		SAMSequenceDictionary sequenceDict = new SAMSequenceDictionary();

		SAMSequenceRecord newSequence = new SAMSequenceRecord(name, length);

		sequenceDict.addSequence(newSequence);

		return sequenceDict;

	}

}
