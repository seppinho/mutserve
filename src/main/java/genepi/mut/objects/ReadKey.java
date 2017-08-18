package genepi.mut.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

public class ReadKey implements WritableComparable<ReadKey> {

	public String sample;

	public String sequence;

	public long position;

	public String readname;

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public long getPosition() {
		return position;
	}

	public void setPosition(long position) {
		this.position = position;
	}

	public String getSample() {
		return sample;
	}

	public void setSample(String sample) {
		this.sample = sample;
	}

	public void setReadName(String readname) {
		this.readname = readname;
	}

	public String getReadName() {
		return readname;
	}

	@Override
	public void readFields(DataInput arg0) throws IOException {
		this.sample = arg0.readUTF();
		this.sequence = arg0.readUTF();
		this.position = arg0.readLong();
		this.readname = arg0.readUTF();
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeUTF(sample);
		out.writeUTF(sequence);
		out.writeLong(position);
		out.writeUTF(readname);
	}

	public static class BamSplitComparator extends WritableComparator {
		public BamSplitComparator() {
			super(ReadKey.class);
		}

		public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
			return compareBytes(b1, s1, l1, b2, s2, l2);
		}
	}

	static { // register this comparator
	//	WritableComparator.define(ReadKey.class, new BamSplitComparator());
	}

	@Override
	public int compareTo(ReadKey arg0) {

		if (sample.equals(arg0.sample)) {

			if (sequence.equals(arg0.sequence)) {

				if (position == arg0.position) {

					return readname.compareTo(arg0.readname);

				} else {

					return position > arg0.position ? 1 : -1;

				}

			}

			else {
				return sequence.compareTo(arg0.sequence);
			}

		}

		else {

			return sample.compareTo(arg0.sample);
		}
	}

}
