package com.github.lindenb.jbwa.jni;
import java.io.File;
import java.io.IOException;

public class KSeq	
	{
	protected long ref=0L;
	public KSeq(File f) throws IOException
		{
		this.ref=KSeq.init(f==null?"-":f.toString());
		}
	
	public KSeq() throws IOException
		{
		this(null);
		}
	
	public native ShortRead next() throws IOException;
	
	@Override
	protected void finalize()
		{
		dispose();
		}
	
	public native void dispose();
	
	private static native long init(String file);
	}
