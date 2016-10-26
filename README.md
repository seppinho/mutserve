# vc-toolbox

This repository includes necessary steps for a parallelized approach to detect low level variants, also called heteroplasmy in mtDNA context. We heavily used Hadoop MapReduce and are using [Cloudgene](http://cloudgene.uibk.ac.at) for setting up a web service. Please checkout [mtDNA-Server](https://mtdna-server.uibk.ac.at) to see everything in action.

## Included steps:

* Align: Aligning single-end and paired-end reads with BWA. It uses the JNI bindings from [JBWA](https://github.com/lindenb/jbwa) for that. 
* Sort: Sorting aligned reads and creating a BAM file. It uses the secondary sort mechanism of Hadoop. 
* Pileup: Calculating base-pair information at each chromosomal position. Several methods can be applied to each read (e.g. BAQ, trimming). It also calculates several metrics, which are included in the pileup file.
* Variant detection: Detect variants and low-level variants (also called heteroplasmy in context of mtDNA)

