package genepi.mut.util;

import java.io.File;
import java.util.HashMap;

import genepi.io.table.reader.CsvTableReader;
import genepi.mut.objects.Sample;
import genepi.mut.objects.Variant;

public class MutationServerReader {

	private String file;

	public MutationServerReader(String file) {
		this.file = file;
	}

	public HashMap<String, Sample> parse() {
		return parse(0.00);
	}

	public HashMap<String, Sample> parse(double requiredHetLevel) {

		CsvTableReader reader = new CsvTableReader(new File(file).getAbsolutePath(), '\t');
		HashMap<String, Sample> samples = new HashMap<String, Sample>();

		String tmp = null;
		Sample sample = new Sample();

		while (reader.next()) {

			String id = reader.getString("ID");

			if (tmp != null && !id.equals(tmp)) {
				samples.put(sample.getId(), sample);
				sample = new Sample();
			}

			int type = reader.getInteger("Type");

			if (type == 1 || type == 2) {

				double majorLevel = 0;
				double minorLevel = 0;
				int coverage = -1;
				int pos = reader.getInteger("Pos");
				char ref = reader.getString("Ref").charAt(0);
				char var = reader.getString("Variant").charAt(0);
				double level = reader.getDouble("VariantLevel");
				char major = reader.getString("MajorBase").charAt(0);
				char minor = reader.getString("MinorBase").charAt(0);

				if (reader.hasColumn("MajorLevel")) {
					majorLevel = Double.valueOf(reader.getString("MajorLevel"));
				}

				if (reader.hasColumn("MinorLevel")) {
					minorLevel = Double.valueOf(reader.getString("MinorLevel"));
				}

				if (reader.hasColumn("Coverage")) {
					coverage = reader.getInteger("Coverage");
				}

				sample.setId(id);

				if (type == 2 && minorLevel < requiredHetLevel) {
					continue;
				}

				Variant variant = new Variant();
				variant.setPos(pos);
				variant.setRef(ref);
				variant.setVariantBase(var);
				variant.setLevel(level);
				variant.setMajor(major);
				variant.setMinor(minor);
				variant.setMajorLevel(majorLevel);
				variant.setMinorLevel(minorLevel);
				variant.setCoverage(coverage);
				variant.setType(type);

				sample.addVariant(variant);
				tmp = id;
			}
		}
		samples.put(sample.getId(), sample);

		return samples;
	}

}
