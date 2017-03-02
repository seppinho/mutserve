package genepi.cnv.objects;

import org.apache.hadoop.mapreduce.Partitioner;
import org.seqdoop.hadoop_bam.SAMRecordWritable;

public class ReadKeyPartitioner extends Partitioner<ReadKey, SAMRecordWritable> {

	@Override
	public int getPartition(ReadKey key, SAMRecordWritable value, int numPartitions) {
		return Math.abs((int) ((key.getSample().hashCode())
				* key.getSequence().hashCode() * 127))
				% numPartitions;
	}
}