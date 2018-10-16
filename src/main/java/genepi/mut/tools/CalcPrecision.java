package genepi.mut.tools;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import genepi.base.Tool;
import genepi.io.table.reader.CsvTableReader;

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

	}

	@Override
	public void init() {
		System.out.println("Precision Calculator");
	}

	@Override
	public int run() {

		Set<Integer> allPos = new TreeSet<Integer>();
		Set<Integer> goldPos = new TreeSet<Integer>();
		Set<Integer> falsePositives = new TreeSet<Integer>();
		Set<Integer> both = new TreeSet<Integer>();

		String in = (String) getValue("in");
		
		String gold = (String) getValue("gold");
		int length= (int)getValue("length");

		CsvTableReader idReader = new CsvTableReader(in, '\t');

		Set<String> ids = new TreeSet<String>();

		while (idReader.next()) {

			ids.add(idReader.getString("ID"));

		}
		idReader.close();
		
		System.out.println("Sample\tfound\tfalse\tprec.\tsens.\tspec");
		
		for (String id : ids) {

			CsvTableReader goldReader = new CsvTableReader(gold, '\t');

			while (goldReader.next()) {

				goldPos.add(goldReader.getInteger("POS"));

				allPos.add(goldReader.getInteger("POS"));

			}

			CsvTableReader variantReader = new CsvTableReader(in, '\t');

			falsePositives.clear();

			both.clear();
			int goldpos=goldPos.size();
			int truePositiveCount = 0;
			int falsePositiveCount = 0;
			int trueNegativeCount = 0;
			int falseNegativeCount = 0;
			int foundInGold = 0;

			while (variantReader.next()) {

				String idSample = variantReader.getString("ID");

				int posSample = variantReader.getInteger("POS");

				if (id.equals(idSample)) {

					int position = posSample;
					
					if (goldPos.contains(position)) {
						System.out.println(position + "\t" + 1 + "\t" +1);
						goldPos.remove(position);
						truePositiveCount++;
						both.add(position);

					} else {
						System.out.println(position + "\t" + 0 + "\t" +1 );
						falsePositives.add(position);
						falsePositiveCount++;

					}

				}
			}

			for (int j = 1; j <= length; j++) {

				if (!falsePositives.contains(j) && !both.contains(j)) {

					if (!allPos.contains(j)) {

						trueNegativeCount++;
						System.out.println(j + "\t" + 0 + "\t" +0 );
					}

					else {
						falseNegativeCount++;
						
					}
				}

			}
		

			variantReader.close();
			
			 Iterator value = goldPos.iterator(); 
				while (value.hasNext()) {
					System.out.println(value.next() + "\t" + 1 + "\t" + 0 );
				}
			foundInGold = goldPos.size();
			
			

			double sens2 = truePositiveCount / (double) (truePositiveCount + falseNegativeCount) * 100;
			double spec2 = trueNegativeCount / (double) (falsePositiveCount + trueNegativeCount) * 100;
			double prec2 = truePositiveCount / (double) (truePositiveCount + falsePositiveCount) * 100;

			df.setMinimumFractionDigits(2);
			df.setMaximumFractionDigits(3);

			String sens = df.format(sens2);
			String spec = df.format(spec2);
			String prec = df.format(prec2);

			System.out.println(id+"\t"+(goldpos-foundInGold)+"/"+goldpos+"\t"+falsePositiveCount+"\t"+prec+"\t"+sens+"\t"+spec);

		}
		return 0;
	}

	public static void main(String[] args) {
		
		String gold ="/home/hansi/git/mutation-server/test-data/mtdna/mixtures/expected/sanger.txt";
		String in = "/home/hansi/git/mutation-server/test-data/mtdna/raw-results/pgm-variants.txt";
		String length = "16569";
		CalcPrecision precison = new CalcPrecision(new String[] { "--gold", gold, "--in", 	in, "--length", length });

		precison.start();

	}

}
