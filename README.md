# vc-toolbox

This repository includes necessary steps for detecting low level variants, also called heteroplasmy in mtDNA context. We heavily used Hadoop MapReduce for parallelzing the steps and are using Cloudgene to provide it as a service to users. Please go to [mtDNA-Server](https://mtdna-server.uibk.ac.at) to see it in action. [Hadoop-BAM](https://github.com/HadoopGenomics/Hadoop-BAM) v 7.7.1 is heavily used.

* Align: Aligning single-end and paired-end reads with BWA. It uses the JNI bindings from [JBWA](https://github.com/lindenb/jbwa) for that. 
* Sort: Sorting aligned reads and creating a BAM file. It uses the secondary sort mechanism of Hadoop. 
* Pileup: Calculating base-pair information at each chromosomal position. Several methods can be applied to each read (e.g. BAQ, trimming). It also calculates several metrics, which are included in the pileup file.
* Variant detection: Detect variants and low-level variants (also called heteroplasmy in context of mtDNA)

