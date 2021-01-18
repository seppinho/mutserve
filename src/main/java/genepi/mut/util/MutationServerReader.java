package genepi.mut.util;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

import genepi.io.table.reader.CsvTableReader;
import genepi.mut.objects.Sample;
import genepi.mut.objects.Variant;
import genepi.mut.pileup.VariantCaller.Filter;

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
			Filter filter = Filter.valueOf(reader.getString("Filter"));

			if (tmp != null && !id.equals(tmp)) {
				samples.put(sample.getId(), sample);
				sample = new Sample();
			}

			int type = reader.getInteger("Type");

			Variant variant = new Variant();

			double majorLevel = 0;
			double minorLevel = 0;
			int coverage = -1;
			int pos;
 
			if (!reader.getString("Pos").contains(".")) {
				pos = reader.getInteger("Pos");
			} else {
				pos = Integer.valueOf(reader.getString("Pos").split("\\.")[0]);
				variant.setInsertion(reader.getString("Pos"));
			}

			char ref = reader.getString("Ref").charAt(0);
			char var = reader.getString("Variant").charAt(0);
			double level = reader.getDouble("VariantLevel");
			char major = reader.getString("MajorBase").charAt(0);
			char minor = reader.getString("MinorBase").charAt(0);

			if (reader.hasColumn("MajorLevel") && type != 1) {
				majorLevel = Double.valueOf(reader.getString("MajorLevel"));
			}

			if (reader.hasColumn("MinorLevel")  && type != 1) {
				minorLevel = Double.valueOf(reader.getString("MinorLevel"));
			}

			if (reader.hasColumn("Coverage")) {
				coverage = reader.getInteger("Coverage");
			}

			sample.setId(id);

			if (type == 2 && minorLevel < requiredHetLevel) {
				continue;
			}

			variant.setPos(pos);
			variant.setFilter(filter);
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

		samples.put(sample.getId(), sample);

		return samples;
	}

}
