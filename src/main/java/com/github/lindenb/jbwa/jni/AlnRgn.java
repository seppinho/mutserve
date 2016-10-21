package com.github.lindenb.jbwa.jni;


public class AlnRgn	
	{
	private String chrom;
	private long pos;
	private byte strand;
	private String cigar;
	private int mqual;
	private int NM;
	private int secondary;
	public AlnRgn(String chrom,long pos,byte strand,String cigar,int mqual,int NM,int secondary)
		{
		this.chrom=chrom;
		this.pos=pos;
		this.strand=strand;
		this.cigar=cigar;
		this.mqual=mqual;
		this.NM=NM;
		this.secondary=secondary;
		}
	
	public String getChrom() { return this.chrom;}
	public long getPos() { return this.pos;}
	public char getStrand() { return (char)this.strand;}
	public String getCigar() { return this.cigar;}	
	public int getMQual() { return this.mqual;}	
	public int getNm() { return this.NM;}	
	public int getSecondary() { return this.secondary;}
	
	@Override
	public String toString()
		{
		return ""+chrom+":"+String.valueOf(pos)+"("+(char)this.strand+");"+cigar+";"+mqual+";"+NM+";"+getSecondary();
		}
	}
