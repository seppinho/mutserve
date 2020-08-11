package genepi.mut.vc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;

import genepi.io.text.LineWriter;
import genepi.mut.pileup.BamAnalyser;
import lukfor.progress.tasks.ITaskRunnable;
import lukfor.progress.tasks.monitors.ITaskMonitor;

public class MergeTask implements ITaskRunnable {

	private File[] variantInputs;

	private File[] rawInputs;

	private String rawPath;

	private String variantPath;

	public void setInputs(List<VariantCallingTask> tasks) {
		this.variantInputs = new File[tasks.size()];
		this.rawInputs = new File[tasks.size()];

		for (int i = 0; i < variantInputs.length; i++) {
			VariantCallingTask task = tasks.get(i);
			this.variantInputs[i] = new File(task.getVarName());
			this.rawInputs[i] = new File(task.getRawName());
		}
	}

	@Override
	public void run(ITaskMonitor monitor) throws Exception {

		monitor.begin("Merge output files");

		assert (variantInputs != null);
		assert (variantInputs.length > 0);

		new File(variantPath).delete();
		new File(rawPath).delete();

		LineWriter writerVar = new LineWriter(variantPath);
		writerVar.write(BamAnalyser.headerVariants + "\n");
		writerVar.close();

		LineWriter writerRaw = new LineWriter(rawPath);
		writerRaw.write(BamAnalyser.headerRaw + "\n");
		writerRaw.close();

		appendFiles(new File(variantPath), variantInputs);
		if (rawInputs != null) {
			appendFiles(new File(rawPath), rawInputs);
		}

		monitor.done();
	}

	public static void appendFiles(File destination, File[] sources) throws IOException {
		OutputStream output = null;
		try {
			output = new BufferedOutputStream(new FileOutputStream(destination, true));
			for (File source : sources) {
				appendFile(output, source);
				source.delete();
			}
		} finally {
			IOUtils.closeQuietly(output);
		}
	}

	private static void appendFile(OutputStream output, File source) throws IOException {
		InputStream input = null;
		try {
			input = new BufferedInputStream(new FileInputStream(source));
			IOUtils.copy(input, output);
		} finally {
			IOUtils.closeQuietly(input);
		}
	}

	public String getRawPath() {
		return rawPath;
	}

	public void setRawPath(String rawPath) {
		this.rawPath = rawPath;
	}

	public String getVariantPath() {
		return variantPath;
	}

	public void setVariantPath(String variantPath) {
		this.variantPath = variantPath;
	}

}
