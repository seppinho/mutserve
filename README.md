[![Build Status](https://travis-ci.org/seppinho/mutation-server.svg?branch=master)](https://travis-ci.org/seppinho/mutation-server)

# Mutation Server (aka mtDNA-Server)

Mutation Server is a cloud service available [here](https://mtdna-server.uibk.ac.at). It detects variants and heteroplasmies in mitochondrial DNA. Users can specify FASTQ (Single-End or Paired-End), BAM and CRAM files as an input and receive annotated variants in return. The underlying methods have been applied to mitochondrial DNA ([see publication](http://nar.oxfordjournals.org/content/early/2016/04/15/nar.gkw247.full)).


## Run mtDNA-Server locally
* git clone https://github.com/seppinho/mutation-server
* maven install -Dmaven.test.skip=true

## Included workflow steps:

* Align: Aligning single-end and paired-end reads with BWA. It uses the JNI bindings from [JBWA](https://github.com/lindenb/jbwa). 
* Sort: Sorting aligned reads and creating a BAM file. It uses the secondary sort mechanism of Hadoop. 
* Pileup: Calculating base-pair information at each chromosomal position. It also applies several models, which are included in the pileup file. Output file includes variants and heteroplasmies.
* Statistics: Generate sample statistics.

## Checkout and contribute

* Check out repository  
* Import Maven project into Eclipse
* maven install