package genepi.mut.validate;

import genepi.hadoop.HdfsUtil;
import genepi.hadoop.importer.FileItem;
import genepi.hadoop.importer.IImporter;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SamInputResource;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class ImporterBamHttp implements IImporter {

	public static final int REFERENCE_LENGTH = 16569;

	private String url;

	private String path;

	private String error;

	public ImporterBamHttp(String url, String path) {

		this.url = url.split(";")[0];
		this.path = path;

	}

	public long getFileSize() {

		try {
			URL webUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) webUrl
					.openConnection();
			return conn.getContentLength();

		} catch (MalformedURLException e) {
			error = e.getMessage();
			return -1;
		} catch (IOException e) {
			error = e.getMessage();
			return -1;

		}

	}

	@Override
	public boolean importFiles() {
		return importFiles(null);
	}

	@Override
	public boolean importFiles(String extension) {

		Configuration conf = HdfsUtil.getConfiguration();
		FileSystem fileSystem;
		try {
			fileSystem = FileSystem.get(conf);
			return importIntoHdfs(url, fileSystem, path);
		} catch (IOException e) {
			error = e.getMessage();
			e.printStackTrace();
			return false;
		}

	}

	public boolean importIntoHdfs(String weburl, FileSystem fileSystem,
			String path) throws IOException {

		// check if bai is available else download whole file

		final SamReader reader = SamReaderFactory.makeDefault().validationStringency(htsjdk.samtools.ValidationStringency.SILENT).open(
				SamInputResource.of(new URL(weburl)).index(
						new URL(weburl + ".bai")));

		// path in hdfs
		String[] tiles = weburl.split("/");
		String name = tiles[tiles.length - 1];

		String target = HdfsUtil.path(path, name);

		SAMFileHeader header = reader.getFileHeader();
		SAMSequenceDictionary seqDictionary = header.getSequenceDictionary();

		String referenceName = null;

		for (SAMSequenceRecord record : seqDictionary.getSequences()) {

			if (record.getSequenceLength() == REFERENCE_LENGTH) {
				referenceName = record.getSequenceName();
			}
		}

		if (referenceName == null) {
			reader.close();
			error = "No mitochondrial contig found in " + weburl + ".";
			return false;
		}

		FSDataOutputStream out = fileSystem.create(new Path(target));
		SAMFileWriter writer = new SAMFileWriterFactory().makeBAMWriter(
				reader.getFileHeader(), true, out);

		SAMRecordIterator reads = reader.query(referenceName, 0, 0, false);
		int good = 0;
		int bad = 0;
		int written = 0;
		SAMRecord read = null;
		while (reads.hasNext()) {
			try { // hansi style solution TODO!
				read = reads.next();
				good++;
			} catch (Exception e) {
				// e.printStackTrace(s);
				bad++;
			}
			writer.addAlignment(read);
			written++;
		}

		writer.close();
		reader.close();

		System.out.println("Bad reads: " + bad);
		System.out.println("Good reads: " + good);
		System.out.println("Written reads: " + written);
		
		return true;
	}

	@Override
	public List<FileItem> getFiles() {
		List<FileItem> items = new Vector<FileItem>();
		FileItem file = new FileItem();
		file.setText(FilenameUtils.getName(url));
		file.setPath("/");
		file.setId("/");
		file.setSize(FileUtils.byteCountToDisplaySize(getFileSize()));
		items.add(file);
		return items;
	}

	@Override
	public String getErrorMessage() {
		return error;
	}

}
