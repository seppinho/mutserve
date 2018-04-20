[![Build Status](https://travis-ci.org/seppinho/mutation-server.svg?branch=master)](https://travis-ci.org/seppinho/mutation-server)
[![Twitter Follow](https://img.shields.io/twitter/follow/mtdnaserver.svg?style=social&label=Follow)](https://twitter.com/mtdnaserver)

# Mutation-Server
Mutation-Server is a Java tool to detect heteroplasmic and homoplasmic sites in mtDNA data. 
It is especially used within the cloud service [mtDNA-Server](https://mtdna-server.uibk.ac.at) based on [Cloudgene](https://github.com/genepi/cloudgene). For scalability, Mutation-Server is parallelized with Hadoop MapReduce but is also available as a standalone tool (see below). 

## Getting Started
Execute the following steps to run Mutation-Server as a standalone tool. Please checkout [this repository](https://github.com/seppinho/mtdna-server-workflow) to execute the complete mtDNA-Server workflow (including Mutation-Server). 

### Get Mutation-Server
```
mkdir mutation-server
wget https://github.com/seppinho/mutation-server/releases/download/1.1.1/mutation-server-1.1.1.jar -O mutation-server/mutation-server-1.1.1.jar
```
### Download Test Data
For this scenario we're downloading mtDNA NGS data.
```
mkdir mutation-server/input-files
wget https://mtdna-server.uibk.ac.at/static/bam/rCRS.fasta -O mutation-server/input-files/rCRS.fasta
wget https://mtdna-server.uibk.ac.at/static/bam/HG00096.mapped.ILLUMINA.bwa.GBR.low_coverage.20101123.bam  -O mutation-server/input-files/HG00096.mapped.ILLUMINA.bwa.GBR.low_coverage.20101123.bam
```
### Run Mutation-Server
```
cd mutation-server
java -jar mutation-server-1.1.1.jar  analyse-local --input input-files  --reference input-files/rCRS.fasta --level 0.01 --outputRaw raw.txt --outputVar var.txt --baq true --baseQ 20 --mapQ 20 --alignQ 30 --indel true
```
The `--indel` feature currently checks for deletions only. 

## Current Limitations
- Homoplasmic model does not work for coverage < 30
- Insertions are currently not included in the output

## Citation

If you use this tool, please cite [this paper](http://nar.oxfordjournals.org/content/early/2016/04/15/nar.gkw247.full).

## Checkout and contribute
* git clone https://github.com/seppinho/mutation-server
* Import Maven project into your favourite IDE
* maven install
