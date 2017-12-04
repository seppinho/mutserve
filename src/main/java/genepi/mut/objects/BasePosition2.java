package genepi.mut.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.Writable;

public class BasePosition2 extends BasePosition implements Writable {

	@Override
	public void readFields(DataInput arg0) throws IOException {
		
		aFor = arg0.readInt();
		cFor = arg0.readInt();
		gFor = arg0.readInt();
		tFor = arg0.readInt();
		dFor = arg0.readInt();
		nFor = arg0.readInt();

		aRev = arg0.readInt();
		cRev = arg0.readInt();
		gRev = arg0.readInt();
		tRev = arg0.readInt();
		dRev = arg0.readInt();
		nRev = arg0.readInt();

		aForQ.clear();
		cForQ.clear();
		gForQ.clear();
		tForQ.clear();
		dForQ.clear();

		aRevQ.clear();
		cRevQ.clear();
		gRevQ.clear();
		tRevQ.clear();
		dRevQ.clear();

		for (int i = 0; i < aFor; i++) {
			aForQ.add(arg0.readByte());
		}
		for (int i = 0; i < cFor; i++) {
			cForQ.add(arg0.readByte());
		}
		for (int i = 0; i < gFor; i++) {
			gForQ.add(arg0.readByte());
		}
		for (int i = 0; i < tFor; i++) {
			tForQ.add(arg0.readByte());
		}
		for (int i = 0; i < dFor; i++) {
			dForQ.add(arg0.readByte());
		}
		for (int i = 0; i < aRev; i++) {
			aRevQ.add(arg0.readByte());
		}
		for (int i = 0; i < cRev; i++) {
			cRevQ.add(arg0.readByte());
		}
		for (int i = 0; i < gRev; i++) {
			gRevQ.add(arg0.readByte());
		}
		for (int i = 0; i < tRev; i++) {
			tRevQ.add(arg0.readByte());
		}
		for (int i = 0; i < dRev; i++) {
			dRevQ.add(arg0.readByte());
		}
	}

	@Override
	public void write(DataOutput arg0) throws IOException {

		arg0.writeInt(aFor);
		arg0.writeInt(cFor);
		arg0.writeInt(gFor);
		arg0.writeInt(tFor);
		arg0.writeInt(dFor);
		arg0.writeInt(nFor);

		arg0.writeInt(aRev);
		arg0.writeInt(cRev);
		arg0.writeInt(gRev);
		arg0.writeInt(tRev);
		arg0.writeInt(dRev);
		arg0.writeInt(nRev);
		

		for (int i = 0; i < aFor; i++) {
			arg0.writeByte(aForQ.get(i));
		}

		for (int i = 0; i < cFor; i++) {
			arg0.writeByte(cForQ.get(i));
		}

		for (int i = 0; i < gFor; i++) {
			arg0.writeByte(gForQ.get(i));
		}

		for (int i = 0; i < tFor; i++) {
			arg0.writeByte(tForQ.get(i));
		}
		
		for (int i = 0; i < dFor; i++) {
			arg0.writeByte(dForQ.get(i));
		}

		for (int i = 0; i < aRev; i++) {
			arg0.writeByte(aRevQ.get(i));
		}

		for (int i = 0; i < cRev; i++) {
			arg0.writeByte(cRevQ.get(i));
		}

		for (int i = 0; i < gRev; i++) {
			arg0.writeByte(gRevQ.get(i));
		}
		
		for (int i = 0; i < tRev; i++) {
			arg0.writeByte(tRevQ.get(i));
		}
		
		for (int i = 0; i < dRev; i++) {
			arg0.writeByte(dRevQ.get(i));
		}

	}
}