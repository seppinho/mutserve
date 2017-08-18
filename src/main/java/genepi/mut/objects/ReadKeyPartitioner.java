package genepi.mut.objects;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

public class ReadKeyPartitioner extends Partitioner<ReadKey, Text> {

	@Override
	public int getPartition(ReadKey key, Text value, int numPartitions) {
		return Math.abs((int) ((key.getSample().hashCode())
				* key.getSequence().hashCode() * 127))
				% numPartitions;
	}
}