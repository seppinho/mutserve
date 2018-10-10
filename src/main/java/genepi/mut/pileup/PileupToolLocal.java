package genepi.mut.pileup;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import genepi.base.Tool;
import genepi.io.FileUtil;
import genepi.io.text.LineWriter;
import genepi.mut.objects.BasePosition;
import genepi.mut.objects.VariantLine;
import genepi.mut.objects.VariantResult;
import genepi.mut.util.ReferenceUtil;
import genepi.mut.util.ReferenceUtil.Reference;
import genepi.mut.util.VariantCaller;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;

public class PileupToolLocal extends Tool {

	String version = "v1.1.7";

	public PileupToolLocal(String[] args) {
		super(args);
		System.out.println("Command " + Arrays.toString(args));
	}

	@Override
	public void createParameters() {

		addParameter("input", "input bam file or folder", Tool.STRING);
		addParameter("output", "output folder", Tool.STRING);
		addParameter("level", "detection level", Tool.DOUBLE);
		addParameter("reference", "reference as fasta", Tool.STRING);
		addOptionalParameter("baseQ", "base quality", Tool.STRING);
		addOptionalParameter("mapQ", "mapping quality", Tool.STRING);
		addOptionalParameter("alignQ", "alignment quality", Tool.STRING);
		addFlag("baq", "Apply BAQ");
		addFlag("indel", "Call indels");
	}

	@Override
	public void init() {
		System.out.println("Low-frequency Variant Detection" + version);
		System.out.println("Division of Genetic Epidemiology - Medical University of Innsbruck");
		System.out.println("(c) Sebastian Schoenherr, Hansi Weissensteiner, Lukas Forer");
		System.out.println("");
	}

	@Override
	public int run() {

		String input = (String) getValue("input");

		String output = (String) getValue("output");

		boolean baq = isFlagSet("baq");

		boolean indel = isFlagSet("indel");

		double level = (double) getValue("level");

		int baseQ;

		if (getValue("baseQ") == null) {
			baseQ = 20;
		} else {
			baseQ = Integer.valueOf((String) getValue("baseQ"));
		}

		int mapQ;
		if (getValue("mapQ") == null) {
			mapQ = 30;
		} else {
			mapQ = Integer.valueOf((String) getValue("mapQ"));
		}

		int alignQ;
		if (getValue("alignQ") == null) {
			alignQ = 30;
		} else {
			alignQ = Integer.valueOf((String) getValue("alignQ"));
		}

		String refPath = (String) getValue("reference");

		LineWriter writerRaw = null;

		LineWriter writerVar = null;

		File folderIn = new File(input);
		File[] files;

		if (folderIn.isFile()) {
			files = new File[1];
			files[0] = new File(folderIn.getAbsolutePath());

		} else {
			files = folderIn.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".bam");
				}
			});

			if (files.length == 0) {

				System.out.println("no BAM files found. Please check input folder " + folderIn.getAbsolutePath());

				return 1;
			}
		}

		try {

			long time = System.currentTimeMillis();
			String varOut = FileUtil.path(output, "variants_" + time + ".txt");
			String rawOut = FileUtil.path(output, "raw_" + time + ".txt");

			File outRaw = new File(rawOut);
			File outVar = new File(varOut);

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

		System.out.println("");
		System.out.println("Input Folder: " + input);
		System.out.println("Output Folder: " + output);
		System.out.println("Detection limit: " + level);
		System.out.println("Base Quality: " + baseQ);
		System.out.println("Map Quality: " + mapQ);
		System.out.println("Alignment Quality: " + alignQ);
		System.out.println("BAQ: " + baq);
		System.out.println("Indel: " + indel);
		System.out.println("BaseQ: " + baseQ);
		System.out.println("");

		for (File file : files) {

			Reference reference = ReferenceUtil.determineReference(file);

			if (reference == Reference.hg19) {

				System.out.println(" File " + file.getName()
						+ " excluded! File is aligned to Yoruba (Reference length 16571) and not rCRS. ");

				continue;

			}

			else if (reference == Reference.rcrs || reference == Reference.precisionId) {

				BamAnalyser analyser = new BamAnalyser(file.getName(), refPath, baseQ, mapQ, alignQ, baq, version);

				System.out.println(" Processing: " + file.getName());
				System.out.println(" Detected reference: " + reference.toString());

				try {

					analyseReads(file, analyser, indel);

					determineVariants(analyser, writerRaw, writerVar, level);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return 1;
				}

			} else {

				System.out.println("File " + file.getName() + " excluded. Can not identify a valid reference length!");

				continue;
			}
		}

		try {

			writerVar.close();

			writerRaw.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Time: " + (System.currentTimeMillis() - start) / 1000 + " sec");
		return 0;
	}

	// mapper
	private void analyseReads(File file, BamAnalyser analyser, boolean indelCalling) throws Exception, IOException {

		// TODO double check if primary and secondary alignment is used for
		// CNV-Server
		final SamReader reader = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT)
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

					ref = '-';

					line.setInsertion(true);

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

					double llrFwd = VariantCaller.determineLlrFwd(line, base);

					double llrRev = VariantCaller.determineLlrRev(line, base);

					VariantResult varResult = VariantCaller.determineLowLevelVariant(line, minorPercentageFwd,
							minorPercentageRev, llrFwd, llrRev, level, base);

					if (varResult.getType() == VariantCaller.LOW_LEVEL_DELETION
							|| varResult.getType() == VariantCaller.LOW_LEVEL_VARIANT) {

						isHeteroplasmy = true;

						// set correct minor base for output result!
						varResult.setMinor(base);

						double hetLevel = VariantCaller.calcVariantLevel(line, minorPercentageFwd, minorPercentageRev);

						double levelTop = VariantCaller.calcLevelTop(line);

						double levelMinor = VariantCaller.calcLevelMinor(line, minorPercentageFwd, minorPercentageRev);

						varResult.setLevelTop(levelTop);

						varResult.setLevelMinor(levelMinor);

						varResult.setLevel(hetLevel);

						String res = VariantCaller.writeVariant(varResult);

						writerVariants.write(res);

					}
				}

				if (!isHeteroplasmy) {

					VariantResult varResult = VariantCaller.determineVariants(line);

					if (varResult != null) {

						double hetLevel = VariantCaller.calcVariantLevel(line, line.getMinorBasePercentsFWD(),
								line.getMinorBasePercentsREV());

						double levelTop = VariantCaller.calcLevelTop(line);

						double levelMinor = VariantCaller.calcLevelMinor(line, line.getMinorBasePercentsFWD(),
								line.getMinorBasePercentsREV());

						varResult.setLevelTop(levelTop);

						varResult.setLevelMinor(levelMinor);

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
	}

	public static void main(String[] args) {

		String input = "test-data/mtdna/bam/input/HG00096.mapped.ILLUMINA.bwa.GBR.low_coverage.20101123.bam";
		String output = "test-data/tmp/";
		String fasta = "test-data/mtdna/bam/reference/rCRS.fasta";

		fasta = "test-data/mtdna/bam/reference/rCRS.fasta";

		PileupToolLocal pileup = new PileupToolLocal(
				new String[] { "--input", input, "--reference", fasta, "--output", output, "--level", "0.01" });

		pileup.start();

	}

}
