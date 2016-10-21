package com.github.lindenb.jbwa.jni;
import java.io.File;
import java.io.IOException;

public class BwaIndex
	{
	protected long ref=0L;
	public BwaIndex(File index)  throws IOException
		{
		ref=_open(index.toString());
		}
	
	
	@Override
	protected void finalize()
		{
		close();
		}
	
	public native void close();
	
	
	private static native long _open(String s)   throws IOException;
	
	
	}
