package genepi.mut.util;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import genepi.base.Tool;
import genepi.io.table.reader.CsvTableReader;
import genepi.io.table.writer.CsvTableWriter;

public class CalcPrecision extends Tool {

	NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
	DecimalFormat df = (DecimalFormat) nf;

	public CalcPrecision(String[] args) {
		super(args);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createParameters() {

		addParameter("gold", "expected positions");
		addParameter("in", "input csv file");
		addParameter("length", "length of genome/locus", INTEGER);
		addParameter("level", "heteroplasmy level applied", DOUBLE);

	}

	@Override
	public void init() {
		System.out.println("Precision Calculator");
	}

	@Override
	public int run() {

		final String pos = "Pos";
		final String sampleId = "ID";
		final String variantlevel = "VariantLevel";

		Set<Integer> allPos = new TreeSet<Integer>();
		Set<Integer> goldPos = new TreeSet<Integer>();
		Set<Integer> falsePositives = new TreeSet<Integer>();
		Set<Integer> falseNegatives = new TreeSet<Integer>();
		Set<Integer> both = new TreeSet<Integer>();

		String in = (String) getValue("in");

		String gold = (String) getValue("gold");
		int length = (int) getValue("length");
		double level = (double) getValue("level");

		CsvTableReader idReader = new CsvTableReader(in, '\t');

		FileWriter writePerformance=null;
		try {
			writePerformance = new FileWriter(in+"_perform.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Set<String> ids = new TreeSet<String>();

		while (idReader.next()) {

			ids.add(idReader.getString(sampleId));

		}
		idReader.close();

		System.out.println("SampleID\tFound\tTotal\tFalsePosN\tFalsePosSNPs\tFalseNeg\tFalseNegSNP\tPrecision\tSensitivity\tSpecificity\tIgnored\tIgnoredSNPs");
		try {
			writePerformance.write("SampleID\tFound\tTotal\tFalsePos\tFalsePosSNPs\tFalseNeg\tFalseNegSNP\tPrecision\tSensitivity\tSpecificity\tIgnored\tIgnoredSNPs\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (String id : ids) {

			CsvTableReader goldReader = new CsvTableReader(gold, '\t');

			while (goldReader.next()) {
				if (goldReader.getDouble(variantlevel)>=level) {
				goldPos.add(goldReader.getInteger(pos));

				allPos.add(goldReader.getInteger(pos));
				}

			}

			CsvTableReader variantReader = new CsvTableReader(in, '\t');

			falsePositives.clear();
			falseNegatives.clear();

			both.clear();
			int truePositiveCount = 0;
			int falsePositiveCount = 0;
			int trueNegativeCount = 0;
			int falseNegativeCount = 0;
			String level2low = "";
			int level2lowCount = 0;

			while (variantReader.next()) {

				String idSample = variantReader.getString(sampleId);

				int posSample = variantReader.getInteger(pos);

				double variantlevSample = variantReader.getDouble(variantlevel);

				if (id.equals(idSample)) {

					int position = posSample;

					if (variantlevSample >= level) {

						if (goldPos.contains(position)) {
							goldPos.remove(position);
							truePositiveCount++;
							both.add(position);

						} else {
							falsePositives.add(position);
							falsePositiveCount++;
						}
					} else {
						level2low += position + "(" + variantlevSample + ") ";
						level2lowCount++;
					}
				}

			}

			for (int j = 1; j <= length; j++) {

				if (!falsePositives.contains(j) && !both.contains(j)) {

					if (!allPos.contains(j)) {

						trueNegativeCount++;
					}

					else {
						falseNegativeCount++;
						falseNegatives.add(j);
					}
				}
			}

			variantReader.close();

			double sens2 = truePositiveCount / (double) (truePositiveCount + falseNegativeCount) * 100;
			double spec2 = trueNegativeCount / (double) (falsePositiveCount + trueNegativeCount) * 100;
			double prec2 = truePositiveCount / (double) (truePositiveCount + falsePositiveCount) * 100;

			df.setMinimumFractionDigits(2);
			df.setMaximumFractionDigits(3);

			String sens = df.format(sens2);
			String spec = df.format(spec2);
			String prec = df.format(prec2);

			System.out.println(id + "\t" + truePositiveCount + "\t " + (truePositiveCount + falseNegativeCount) + "\t"
					+ falsePositiveCount + "\t" + " " + "\t" + falseNegativeCount + "\t"
					+ " " + "\t" + prec + "\t" + sens + "\t" + spec + "\t" + level2lowCount + " ["
					+ " " + "]");
			try {
				writePerformance.write(id + "\t" + truePositiveCount + "\t " + (truePositiveCount + falseNegativeCount) + "\t"
						+ falsePositiveCount + "\t" + falsePositives.toString() + "\t" + falseNegativeCount + "\t"
						+ falseNegatives.toString() + "\t" + prec + "\t" + sens + "\t" + spec + "\t" + level2lowCount + "\t["
						+ level2low + "]\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		try {
			writePerformance.flush();
			writePerformance.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 0;
	}

	public static void main(String[] args) {

		String gold = "test-data/mtdna/raw-results/sanger.txt";
		String in = "test-data/tmp/file.txt";
		String length = "16569";
		CalcPrecision precison = new CalcPrecision(new String[] { "--gold", gold, "--in", in, "--length", length });

		precison.start();

	}

}
