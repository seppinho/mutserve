package genepi.mut.tools;

import java.text.DecimalFormat;
import java.text.NumberFormat;
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

	}

	@Override
	public void init() {
		System.out.println("Precision Calculator");
	}

	@Override
	public int run() {

		Set<Integer> allPos = new TreeSet<Integer>();
		Set<Integer> sangerPos = new TreeSet<Integer>();
		Set<Integer> falsePositives = new TreeSet<Integer>();
		Set<Integer> both = new TreeSet<Integer>();

		String in = (String) getValue("in");

		String gold = (String) getValue("gold");

		CsvTableReader idReader = new CsvTableReader(in, '\t');

		Set<String> ids = new TreeSet<String>();

		while (idReader.next()) {

			ids.add(idReader.getString("ID"));

		}
		idReader.close();

		for (String id : ids) {

			CsvTableReader goldReader = new CsvTableReader(gold, '\t');

			while (goldReader.next()) {

				sangerPos.add(goldReader.getInteger("POS"));

				allPos.add(goldReader.getInteger("POS"));

			}

			CsvTableReader variantReader = new CsvTableReader(in, '\t');

			falsePositives.clear();

			both.clear();

			int truePositiveCount = 0;
			int falsePositiveCount = 0;
			int trueNegativeCount = 0;
			int falseNegativeCount = 0;
			int foundBySanger = 0;

			while (variantReader.next()) {

				String idSample = variantReader.getString("ID");

				int posSample = variantReader.getInteger("POS");

				if (id.equals(idSample)) {

					int position = posSample;

					if (sangerPos.contains(position)) {

						sangerPos.remove(position);
						truePositiveCount++;
						both.add(position);

					} else {

						falsePositives.add(position);
						falsePositiveCount++;

					}

				}
			}

			for (int j = 1; j <= 5104; j++) {

				if (!falsePositives.contains(j) && !both.contains(j)) {

					if (!allPos.contains(j)) {

						trueNegativeCount++;

					}

					else {
						falseNegativeCount++;
					}
				}

			}

			variantReader.close();

			foundBySanger = sangerPos.size();

			//System.out.println("  ID: " + id);

			//System.out.println("  Correct hits : " + truePositiveCount + "/" + gold);

			//System.out.println("    " + both);

			//System.out.println("  Not correctly found: " + foundBySanger);

			//System.out.println("    " + sangerPos);

			//System.out.println("  Found additionally: " + falsePositiveCount);

			//System.out.println("    " + falsePositives);

			double sens2 = truePositiveCount / (double) (truePositiveCount + falseNegativeCount) * 100;
			double spec2 = trueNegativeCount / (double) (falsePositiveCount + trueNegativeCount) * 100;
			double prec2 = truePositiveCount / (double) (truePositiveCount + falsePositiveCount) * 100;

			df.setMinimumFractionDigits(2);
			df.setMaximumFractionDigits(3);

			String sens = df.format(sens2);
			String spec = df.format(spec2);
			String prec = df.format(prec2);

			//System.out.println("Precision\t" + prec);
			//System.out.println("Sensitivity\t" + sens);
			//System.out.println("Specificity\t" + spec);
			
			System.out.println(id+"\t"+prec+"\t"+sens+"\t"+spec);

		}
		return 0;
	}

	public static void main(String[] args) {

		String goldPlasmid12 = "test-data/dna/plasmids/plasmid12/gold/plasmid12_major.txt";

		String goldPlasmid13 = "test-data/dna/plasmids/plasmid13/gold/plasmid13_minor.txt";
		
		String goldPlasmid12Del = "test-data/dna/plasmids/plasmid12/gold/plasmid12_major_del.txt";
		
		String goldPlasmid13Del = "test-data/dna/plasmids/plasmid13/gold/plasmid13_minor_del.txt";

		System.out.println("PLASMID 12");
		System.out.println(" ");

		CalcPrecision mutserver = new CalcPrecision(new String[] { "--gold", goldPlasmid12, "--in",
				"test-data/dna/plasmids/plasmid12/results/plasmid12-mutserver.txt" });

		mutserver.start();

		System.out.println("");
		System.out.println("FREEBAYES");

		CalcPrecision freebayes = new CalcPrecision(new String[] { "--gold", goldPlasmid12, "--in",
				"test-data/dna/plasmids/plasmid12/results/plasmid12-freebayes.txt" });

		freebayes.start();

		System.out.println("");
		System.out.println("GATK");

		CalcPrecision gatk = new CalcPrecision(
				new String[] { "--gold", goldPlasmid12, "--in", "test-data/dna/plasmids/plasmid12/results/plasmid12-gatk.txt" });

		gatk.start();

		System.out.println("");
		System.out.println("LOFREQ");

		CalcPrecision lofreq = new CalcPrecision(new String[] { "--gold", goldPlasmid12, "--in",
				"test-data/dna/plasmids/plasmid12/results/plasmid12-lofreq.txt" });

		lofreq.start();

		System.out.println("");
		System.out.println("");
		System.out.println("PLASMID 13");

		System.out.println("");
		System.out.println("MUTATION SERVER");

		mutserver = new CalcPrecision(new String[] { "--gold", goldPlasmid13, "--in",
				"test-data/dna/plasmids/plasmid13/results/plasmid13-mutserver.txt" });

		mutserver.start();

		System.out.println("");
		System.out.println("FREEBAYES");

		freebayes = new CalcPrecision(new String[] { "--gold", goldPlasmid13, "--in",
				"test-data/dna/plasmids/plasmid13/results/plasmid13-freebayes.txt" });

		freebayes.start();

		System.out.println("");
		System.out.println("GATK");

		gatk = new CalcPrecision(
				new String[] { "--gold", goldPlasmid13, "--in", "test-data/dna/plasmids/plasmid13/results/plasmid13-gatk.txt" });

		gatk.start();

		System.out.println("");
		System.out.println("LOFREQ");

		lofreq = new CalcPrecision(new String[] { "--gold", goldPlasmid13, "--in",
				"test-data/dna/plasmids/plasmid13/results/plasmid13-lofreq.txt" });

		lofreq.start();
		
		System.out.println("");
		System.out.println("PLASMID 12 INDEL");

		System.out.println("");
		System.out.println("MUTATION SERVER");

		mutserver = new CalcPrecision(new String[] { "--gold", goldPlasmid12Del, "--in",
				"test-data/dna/plasmids/plasmid12/results/plasmid12-mutserver-del.txt" });

		mutserver.start();
		
		System.out.println("");
		System.out.println("PLASMID 13 INDEL");

		System.out.println("");
		System.out.println("MUTATION SERVER");

		mutserver = new CalcPrecision(new String[] { "--gold", goldPlasmid13Del, "--in",
				"test-data/dna/plasmids/plasmid13/results/plasmid13-mutserver-del.txt" });

		mutserver.start();
		
		System.out.println("");
		System.out.println("*****************************************************");


		mutserver = new CalcPrecision(new String[] { "--gold", "test-data/dna/plasmids/mixtures/gold/gold.txt", "--in",
				"test-data/dna/plasmids/mixtures/results/variants_plasmid_5104_NOBAQ.txt" });
		
		 mutserver.start();
		 System.out.println("*****************************************************");
		
		mutserver = new CalcPrecision(new String[] { "--gold", "test-data/dna/plasmids/mixtures/gold/gold.txt", "--in",
		"test-data/dna/plasmids/mixtures/results/variants_plasmid_5104_BAQ.txt" });
		
	   mutserver.start();
	 

	}

}
