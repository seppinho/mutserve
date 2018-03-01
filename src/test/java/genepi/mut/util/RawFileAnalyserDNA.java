package genepi.mut.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import genepi.io.table.reader.CsvTableReader;
import genepi.mut.objects.VariantLine;

public class RawFileAnalyserDNA {

	public ArrayList<QCMetric> calculateLowLevelForTest(String in, String refpath, String sangerpos, double hetLevel) {

		ArrayList<QCMetric> metrics = new ArrayList<QCMetric>();

		NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
		DecimalFormat df = (DecimalFormat) nf;
		df.setMinimumFractionDigits(2);
		df.setMaximumFractionDigits(3);
		Set<Integer> allPos = new TreeSet<Integer>();
		Set<Integer> sangerPos = new TreeSet<Integer>();
		Set<Integer> falsePositives = new TreeSet<Integer>();
		Set<Integer> both = new TreeSet<Integer>();
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

					int position = line.getPosition();

					VariantCaller.determineLowLevelVariant(line, line.getMinorBasePercentsFWD(),
							line.getMinorBasePercentsREV(), line.getLlrFWD(), line.getLlrREV(), hetLevel);

					VariantCaller.determineVariants(line);

					if (VariantCaller.isFinalVariant(line)) {

						hetero.add(cloudgeneReader.getDouble("LEVEL"));

						if (sangerPos.contains(position)) {

							sangerPos.remove(position);
							truePositiveCount++;
							both.add(position);

						} else {

							falsePositives.add(position);
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

			cloudgeneReader.close();

			foundBySanger = sangerPos.size();

			System.out.println("  ID: " + id);

			System.out.println("  Correct hits : " + truePositiveCount + "/" + gold);

			System.out.println("    " + both);

			System.out.println("  Not correctly found: " + foundBySanger);

			System.out.println("    " + sangerPos);

			System.out.println("  Found additionally with Mutation Server: " + falsePositiveCount);

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

}
