package genepi.mut.commands;

import java.io.File;
import java.util.concurrent.Callable;
import genepi.mut.tasks.ReportTask;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "report", description = "Annotate your mutserve variant file.")
public class ReportCommand implements Callable<Integer> {

	@Option(names = { "--input" }, description = "Input", required = true)
	private String input;

	@Option(names = { "--output" }, description = "Output", required = true)
	private String output;

	@Override
	public Integer call() throws Exception {
		
		File inputFile = new File(input);
		if (!inputFile.exists()) {
			System.out.println("Input file '" + inputFile.getAbsolutePath() + "' not found.");
			return 1;
		}
		
		ReportTask task = new ReportTask();
		task.setInput(input);
		task.setOutput(output);
		task.createReport();
		return 0;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public void setOutput(String output) {
		this.output = output;
	}

}
