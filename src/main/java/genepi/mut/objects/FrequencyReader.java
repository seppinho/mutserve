package genepi.mut.objects;

import java.io.IOException;
import java.util.HashMap;

import genepi.io.text.LineReader;

public class FrequencyReader {

	private String file;

	public FrequencyReader(String file) {
		this.file = file;
	}

	public HashMap<String, Double> parse() {

		HashMap<String, Double> frequencies = new HashMap<String, Double>();

		try {

			LineReader reader;
			reader = new LineReader(file);
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

		return frequencies;

	}
}
