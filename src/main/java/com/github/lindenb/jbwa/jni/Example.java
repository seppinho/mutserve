package com.github.lindenb.jbwa.jni;
import java.io.File;
import java.io.IOException;

public class Example	
	{
	public static void main(String args[]) throws IOException
		{
		System.loadLibrary("bwajni");
		if(args.length==0)
			{
			System.out.println("Usage [ref.fa] (stdin|fastq)\n");
			return;	
			}

		BwaIndex index=new BwaIndex(new File(args[0]));
		BwaMem mem=new BwaMem(index);
		KSeq kseq=new KSeq(args.length<2 || args[1].equals("-")?null:new File(args[1]));

		ShortRead read=null;
		while((read=kseq.next())!=null)
			{
			for(AlnRgn a: mem.align(read))
				{
				if(a.getSecondary()>=0) continue;
				System.out.println(
					read.getName()+"\t"+
					a.getStrand()+"\t"+
					a.getChrom()+"\t"+
					a.getPos()+"\t"+
					a.getMQual()+"\t"+
					a.getCigar()+"\t"+
					a.getNm()
					);
				}
			}
		kseq.dispose();
		index.close();
		mem.dispose();
		}
	}

