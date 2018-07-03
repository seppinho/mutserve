package genepi.mut.annotate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import genepi.base.Tool;
import genepi.io.table.reader.CsvTableReader;
import genepi.io.table.writer.CsvTableWriter;

public class AnnotateTool extends Tool {

	public AnnotateTool(String[] args) {
		super(args);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createParameters() {
		addParameter("input", "input variant file");
		addParameter("annotation", "input annotation file");
		addParameter("output", "output variant file");
		
		
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
		
		Map<String, String> annoMap = loadFile(annotation,"value","Pos","TypeB");
		
		CsvTableReader reader = new CsvTableReader(input, '\t');

		CsvTableWriter writer = new CsvTableWriter(output, '\t');

		String[] readerCols = reader.getColumns();

		String[] writerCols = Arrays.copyOf(readerCols, readerCols.length + 1);
		writerCols[readerCols.length] = "TypeB";

		writer.setColumns(writerCols);

		while (reader.next()) {
			for (String col : readerCols) {
				writer.setString(col, reader.getString(col));
			}
			writer.setString("TypeB", annoMap.get(reader.getString("Pos")+reader.getString("Variant")));
			writer.next();
		}
		
		reader.close();
		writer.close();
		
		return 0;
	}
	
	public static Map<String, String> loadFile(String filename, String value, String... key) {
		HashMap<String, String> annoMap = new HashMap<String, String>();
		CsvTableReader reader = new CsvTableReader(filename, '\t');
		while (reader.next()) {
			
			StringBuilder keyBuilder = new StringBuilder();
			for(String key1: key) {
				keyBuilder.append(reader.getString(key1));
				
			}
			annoMap.put(keyBuilder.toString(), reader.getString(value));
		
		}
		reader.close();

		return annoMap;
	}
	
	/*public static void main(String[] args) {

		AnnotateTool annoTool = new AnnotateTool(args);

		annoTool = new AnnotateTool(new String[] { "--input",
		 "test-data/tmp/variantsLocal1000G", "--annotation",
		 "2018-03 True Type B_SS.csv", "--output", "test-data/tmp/variantsLocal1000G_annotate.txt"});

		annoTool.start();

	}*/
	

}
