[![Build Status](https://travis-ci.org/seppinho/mutation-server.svg?branch=master)](https://travis-ci.org/seppinho/mutation-server)
[![Twitter Follow](https://img.shields.io/twitter/follow/mtdnaserver.svg?style=social&label=Follow)](https://twitter.com/mtdnaserver)

Mutation-Server Core is a Java library to detect heteroplasmic and homoplasmic sites in mtDNA data. 
It is especially used within the cloud service [mtDNA-Server](https://mtdna-server.uibk.ac.at) based on [Cloudgene](https://github.com/genepi/cloudgene). For scalability, Mutation-Server Core is parallelized with Hadoop MapReduce but is also available as a standalone tool.

## mtDNA-Server
The recommended way to execute mtDNA-Server is described [here](https://github.com/seppinho/mtdna-server-workflow). It executes the mtDNA-Server workflow and is based on this library. 

## Standalone Tool
Execute the following steps to run Mutation-Server as a standalone tool: 

```
mkdir mutation-server
wget https://github.com/seppinho/mutation-server/releases/download/1.1.1/mutation-server-1.1.1.jar -O mutation-server/mutation-server-1.1.1.jar

-java -jar mutation-server-1.1.1.jar  analyse-local --input input-files  --reference input-files/rCRS.fasta --level 0.01 --outputRaw raw.txt --outputVar var.txt --baq true --baseQ 20 --mapQ 20 --alignQ 30 --indel true
```

## Citation
If you use this tool, please cite [this paper](http://nar.oxfordjournals.org/content/early/2016/04/15/nar.gkw247.full).

## Checkout and contribute
* git clone https://github.com/seppinho/mutation-server
* Import Maven project into your favourite IDE
* maven install
