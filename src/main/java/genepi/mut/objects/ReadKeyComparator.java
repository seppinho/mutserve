package genepi.mut.objects;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

public class ReadKeyComparator extends WritableComparator {

	protected ReadKeyComparator() {
		super(ReadKey.class, true);
	}

	@Override
	public int compare(WritableComparable w1, WritableComparable w2) {

		ReadKey rk1 = (ReadKey) w1;
		ReadKey rk2 = (ReadKey) w2;

		if (rk1.sample.equals(rk2.sample)) {

			if (rk1.sequence.equals(rk2.sequence)) {

				if (rk1.position == rk2.position) {

					return rk1.readname.compareTo(rk2.readname);

				} else {
					return rk1.position > rk2.position ? 1 : -1;
				}

			} else {
				return rk1.sequence.compareTo(rk2.sequence);
			}

		} else {

			return rk1.sample.compareTo(rk2.sample);
		}

	}

}
