package genepi.mut.stats;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import genepi.io.table.reader.CsvTableReader;
import genepi.io.table.writer.CsvTableWriter;

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
		CsvTableWriter writer = new CsvTableWriter(output, ',', true);
		writer.setColumns(new String[] { "SampleID", "Mean-Coverage", "SD-Coverage", "Min-Coverage", "Max-Coverage" });

		String sample = null;
		ArrayList<Double> positions = new ArrayList<Double>();

		while (reader.next()) {

			if (!reader.getString(0).equals(sample) && sample != null) {

				DescriptiveStatistics statistics = calcStats(positions);
				writer.setString(0, sample);
				writer.setDouble(1, statistics.getMean());
				writer.setDouble(2, statistics.getStandardDeviation());
				writer.setDouble(3, statistics.getMin());
				writer.setDouble(4, statistics.getMax());
				writer.next();

				positions = new ArrayList<Double>();
			}

			double posCov = Double.valueOf(reader.getRow()[reader.getColumnIndex("Coverage-FWD")])
					+ Double.valueOf(reader.getRow()[reader.getColumnIndex("Coverage-REV")]);

			positions.add(posCov);

			sample = reader.getString(0);

		}

		// last sample
		DescriptiveStatistics statistics = calcStats(positions);
		writer.setString(0, sample);
		writer.setDouble(1, statistics.getMean());
		writer.setDouble(2, statistics.getStandardDeviation());
		writer.setDouble(3, statistics.getMin());
		writer.setDouble(4, statistics.getMax());
		writer.next();

		reader.close();
		writer.close();

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
