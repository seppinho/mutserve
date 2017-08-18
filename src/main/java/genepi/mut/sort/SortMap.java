package genepi.mut.sort;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import genepi.hadoop.HdfsUtil;
import genepi.mut.objects.ReadKey;

public class SortMap extends Mapper<Object, Text, ReadKey, Text> {

	private ReadKey outKey = new ReadKey();

	@Override
	protected void setup(Context context) throws IOException, InterruptedException {

		HdfsUtil.setDefaultConfiguration(context.getConfiguration());

	}

	public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

		if (!value.toString().trim().equals("")) {
			String tilesValue[] = value.toString().split("\t", 2);
			String sample = tilesValue[0].replaceAll(".fastq", "").replaceAll(".fq", "");
			
			String[] tiles = tilesValue[1].split("\t");
			String readName = tiles[0].trim();
			String contig = tiles[2].trim();
			String start = tiles[3].trim();

			outKey.setSample(sample);
			outKey.setPosition(Integer.valueOf(start));
			outKey.setSequence(contig);
			outKey.setReadName(readName);

			context.write(outKey, new Text(tilesValue[1]));
		}

	}

}
