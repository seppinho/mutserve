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
import genepi.mut.objects.PositionObject;
import genepi.mut.util.ReferenceUtil;
import genepi.mut.util.StatUtil;

public class PileupReducer extends Reducer<Text, BasePosition, Text, Text> {

	private BasePosition posInput = new BasePosition();

	String reference;

	String hdfsVariants;

	HdfsLineWriter writer;

	String version;

	protected void setup(Context context) throws IOException, InterruptedException {

		HdfsUtil.setDefaultConfiguration(context.getConfiguration());
		PreferenceStore store = new PreferenceStore(context.getConfiguration());
		version = store.getString("server.version");

		CacheStore cache = new CacheStore(context.getConfiguration());
		File referencePath = new File(cache.getArchive("reference"));
		String fastaPath = ReferenceUtil.findFileinDir(referencePath, ".fasta");
		reference = ReferenceUtil.readInReference(fastaPath);

		hdfsVariants = context.getConfiguration().get("variantsHdfs");
		HdfsUtil.create(hdfsVariants + "/" + context.getTaskAttemptID());
		writer = new HdfsLineWriter(hdfsVariants + "/" + context.getTaskAttemptID());
	}

	protected void reduce(Text key, java.lang.Iterable<BasePosition> values, Context context)
			throws java.io.IOException, InterruptedException {

		posInput.clear();
		List<Byte> combinedAFor = new ArrayList<Byte>();
		List<Byte> combinedCFor = new ArrayList<Byte>();
		List<Byte> combinedGFor = new ArrayList<Byte>();
		List<Byte> combinedTFor = new ArrayList<Byte>();
		List<Byte> combinedARev = new ArrayList<Byte>();
		List<Byte> combinedCRev = new ArrayList<Byte>();
		List<Byte> combinedGRev = new ArrayList<Byte>();
		List<Byte> combinedTRev = new ArrayList<Byte>();

		for (BasePosition value : values) {

			posInput.add(value);
			combinedAFor.addAll(value.getaForQ());
			combinedCFor.addAll(value.getcForQ());
			combinedGFor.addAll(value.getgForQ());
			combinedTFor.addAll(value.gettForQ());
			combinedARev.addAll(value.getaRevQ());
			combinedCRev.addAll(value.getcRevQ());
			combinedGRev.addAll(value.getgRevQ());
			combinedTRev.addAll(value.gettRevQ());

		}

		posInput.setaForQ(combinedAFor);
		posInput.setcForQ(combinedCFor);
		posInput.setgForQ(combinedGFor);
		posInput.settForQ(combinedTFor);

		posInput.setaRevQ(combinedARev);
		posInput.setcRevQ(combinedCRev);
		posInput.setgRevQ(combinedGRev);
		posInput.settRevQ(combinedTRev);

		posInput.setId(key.toString().split(":")[0]);
		int pos = Integer.valueOf(key.toString().split(":")[1]);
		posInput.setPos(pos);

		if (pos > 0 && pos <= reference.length()) {

			char ref = reference.charAt(pos - 1);

			PositionObject positionObj = new PositionObject();
			
			positionObj.setRef(ref);

			positionObj.analysePosition(posInput);

			context.write(null, new Text(positionObj.toRawString()));

			// write heteroplasmy files

			positionObj.determineLowLevelVariant();

			// only execute if no low-level variants have been detected
			if (positionObj.getVariantType() == 0) {
				positionObj.determineVariants();
			}

			if (positionObj.getVariantType() == PositionObject.VARIANT
					|| positionObj.getVariantType() == PositionObject.LOW_LEVEL_VARIANT) {
				writer.write(positionObj.writeVariant());
			}
		}

	}

	protected void cleanup(Context context) throws IOException, InterruptedException {
		writer.close();
	}

}
