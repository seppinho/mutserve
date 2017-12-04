package genepi.mut.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public class BasePositionHadoop implements Writable {

	private BasePosition basePosition = new BasePosition();
	
	@Override
	public void readFields(DataInput arg0) throws IOException {
		
		
		basePosition.aFor = arg0.readInt();
		basePosition.cFor = arg0.readInt();
		basePosition.gFor = arg0.readInt();
		basePosition.tFor = arg0.readInt();
		basePosition.dFor = arg0.readInt();
		basePosition.nFor = arg0.readInt();

		basePosition.aRev = arg0.readInt();
		basePosition.cRev = arg0.readInt();
		basePosition.gRev = arg0.readInt();
		basePosition.tRev = arg0.readInt();
		basePosition.dRev = arg0.readInt();
		basePosition.nRev = arg0.readInt();

		basePosition.aForQ.clear();
		basePosition.cForQ.clear();
		basePosition.gForQ.clear();
		basePosition.tForQ.clear();
		basePosition.dForQ.clear();

		basePosition.aRevQ.clear();
		basePosition.cRevQ.clear();
		basePosition.gRevQ.clear();
		basePosition.tRevQ.clear();
		basePosition.dRevQ.clear();

		for (int i = 0; i < basePosition.aFor; i++) {
			basePosition.aForQ.add(arg0.readByte());
		}
		for (int i = 0; i < basePosition.cFor; i++) {
			basePosition.cForQ.add(arg0.readByte());
		}
		for (int i = 0; i < basePosition.gFor; i++) {
			basePosition.gForQ.add(arg0.readByte());
		}
		for (int i = 0; i < basePosition.tFor; i++) {
			basePosition.tForQ.add(arg0.readByte());
		}
		for (int i = 0; i < basePosition.dFor; i++) {
			basePosition.dForQ.add(arg0.readByte());
		}
		for (int i = 0; i < basePosition.aRev; i++) {
			basePosition.aRevQ.add(arg0.readByte());
		}
		for (int i = 0; i < basePosition.cRev; i++) {
			basePosition.cRevQ.add(arg0.readByte());
		}
		for (int i = 0; i < basePosition.gRev; i++) {
			basePosition.gRevQ.add(arg0.readByte());
		}
		for (int i = 0; i < basePosition.tRev; i++) {
			basePosition.tRevQ.add(arg0.readByte());
		}
		for (int i = 0; i < basePosition.dRev; i++) {
			basePosition.dRevQ.add(arg0.readByte());
		}
	}

	@Override
	public void write(DataOutput arg0) throws IOException {

		arg0.writeInt(basePosition.aFor);
		arg0.writeInt(basePosition.cFor);
		arg0.writeInt(basePosition.gFor);
		arg0.writeInt(basePosition.tFor);
		arg0.writeInt(basePosition.dFor);
		arg0.writeInt(basePosition.nFor);

		arg0.writeInt(basePosition.aRev);
		arg0.writeInt(basePosition.cRev);
		arg0.writeInt(basePosition.gRev);
		arg0.writeInt(basePosition.tRev);
		arg0.writeInt(basePosition.dRev);
		arg0.writeInt(basePosition.nRev);
		

		for (int i = 0; i < basePosition.aFor; i++) {
			arg0.writeByte(basePosition.aForQ.get(i));
		}

		for (int i = 0; i < basePosition.cFor; i++) {
			arg0.writeByte(basePosition.cForQ.get(i));
		}

		for (int i = 0; i < basePosition.gFor; i++) {
			arg0.writeByte(basePosition.gForQ.get(i));
		}

		for (int i = 0; i < basePosition.tFor; i++) {
			arg0.writeByte(basePosition.tForQ.get(i));
		}
		
		for (int i = 0; i < basePosition.dFor; i++) {
			arg0.writeByte(basePosition.dForQ.get(i));
		}

		for (int i = 0; i < basePosition.aRev; i++) {
			arg0.writeByte(basePosition.aRevQ.get(i));
		}

		for (int i = 0; i < basePosition.cRev; i++) {
			arg0.writeByte(basePosition.cRevQ.get(i));
		}

		for (int i = 0; i < basePosition.gRev; i++) {
			arg0.writeByte(basePosition.gRevQ.get(i));
		}
		
		for (int i = 0; i < basePosition.tRev; i++) {
			arg0.writeByte(basePosition.tRevQ.get(i));
		}
		
		for (int i = 0; i < basePosition.dRev; i++) {
			arg0.writeByte(basePosition.dRevQ.get(i));
		}

	}
	
	public void setBasePosition(BasePosition basePosition) {
		this.basePosition = basePosition;
	}
	
	public BasePosition getBasePosition() {
		return basePosition;
	}
}