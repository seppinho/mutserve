package genepi.mut.pileup;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import genepi.base.Tool;
import genepi.io.text.LineWriter;
import genepi.mut.objects.BasePosition;
import genepi.mut.objects.VariantLine;
import genepi.mut.objects.VariantResult;
import genepi.mut.util.VariantCaller;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;

public class PileupToolLocal extends Tool {

	public PileupToolLocal(String[] args) {
		super(args);
		System.out.println("Command " + Arrays.toString(args));
	}

	@Override
	public void createParameters() {

		addParameter("input", "input bam folder", Tool.STRING);
		addParameter("outputRaw", "output raw file", Tool.STRING);
		addParameter("outputVar", "output variants file", Tool.STRING);
		addParameter("level", "detection level", Tool.DOUBLE);
		addParameter("reference", "reference as fasta", Tool.STRING);
		addParameter("indel", "call deletions?", Tool.STRING);
		addParameter("baq", "apply BAQ?", Tool.STRING);
		addParameter("baseQ", "base quality", Tool.INTEGER);
		addParameter("mapQ", "mapping quality", Tool.INTEGER);
		addParameter("alignQ", "alignment quality", Tool.INTEGER);
	}

	@Override
	public void init() {
		System.out.println("Execute mtDNA-Server locally \n");
	}

