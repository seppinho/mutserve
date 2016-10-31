# vc-toolbox

A parallelized approach to detect low level variants, also called heteroplasmies in mtDNA context. For parallelization, Hadoop MapReduce has been used. For generating a SaaS, we used [Cloudgene](http://cloudgene.uibk.ac.at). Please checkout [mtDNA-Server](https://mtdna-server.uibk.ac.at) to see all components in action. Publication in Nucleid Acids Research can be found [here](http://nar.oxfordjournals.org/content/early/2016/04/15/nar.gkw247.full).

## Included steps:

* Align: Aligning single-end and paired-end reads with BWA. It uses the JNI bindings from [JBWA](https://github.com/lindenb/jbwa). 
* Sort: Sorting aligned reads and creating a BAM file. It uses the secondary sort mechanism of Hadoop. 
* Pileup: Calculating base-pair information at each chromosomal position. It also applies several models, which are included in the pileup file.
* Variant detection: Detect variants and low-level variants (also called heteroplasmy in context of mtDNA).
* Statistics: Generate sample statistics.
