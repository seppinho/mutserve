package genepi.mut.objects;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

public class ReadKeyGroupingComparator extends WritableComparator {

	protected ReadKeyGroupingComparator() {
		super(ReadKey.class, true);
	}

	@Override
	public int compare(WritableComparable o1, WritableComparable o2) {

		ReadKey rk1 = (ReadKey) o1;
		ReadKey rk2 = (ReadKey) o2;

		if (rk1.sample.equals(rk2.sample)) {

			if (rk1.sequence.equals(rk2.sequence)) {
				return 0;

			} else {
				return rk1.sequence.compareTo(rk2.sequence);
			}
		} else {
			return rk1.sample.compareTo(rk2.sample);

		}

	}

}