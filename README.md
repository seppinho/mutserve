[![Build Status](https://travis-ci.org/seppinho/mutation-server.svg?branch=master)](https://travis-ci.org/seppinho/mutation-server)
[![Twitter Follow](https://img.shields.io/twitter/follow/mtdnaserver.svg?style=social&label=Follow)](https://twitter.com/mtdnaserver)

# CNV Mutation Server (aka mtDNA-Server)

CNV Mutation Server is an online [cloud service](https://mtdna-server.uibk.ac.at) to detect variants and heteroplasmies in mitochondrial NGS DNA. For scalability, the tool is parallelized with Hadoop MapReduce. 

## Run mtDNA-Server locally

For local execution, only BAM files are currently accepted. Note: For read mapping, please use a circular mapper  (e.g. [bwa mem](https://github.com/lh3/bwa)). 

### Download Tool
```
mkdir mutation-server
wget https://github.com/seppinho/mutation-server/releases/download/1.0/cnv-mutation-server-1.0.jar -O mutation-server/cnv-mutation-server-1.0.jar
```
### Download Test Data

```
mkdir mutation-server/input-files
wget https://mtdna-server.uibk.ac.at/static/bam/rCRS.fasta -O mutation-server/input-files/rCRS.fasta
wget https://mtdna-server.uibk.ac.at/static/bam/HG00096.mapped.ILLUMINA.bwa.GBR.low_coverage.20101123.bam  -O mutation-server/input-files/HG00096.mapped.ILLUMINA.bwa.GBR.low_coverage.20101123.bam
```
### Run Tool
```
cd mutation-server
java -jar cnv-mutation-server-1.0.jar  analyse-local --input input-files  --reference input-files/rCRS.fasta --level 0.01 --outputRaw raw.txt --outputVar var.txt --baq true --baseQ 20 --mapQ 20 --alignQ 30 --indel false
```
The indel feature is currently in beta. 

## Citation

If you use this tool, please cite [this paper](http://nar.oxfordjournals.org/content/early/2016/04/15/nar.gkw247.full).

## Included workflow steps:

The following steps are available online. For local execution, only the Pileup step is executed:

* Align: Aligning single-end and paired-end reads with BWA. It uses the JNI bindings from [JBWA](https://github.com/lindenb/jbwa). 
* Sort: Sorting aligned reads and creating a BAM file. It uses the secondary sort mechanism of Hadoop. 
* Pileup: Calculating base-pair information at each chromosomal position. It also applies several models, which are included in the pileup file. Output file includes variants and heteroplasmies.
* Statistics: Generate sample statistics.

## Checkout and contribute
* git clone https://github.com/seppinho/mutation-server
* Import Maven project into your favourite IDE
* maven install
