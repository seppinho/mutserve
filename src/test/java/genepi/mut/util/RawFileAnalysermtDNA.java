package genepi.mut.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import genepi.io.table.reader.CsvTableReader;
import genepi.mut.objects.VariantLine;
import genepi.mut.objects.VariantResult;

public class RawFileAnalysermtDNA {

	private static Set<Integer> privMutationsL02 = new HashSet<Integer>(Arrays.asList(15372, 16183));

	private static Set<Integer> privMutationsL11 = new HashSet<Integer>(Arrays.asList(7076, 9462, 11150, 15236, 16129));

	private static Set<Integer> hotspots = new HashSet<Integer>(
			Arrays.asList(302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 315, 316, 3105, 3106, 3107));

	private boolean callDel;

	public ArrayList<QCMetric> calculateLowLevelForTest(String in, String refpath, String sangerpos, double level) {

		ArrayList<QCMetric> metrics = new ArrayList<QCMetric>();

		NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
		DecimalFormat df = (DecimalFormat) nf;
		df.setMinimumFractionDigits(2);
		df.setMaximumFractionDigits(3);
		Set<Integer> allPos = new TreeSet<Integer>();
		Set<Integer> sangerPos = new TreeSet<Integer>();
		Set<String> falsePositives = new TreeSet<String>();
		Set<String> both = new TreeSet<String>();
		List<Double> hetero = new ArrayList<Double>();

		CsvTableReader idReader = new CsvTableReader(in, '\t');

		Set<String> ids = new TreeSet<String>();
		while (idReader.next()) {
			ids.add(idReader.getString("SAMPLE"));
		}
		idReader.close();

		for (String id : ids) {

			CsvTableReader goldReader = new CsvTableReader(sangerpos, '\t');
			while (goldReader.next()) {
				sangerPos.add(goldReader.getInteger("POS"));
				allPos.add(goldReader.getInteger("POS"));
			}
			CsvTableReader cloudgeneReader = new CsvTableReader(in, '\t');
			int gold = sangerPos.size();

			falsePositives.clear();
			both.clear();

			int truePositiveCount = 0;
			int falsePositiveCount = 0;
			int trueNegativeCount = 0;
			int falseNegativeCount = 0;
			int foundBySanger = 0;
			hetero.clear();

			while (cloudgeneReader.next()) {

				VariantLine line = new VariantLine();

				line.parseLineFromFile(cloudgeneReader);

				if (id.equals(line.getId())) {

					if (!isHotspot(line.getPosition())) {

						if (!isSampleMutation(line.getPosition())) {

							int position = line.getPosition();

							if (checkBases(line)) {

								double hetLevel = VariantCaller.calcLevel(line, line.getMinorBasePercentsFWD(),
										line.getMinorBasePercentsREV());

								VariantResult varResult = VariantCaller.determineLowLevelVariant(line,
										line.getMinorBasePercentsFWD(), line.getMinorBasePercentsREV(),
										line.getLlrFWD(), line.getLlrREV(), level);

								varResult.setLevel(hetLevel);

								if (varResult.getType() == VariantCaller.LOW_LEVEL_VARIANT
										|| (callDel && varResult.getType() == VariantCaller.LOW_LEVEL_DELETION)) {

									System.out.println("Lowlevel Variant: " + line.getPosition());

									hetero.add(cloudgeneReader.getDouble("LEVEL"));

									if (sangerPos.contains(position)) {

										sangerPos.remove(position);
										truePositiveCount++;
										both.add(position + " (" + Math.abs(line.getLlrFWD()) + ")");

									} else {

										falsePositives.add(position + " (" + Math.abs(line.getLlrFWD()) + ")");
										falsePositiveCount++;

									}

								}

								else {

									if (!allPos.contains(position)) {

										trueNegativeCount++;

									}

									else {
										falseNegativeCount++;
									}
								}

							}
						}
					}
				}
			}

			cloudgeneReader.close();

			foundBySanger = sangerPos.size();

			System.out.println("  ID: " + id);

			System.out.println("  Correct hits : " + truePositiveCount + "/" + gold);

			System.out.println("    " + both);

			System.out.println("  Not correctly found: " + foundBySanger);

			System.out.println("    " + sangerPos);

			System.out.println("  Found additionally with Cloudgene: " + falsePositiveCount);

			System.out.println("    " + falsePositives);

			double sens2 = truePositiveCount / (double) (truePositiveCount + falseNegativeCount) * 100;
			double spec2 = trueNegativeCount / (double) (falsePositiveCount + trueNegativeCount) * 100;
			double prec2 = truePositiveCount / (double) (truePositiveCount + falsePositiveCount) * 100;

			String sens = df.format(sens2);
			String spec = df.format(spec2);
			String prec = df.format(prec2);

			/*
			 * System.out.println("  Sensitivity (Recall) -> " + sens +
			 * " values " + truePositiveCount + "/" + (truePositiveCount +
			 * falseNegativeCount)); System.out.println("  Specificity -> " +
			 * " values " + trueNegativeCount + "/" + (falsePositiveCount +
			 * trueNegativeCount)); System.out.println("  Precision -> " +
			 * " values " + truePositiveCount + "/" + (truePositiveCount +
			 * falsePositiveCount));
			 */

			System.out.println("");
			System.out.println("Precision\t" + prec);
			System.out.println("Sensitivity\t" + sens);
			System.out.println("Specificity\t" + spec);

			QCMetric metric = new QCMetric();
			metric.setId(id);
			metric.setSensitivity(sens2);
			metric.setSpecificity(spec2);
			metric.setPrecision(prec2);

			metrics.add(metric);
		}
		return metrics;
	}

