/*package genepi.mut.util;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import genepi.io.table.reader.CsvTableReader;
import genepi.io.text.LineReader;
import genepi.mut.objects.VariantLine;
import genepi.mut.objects.VariantLineUtil;

public class MultiallelicAnalyser {

	ArrayList<QCMetric> metrics = new ArrayList<QCMetric>();

	public ArrayList<QCMetric> analyseFile(String in, String expected, String refpath, double hetLevel) {

		NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
		DecimalFormat df = (DecimalFormat) nf;
		df.setMinimumFractionDigits(2);
		df.setMaximumFractionDigits(3);
		Set<String> allPos = new TreeSet<String>();
		Set<String> expectedPos = new TreeSet<String>();
		CsvTableReader idReader = new CsvTableReader(in, '\t');

		Set<String> ids = new TreeSet<String>();
		while (idReader.next()) {
			ids.add(idReader.getString("SAMPLE"));
		}
		idReader.close();

		for (String id : ids) {

			int truePositiveCount = 0;
			int falsePositiveCount = 0;
			int trueNegativeCount = 0;
			int falseNegativeCount = 0;

			CsvTableReader expectedReader = new CsvTableReader(expected, '\t');
			//DetectVariants detecter = new DetectVariants();
			//detecter.setRefAsString(ReferenceUtil.readInReference(refpath));
			//detecter.setDetectionLevel(hetLevel);

			while (expectedReader.next()) {
				int pos = expectedReader.getInteger("POS");
				String variant = expectedReader.getString("VAR");
				String[] splits = variant.split(",");
				//char ref = detecter.getRefAsString().charAt(pos - 1);
				for (String split : splits) {
			//		if (!split.equals(String.valueOf(ref))) {
						// TODO DELETIONS need to be included at the end
						if (!split.equals("D")) {
							expectedPos.add(pos + split);
							allPos.add(pos + split);
				//		}
					}
				}
			}
			expectedReader.close();

			CsvTableReader cloudgeneReader = new CsvTableReader(in, '\t');

			while (cloudgeneReader.next()) {

				VariantLine posObj = new VariantLine();
				posObj.parseLineFromFile(cloudgeneReader);

				// look at exome only
				if ((posObj.getPosition() >= 581 && posObj.getPosition() <= 740)
						|| (posObj.getPosition() >= 4744 && posObj.getPosition() <= 4925)) {

					//detecter.determineMultiAllelicSites(posObj);

					if (posObj.getVariantType() == VariantLineUtil.MULTI_ALLELIC) {

						String variant = posObj.getMultiAllelic();
						String[] splits = variant.split(",");

						if (id.equals(posObj.getId())) {

							for (String split : splits) {
								String currentPos = posObj.getPosition() + split;

								if (expectedPos.contains(currentPos)) {
									expectedPos.remove(currentPos);
									truePositiveCount++;
								} else {
									falsePositiveCount++;
								}

							}
						}
					} else {

						String currentPos = posObj.getPosition() + "" + posObj.getTopBaseFWD();

						if (!allPos.contains(currentPos)) {
							trueNegativeCount++;
						}

						else {
							falseNegativeCount++;
						}
					}

				}
			}
			System.out.println("total: " + allPos.size());
			System.out.println("missing: " + expectedPos.size());
			System.out.println("missing from expected: " + expectedPos.toString());
			
			//checkKIV(expectedPos);
			
			// this is needed since EACH line is only parsed once. it happens
			// that e.g 4778 has two alleles, but only first is checked!!
			// this was no problem for mtDNA since only variant for each pos has
			// to be found!

			falseNegativeCount += expectedPos.size();

			double prec = truePositiveCount / (double) (truePositiveCount + falsePositiveCount) * 100;
			double sens = truePositiveCount / (double) (truePositiveCount + falseNegativeCount) * 100;
			double spec = trueNegativeCount / (double) (falsePositiveCount + trueNegativeCount) * 100;

			QCMetric metric = new QCMetric();
			metric.setId(id);
			metric.setPrecision(prec);
			metric.setSensitivity(sens);
			metric.setSpecificity(spec);
			metrics.add(metric);
		}
		return metrics;
	}

	private void checkKIV(Set<String> missingPos) {
		HashMap<Integer, String> positions = new HashMap<Integer, String>();
		try {
			LineReader read = new LineReader("test-data/lpa/lpa-exome-kiv2-raw-results/2017-04_exonic_refs_allpos.txt");
			
			while (read.next()){
				String line = read.get();
				positions.put(Integer.valueOf(line.split("\t")[0]), line.split("\t")[1]);
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int[] myIntArray = new int[11];
		
		for(String miss : missingPos){
			int posMissing = Integer.valueOf(miss.substring(0, miss.length()-1));
			String variantMissing = String.valueOf(miss.charAt(miss.length()-1));
			String value = positions.get(posMissing);
			String[] splits = value.split(",");

			int stringPos = 0;
			for(String split:splits){
				
				if (split.equals(variantMissing)){
					myIntArray[stringPos]++;
				}
				stringPos++;	
				
			}
			
		}
		
		
		for (int a : myIntArray){
		System.out.println(a);
}
	}

}
*/