package com.github.lindenb.jbwa.jni;
import java.io.*;
import java.util.List;

public class BwaMem	
	{
	protected long ref=0L;
	private BwaIndex bwaIndex=null;
	public BwaMem(BwaIndex bwaIndex)
		{
		this.ref=BwaMem.mem_opt_init();
		this.bwaIndex=bwaIndex;
		}
	
	public AlnRgn[] align(ShortRead read) throws IOException
		{
		if(ref==0L) return null;
		return align(this.bwaIndex,read.getBases());
		}
	
	public String[] align(final List<ShortRead> ks1,final List<ShortRead> ks2) throws IOException
		{
		if(ref==0L) return null;
		if(ks1==null) throw new IllegalArgumentException("ks1 is null");
		if(ks2==null) throw new IllegalArgumentException("ks2 is null");
		return align(
			ks1.toArray(new ShortRead[ks1.size()]),
			ks2.toArray(new ShortRead[ks2.size()])
			);
		}
	
	public String[] align(final ShortRead ks1[],final ShortRead ks2[]) throws IOException
		{
		if(ref==0L) return null;
		if(ks1==null) throw new IllegalArgumentException("ks1 is null");
		if(ks2==null) throw new IllegalArgumentException("ks2 is null");
		if(ks1.length!=ks2.length) throw new IllegalArgumentException("ks1.length!=ks2.length");
		if(ks1.length==0) return null;
		return align2(this.bwaIndex,ks1,ks2);
		}
	
	
	@Override
	protected void finalize()
		{
		dispose();
		}
	
	public native void dispose();
	
	private static native long mem_opt_init();
	private native AlnRgn[] align(BwaIndex bwaIndex,byte bases[])  throws IOException;
	private native String[] align2(BwaIndex bwaIndex,final ShortRead ks1[],final ShortRead ks2[])  throws IOException;
	}
