# vc-toolbox

This repository includes three pre-processing steps for variant calling (vc). All steps have been implemented with Hadoop MapReduce and are included in the [mtDNA-Server](https://mtdna-server.uibk.ac.at). [Hadoop-BAM)] https://github.com/HadoopGenomics/Hadoop-BAM) v 7.7.1 is used for all steps.

* Align: Aligning single-end and paired-end reads with BWA. It uses the JNI bindings from [JBWA](https://github.com/lindenb/jbwa) for that. 
* Sort: Sorting aligned reads and creating a BAM file. Use the secondary sort mechanism of Hadoop. 
* Pileup: base-pair information at each chromosomal position including several methods (e.g. BAQ). It calculates several metrics, I'll add information later on. 