	@Override
	public int run() {

		String version = "mtdna";

		String input = (String) getValue("input");

		String outputRaw = (String) getValue("outputRaw");

		String outputVar = (String) getValue("outputVar");

		String indel = (String) getValue("indel");

		String baq = (String) getValue("baq");

		double level = (double) getValue("level");

		int baseQ = (int) getValue("baseQ");

		int mapQ = (int) getValue("mapQ");

		int alignQ = (int) getValue("alignQ");

		String refPath = (String) getValue("reference");

		LineWriter writerRaw = null;

		LineWriter writerVar = null;

		File folderIn = new File(input);

		if (!folderIn.exists()) {

			System.out.println("Please check if input folder exists");
			System.out.println(folderIn.getAbsolutePath());
			return 1;
		}

		File[] files = folderIn.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".bam");
			}
		});

		if (files.length == 0) {

			System.out.println("no BAM files found. Please check input folder " + folderIn.getAbsolutePath());
			return 1;
		}

		try {

			File outRaw = new File(outputRaw);

			File outVar = new File(outputVar);

			File parentRaw = outRaw.getParentFile();
			File parentVar = outVar.getParentFile();

			if (parentRaw != null) {
				outRaw.getParentFile().mkdirs();
			}

			if (parentVar != null) {
				outVar.getParentFile().mkdirs();
			}

			writerRaw = new LineWriter(outRaw.getAbsolutePath());

			writerVar = new LineWriter(outVar.getAbsolutePath());

			writerRaw.write(BamAnalyser.headerRaw);

			writerVar.write(BamAnalyser.headerVariants);

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		long start = System.currentTimeMillis();

		for (File file : files) {

			BamAnalyser analyser = new BamAnalyser(file.getName(), refPath, baseQ, mapQ, alignQ, Boolean.valueOf(baq),
					version);

			System.out.println(" Processing: " + file.getName());

			try {

				analyseReads(file, analyser, Boolean.valueOf(indel));

				determineVariants(analyser, writerRaw, writerVar, level);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return 1;
			}

		}

		try {
			writerVar.close();
			writerRaw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Raw file written to " + new File(outputRaw).getAbsolutePath());
		System.out.println("Variants file written to " + new File(outputVar).getAbsolutePath());
		System.out.println("Time: " + (System.currentTimeMillis() - start) / 1000 + " sec");
		return 0;
	}

	// mapper
	private void analyseReads(File file, BamAnalyser analyser, boolean indelCalling) throws Exception, IOException {

		// TODO double check if primary and secondary alignment is used for
		// CNV-Server
		final SamReader reader = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.LENIENT)
				.open(file);

		SAMRecordIterator fileIterator = reader.iterator();

		while (fileIterator.hasNext()) {

			SAMRecord record = fileIterator.next();

			analyser.analyseRead(record, indelCalling);

		}
		reader.close();
	}

	// reducer
	private void determineVariants(BamAnalyser analyser, LineWriter writerRaw, LineWriter writerVariants, double level)
			throws IOException {

		HashMap<String, BasePosition> counts = analyser.getCounts();

		String reference = analyser.getReferenceString();

		for (String key : counts.keySet()) {

			String idKey = key.split(":")[0];

			String positionKey = key.split(":")[1];

			int pos;

			boolean insertion = false;

			if (positionKey.contains(".")) {
				pos = Integer.valueOf(positionKey.split("\\.")[0]);
				insertion = true;
			} else {
				pos = Integer.valueOf(positionKey);
			}

			if (pos > 0 && pos <= reference.length()) {

				char ref = 'N';

				BasePosition basePos = counts.get(key);

				basePos.setId(idKey);

				basePos.setPos(pos);

				VariantLine line = new VariantLine();

				if (!insertion) {

					ref = reference.charAt(pos - 1);

				} else {
					line.setInsPosition(positionKey);
				}

				line.setRef(ref);

				// create all required frequencies for one position
				// applies checkBases()
				line.parseLine(basePos, level);
				
				boolean isHeteroplasmy = false;

				for (char base : line.getMinors()) {

					double minorPercentageFwd = VariantCaller.getMinorPercentageFwd(line, base);

					double minorPercentageRev = VariantCaller.getMinorPercentageRev(line, base);
					
					double llrFwd = VariantCaller.determineLlrFwd(line, base)
							;
					double llrRev =VariantCaller.determineLlrRev(line, base);

					VariantResult varResult = VariantCaller.determineLowLevelVariant(line, minorPercentageFwd, minorPercentageRev, llrFwd, llrRev, level);
					
					if (varResult.getType() == VariantCaller.LOW_LEVEL_DELETION
							|| varResult.getType() == VariantCaller.LOW_LEVEL_VARIANT) {

						isHeteroplasmy = true;
						
						double hetLevel = VariantCaller.calcLevel(line, minorPercentageFwd, minorPercentageRev);

						varResult.setLevel(hetLevel);
						
						String res = VariantCaller.writeVariant(varResult);

						writerVariants.write(res);

					}

				}

				if(!isHeteroplasmy) {
					
					VariantResult varResult = VariantCaller.determineVariants(line);
					
					if (varResult.getType() == VariantCaller.VARIANT) {
						
						double hetLevel = VariantCaller.calcLevel(line, line.getMinorBasePercentsFWD(),
								line.getMinorBasePercentsREV());
						
						varResult.setLevel(hetLevel);

						String res = VariantCaller.writeVariant(varResult);

						writerVariants.write(res);

					}

				}

				// raw data
				String raw = line.toRawString();
				writerRaw.write(raw);

			}

		}

		// }
	}

	public static void main(String[] args) {

		String input = "test-data/mtdna/bam/input/";
		String outputVar = "test-data/tmp/out_var.txt";
		String outputRaw = "test-data/tmp/out_raw.txt";
		String fasta = "test-data/mtdna/bam/reference/rCRS.fasta";

		input = "/media/seb/DATA-HDD4/data-genepi/2017/Projects/cnv-server/evaluation/lpa/type-b/bam-realigned";
		fasta = "/home/seb/Desktop/realign/kiv2_6.fasta";
		
		input = "test-data/dna/plasmids/plasmid13/input/";
		fasta = "test-data/dna/plasmids/reference/kiv2_6.fasta";

		PileupToolLocal pileup = new PileupToolLocal(new String[] { "--input", input, "--reference", fasta,
				"--outputVar", outputVar, "--outputRaw", outputRaw, "--level", "0.01", "--baq", "false", "--indel",
				"false", "--baseQ", "20", "--mapQ", "20", "--alignQ", "30" });

		pileup.start();

	}

}
