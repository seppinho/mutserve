[![Build Status](https://travis-ci.org/seppinho/mutation-server.svg?branch=master)](https://travis-ci.org/seppinho/mutation-server)
[![Twitter Follow](https://img.shields.io/twitter/follow/mtdnaserver.svg?style=social&label=Follow)](https://twitter.com/mtdnaserver)

Mutation-Server Core is a Java library to detect heteroplasmic and homoplasmic sites in mtDNA data. 
It is especially used for [mtDNA-Server](https://mtdna-server.uibk.ac.at). For scalability, Mutation-Server Core is parallelized with Hadoop MapReduce but is also available as a standalone tool.

## mtDNA-Server Usage
mtDNA-Server uses this library to detect heteroplasmic and homoplasmic sites. Please have a look [here](https://github.com/seppinho/mtdna-server-workflow) to run the complete workflow including also several other workflow steps like contamination detection or report creation.

## Standalone Usage
You can also run Mutation-Server Core as a standalone tool starting with BAM files. 
```
mkdir mutation-server

wget https://github.com/seppinho/mutation-server/releases/download/v1.1.4/mutation-server-1.1.4.jar

java -jar mutation-server-1.1.4.jar  analyse-local --input <input-folder> --reference <ref.fasta> --level 0.01 --outputRaw raw.txt --outputVar var.txt --baq true --baseQ 20 --mapQ 20 --alignQ 30 --indel true
```

## Citation
If you use this tool, please cite [this paper](http://nar.oxfordjournals.org/content/early/2016/04/15/nar.gkw247.full).

## Checkout and contribute
* git clone https://github.com/seppinho/mutation-server
* Import Maven project into your favourite IDE
* maven install
