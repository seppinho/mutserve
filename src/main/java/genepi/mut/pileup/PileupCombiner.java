package genepi.mut.pileup;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import genepi.mut.objects.BasePosition;

public class PileupCombiner extends
		Reducer<Text, BasePosition, Text, BasePosition> {

	private BasePosition valueOut = new BasePosition();

	protected void reduce(Text key, java.lang.Iterable<BasePosition> values,
			Context context) throws java.io.IOException, InterruptedException {

		valueOut.clear();

		List<Byte> combinedAFor = new ArrayList<Byte>();
		List<Byte> combinedCFor = new ArrayList<Byte>();
		List<Byte> combinedGFor = new ArrayList<Byte>();
		List<Byte> combinedTFor = new ArrayList<Byte>();
		List<Byte> combinedARev = new ArrayList<Byte>();
		List<Byte> combinedCRev = new ArrayList<Byte>();
		List<Byte> combinedGRev = new ArrayList<Byte>();
		List<Byte> combinedTRev = new ArrayList<Byte>();
		
		for (BasePosition value : values) {
			valueOut.add(value);
			combinedAFor.addAll(value.getaForQ());
			combinedCFor.addAll(value.getcForQ());
			combinedGFor.addAll(value.getgForQ());
			combinedTFor.addAll(value.gettForQ());
			combinedARev.addAll(value.getaRevQ());
			combinedCRev.addAll(value.getcRevQ());
			combinedGRev.addAll(value.getgRevQ());
			combinedTRev.addAll(value.gettRevQ());
			
		}
		
		
		valueOut.setaForQ(combinedAFor);
		valueOut.setcForQ(combinedCFor);
		valueOut.setgForQ(combinedGFor);
		valueOut.settForQ(combinedTFor);
		
		valueOut.setaRevQ(combinedARev);
		valueOut.setcRevQ(combinedCRev);
		valueOut.setgRevQ(combinedGRev);
		valueOut.settRevQ(combinedTRev);
		
		context.write(key, valueOut);
	};

}
