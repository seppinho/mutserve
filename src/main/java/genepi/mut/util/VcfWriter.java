package genepi.mut.util;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypeBuilder;
import htsjdk.variant.variantcontext.GenotypesContext;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.variantcontext.writer.Options;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.vcf.VCFConstants;
import htsjdk.variant.vcf.VCFFormatHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFHeaderVersion;

public class VcfWriter {

	public static void createVCF(String in, String out) throws FileNotFoundException {

		MutationServerReader reader = new MutationServerReader(in);
		String chromosome = "chrM";
		VCFHeader header = generateHeader(chromosome, 16569);

		HashMap<String, Sample> samples = reader.parse();
		HashSet<Integer> positions = new HashSet<Integer>();

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
			
			VariantContext vc = null;

			for (Sample sample : samples.values()) {

				if (sample.getPositions().containsKey(pos)) {
					Variant var = sample.getPositions().get(pos);
					
					System.out.println(var.getPos());
					
					Genotype gt = GenotypeBuilder.create(sample.getId(),
							Arrays.asList(Allele.create(var.getVariant() + ""), Allele.create(var.getVariant() + "")));

					final List<Allele> alleles = new ArrayList<Allele>();
					Allele refAllele = Allele.create(var.getRef() + "", true);
					Allele altAllele = Allele.create(var.getVariant() + "", false);
					alleles.add(refAllele);
					alleles.add(altAllele);

					vc = new VariantContextBuilder().start(var.getPos()).stop(var.getPos()).alleles(alleles)
							.genotypes(gt).chr("chrM").make();

				}
				
			}
			
			if(vc!=null)
			vcfWriter.add(vc);

		}

		vcfWriter.close();

	}

	public static void main(String[] args) {

		String input = "test-data/tmp/file.txt";
		String output = "/data2/git/mutation-server/test-data/results/variantsLocal1000G.vcf";

		try {
			createVCF(input, output);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static VCFHeader generateHeader(String chromosome, int length) {

		Set<VCFHeaderLine> headerLines = new HashSet<VCFHeaderLine>();
		Set<String> additionalColumns = new HashSet<String>();

		headerLines.add(new VCFHeaderLine(VCFHeaderVersion.VCF4_2.getFormatString(),
				VCFHeaderVersion.VCF4_2.getVersionString()));

		headerLines.add(new VCFFormatHeaderLine(VCFConstants.GENOTYPE_KEY, 1, VCFHeaderLineType.String, "Genotype"));

		SAMSequenceDictionary sequenceDict = generateSequenceDictionary(chromosome, length);

		VCFHeader header = new VCFHeader(headerLines, additionalColumns);
		header.setSequenceDictionary(sequenceDict);

		return header;

	}

	private VariantContext createVC(VCFHeader header, String chrom, String rsid, List<Allele> alleles,
			List<Allele> genotype, int position) {

		final Map<String, Object> attributes = new HashMap<String, Object>();
		final GenotypesContext genotypes = GenotypesContext.create(header.getGenotypeSamples().size());

		for (final String name : header.getGenotypeSamples()) {
			final Genotype gt = new GenotypeBuilder(name, genotype).phased(false).make();
			genotypes.add(gt);
		}

		return new VariantContextBuilder("23andMe", chrom, position, position, alleles).genotypes(genotypes)
				.attributes(attributes).id(rsid).make();
	}

	private static SAMSequenceDictionary generateSequenceDictionary(String name, int length) {

		SAMSequenceDictionary sequenceDict = new SAMSequenceDictionary();

		SAMSequenceRecord newSequence = new SAMSequenceRecord(name, length);

		sequenceDict.addSequence(newSequence);

		return sequenceDict;

	}

}