	public void analyseRaw(String in, String refpath, String sangerpos, double level) {

		NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
		DecimalFormat df = (DecimalFormat) nf;
		df.setMinimumFractionDigits(2);
		df.setMaximumFractionDigits(3);

		CsvTableReader cloudgeneReader = new CsvTableReader(in, '\t');

		while (cloudgeneReader.next()) {

			VariantLine line = new VariantLine();

			line.parseLineFromFile(cloudgeneReader);

			if (!isHotspot(line.getPosition())) {

				int position = line.getPosition();

				if (checkBases(line)) {

					double hetLevel = VariantCaller.calcLevel(line, line.getMinorBasePercentsFWD(),
							line.getMinorBasePercentsREV());

					VariantResult varResult = VariantCaller.determineLowLevelVariant(line,
							line.getMinorBasePercentsFWD(), line.getMinorBasePercentsREV(), line.getLlrFWD(),
							line.getLlrREV(), level);

					varResult.setLevel(hetLevel);

					if (varResult.getType() == VariantCaller.LOW_LEVEL_DELETION
							|| varResult.getType() == VariantCaller.LOW_LEVEL_VARIANT) {

						System.out.println("Low Level Variant: " + line.getPosition());

					}

				}

				double hetLevel = VariantCaller.calcLevel(line, line.getMinorBasePercentsFWD(),
						line.getMinorBasePercentsREV());

				System.out.println("pos " + position);

				System.out.println(hetLevel);

				System.out.println(1 - level);

				VariantResult varResult = VariantCaller.determineVariants(line);

				varResult.setLevel(hetLevel);

				if (varResult.getType() == VariantCaller.VARIANT) {

					System.out.println("Variant: " + line.getPosition());

				}

			}
		}

		cloudgeneReader.close();

	}

	public static boolean isSampleMutation(int pos) {
		return privMutationsL02.contains(pos) || privMutationsL11.contains(pos);
	}

	public static boolean isHotspot(int pos) {
		return hotspots.contains(pos);
	}

	public boolean isCallDel() {
		return callDel;
	}

	public void setCallDel(boolean callDel) {
		this.callDel = callDel;
	}

	private boolean checkBases(VariantLine line) {
		return (line.getMinorBaseFWD() == line.getMinorBaseREV() && line.getTopBaseFWD() == line.getTopBaseREV())
				|| ((line.getMinorBaseFWD() == line.getTopBaseREV() && line.getTopBaseFWD() == line.getMinorBaseREV()));
	}

}
