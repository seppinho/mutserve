package genepi.mut.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import genepi.base.Tool;
import genepi.io.table.reader.CsvTableReader;

public class GenerateGoldStandard extends Tool {

	NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
	DecimalFormat df = (DecimalFormat) nf;

	public GenerateGoldStandard(String[] args) {
		super(args);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createParameters() {

		addParameter("file1", "major component file");
		addParameter("file2", "minor component file");
		addParameter("level", "mixture-level", DOUBLE);
		addParameter("output", "output file for performance step");

	}

	@Override
	public void init() {
		System.out.println("Generate Gold-Standards for Mixture-Model");
	}

	@Override
	public int run() {

		final String pos = "Pos";
		final String sampleId = "ID";
		final String variantlevel = "VariantLevel";

	    DecimalFormat df3 = new DecimalFormat("#.####");

		
		String inputMajor = (String) getValue("file1");
		String inputMinor = (String) getValue("file2");
		double level = (double) getValue("level");
		String outputGoldfile = (String) getValue("output");
		
		CsvTableReader variantMajor = new CsvTableReader(inputMajor, '\t');
		TreeMap<Integer, Double> goldMap = new TreeMap<>();

		// read major variants file and save all positions with level in goldMap
		while (variantMajor.next()) {
			int posSample = variantMajor.getInteger(pos);
			double variantlevSample = variantMajor.getDouble(variantlevel);
			goldMap.put(posSample, variantlevSample * (1 - level));
		}
		// read minor variants - if one is found in goldMap - with level (1-level) > 95%
		// and level in minor ~1
		CsvTableReader variantMinor = new CsvTableReader(inputMinor, '\t');
		
		while (variantMinor.next()) {
			int posSample = variantMinor.getInteger(pos);
			double variantlevSample = variantMinor.getDouble(variantlevel);

			if (goldMap.containsKey(posSample)) {
				goldMap.put(posSample, (goldMap.get(posSample) + variantlevSample * (level)));
				
			} else {
				goldMap.put(posSample, variantlevSample * (level));
			}
		}
		
		
		try {
			FileWriter writer = new FileWriter(new File(outputGoldfile));
			StringBuilder build = new StringBuilder();
			build.append(pos +"\t"+ variantlevel+"\n");
			Iterator it = goldMap.entrySet().iterator();
			while (it.hasNext()) {
				HashMap.Entry pair = (HashMap.Entry) it.next();
				int hpos = (int) pair.getKey();
				//if (!(hpos >300  && hpos < 315) && !(hpos >3105  && hpos <3111))
				build.append(pair.getKey() + "\t" + df3.format(pair.getValue())+"\n");
				it.remove(); // avoids a ConcurrentModificationException
			}
			writer.write(build.toString());
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static void main(String[] args) {

		String file1 = "test-data/mtdna/mixtures/HG01500_HG00183/HG01500.txt";
		String file2 = "test-data/mtdna/mixtures/HG01500_HG00183/HG00183.txt";
		String gold = "test-data/mtdna/mixtures/HG01500_HG00183/out/HG01500_HG00183.gold.txt";
	    String fileMixture50 = "test-data/mtdna/mixtures/HG01500_HG00183/HG01500_HG00183.txt";

		String length = "16569";
		String mixtureLevel ="0.5";
		String detectionLevel="0.01";
		GenerateGoldStandard gs = new GenerateGoldStandard(new String[] { "--file1", file1, "--file2", file2, "--output", gold, "--level", mixtureLevel });
		gs.start();
		
		CalcPrecision precison = new CalcPrecision(new String[] { "--gold", gold, "--in", fileMixture50, "--length", length, "--level",  detectionLevel});
		precison.start();
	}

}
