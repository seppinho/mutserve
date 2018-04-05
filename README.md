[![Build Status](https://travis-ci.org/seppinho/mutation-server.svg?branch=master)](https://travis-ci.org/seppinho/mutation-server)
[![Twitter Follow](https://img.shields.io/twitter/follow/mtdnaserver.svg?style=social&label=Follow)](https://twitter.com/mtdnaserver)

# Mutation-Server

Mutation-Server is used within mtDNA-Server, an online [cloud service](https://mtdna-server.uibk.ac.at) to detect variants and heteroplasmies in mitochondrial NGS DNA. For scalability, the tool is parallelized with Hadoop MapReduce. 

## Run mtDNA-Server locally

We also provide a [local workflow](https://github.com/seppinho/mtdna-server-workflow) of mtDNA-Server without the need to transfer data to our service (BAM input only). 

## Citation

If you use this tool, please cite [this paper](http://nar.oxfordjournals.org/content/early/2016/04/15/nar.gkw247.full).

## Checkout and contribute
* git clone https://github.com/seppinho/mutation-server
* Import Maven project into your favourite IDE
* maven install
