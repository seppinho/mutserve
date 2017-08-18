package genepi.cnv.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math.MathException;

import genepi.io.table.reader.CsvTableReader;
import genepi.mut.detect.DetectVariants;
import genepi.mut.objects.PositionObject;
import genepi.mut.util.ReferenceUtil;

public class RawFileAnalyser {

	private static Set<Integer> privMutationsL02 = new HashSet<Integer>(Arrays.asList(15372, 16183));

	private static Set<Integer> privMutationsL11 = new HashSet<Integer>(Arrays.asList(7076, 9462, 11150, 15236, 16129));

	private static Set<Integer> hotspots = new HashSet<Integer>(
			Arrays.asList(302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 315, 316, 3105, 3106, 3107));

	ArrayList<QCMetric> metrics = new ArrayList<QCMetric>();
	
	public ArrayList<QCMetric> analyseFile(String in, String refpath, String sangerpos, double hetLevel) throws MathException {

		NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
		DecimalFormat df = (DecimalFormat) nf;
		df.setMinimumFractionDigits(2);
		df.setMaximumFractionDigits(3);
		DetectVariants detecter = new DetectVariants();
		detecter.setRefAsString(ReferenceUtil.readInReference(refpath));
		detecter.setDetectionLevel(hetLevel);
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

				PositionObject obj = parseRaw(cloudgeneReader);

				if (id.equals(obj.getId())) {

					if (!isHotspot(obj.getPosition())) {

						if (!isSampleMutation(obj.getPosition())) {

							List<PositionObject> uncoveredPosList = new ArrayList<PositionObject>();
							detecter.determineLowLevelVariants(obj, uncoveredPosList);

							int position = obj.getPosition();

							if (obj.getVariantType() == DetectVariants.LOW_LEVEL_VARIANT
									|| obj.getVariantType() == DetectVariants.SUSPICOUS_LOW_LEVEL_VARIANT) {

								System.out.println("----" + obj.getPosition());
								
								hetero.add(cloudgeneReader.getDouble("LEVEL"));

								if (sangerPos.contains(position)) {

									sangerPos.remove(position);
									truePositiveCount++;
									both.add(position + " (" + Math.abs(obj.getLlrFWD()) + ")");

								} else {

									falsePositives.add(position + " (" + Math.abs(obj.getLlrFWD()) + ")");
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

			cloudgeneReader.close();

			foundBySanger = sangerPos.size();

			/*System.out.println("  ID: " + id);

			System.out.println("  Correct hits : " + truePositiveCount + "/" + gold);

			System.out.println("    " + both);

			System.out.println("  Not correctly found: " + foundBySanger);

			System.out.println("    " + sangerPos);

			System.out.println("  Found additionally with Cloudgene: " + falsePositiveCount);

			System.out.println("    " + falsePositives);*/

			double sens2 = truePositiveCount / (double) (truePositiveCount + falseNegativeCount) * 100;
			double spec2 = trueNegativeCount / (double) (falsePositiveCount + trueNegativeCount) * 100;
			double prec2 = truePositiveCount / (double) (truePositiveCount + falsePositiveCount) * 100;
			
			
			String sens = df.format(sens2);
			String spec = df.format(spec2);
			String prec = df.format(prec2);

			/*System.out.println("  Sensitivity (Recall) -> " + sens + " values " + truePositiveCount + "/"
					+ (truePositiveCount + falseNegativeCount));
			System.out.println("  Specificity -> " + " values " + trueNegativeCount + "/"
					+ (falsePositiveCount + trueNegativeCount));
			System.out.println("  Precision -> " + " values " + truePositiveCount + "/"
					+ (truePositiveCount + falsePositiveCount));*/

			System.out.println(prec + " / " + sens + " / " + spec);
			
			QCMetric metric = new QCMetric();
			metric.setId(id);
			metric.setSensitivity(sens2);
			metric.setSpecificity(spec2);
			metric.setPrecision(prec2);
			
			metrics.add(metric);
		}
		return metrics;
	}

	public static PositionObject parseRaw(CsvTableReader cloudgeneReader) {
		PositionObject obj = new PositionObject();

		obj.setId(cloudgeneReader.getString("SAMPLE"));
		obj.setPosition(cloudgeneReader.getInteger("POS"));

		obj.setLlrFWD(cloudgeneReader.getDouble("LLRFWD"));
		obj.setLlrREV(cloudgeneReader.getDouble("LLRREV"));
		
		obj.setCovFWD(cloudgeneReader.getInteger("COV-FWD"));
		obj.setCovREV(cloudgeneReader.getInteger("COV-REV"));
		
		obj.setLlrAFWD(cloudgeneReader.getDouble("LLRAFWD"));
		obj.setLlrCFWD(cloudgeneReader.getDouble("LLRCFWD"));
		obj.setLlrGFWD(cloudgeneReader.getDouble("LLRGFWD"));
		obj.setLlrTFWD(cloudgeneReader.getDouble("LLRTFWD"));
		
		obj.setLlrAREV(cloudgeneReader.getDouble("LLRAREV"));
		obj.setLlrCREV(cloudgeneReader.getDouble("LLRCREV"));
		obj.setLlrGREV(cloudgeneReader.getDouble("LLRGREV"));
		obj.setLlrTREV(cloudgeneReader.getDouble("LLRTREV"));
		
		
		obj.setaPercentageFWD(cloudgeneReader.getDouble("%A"));
		obj.setcPercentageFWD(cloudgeneReader.getDouble("%C"));
		obj.setgPercentageFWD(cloudgeneReader.getDouble("%G"));
		obj.settPercentageFWD(cloudgeneReader.getDouble("%T"));

		obj.setaPercentageREV(cloudgeneReader.getDouble("%a"));
		obj.setcPercentageREV(cloudgeneReader.getDouble("%c"));
		obj.setgPercentageREV(cloudgeneReader.getDouble("%g"));
		obj.settPercentageREV(cloudgeneReader.getDouble("%t"));

		ArrayList<Double> allelesFWD = new ArrayList<Double>();
		allelesFWD.add(obj.getaPercentageFWD());
		allelesFWD.add(obj.getcPercentageFWD());
		allelesFWD.add(obj.getgPercentageFWD());
		allelesFWD.add(obj.gettPercentageFWD());

		Collections.sort(allelesFWD, Collections.reverseOrder());

		double topBasePercentsFWD = allelesFWD.get(0);
		double minorBasePercentsFWD = allelesFWD.get(1);

		obj.setTopBasePercentsFWD(topBasePercentsFWD);
		obj.setMinorBasePercentsFWD(minorBasePercentsFWD);

		ArrayList<Double> allelesREV = new ArrayList<Double>();
		allelesREV.add(obj.getaPercentageREV());
		allelesREV.add(obj.getcPercentageREV());
		allelesREV.add(obj.getgPercentageREV());
		allelesREV.add(obj.gettPercentageREV());

		Collections.sort(allelesREV, Collections.reverseOrder());
		double topBasePercentsREV = allelesREV.get(0);
		double minorBasePercentsREV = allelesREV.get(1);

		obj.setTopBasePercentsREV(topBasePercentsREV);
		obj.setMinorBasePercentsREV(minorBasePercentsREV);

		obj.setTopBaseFWD(cloudgeneReader.getString("TOP-FWD").charAt(0));
		obj.setTopBaseREV(cloudgeneReader.getString("TOP-REV").charAt(0));
		obj.setMinorBaseFWD(cloudgeneReader.getString("MINOR-FWD").charAt(0));
		obj.setMinorBaseREV(cloudgeneReader.getString("MINOR-REV").charAt(0));

		return obj;
	}

	public static boolean isSampleMutation(int pos) {
		return privMutationsL02.contains(pos) || privMutationsL11.contains(pos);
	}

	public static boolean isHotspot(int pos) {
		return hotspots.contains(pos);
	}

}
