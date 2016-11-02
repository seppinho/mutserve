package genepi.vcbox.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.Writable;

public class BasePosition implements Writable {

	private double llrFWD;

	private double llrREV;
	
	private int pos = 0;
	
	private int aFor = 0;

	private int aRev = 0;

	private int cFor = 0;

	private int cRev = 0;

	private int gFor = 0;

	private int gRev = 0;

	private int tFor = 0;

	private int tRev = 0;

	private int dFor = 0;

	private int dRev = 0;

	private int nFor = 0;

	private int nRev = 0;

	private List<Byte> aForQ = new ArrayList<Byte>();
	private List<Byte> cForQ = new ArrayList<Byte>();
	private List<Byte> gForQ = new ArrayList<Byte>();
	private List<Byte> tForQ = new ArrayList<Byte>();

	private List<Byte> aRevQ = new ArrayList<Byte>();
	private List<Byte> cRevQ = new ArrayList<Byte>();
	private List<Byte> gRevQ = new ArrayList<Byte>();
	private List<Byte> tRevQ = new ArrayList<Byte>();

	public void add(BasePosition postion) {
		aFor += postion.aFor;
		cFor += postion.cFor;
		gFor += postion.gFor;
		tFor += postion.tFor;
		dFor += postion.dFor;
		nFor += postion.nFor;
		aRev += postion.aRev;
		cRev += postion.cRev;
		gRev += postion.gRev;
		tRev += postion.tRev;
		dRev += postion.dRev;
		nRev += postion.nRev;
	}

	public void addaFor(int aFor) {
		this.aFor += aFor;
	}

	public void addaForQ(byte quality) {

		this.aForQ.add(quality);
	}

	public void addaRev(int aRev) {
		this.aRev += aRev;
	}

	public void addaRevQ(byte quality) {

		this.aRevQ.add(quality);
	}

	public void addcFor(int cFor) {
		this.cFor += cFor;
	}

	public void addcForQ(byte quality) {

		this.cForQ.add(quality);
	}

	public void addcRev(int cRev) {
		this.cRev += cRev;
	}

	public void addcRevQ(byte quality) {

		this.cRevQ.add(quality);
	}

	public void addgFor(int gFor) {
		this.gFor += gFor;
	}

	public void addgForQ(byte quality) {

		this.gForQ.add(quality);
	}

	public void addgRev(int gRev) {
		this.gRev += gRev;
	}

	public void addgRevQ(byte quality) {

		this.gRevQ.add(quality);
	}

	public void addtFor(int tFor) {
		this.tFor += tFor;
	}

	public void addtForQ(byte quality) {

		this.tForQ.add(quality);
	}

	public void addtRev(int tRev) {
		this.tRev += tRev;
	}

	public void addtRevQ(byte quality) {

		this.tRevQ.add(quality);
	}

	public void adddFor(int dFor) {
		this.dFor += dFor;
	}

	public void adddRev(int dRev) {
		this.dRev += dRev;
	}

	public void addnFor(int nFor) {
		this.nFor += nFor;
	}

	public void addnRev(int nRev) {
		this.nRev += nRev;
	}

	public void clear() {

		aFor = 0;
		cFor = 0;
		gFor = 0;
		tFor = 0;
		dFor = 0;
		nFor = 0;
		aRev = 0;
		cRev = 0;
		gRev = 0;
		tRev = 0;
		dRev = 0;
		nRev = 0;
		aForQ = new ArrayList<>();
		cForQ = new ArrayList<>();
		gForQ = new ArrayList<>();
		tForQ = new ArrayList<>();
		aRevQ = new ArrayList<>();
		cRevQ = new ArrayList<>();
		gRevQ = new ArrayList<>();
		tRevQ = new ArrayList<>();
	}

	public int getaFor() {
		return aFor;
	}

	public int getaRev() {
		return aRev;
	}

	public int getcFor() {
		return cFor;
	}

	public int getcRev() {
		return cRev;
	}

	public int getgFor() {
		return gFor;
	}

	public int getdFor() {
		return dFor;
	}

	public int getgRev() {
		return gRev;
	}

	public int getnFor() {
		return nFor;
	}

	public int getnRev() {
		return nRev;
	}

	public int gettFor() {
		return tFor;
	}

	public int gettRev() {
		return tRev;
	}

