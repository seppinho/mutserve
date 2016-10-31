package genepi.vcbox.stats;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import genepi.io.table.reader.CsvTableReader;
import genepi.io.text.LineWriter;

public class CreateStatistics {

	private String input;

	private String output;

	public CreateStatistics(String input, String output) {

		this.input = input;
		this.output = output;

	}

	public boolean createSampleStatistics() {

		File file = new File(input);

		CsvTableReader reader = new CsvTableReader(file.getAbsolutePath(), '\t');
		LineWriter writer;
		try {
			writer = new LineWriter(output);
			String sample = null;
			ArrayList<Double> positions = new ArrayList<Double>();
			
			while (reader.next()) {

				if (!reader.getString(0).equals(sample) && sample != null) {
					DescriptiveStatistics statistics = calcStats(positions);
					writer.write(sample + "\t" + statistics.getMean() + "\t" + statistics.getStandardDeviation() + "\t"
							+ statistics.getMin() + "\t" + statistics.getMax() + "\n");
					positions = new ArrayList<Double>();
				}

				double coverage = Double.valueOf(reader.getRow()[reader.getColumnIndex("Coverage-FWD")])
						+ Double.valueOf(reader.getRow()[reader.getColumnIndex("Coverage-REV")]);
				positions.add(coverage);
				sample = reader.getString(0);

			}
			//last sample
			DescriptiveStatistics statistics = calcStats(positions);
			writer.write(sample + "\t" + statistics.getMean() + "\t" + statistics.getStandardDeviation() + "\t"
					+ statistics.getMin() + "\t" + statistics.getMax() + "\n");

			reader.close();
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;

	}

	private DescriptiveStatistics calcStats(ArrayList<Double> positions) {
		Double[] statArray = new Double[positions.size()];
		statArray = positions.toArray(statArray);
		DescriptiveStatistics statistics = new DescriptiveStatistics(ArrayUtils.toPrimitive(statArray));
		return statistics;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}
}
