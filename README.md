# vc-toolbox

This repository includes three pre-processing steps for variant calling (vc). All steps have been implemented with Hadoop MapReduce and are included in the [mtDNA-Server](https://mtdna-server.uibk.ac.at). [Hadoop-BAM](https://github.com/HadoopGenomics/Hadoop-BAM) v 7.7.1 is used for all steps.

* Align: Aligning single-end and paired-end reads with BWA. It uses the JNI bindings from [JBWA](https://github.com/lindenb/jbwa) for that. 
* Sort: Sorting aligned reads and creating a BAM file. It uses the secondary sort mechanism of Hadoop. 
* Pileup: Calculating base-pair information at each chromosomal position. Several methods can be applied to each read (e.g. BAQ, trimming). It also calculates several metrics, which are included in the pileup file.
