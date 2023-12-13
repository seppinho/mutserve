package genepi.mut.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import genepi.mut.objects.StatisticsFile;

public class StatisticsFileUtil {

	public List<StatisticsFile> load(String input) {

		try {
			BufferedReader reader = new BufferedReader(new FileReader(input));
			String line;
			String currentSample = "";
			List<StatisticsFile> samples = new ArrayList<>();
			StatisticsFile file = null;
			
			//header
			reader.readLine();

			while ((line = reader.readLine()) != null) {

				String[] parts = line.split("\t");

				if (file == null) {
					file = new StatisticsFile();
				} else if (!parts[0].equals(currentSample)) {
					samples.add(file);
					file = new StatisticsFile();
				}

				currentSample = parts[0];
				file.setSampleName(currentSample);

				if (parts.length == 3) {

					String key = parts[1];
					String value = parts[2];
					switch (key) {
					case "Contig":
						file.setContig(value);
						break;
					case "NumberofReads":
						file.setNumberOfReads(Integer.parseInt(value));
						break;
					case "CoveredBases":
						file.setCoveredBases(Integer.parseInt(value));
						break;
					case "CoveragePercentage":
						file.setCoveredPercentage(Integer.parseInt(value));
						break;
					case "MeanDepth":
						file.setMeanDepth(Double.parseDouble(value));
						break;
					case "MeanBaseQuality":
						file.setMeanBaseQuality(Double.parseDouble(value));
						break;
					case "MeanMapQuality":
						file.setMeanMapQuality(Double.parseDouble(value));
						break;
					}
				}

			}
			samples.add(file);
			reader.close();
			return samples;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

}
