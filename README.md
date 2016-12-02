# CNV Mutation Server

This repository includes all workflow steps to detect (a) variants in genomic CNV regions and (b) heteroplasmies in mitochondrial DNA. Users can specify fastq (SE or PE), BAM and CRAM files as an input and receive annotated variant files in return. To provide it as a service to everyone, [Cloudgene](http://cloudgene.uibk.ac.at) has been used. The underlying methods have been first applied to mitochondrial DNA ([see publication](http://nar.oxfordjournals.org/content/early/2016/04/15/nar.gkw247.full)).

## Included steps:

* Align: Aligning single-end and paired-end reads with BWA. It uses the JNI bindings from [JBWA](https://github.com/lindenb/jbwa). 
* Sort: Sorting aligned reads and creating a BAM file. It uses the secondary sort mechanism of Hadoop. 
* Pileup: Calculating base-pair information at each chromosomal position. It also applies several models, which are included in the pileup file.
* Variant detection: Detect variants and low-level variants
* Statistics: Generate sample statistics.
