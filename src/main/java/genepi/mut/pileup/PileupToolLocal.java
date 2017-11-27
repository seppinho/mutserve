package genepi.mut.pileup;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import genepi.base.Tool;
import genepi.io.text.LineWriter;
import genepi.mut.objects.BasePosition;
import genepi.mut.objects.VariantLine;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;

public class PileupToolLocal extends Tool {

	public PileupToolLocal(String[] args) {
		super(args);
	}

	@Override
	public void createParameters() {

		addParameter("input", "input bam file", Tool.STRING);
		addParameter("reference", "reference as fasta", Tool.STRING);
		addParameter("out-prefix", "output prefix", Tool.STRING);
		addOptionalParameter("indel", "call indels?", Tool.STRING);
	}

	@Override
	public void init() {
		System.out.println("Execute CNV-Server locally \n");
	}

	@Override
	public int run() {
		
		long start = System.currentTimeMillis();

		String input = (String) getValue("input");

		String outputPrefix = (String) getValue("out-prefix");

		String indel = (String) getValue("indel");

		String refPath = (String) getValue("reference");

		try {

			BamAnalyser analyser = new BamAnalyser(input, refPath);

			analyseReads(analyser);

			determineVariants(analyser, outputPrefix, Boolean.valueOf(indel));

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Fin. Took "+ (System.currentTimeMillis() - start)/1000 +" sec");
		
		System.out.println("Output location: " + outputPrefix+".filtered.txt");

		return 0;

	}

	// mapper
	private void analyseReads(BamAnalyser analyser) throws Exception, IOException {

		// TODO double check if primary and secondary alignment is used for
		// CNV-Server
		final SamReader reader = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.LENIENT)
				.open(new File(analyser.getFilename()));

		SAMRecordIterator fileIterator = reader.iterator();

		while (fileIterator.hasNext()) {

			SAMRecord record = fileIterator.next();

			analyser.analyseRead(record);

		}
		reader.close();
	}

	// reducer
	private void determineVariants(BamAnalyser analyser, String output, boolean indel) throws IOException {

		String outputFiltered = output + "_filtered.txt";

		LineWriter writer = new LineWriter(outputFiltered);

		writer.write(
				"SampleID\tPos\tRef\tVariant\tMajor/Minor\tVariant-Level\tCoverage-FWD\tCoverage-Rev\tCoverage-Total");

		HashMap<String, BasePosition> counts = analyser.getCounts();

		String reference = analyser.getReferenceString();

		for (String key : counts.keySet()) {

			int pos = Integer.valueOf(key);

			if (pos > 0 && pos <= reference.length()) {

				BasePosition basePos = counts.get(key);

				basePos.setId(new File(analyser.getFilename()).getName());

				basePos.setPos(pos);

				char ref = reference.charAt(pos - 1);

				VariantLine line = new VariantLine();

				line.setCallDel(indel);

				line.setRef(ref);

				line.analysePosition(basePos);

				line.determineLowLevelVariant();

				// only execute if no low-level variant has been detected
				if (line.getVariantType() == 0) {
					line.determineVariants();
				}

				if (line.getVariantType() == VariantLine.VARIANT
						|| line.getVariantType() == VariantLine.LOW_LEVEL_VARIANT) {
					writer.write((line.writeVariant()));
				}

				if (indel && line.getVariantType() == VariantLine.LOW_LEVEL_DELETION) {
					writer.write(line.writeVariant());
				}

			}

		}
		writer.close();
	}

	public static void main(String[] args) {

		String input = "/home/seb/git/mutation-server/test-data/mtdna/bam/input/HG00096.mapped.ILLUMINA.bwa.GBR.low_coverage.20101123.bam";

		PileupToolLocal pileup = new PileupToolLocal(
				new String[] { "--input", input, "--reference", "/home/seb/Desktop/rcrs/rCRS.fasta", "--out-prefix",
						"/home/seb/Desktop/prefix_1KP3", "--indel", "false" });

		pileup.start();

	}

}