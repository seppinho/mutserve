package genepi.mut.steps.report;

import org.junit.Test;

import genepi.io.FileUtil;
import genepi.mut.tasks.ReportTask;

public class ReportGeneratorTest {

	@Test
	public void testGenerate() throws Exception {

		String input = "test-data/mtdna/report/1000g.txt";
		String output = "test-data/mtdna/report/out.html";

		ReportTask task = new ReportTask();
		task.setInput(input);
		task.setOutput(output);
		task.createReport();

		FileUtil.deleteFile(output);

	}
}
