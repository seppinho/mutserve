package genepi.mut.tasks;

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
		}
		if (rawPath != null) {
			for (int i = 0; i < rawInputs.length; i++) {
				VariantCallingTask task = tasks.get(i);
				this.rawInputs[i] = new File(task.getRawName());
			}

		}
	}

	@Override
	public void run(ITaskMonitor monitor) throws Exception {

		try {
			monitor.begin("Merge output files");

			assert (variantInputs != null);
			assert (variantInputs.length > 0);

			new File(variantPath).delete();

			LineWriter writerVar = new LineWriter(variantPath);
			writerVar.write(BamAnalyser.headerVariants + "\n");
			writerVar.close();

			appendFiles(new File(variantPath), variantInputs);

			if (rawPath != null) {
				new File(rawPath).delete();
				LineWriter writerRaw = new LineWriter(rawPath);
				writerRaw.write(BamAnalyser.headerRaw + "\n");
				writerRaw.close();
				appendFiles(new File(rawPath), rawInputs);
			}

			monitor.done();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void appendFiles(File destination, File[] sources) throws IOException {
		try (OutputStream output = new BufferedOutputStream(new FileOutputStream(destination, true))) {
			for (File source : sources) {
				appendFile(output, source);
				source.delete();
			}
		}
	}

	private static void appendFile(OutputStream output, File source) throws IOException {
		try (InputStream input = new BufferedInputStream(new FileInputStream(source))) {
			IOUtils.copy(input, output);
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
