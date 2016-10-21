package com.github.lindenb.jbwa.jni;

public class ShortRead	
	{
	protected String name;
	protected byte[] seq;
	protected byte[] qual;
	
	public ShortRead(String name,byte[] seq,byte[] qual)
		{
		this.name=name;
		this.seq=seq;
		this.qual=qual;
		}
	
	public String getName()
		{
		return this.name;
		}
	
	public byte[] getBases()
		{
		return this.seq;
		}
		
	public byte[] getQualities()
		{
		return this.qual;
		}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setBases(byte[] seq) {
		this.seq = seq;
	}

	public void setQual(byte[] qual) {
		this.qual = qual;
	}
	
	@Override
	public String toString()
		{
	//	return "@"+name+"\n"+new String(this.seq)+"\n+\n"+new String(qual);
		return "@"+name+"\t"+new String(this.seq)+"\t"+new String(qual);
		}
	}
