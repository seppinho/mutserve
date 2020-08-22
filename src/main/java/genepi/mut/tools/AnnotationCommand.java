package genepi.mut.tools;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import lukfor.tables.Table;
import lukfor.tables.columns.IBuildValueFunction;
import lukfor.tables.columns.types.StringColumn;
import lukfor.tables.io.TableBuilder;
import lukfor.tables.io.TableWriter;
import lukfor.tables.rows.Row;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "annotate", description = "Annotate your mutserve variant file.")
public class AnnotationCommand implements Callable<Integer> {

	@Option(names = { "--input" }, description = "Input", required = true)
	private String input;

	@Option(names = { "--output" }, description = "Output", required = true)
	private String output;

	@Option(names = { "--annotation" }, description = "Annotation File", required = true)
	private String annotation;

	@Override
	public Integer call() throws Exception {

		File inputFile = new File(input);
		if (!inputFile.exists()) {
			System.out.println("Input file '" + inputFile.getAbsolutePath() + "' not found.");
			return 1;
		}

		File annotationFile = new File(annotation);
		if (!annotationFile.exists()) {
			System.out.println("Annotation file '" + annotationFile.getAbsolutePath() + "' not found.");
			return 1;
		}

		Table inputTable = TableBuilder.fromCsvFile(input).withColumnTypeDetection(false).withSeparator('\t').load();

		if (inputTable.getColumn("Pos") == null) {
			System.out.println("Missing column 'Pos' in input file '" + inputFile.getAbsolutePath() + "'.");
			return 1;
		}

		if (inputTable.getColumn("Variant") == null) {
			System.out.println("Missing column 'Variant' in input file '" + inputFile.getAbsolutePath() + "'.");
			return 1;
		}

		inputTable.getColumns().append(new StringColumn("Mutation"), new IBuildValueFunction() {
			public String buildValue(Row row) throws IOException {
				return row.getString("Pos") + row.getString("Variant");
			}
		});

		Table annotationTable = TableBuilder.fromCsvFile(annotation).withColumnTypeDetection(false).withSeparator('\t')
				.load();

		if (annotationTable.getColumn("Mutation") == null) {
			System.out
					.println("Missing column 'Mutation' in annotation file '" + annotationFile.getAbsolutePath() + "'.");
			return 1;
		}

		inputTable.merge(annotationTable, "Mutation");

		TableWriter.writeToCsv(inputTable, output, '\t');

		return 0;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public void setOutput(String output) {
		this.output = output;
	}

}
