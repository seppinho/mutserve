package genepi.mut.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

import com.github.lindenb.jbwa.jni.ShortRead;

public class SingleRead extends ShortRead implements Writable {

	/** extend ShortRead to add the filename and the readNumber */

	String filename;
	int readNumber;
	int readLength;

	public SingleRead() {
		super(null, null, null);
	}

	public SingleRead(String name, byte[] seq, byte[] qual) {
		super(name, seq, qual);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void readFields(DataInput arg0) throws IOException {
		// TODO Auto-generated method stub

		
		/** read1 */

		readLength = arg0.readInt();

		seq = new byte[readLength];
		arg0.readFully(seq);

		qual = new byte[readLength];
		arg0.readFully(qual);

		name = arg0.readUTF();

		filename = arg0.readUTF();

		readNumber = arg0.readInt();

	}

	@Override
	public void write(DataOutput arg0) throws IOException {
		// TODO Auto-generated method stub

		arg0.writeInt(readLength);
		arg0.write(seq);
		arg0.write(qual);
		arg0.writeUTF(name);
		arg0.writeUTF(filename);
		arg0.writeInt(readNumber);

	}

	public int getReadLength() {
		return readLength;
	}

	public void setReadLength(int readLength) {
		this.readLength = readLength;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public int getReadNumber() {
		return readNumber;
	}

	public void setReadNumber(int readNumber) {
		this.readNumber = readNumber;
	}

}
