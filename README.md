[![Build Status](https://travis-ci.org/seppinho/mutation-server.svg?branch=master)](https://travis-ci.org/seppinho/mutation-server)
[![Twitter Follow](https://img.shields.io/twitter/follow/espadrine.svg?style=social&label=Follow)](https://twitter.com/mtdnaserver)

# Mutation Server (aka mtDNA-Server)

Mutation Server is a [cloud service](https://mtdna-server.uibk.ac.at) to detects variants and heteroplasmies in mitochondrial DNA in NGS data. 

## Citation

If you use the service, please cite [this paper](http://nar.oxfordjournals.org/content/early/2016/04/15/nar.gkw247.full).

## Run mtDNA-Server locally
* git clone https://github.com/seppinho/mutation-server
* maven install -Dmaven.test.skip=true
* Run the jar

```
java -jar target/cnv-mutation-server-1.0.jar  analyse-local --input <input-folder> --baq true --baseQ 20 --indel false --mapQ 20 --alignQ 30 --reference <rCRS fasta file> --level 0.01 --outputRaw raw.txt --outputVar var.txt
```

## Included workflow steps:

* Align: Aligning single-end and paired-end reads with BWA. It uses the JNI bindings from [JBWA](https://github.com/lindenb/jbwa). 
* Sort: Sorting aligned reads and creating a BAM file. It uses the secondary sort mechanism of Hadoop. 
* Pileup: Calculating base-pair information at each chromosomal position. It also applies several models, which are included in the pileup file. Output file includes variants and heteroplasmies.
* Statistics: Generate sample statistics.

## Checkout and contribute

* Check out repository  
* Import Maven project into Eclipse
* maven install
