package genepi.cnv.pileup;

import java.io.File;
import java.io.IOException;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;

public class ReadBamTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		 final SamReader reader = SamReaderFactory.makeDefault().open(new File("test-data/mtdna/bam/test.bam"));
		 
	   SAMRecordIterator d = reader.iterator();
	   
	   while(d.hasNext()){
		   SAMRecord record = d.next();
		   System.out.println(record.getSAMString());
	   }
	   
	   try {
		reader.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}

}
