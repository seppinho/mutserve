package genepi.vcbox.align.single;

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

import genepi.hadoop.CacheStore;
import genepi.io.FileUtil;
import genepi.vcbox.util.ReferenceUtil;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMLineParser;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.util.SequenceUtil;

public class SingleAlignerMap extends
		Mapper<Object, SequencedFragment, Text, Text> {

	BwaIndex index;
	BwaMem mem;
	String filename;
	//int trimBasesStart;
	//int trimBasesEnd;
	private Text outValue = new Text();
	private SAMFileHeader header = new SAMFileHeader();
	private SAMLineParser parser = null;
	StringBuilder samRecordBulder = new StringBuilder();

	enum Counters {

		BAD_MAPPING, BAD_QUALITY, GOOD_QUALITY

	}

	protected void setup(Context context) throws IOException,
			InterruptedException {

		/** load jbwa lib and reference */
		String refString = null;

		CacheStore cache = new CacheStore(context.getConfiguration());
		String jbwaLibLocation = cache.getArchive("jbwaLib");
		String jbwaLib = FileUtil.path(jbwaLibLocation, "native",
				"libbwajni.so");
		String referencePath = cache.getArchive("reference");

		//trimBasesStart = context.getConfiguration().getInt("trimReadsStart", 0);
		//trimBasesEnd = context.getConfiguration().getInt("trimReadsEnd", 0);

		FileSplit fileSplit = (FileSplit) context.getInputSplit();
		filename = fileSplit.getPath().getName();

		/** load JNI */
		System.load(jbwaLib);

		File reference = new File(referencePath);

		refString = ReferenceUtil.findFileinReferenceArchive(reference, ".fasta");

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

	public void map(Object key, SequencedFragment value, Context context)
			throws IOException, InterruptedException {

		String seq = value.getSequence().toString();
		String qual = value.getQuality().toString();

		ShortRead read = new ShortRead(key.toString(), seq.getBytes(),
				qual.getBytes());

		for (AlnRgn alignedRead : mem.align(read)) {

			// edit header
			if (header.getSequence(alignedRead.getChrom()) == null) {
				// add contig with mtSequence length
				header.addSequence(new SAMSequenceRecord(
						alignedRead.getChrom(), 16569));
			}

			// reset builder
			samRecordBulder.setLength(0);

			samRecordBulder.append(key.toString()); // READNAME
			samRecordBulder.append("\t");

			if (alignedRead.getStrand() == '-') { //Flag is 16

				// see for secondary: https://github.com/lh3/bwa/blob/master/bwamem.h
				// see for flags: http://picard.sourceforge.net/explain-flags.html
				if (alignedRead.getSecondary() < 0)
					samRecordBulder.append(16); // FLAGS REVERSE, PRIMARY ALIGNMENT
				else
					samRecordBulder.append((16 | 256));

			} else { //flag 0 for "+"-Strand

				if (alignedRead.getSecondary() < 0)
					samRecordBulder.append(0); // FLAGS FORWARD, PRIMARY ALIGNMENT
				else
					samRecordBulder.append((0 | 256)); // FLAGS FORWARD
			}

			samRecordBulder.append("\t");

			samRecordBulder.append(alignedRead.getChrom()); // REFERENCE

			samRecordBulder.append("\t");

			samRecordBulder.append(alignedRead.getPos() + 1); // LEFT MOST POS

			samRecordBulder.append("\t");

			samRecordBulder.append(alignedRead.getMQual()); // QUAL

			samRecordBulder.append("\t");

			samRecordBulder.append(alignedRead.getCigar()); // CIGAR

			samRecordBulder.append("\t");

			samRecordBulder.append("*\t0\t0\t"); // RNEXT (REF NAME OF THE MATE)
													// PNEXT
			// (POS OF THE MATE) LENGTH OF TEMPLATE

			if (alignedRead.getStrand() == '-') { // SEQ REVERSE
				byte[] temp = value.getSequence().toString().getBytes();
				SequenceUtil.reverseComplement(temp);
				samRecordBulder.append(new String(temp));
			}

			else {
				samRecordBulder.append(value.getSequence()); // SEQ FORWARD
			}

			samRecordBulder.append("\t");

			if (alignedRead.getStrand() == '-') { // QUAL REVERSE
				byte[] temp = value.getQuality().toString().getBytes();
				SequenceUtil.reverseQualities(temp);
				samRecordBulder.append(new String(temp));
			} else {
				samRecordBulder.append(value.getQuality()); // QUAL FORWARD
			}

			samRecordBulder.append("\t");

			// jbwa hack. changing NM flag to AS FLAG
			samRecordBulder.append("AS:i:" + alignedRead.getNm());

			SAMRecord samRecord = parser.parseLine(samRecordBulder.toString());

			outValue.set(filename + "\t" + samRecord.getSAMString());
			
			context.write(null, outValue);

		}

	}

}
