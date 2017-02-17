package genepi.cnv.align.single;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.seqdoop.hadoop_bam.SequencedFragment;

import com.github.lindenb.jbwa.jni.AlnRgn;
import com.github.lindenb.jbwa.jni.BwaIndex;
import com.github.lindenb.jbwa.jni.BwaMem;
import com.github.lindenb.jbwa.jni.ShortRead;

import genepi.cnv.util.ReferenceUtil;
import genepi.hadoop.CacheStore;
import genepi.io.FileUtil;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMLineParser;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.util.SequenceUtil;

public class SingleAlignerMap extends Mapper<Object, SequencedFragment, Text, Text> {

	BwaIndex index;
	BwaMem mem;
	int length;
	String filename;
	private Text outValue = new Text();
	private SAMFileHeader header = new SAMFileHeader();
	private SAMLineParser parser = null;
	StringBuilder samRecordBulder = new StringBuilder();

	enum Counters {

		BAD_MAPPING, BAD_QUALITY, GOOD_QUALITY

	}

	protected void setup(Context context) throws IOException, InterruptedException {

		/** load jbwa lib and reference */

		CacheStore cache = new CacheStore(context.getConfiguration());
		String jbwaLibLocation = cache.getArchive("jbwaLib");
		String jbwaLib = FileUtil.path(jbwaLibLocation, "libbwajni.so");
		String referencePath = cache.getArchive("reference");

		FileSplit fileSplit = (FileSplit) context.getInputSplit();
		filename = fileSplit.getPath().getName();

		/** load JNI */
		System.load(jbwaLib);

		File reference = new File(referencePath);

		String refString = ReferenceUtil.findFileinReferenceArchive(reference, ".fasta");
		length = (ReferenceUtil.readInReference(refString)).length();

		/** load index, aligner */
		if (refString != null) {
			index = new BwaIndex(new File(refString));
			mem = new BwaMem(index);
		} else {
			System.exit(-1);
		}

		header = new SAMFileHeader();
		parser = new SAMLineParser(header);

	}

	public void map(Object key, SequencedFragment value, Context context) throws IOException, InterruptedException {

		String seq = value.getSequence().toString();
		String qual = value.getQuality().toString();

		ShortRead read = new ShortRead(key.toString(), seq.getBytes(), qual.getBytes());

		// equivalent to setting bwa mem -x flag set to "intractg"
		// mem.updateScoringParameters(9, 16, 16, 1, 1, 5, 5);

		for (AlnRgn alignedRead : mem.align(read)) {

			// edit header
			if (header.getSequence(alignedRead.getChrom()) == null) {
				header.addSequence(new SAMSequenceRecord(alignedRead.getChrom(), length));
			}

			// reset builder
			samRecordBulder.setLength(0);
			
			//as defined by BWA
			if(alignedRead.getAs()<30) {
				continue;
			}

			// READNAME
			samRecordBulder.append(key.toString());
			samRecordBulder.append("\t");

			// see for secondary:
			// https://github.com/lh3/bwa/blob/master/bwamem.h
			// see for flags: http://picard.sourceforge.net/explain-flags.html

			if (alignedRead.getStrand() == '-') {

				if (alignedRead.getSecondary() < 0) {
					// FLAGS REVERSE, PRIMARY ALIGNMENT
					samRecordBulder.append(16);
				} else {
					// dont output secondary alignments!!
					continue;
				}

			} else {

				if (alignedRead.getSecondary() < 0) {
					// FLAGS FORWARD, PRIMARY ALIGNMENT
					samRecordBulder.append(0);
				} else {
					// dont output secondary alignments!!
					continue;
				}
			}

			samRecordBulder.append("\t");

			// REFERENCE
			samRecordBulder.append(alignedRead.getChrom());

			samRecordBulder.append("\t");

			// LEFT MOST POS
			samRecordBulder.append(alignedRead.getPos());

			samRecordBulder.append("\t");

			// QUAL
			samRecordBulder.append(alignedRead.getMQual());

			samRecordBulder.append("\t");

			// CIGAR
			samRecordBulder.append(alignedRead.getCigar());

			samRecordBulder.append("\t");

			// RNEXT (REF NAME OF THE MATE)
			// PNEXT (POS OF THE MATE) LENGTH OF TEMPLATE
			samRecordBulder.append("*\t0\t0\t");

			// SEQ REVERSE
			if (alignedRead.getStrand() == '-') {
				byte[] temp = value.getSequence().toString().getBytes();
				SequenceUtil.reverseComplement(temp);
				samRecordBulder.append(new String(temp));
			}

			else {
				// SEQ FORWARD
				samRecordBulder.append(value.getSequence());
			}

			samRecordBulder.append("\t");

			// QUAL REVERSE
			if (alignedRead.getStrand() == '-') {
				byte[] temp = value.getQuality().toString().getBytes();
				SequenceUtil.reverseQualities(temp);
				samRecordBulder.append(new String(temp));
			} else {
				// QUAL FORWARD
				samRecordBulder.append(value.getQuality());
			}

			samRecordBulder.append("\t");

			// jbwa fork
			samRecordBulder.append("NM:i:" + alignedRead.getNm());
			samRecordBulder.append("\t");
			samRecordBulder.append("AS:i:" + alignedRead.getAs());

			SAMRecord samRecord = parser.parseLine(samRecordBulder.toString());

			outValue.set(filename + "\t" + samRecord.getSAMString());

			context.write(null, outValue);

		}

	}

}
