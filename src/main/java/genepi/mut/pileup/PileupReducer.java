package genepi.mut.pileup;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import genepi.hadoop.CacheStore;
import genepi.hadoop.HdfsUtil;
import genepi.hadoop.PreferenceStore;
import genepi.hadoop.io.HdfsLineWriter;
import genepi.mut.objects.BasePosition;
import genepi.mut.objects.BasePosition2;
import genepi.mut.objects.VariantLine;
import genepi.mut.util.ReferenceUtil;
import genepi.mut.util.ReferenceUtilHdfs;
import genepi.mut.util.StatUtil;

public class PileupReducer extends Reducer<Text, BasePosition, Text, Text> {

	private BasePosition2 posInput = new BasePosition2();

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

	protected void reduce(Text key, java.lang.Iterable<BasePosition2> values, Context context)
			throws java.io.IOException, InterruptedException {

		posInput.clear();
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

		for (BasePosition value : values) {

			posInput.add(value);

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

		posInput.setaForQ(combinedAFor);
		posInput.setcForQ(combinedCFor);
		posInput.setgForQ(combinedGFor);
		posInput.settForQ(combinedTFor);
		posInput.setdForQ(combinedDFor);

		posInput.setaRevQ(combinedARev);
		posInput.setcRevQ(combinedCRev);
		posInput.setgRevQ(combinedGRev);
		posInput.settRevQ(combinedTRev);
		posInput.setdRevQ(combinedDRev);

		posInput.setId(key.toString().split(":")[0]);
		
		int pos = Integer.valueOf(key.toString().split(":")[1]);
		
		posInput.setPos(pos);

		if (pos > 0 && pos <= reference.length()) {

			char ref = reference.charAt(pos - 1);

			VariantLine line = new VariantLine();
			
			//needed so we can ignore bases when sorting them (e.g. ignore if minor is D)
			line.setCallDel(callDel);
			
			line.setRef(ref);

			line.analysePosition(posInput);

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
