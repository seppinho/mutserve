package genepi.mut.validate;

import genepi.hadoop.HdfsUtil;
import genepi.hadoop.common.WorkflowContext;
import genepi.hadoop.common.WorkflowStep;
import genepi.hadoop.importer.IImporter;
import genepi.hadoop.importer.ImporterFactory;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SamInputResource;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.fastq.FastqReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class InputValidation extends WorkflowStep {

	protected static final Log log = LogFactory.getLog(InputValidation.class);

	StringBuffer text = new StringBuffer();

	int amountBams;
	int amountSE;
	int amountPE;
	String msgType;
	String msgType1;
	int amountFiles = 0;
	Set<String> platforms = new HashSet<String>();

	@Override
	public boolean run(WorkflowContext context) {

		String type = context.get("inType");
		String input = context.get("input");

		// track job mission using counters (web, client, ..)
		String source = context.get("submissionSource");
		context.incCounter("jobs.submission." + source, 1);
		context.submitCounter("jobs.submission." + source);

		if (!importFiles(context)) {
			context.endTask("Upload failed ", WorkflowContext.ERROR);
			return false;
		}

		context.beginTask("Analyze files ");

		input = context.get("input");

		int fileSize = Integer.valueOf(context.get("fileSize"));

		if (calcFileSize(input) > fileSize) {

			context.endTask("Please contact sebastian.schoenherr@i-med.ac.at to discuss this large analysis.",
					WorkflowContext.ERROR);
			return false;

		}

		String detectedType = inputDectection(input);

		switch (type) {
		case "se":
			msgType = "single-end file(s)";
			break;
		case "pe":
			msgType = "paired-end file(s)";
			break;
		case "bam":
			msgType = "BAM file(s)";
			break;

		}

		switch (detectedType) {
		case "se":
			msgType1 = "single-end file(s)";
			break;
		case "pe":
			msgType1 = "paired-end file(s)";
			break;
		case "bam":
			msgType1 = "BAM file(s)";
			break;
		default:
			msgType1 = "Unsupported";
		}

		if (!type.equals(detectedType)) {
			context.endTask("Please check your input data type. You selected " + msgType + " but mtDNA-Server detected:"
					+ msgType1, WorkflowContext.ERROR);
			return false;
		}

		switch (detectedType) {
		case "se":
			context.incCounter("FASTQ_SE", amountSE);
			amountFiles = amountSE;
			break;
		case "pe":
			context.incCounter("FASTQ_PE", amountPE);
			amountFiles = amountPE;
			break;
		case "bam":
			context.incCounter("BAM", amountBams);
			amountFiles = amountBams;
			break;

		}

		if (amountFiles == 0) {
			context.endTask("No files to process!", WorkflowContext.ERROR);
			return false;
		}

		if (amountFiles > 100) {
			context.endTask("Please contact sebastian.schoenherr@i-med.ac.at to discuss this large analysis.",
					WorkflowContext.ERROR);
			return false;
		}

		StringBuffer text = new StringBuffer();
		text.append("Validation succeeded! <br>");
		text.append("<b>Parameters:</b> <br>");
		text.append("Detected file type: " + msgType + "<br>");
		if (platforms.size() > 0) {
			text.append("Detected platforms: " + platforms + "<br>");
		}
		text.append("Amount files: " + amountFiles + "<br>");
		/*
		 * if (detectedType.equals("bam") && ref == MitoUtil.Reference.hg19) {
		 * text.append("Reference " + ref.name() +
		 * " detected. <b> Important:</b> All positions are mapped to rCRS automatically!"
		 * ); }
		 */
		context.endTask(text.toString(), WorkflowContext.OK);

		return true;

	}

	private long calcFileSize(String folder) {

		try {

			long size = 0;

			FileSystem fileSystem = FileSystem.get(HdfsUtil.getConfiguration());
			FileStatus[] fileList = fileSystem.listStatus(new Path(folder));

			for (FileStatus file : fileList) {

				size += file.getLen();

			}

			return size / 1024 / 1024;

		} catch (IOException e2) {
			e2.printStackTrace();

			return -1;
		}

	}

	private String inputDectection(String input) {

		String outType = "unsupported file format";
		amountBams = 0;
		amountSE = 0;
		amountPE = 0;
		int invalid = 0;
		FileStatus[] fileList = null;
		FileSystem fileSystem = null;
		HashMap<String, List<String>> mapPairs = new HashMap<String, List<String>>();

		try {
			fileSystem = FileSystem.get(HdfsUtil.getConfiguration());
			fileList = fileSystem.listStatus(new Path(input));

		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		for (FileStatus file : fileList) {

			System.out.println("file is " + file.getPath());

			try {

				FSDataInputStream in = fileSystem.open(file.getPath());

				// BAM CHECK
				final SamReader reader = SamReaderFactory.makeDefault().open(SamInputResource.of(in));

				SAMFileHeader header = reader.getFileHeader();

				amountBams++;

				in.close();
				
				reader.close();

			} catch (Exception e) {

				try {

					FSDataInputStream in = fileSystem.open(file.getPath());

					if (file.getPath().getName().endsWith(".gz")) {
						// FASTQ GZ PARSABLE?
						GZIPInputStream gzis = new GZIPInputStream(in);
						InputStreamReader reader = new InputStreamReader(gzis);
						BufferedReader br = new BufferedReader(reader);
						FastqReader readerGZ = new FastqReader(br);
						readerGZ.close();
					} else {
						// FASTQ PARSABLE?
						FastqReader reader = new FastqReader(new BufferedReader(new InputStreamReader(in)));

						reader.close();
					}

					in.close();

					// GENERATE PAIRS
					detectPairs(mapPairs, file);

				} catch (Exception e1) {
					invalid++;
				}

			}

		}

		Set<String> keys = mapPairs.keySet();
		for (String key : keys) {
			List<String> values = mapPairs.get(key);

			if (values.size() == 2) {
				amountPE++;
			} else {
				amountSE++;
			}
		}

		if (amountBams > 0 && amountBams == fileList.length) {
			outType = "bam";

		}

		else if (amountSE > 0 && amountSE == fileList.length) {
			outType = "se";
		}

		else if (amountPE > 0 && fileList.length != 0 && amountPE == fileList.length / 2) {
			outType = "pe";
		}

		log.info("Bams: " + amountBams + " FASTQ-SE: " + amountSE + " FASTQ-PE: " + amountPE + " Invalid:" + invalid);

		return outType;
	}

	private void detectPairs(HashMap<String, List<String>> mapPairs, FileStatus file) {
		try {
			FileSystem fileSystem;
			fileSystem = FileSystem.get(HdfsUtil.getConfiguration());
			FSDataInputStream in = fileSystem.open(file.getPath());
			BufferedReader reader;

			if (file.getPath().getName().endsWith(".gz")) {
				GZIPInputStream gzis = new GZIPInputStream(in);
				InputStreamReader stream = new InputStreamReader(gzis);
				reader = new BufferedReader(stream);
			} else {
				reader = new BufferedReader(new InputStreamReader(in));
			}

			String line = reader.readLine();
			String[] keySplits = line.split(" ");
			String key;

			// old ILUMINA format
			if (line.charAt(line.length() - 2) == '/')
				key = keySplits[1].substring(0, keySplits[1].length() - 2);
			else
				// new ILUMINA format (see Mapper!)
				key = keySplits[0];

			List<String> values = mapPairs.get(key);

			if (values == null) {
				values = new ArrayList<String>();
			}

			values.add(file.getPath().toString());
			mapPairs.put(key, values);
			in.close();
			reader.close();

		}

		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean importFiles(WorkflowContext context) {

		for (String input : context.getInputs()) {

			String address = context.get(input).trim();

			if (ImporterFactory.needsImport(address)) {

				context.beginTask("Importing files...");

				String[] urlList = address.split(";")[0].split("\\s+");

				String username = "";
				if (address.split(";").length > 1) {
					username = address.split(";")[1];
				}

				String password = "";
				if (address.split(";").length > 2) {
					password = address.split(";")[2];
				}

				for (String url2 : urlList) {

					String url = url2 + ";" + username + ";" + password;

					String target = HdfsUtil.path(context.getHdfsTemp(), "importer", input);

					try {

						context.updateTask("Import " + url2 + "...", WorkflowContext.RUNNING);

						IImporter importer = ImporterFactory.createImporter(url, target);

						if (importer != null) {

							/*
							 * // if http url and bam file, use our own
							 * importer! if ((importer instanceof ImporterHttp
							 * || importer instanceof ImporterFtp) &&
							 * context.get("inType").equals("bam")) { importer =
							 * new ImporterBamHttp(url, target); context.
							 * updateTask("Import only mitochondrial part from "
							 * + url2 + "...", WorkflowContext.RUNNING); }
							 */

							boolean successful = importer.importFiles(".bam|.fq|.fastq|.gz");

							if (successful) {

								context.setInput(input, target);

							} else {

								context.updateTask("Import " + url2 + " failed: " + importer.getErrorMessage(),
										WorkflowContext.ERROR);

								return false;

							}

						} else {

							context.updateTask("Import " + url2 + " failed: Protocol not supported",
									WorkflowContext.ERROR);

							return false;

						}

					} catch (Exception e) {
						context.updateTask("Import File(s) " + url2 + " failed: " + e.toString(),
								WorkflowContext.ERROR);
						e.printStackTrace();
						return false;
					}

				}

				context.updateTask("File Import successful. ", WorkflowContext.OK);

			}

		}

		return true;

	}

}