	public int getdRev() {
		return dRev;
	}

	public void setaFor(int aFor) {
		this.aFor = aFor;
	}

	public void setaRev(int aRev) {
		this.aRev = aRev;
	}

	public void setcFor(int cFor) {
		this.cFor = cFor;
	}

	public void setcRev(int cRev) {
		this.cRev = cRev;
	}

	public void setgFor(int gFor) {
		this.gFor = gFor;
	}

	public void setdFor(int dFor) {
		this.dFor = dFor;
	}

	public void setgRev(int gRev) {
		this.gRev = gRev;
	}

	public void setnFor(int nFor) {
		this.nFor = nFor;
	}

	public void setnRev(int nRev) {
		this.nRev = nRev;
	}

	public void settFor(int tFor) {
		this.tFor = tFor;
	}

	public void settRev(int tRev) {
		this.tRev = tRev;
	}

	public void setdRev(int dRev) {
		this.dRev = dRev;
	}

	@Override
	public String toString() {

		return aFor + "\t" + cFor + "\t" + gFor + "\t" + tFor + "\t" + nFor
				+ "\t" + aRev + "\t" + cRev + "\t" + gRev + "\t" + tRev + "\t"
				+ nRev + "\t" + dFor + "\t" + dRev+ "\t" + llrFWD+ "\t" + llrREV;
	}

	@Override
	public void readFields(DataInput arg0) throws IOException {
		
		aFor = arg0.readInt();
		cFor = arg0.readInt();
		gFor = arg0.readInt();
		tFor = arg0.readInt();
		nFor = arg0.readInt();

		aRev = arg0.readInt();
		cRev = arg0.readInt();
		gRev = arg0.readInt();
		tRev = arg0.readInt();
		nRev = arg0.readInt();

		dFor = arg0.readInt();
		dRev = arg0.readInt();

		aForQ.clear();
		cForQ.clear();
		gForQ.clear();
		tForQ.clear();

		aRevQ.clear();
		cRevQ.clear();
		gRevQ.clear();
		tRevQ.clear();

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
	}

	@Override
	public void write(DataOutput arg0) throws IOException {

		arg0.writeInt(aFor);
		arg0.writeInt(cFor);
		arg0.writeInt(gFor);
		arg0.writeInt(tFor);
		arg0.writeInt(nFor);

		arg0.writeInt(aRev);
		arg0.writeInt(cRev);
		arg0.writeInt(gRev);
		arg0.writeInt(tRev);
		arg0.writeInt(nRev);

		arg0.writeInt(dFor);
		arg0.writeInt(dRev);

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

	}

	public List<Byte> getaForQ() {
		return aForQ;
	}

	public void setaForQ(List<Byte> aForQ) {
		this.aForQ = aForQ;
	}

	public List<Byte> getcForQ() {
		return cForQ;
	}

	public void setcForQ(List<Byte> cForQ) {
		this.cForQ = cForQ;
	}

	public List<Byte> getgForQ() {
		return gForQ;
	}

	public void setgForQ(List<Byte> gForQ) {
		this.gForQ = gForQ;
	}

	public List<Byte> gettForQ() {
		return tForQ;
	}

	public void settForQ(List<Byte> tForQ) {
		this.tForQ = tForQ;
	}

	public List<Byte> getaRevQ() {
		return aRevQ;
	}

	public void setaRevQ(List<Byte> aRevQ) {
		this.aRevQ = aRevQ;
	}

	public List<Byte> getcRevQ() {
		return cRevQ;
	}

	public void setcRevQ(List<Byte> cRevQ) {
		this.cRevQ = cRevQ;
	}

	public List<Byte> getgRevQ() {
		return gRevQ;
	}

	public void setgRevQ(List<Byte> gRevQ) {
		this.gRevQ = gRevQ;
	}

	public List<Byte> gettRevQ() {
		return tRevQ;
	}

	public void settRevQ(List<Byte> tRevQ) {
		this.tRevQ = tRevQ;
	}

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public double getLlrFWD() {
		return llrFWD;
	}

	public void setLlrFWD(double llrFWD) {
		this.llrFWD = llrFWD;
	}

	public double getLlrREV() {
		return llrREV;
	}

	public void setLlrREV(double llrREV) {
		this.llrREV = llrREV;
	}

}