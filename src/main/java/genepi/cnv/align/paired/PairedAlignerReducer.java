package genepi.cnv.align.paired;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import genepi.cnv.objects.SingleRead;
import genepi.cnv.util.ReferenceUtil;
import genepi.hadoop.CacheStore;
import genepi.io.FileUtil;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import com.github.lindenb.jbwa.jni.BwaIndex;
import com.github.lindenb.jbwa.jni.BwaMem;
import com.github.lindenb.jbwa.jni.ShortRead;

public class PairedAlignerReducer extends Reducer<Text, SingleRead, Text, Text> {

	SingleRead first = new SingleRead();
	SingleRead second = new SingleRead();
	BwaIndex index;
	BwaMem mem;
	List<ShortRead> L1;
	List<ShortRead> L2;
	Text out;
	String[] result;
	int countReads;
	int trimBasesStart;
	int trimBasesEnd;

	enum Counters {

		NOPAIR, NEWFLAG

	}

	protected void setup(Context context) throws IOException,
			InterruptedException {
		String refString = null;
		L1 = new ArrayList<ShortRead>();
		L2 = new ArrayList<ShortRead>();
		out = new Text();
		countReads = 0;

		/** load jbwa lib and reference */
		CacheStore cache = new CacheStore(context.getConfiguration());
		String jbwaLibLocation = cache.getArchive("jbwaLib");
		String jbwaLib = FileUtil.path(jbwaLibLocation, "native",
				"libbwajni.so");
		String referencePath = cache.getArchive("reference");
		trimBasesStart = context.getConfiguration().getInt("trimReadsStart", 0);
		trimBasesEnd = context.getConfiguration().getInt("trimReadsEnd", 0);

		/** load JNI */
		System.load(jbwaLib);

		File reference = new File(referencePath);

		refString = ReferenceUtil.findFileinReferenceArchive(reference, ".fasta");

		/** load index, aligner */
		index = new BwaIndex(new File(refString));
		mem = new BwaMem(index);

	}

	protected void reduce(Text key, java.lang.Iterable<SingleRead> values,
			Context context) throws java.io.IOException, InterruptedException {

		/** preprocessing reads, trim *//*
		String seq = new String(value.getBases());
		String qual= new String(value.getQualities());
		seq = seq.substring(trimBasesStart, seq.length());
		qual = qual.substring(trimBasesStart, qual.length());
		
		if(trimBasesEnd!=0 && seq.length() >= trimBasesEnd){
			seq = seq.substring(0, trimBasesEnd);
			qual = qual.substring(0, trimBasesEnd);
		}*/
		
		for (SingleRead value : values) {

			countReads++;

			if (value.getReadNumber() == 1) {

				first = new SingleRead();
				/** copy object, otherwise same reference */
				first.setReadLength(value.getReadLength());
				first.setName(value.getName());
				first.setBases(value.getBases());
				first.setQual(value.getQualities());
				first.setFilename(value.getFilename());
				L1.add(first);

			} else {

				second = new SingleRead();
				second.setReadLength(value.getReadLength());
				second.setName(value.getName());
				second.setBases(value.getBases());
				second.setQual(value.getQualities());
				second.setFilename(value.getFilename());
				L2.add(second);

			}

		}

		if (countReads % 99010 == 0) {

			System.out.println("count is " + countReads);
			/** main JBWA JNI */
			align(context);
		}

	}

	protected void cleanup(Context context) throws IOException,
			InterruptedException {

		if (L1.size() > 0) {

			System.out.println("last call");

			System.out.println("align directly");
			align(context);

		} else {

			System.out.println("nothing to align in cleanup");

		}

		index.close();
		mem.dispose();

	}

	private void align(Context context) throws IOException,
			InterruptedException {

		result = mem.align(L1, L2);

		for (int i = 0; i < result.length; i++) {

			String read = result[i];
			String sample = ((SingleRead) L1.get(i / 2)).getFilename();

			/**
			 * hack to write a valid SAM since BWA MEM outputs tabs at the end,
			 * samtools can not handle this
			 */
			read = read.replaceAll("\\t+$", "");
			read = read.replaceAll("\\s+$", "");
			String tiles[] = read.split("\t+\n");
			
			for (String tile : tiles) {
				out.clear();
				out.set(tile);
				context.write(new Text(sample), out);

			}
			

		}

		L1.clear();
		L2.clear();
	}

}
