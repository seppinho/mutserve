package genepi.mut.tools;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import genepi.io.table.reader.CsvTableReader;
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
	String input;

	@Option(names = { "--output" }, description = "Output", required = true)
	String output;

	@Option(names = { "--annotation" }, description = "Annotation File", required = true)
	String annotation;

	@Override
	public Integer call() throws Exception {

		if (!new File(input).exists()) {

			System.out.println("Please specify input file and annotation file");
			return 1;
		}

		if (!new File(annotation).exists()) {

			System.out.println("Please specify annotation file");
			return 1;
		}

		Table inputTable;
		inputTable = TableBuilder.fromTableReader(input, new CsvTableReader(input, '\t'), false);
		inputTable.getColumns().append(new StringColumn("Mutation"), new IBuildValueFunction() {

			public String buildValue(Row row) throws IOException {
				return row.getString("Pos") + row.getString("Variant");
			}
		});

		Table annotationTable = TableBuilder.fromTableReader(annotation, new CsvTableReader(annotation, '\t'), false);
		inputTable.merge(annotationTable, "Mutation");

		TableWriter.writeToCsv(inputTable, output, '\t');

		return 0;
	}

}
