package genepi.cnv.pileup;


import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import genepi.cnv.objects.BasePosition;
import genepi.cnv.objects.PositionObject;

public class PileupReducer extends
		Reducer<Text, BasePosition, Text, PositionObject> {

	private BasePosition valueIn = new BasePosition();

	protected void reduce(Text key, java.lang.Iterable<BasePosition> values,
			Context context) throws java.io.IOException, InterruptedException {

		valueIn.clear();
		List<Byte> combinedAFor = new ArrayList<Byte>();
		List<Byte> combinedCFor = new ArrayList<Byte>();
		List<Byte> combinedGFor = new ArrayList<Byte>();
		List<Byte> combinedTFor = new ArrayList<Byte>();
		List<Byte> combinedARev = new ArrayList<Byte>();
		List<Byte> combinedCRev = new ArrayList<Byte>();
		List<Byte> combinedGRev = new ArrayList<Byte>();
		List<Byte> combinedTRev = new ArrayList<Byte>();
		
		for (BasePosition value : values) {
			valueIn.add(value);
			combinedAFor.addAll(value.getaForQ());
			combinedCFor.addAll(value.getcForQ());
			combinedGFor.addAll(value.getgForQ());
			combinedTFor.addAll(value.gettForQ());
			combinedARev.addAll(value.getaRevQ());
			combinedCRev.addAll(value.getcRevQ());
			combinedGRev.addAll(value.getgRevQ());
			combinedTRev.addAll(value.gettRevQ());
		}
		
		valueIn.setaForQ(combinedAFor);
		valueIn.setcForQ(combinedCFor);
		valueIn.setgForQ(combinedGFor);
		valueIn.settForQ(combinedTFor);
		
		valueIn.setaRevQ(combinedARev);
		valueIn.setcRevQ(combinedCRev);
		valueIn.setgRevQ(combinedGRev);
		valueIn.settRevQ(combinedTRev);
		
		valueIn.setId(key.toString().split(":")[0]);
		
		valueIn.setPos(Integer.valueOf(key.toString().split(":")[1]));
		
		PositionObject posOut = new PositionObject();
		posOut.analysePosition(valueIn);
		
		context.write(null, posOut);
	};

}
