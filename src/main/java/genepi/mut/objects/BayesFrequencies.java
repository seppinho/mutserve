package genepi.mut.objects;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;

import genepi.io.text.LineReader;

public class BayesFrequencies {

	static HashMap<String, Double> frequencies;


	public static HashMap<String, Double> instance(DataInputStream stream) {

		if (frequencies == null) {
			
			frequencies = new HashMap<String, Double>();
			
			try {

				LineReader reader;

				reader = new LineReader(stream);
				
				reader.next();

				while (reader.next()) {

					String[] splits = reader.get().split("\t");

					String pos = splits[1];

					int amountAlleles = Integer.valueOf(splits[2]);

					for (int i = 4; i < 4 + amountAlleles; i++) {

						String allele = splits[i].split(":")[0];

						double frequency = Double.valueOf(splits[i].split(":")[1]);

						frequencies.put(pos + allele, frequency);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
		
		return frequencies;
	
		
	}
}
