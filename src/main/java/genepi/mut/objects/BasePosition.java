package genepi.mut.objects;

import java.util.ArrayList;
import java.util.List;

public class BasePosition {

	protected double llrFWD;

	protected double llrREV;
	
	protected String id;
	
	protected int pos = 0;
	
	protected int aFor = 0;

	protected int aRev = 0;

	protected int cFor = 0;

	protected int cRev = 0;

	protected int gFor = 0;

	protected int gRev = 0;

	protected int tFor = 0;

	protected int tRev = 0;

	protected int dFor = 0;

	protected int dRev = 0;

	protected int nFor = 0;

	protected int nRev = 0;

	protected List<Byte> aForQ = new ArrayList<Byte>();
	protected List<Byte> cForQ = new ArrayList<Byte>();
	protected List<Byte> gForQ = new ArrayList<Byte>();
	protected List<Byte> tForQ = new ArrayList<Byte>();
	protected List<Byte> dForQ = new ArrayList<Byte>();
	
	protected List<Byte> aRevQ = new ArrayList<Byte>();
	protected List<Byte> cRevQ = new ArrayList<Byte>();
	protected List<Byte> gRevQ = new ArrayList<Byte>();
	protected List<Byte> tRevQ = new ArrayList<Byte>();
	protected List<Byte> dRevQ = new ArrayList<Byte>();

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
	
	public void adddForQ(byte quality) {

		this.dForQ.add(quality);
	}

	public void adddRev(int dRev) {
		this.dRev += dRev;
	}
	
	public void adddRevQ(byte quality) {

		this.dRevQ.add(quality);
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
		dForQ = new ArrayList<>();
		
		aRevQ = new ArrayList<>();
		cRevQ = new ArrayList<>();
		gRevQ = new ArrayList<>();
		tRevQ = new ArrayList<>();
		dRevQ = new ArrayList<>();
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
	
	public List<Byte> getdForQ() {
		return dForQ;
	}

	public void setdForQ(List<Byte> dForQ) {
		this.dForQ = dForQ;
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
	
	public List<Byte> getdRevQ() {
		return dRevQ;
	}

	public void setdRevQ(List<Byte> dRevQ) {
		this.dRevQ = dRevQ;
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}