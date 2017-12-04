package genepi.mut.pileup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import genepi.hadoop.CacheStore;
import genepi.hadoop.HdfsUtil;
import genepi.hadoop.io.HdfsLineWriter;
import genepi.mut.objects.BasePosition;
import genepi.mut.objects.BasePositionHadoop;
import genepi.mut.objects.VariantLine;
import genepi.mut.util.ReferenceUtil;
import genepi.mut.util.ReferenceUtilHdfs;

public class PileupReducer extends Reducer<Text, BasePositionHadoop, Text, Text> {

	private BasePosition basePos = new BasePosition();

	String reference;

	String hdfsVariants;

	HdfsLineWriter writer;

	boolean callDel;

	protected void setup(Context context) throws IOException, InterruptedException {

		HdfsUtil.setDefaultConfiguration(context.getConfiguration());

		CacheStore cache = new CacheStore(context.getConfiguration());
		File referencePath = new File(cache.getArchive("reference"));
		String fastaPath = ReferenceUtilHdfs.findFileinDir(referencePath, ".fasta");
		reference = ReferenceUtil.readInReference(fastaPath);
		
		//default is to ignore deletions
		callDel = context.getConfiguration().getBoolean("callDel", false);

		hdfsVariants = context.getConfiguration().get("variantsHdfs");
		HdfsUtil.create(hdfsVariants + "/" + context.getTaskAttemptID());
		writer = new HdfsLineWriter(hdfsVariants + "/" + context.getTaskAttemptID());
	}

	protected void reduce(Text key, java.lang.Iterable<BasePositionHadoop> values, Context context)
			throws java.io.IOException, InterruptedException {

		basePos.clear();
		
		List<Byte> combinedAFor = new ArrayList<Byte>();
		List<Byte> combinedCFor = new ArrayList<Byte>();
		List<Byte> combinedGFor = new ArrayList<Byte>();
		List<Byte> combinedTFor = new ArrayList<Byte>();
		List<Byte> combinedDFor = new ArrayList<Byte>();
		List<Byte> combinedARev = new ArrayList<Byte>();
		List<Byte> combinedCRev = new ArrayList<Byte>();
		List<Byte> combinedGRev = new ArrayList<Byte>();
		List<Byte> combinedTRev = new ArrayList<Byte>();
		List<Byte> combinedDRev = new ArrayList<Byte>();

		for (BasePositionHadoop valueHadoop : values) {
			
			BasePosition value = valueHadoop.getBasePosition();

			basePos.add(value);

			combinedAFor.addAll(value.getaForQ());
			combinedCFor.addAll(value.getcForQ());
			combinedGFor.addAll(value.getgForQ());
			combinedTFor.addAll(value.gettForQ());
			combinedDFor.addAll(value.getdForQ());
			combinedARev.addAll(value.getaRevQ());
			combinedCRev.addAll(value.getcRevQ());
			combinedGRev.addAll(value.getgRevQ());
			combinedTRev.addAll(value.gettRevQ());
			combinedDRev.addAll(value.getdRevQ());
		}

		basePos.setaForQ(combinedAFor);
		basePos.setcForQ(combinedCFor);
		basePos.setgForQ(combinedGFor);
		basePos.settForQ(combinedTFor);
		basePos.setdForQ(combinedDFor);

		basePos.setaRevQ(combinedARev);
		basePos.setcRevQ(combinedCRev);
		basePos.setgRevQ(combinedGRev);
		basePos.settRevQ(combinedTRev);
		basePos.setdRevQ(combinedDRev);

		basePos.setId(key.toString().split(":")[0]);
		
		int pos = Integer.valueOf(key.toString().split(":")[1]);
		
		basePos.setPos(pos);

		if (pos > 0 && pos <= reference.length()) {

			char ref = reference.charAt(pos - 1);

			VariantLine line = new VariantLine();
			
			//needed so we can ignore bases when sorting them (e.g. ignore if minor is D)
			line.setCallDel(callDel);
			
			line.setRef(ref);

			line.analysePosition(basePos);

			context.write(null, new Text(line.toRawString()));

			line.callVariants();

			if (line.isFinalVariant()) {
				writer.write(line.writeVariant());
			}
		}

	}

	protected void cleanup(Context context) throws IOException, InterruptedException {
		writer.close();
	}

}
