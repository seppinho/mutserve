# vc-toolbox

This repository includes three preprocessing steps for variant calling (vc). All have been implemented with Hadoop MapReduce and are included in the [mtDNA-Server](https://mtdna-server.uibk.ac.at). 

* Align: Aligning single-end and paired-end reads with BWA. It uses the JNI binding from [JBWA](https://github.com/lindenb/jbwa) for that.
* Sort: Sorting aligned reads and creating a BAM file
* Pileup: base-pair information at each chromosomal position including several methods (e.g. BAQ) and calculcates several metrics. I'll add information later on. 