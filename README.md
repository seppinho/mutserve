[![Build Status](https://travis-ci.org/seppinho/mutation-server.svg?branch=master)](https://travis-ci.org/seppinho/mutation-server)
[![Twitter Follow](https://img.shields.io/twitter/follow/mtdnaserver.svg?style=social&label=Follow)](https://twitter.com/mtdnaserver)

Mutation-Server Core is a Java library to detect heteroplasmic and homoplasmic sites in mtDNA data. 
It is especially used for [mtDNA-Server](https://mtdna-server.uibk.ac.at). For scalability, Mutation-Server Core is parallelized with Hadoop MapReduce but is also available as a standalone tool.

## mtDNA-Server Usage
mtDNA-Server uses this library to detect heteroplasmic and homoplasmic sites. Please have a look [here](https://github.com/seppinho/mtdna-server-workflow) to run the complete workflow including also several other workflow steps like contamination detection or report creation.

## Standalone Usage
You can also run Mutation-Server Core as a standalone tool starting with BAM files. 
```
wget https://github.com/seppinho/mutation-server/releases/download/v1.1.5/mutation-server-1.1.5.jar

java -jar mutation-server-1.1.5.jar  analyse-local --input <input-folder> --reference <ref.fasta> --level 0.01 --outputRaw raw.txt --outputVar var.txt --baq true --baseQ 20 --mapQ 20 --alignQ 30 --indel true
```

## Output Format
We report variants in a TAB-delimited file including *SampleID, Position, Reference, Variant & Variant-Level*. Please note that the *Variant-Level* always reports the non-reference variant level. The output file also includes the **most** and **second most base** at a specific position (so called Major/Minor Component). The reported variant can be the major or the minor component. The last column includes the type of the variant (1: Homoplasmy, 2: Heteroplasmy or Low-Level Variant, 3: Low-Level Deletion, 4: Deletion, 5: Insertion). See [here](https://raw.githubusercontent.com/seppinho/mutation-server/master/test-data/results/variantsLocal1000G) for an example. 

## Citation
If you use this tool, please cite [this paper](http://nar.oxfordjournals.org/content/early/2016/04/15/nar.gkw247.full).

## Checkout and contribute
* git clone https://github.com/seppinho/mutation-server
* Import Maven project into your favourite IDE
* maven install
