package genepi.mut.vc;

import java.io.DataInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import genepi.io.text.LineWriter;
import genepi.mut.objects.BasePosition;
import genepi.mut.objects.BayesFrequencies;
import genepi.mut.objects.VariantLine;
import genepi.mut.objects.VariantResult;
import genepi.mut.pileup.BamAnalyser;
import genepi.mut.util.FastaWriter;
import genepi.mut.util.VariantCaller;
import genepi.mut.util.VcfWriter;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;

public class VariantCallingTask {
	
	private String input;
	private String output;
	private double level;
	private int baseQ = 20;
	private int mapQ = 20;
	private int alignQ = 30;
	boolean baq = true;
	boolean freq = false;
	boolean writeFasta;
	String reference;
	String mode = "mtdna";
	String command;
	String version;
	
	public int run() {
		
		
		LineWriter writerRaw = null;
		LineWriter writerVar = null;

		File folderIn = new File(input);
		File[] files;

		if (folderIn.exists()) {
			if (folderIn.isFile()) {
				files = new File[1];
				files[0] = new File(folderIn.getAbsolutePath());

				if (!files[0].getName().toLowerCase().endsWith(".cram")
						&& !files[0].getName().toLowerCase().endsWith(".bam")) {
					System.out.println("Please upload a CRAM/BAM file");
					return 1;
				}

			} else {
				files = folderIn.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.toLowerCase().endsWith(".bam") || name.toLowerCase().endsWith(".cram");
					}
				});
			}
		} else {
			System.out.println("Please check input path!");
			return 1;
		}

		if (files.length > 0) {

			File out = new File(output);

			if (out.isDirectory()) {
				System.out.println("Error. Please specify an output file not a directory");
				return 1;
			}

			String prefix = output;

			if (output.contains(".")) {
				prefix = output.substring(0, output.indexOf('.'));
			}

			String varFile = prefix + ".txt";
			String rawFile = prefix + "_raw.txt";

			try {
				writerVar = new LineWriter(new File(varFile).getAbsolutePath());
				writerVar.write(BamAnalyser.headerVariants);

				writerRaw = new LineWriter(new File(rawFile).getAbsolutePath());
				writerRaw.write(BamAnalyser.headerRaw);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			long start = System.currentTimeMillis();

			// load frequency file
			HashMap<String, Double> freqFile = null;
			if (true) {
				InputStream in = this.getClass().getClassLoader().getResourceAsStream("1000g.frq");
				freqFile = BayesFrequencies.instance(new DataInputStream(in));
			}

			// iterate over several BAM/CRAM files
			for (File file : files) {

				BamAnalyser analyser = new BamAnalyser(file.getName(), reference, baseQ, mapQ, alignQ, baq, mode);

				HashMap<Integer, BasePosition> positions = analyser.getCounts();

				System.out.println("Processing: " + file.getName());

				// first position to analyze
				int pos = 1;

				try {

					final SamReader reader = SamReaderFactory.makeDefault()
							.validationStringency(ValidationStringency.SILENT).open(file);

					SAMRecordIterator fileIterator = reader.iterator();

					String reference = analyser.getReferenceString();

					while (fileIterator.hasNext()) {

						SAMRecord record = fileIterator.next();

						analyser.analyseRead(record);

						int current = record.getStart();
						
						// call variants between pos and current
						while(pos < current) {

							positions.remove(pos - 1);

							if (positions.containsKey(pos) && pos <= reference.length()) {

								callVariant(writerRaw, writerVar, file.getName(), level, pos, positions.get(pos),
										reference, freqFile);

							}
							pos++;
						}
						
					}

					// analyze remaining positions
					for (int i = pos-1; i <= reference.length(); i++) {
						if (positions.containsKey(pos) && pos <= reference.length()) {
							callVariant(writerRaw, writerVar, file.getName(), level, i, positions.get(i), reference,
									freqFile);
						}
						positions.remove(i);
					}

					reader.close();

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

			if (output.endsWith("vcf.gz") || output.endsWith("vcf")) {
				VcfWriter writer = new VcfWriter();
				writer.createVCF(varFile, output, reference, "chrM", 16569, version + ";" + command);
			}

			if (writeFasta) {
				FastaWriter writer2 = new FastaWriter();
				writer2.createFasta(varFile, prefix + ".fasta", reference);
			}

			System.out.println("Time: " + (System.currentTimeMillis() - start) / 1000 + " sec");
			return 0;

		} else {
			System.out.println("No files found.");
			return 0;
		}
	}

	private void callVariant(LineWriter writerRaw, LineWriter writerVar, String id, double level, int pos,
			BasePosition basePosition, String reference, HashMap<String, Double> freqFile) throws IOException {

		basePosition.setId(id);

		basePosition.setPos(pos);

		VariantLine line = new VariantLine();

		char ref = reference.charAt(pos - 1);

		line.setRef(ref);

		line.parseLine(basePosition, level, freqFile);

		boolean isHeteroplasmy = false;

		for (char base : line.getMinors()) {

			// this only works since minorFWD and minorREV are equal
			double minorPercentageFwd = VariantCaller.getMinorPercentageFwd(line, base);

			double minorPercentageRev = VariantCaller.getMinorPercentageRev(line, base);

			double llrFwd = VariantCaller.determineLlrFwd(line, base);

			double llrRev = VariantCaller.determineLlrRev(line, base);

			VariantResult varResult = VariantCaller.determineLowLevelVariant(line, minorPercentageFwd,
					minorPercentageRev, llrFwd, llrRev, level, base);

			if (varResult.getType() == VariantCaller.LOW_LEVEL_VARIANT) {

				isHeteroplasmy = true;

				// set correct minor base for output result!
				varResult.setMinor(base);

				double hetLevel = VariantCaller.calcVariantLevel(line, minorPercentageFwd, minorPercentageRev);
				double levelTop = VariantCaller.calcLevel(line, line.getTopBasePercentsFWD(),
						line.getTopBasePercentsREV());

				double levelMinor = VariantCaller.calcLevel(line, minorPercentageFwd, minorPercentageRev);

				varResult.setLevelTop(levelTop);

				varResult.setLevelMinor(levelMinor);

				varResult.setLevel(hetLevel);

				String res = VariantCaller.writeVariant(varResult);

				writerVar.write(res);
			}
		}

		if (!isHeteroplasmy) {

			VariantResult varResult = VariantCaller.determineVariants(line);

			if (varResult != null) {

				String res = VariantCaller.writeVariant(varResult);

				writerVar.write(res);
			}
		}
		// raw data
		if (writerRaw != null) {
			String raw = line.toRawString();
			writerRaw.write(raw);
		}

	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public double getLevel() {
		return level;
	}

	public void setLevel(double level) {
		this.level = level;
	}

	public int getBaseQ() {
		return baseQ;
	}

	public void setBaseQ(int baseQ) {
		this.baseQ = baseQ;
	}

	public double getMapQ() {
		return mapQ;
	}

	public void setMapQ(int mapQ) {
		this.mapQ = mapQ;
	}

	public double getAlignQ() {
		return alignQ;
	}

	public void setAlignQ(int alignQ) {
		this.alignQ = alignQ;
	}

	public boolean isBaq() {
		return baq;
	}

	public void setBaq(boolean baq) {
		this.baq = baq;
	}

	public boolean isFreq() {
		return freq;
	}

	public void setFreq(boolean freq) {
		this.freq = freq;
	}

	public boolean isWriteFasta() {
		return writeFasta;
	}

	public void setWriteFasta(boolean writeFasta) {
		this.writeFasta = writeFasta;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
