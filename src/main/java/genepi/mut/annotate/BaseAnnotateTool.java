package genepi.mut.annotate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import genepi.base.Tool;
import genepi.io.table.reader.CsvTableReader;
import genepi.io.table.writer.CsvTableWriter;

public class BaseAnnotateTool extends Tool {

	public BaseAnnotateTool(String[] args) {
		super(args);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createParameters() {
		addParameter("input", "input variant file");
		addParameter("annotation", "input annotation file");
		addParameter("output", "output variant file");
		addParameter("key", "key in mapping file");
		addParameter("value", "value in mapping file");
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
	}

	@Override
	public int run() {

		String input = (String) getValue("input");

		String annotation = (String) getValue("annotation");

		String output = (String) getValue("output");

		String key = (String) getValue("key");

		String value = (String) getValue("value");

		Map<String, String> annoMap = loadFile(annotation, key, value);

		CsvTableReader reader = new CsvTableReader(input, '\t');

		CsvTableWriter writer = new CsvTableWriter(output, '\t');

		String[] readerCols = reader.getColumns();

		String[] writerCols = Arrays.copyOf(readerCols, readerCols.length + 1);

		writerCols[readerCols.length] = value;

		writer.setColumns(writerCols);

		while (reader.next()) {
			for (String col : readerCols) {
				writer.setString(col, reader.getString(col));
			}

			writer.setString(value, annoMap.get(reader.getString("Pos")+reader.getString("Variant")));
			writer.next();
		}

		reader.close();
		writer.close();

		return 0;
	}

	public static Map<String, String> loadFile(String filename, String key, String value) {
		HashMap<String, String> annoMap = new HashMap<String, String>();
		CsvTableReader reader = new CsvTableReader(filename, '\t');
		while (reader.next()) {

			annoMap.put(reader.getString(key), reader.getString(value));

		}
		reader.close();

		return annoMap;
	}

	public static void main(String[] args) {

		BaseAnnotateTool annoTool = new BaseAnnotateTool(args);

		annoTool = new BaseAnnotateTool(new String[] { "--input", "test-data/tmp/variantsLocal1000G", "--annotation",
				"base_annotation.csv", "--output", "test-data/tmp/variantsLocal1000G_annotate.txt","--key", "Position", "--value", "TypeB"});

		annoTool.start();

	}

}
