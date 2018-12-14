[![Build Status](https://travis-ci.org/seppinho/mutation-server.svg?branch=master)](https://travis-ci.org/seppinho/mutation-server)
[![Twitter Follow](https://img.shields.io/twitter/follow/mtdnaserver.svg?style=social&label=Follow)](https://twitter.com/mtdnaserver)

Mutation-Server is a Java library to detect heteroplasmic and homoplasmic sites in mtDNA data. 
It has been integrated in [mtDNA-Server](https://mtdna-server.uibk.ac.at). For scalability, Mutation-Server is parallelized using Hadoop MapReduce but also available as a commandline tool.

## Standalone Usage
You can run Mutation-Server as a standalone tool starting with CRAM/BAM files and detecting heteroplasmic and homoplasmic sites. By default BAQ is set (``--noBaq`` otherwise) and no indels  (``--indel`` otherwise) are called. Please note for indels, that normalization or realignment is applied. 
```
wget https://github.com/seppinho/mutation-server/releases/download/v1.1.11/mutation-server-1.1.11.jar

java -jar mutation-server-1.1.11.jar  analyse-local --input <file/folder> --output <folder> --reference <fasta> --level 0.01 --writeVcf
```

### Default Parameters

| Parameter        | Value           | Command Line Option | 
| ------------- |:-------------:| :-------------:| 
| MappingQuality     | 20 | `--mapQ`|
| BaseQuality     | 20 | `--baseQ`|
| AlignmentQuality     | 30 | `--alignQ`|
| noBAQ     | false | `--noBAQ`|
| indel     | false | `--indel`|
| Write VCF     | false | `--writeVcf`|
| Write raw     | false | `--writeRaw`|


## Output Formats

### Tab delimited File
We also export a TAB-delimited file including *ID, Position, Reference, Variant & VariantLevel*. Please note that the *VariantLevel* always reports the non-reference variant level. The output file also includes the **most** and **second most base** at a specific position (MajorBase + MajorLevel, MinorBase+MinorLevel). The reported variant can be the major or the minor component. The last column includes the type of the variant (1: Homoplasmy, 2: Heteroplasmy or Low-Level Variant, 3: Low-Level Deletion, 4: Deletion, 5: Insertion). See [here](https://raw.githubusercontent.com/seppinho/mutation-server/master/test-data/results/variantsLocal1000G) for an example. 

### VCF
If you want a **VCF** file as an output, please add ``--writeVCF``. Heteroplasmies are coded as 1/0 genotypes, the heteroplasmy level is included in the FORMAT using the **AF** attribute (allele frequency) of the first non-reference allele. Please note that indels are currently not included in the VCF.  This VCF file can be used as an input for https://github.com/seppinho/haplogrep-cmd.

## Performance - Sensitivity and Specificity

If you have a mixture model generated, you can use mutation-server for checking precision, specificity and sensitivity. The expected mutations (homoplasmic and heteroplasmic) need to be provided as gold standard in form of a text file, with one column, containing the positions expected. The variant from *analyse-local* are used as input file and length needs to be specified (usually 16,569, but as there are different reference sequence, this can vary as well).
```
java -jar mutation-server-1.1.8.jar  performance --in <variantfile> --gold <expectedmutations> --length <size of reference>
```


## Citation
If you use this tool, please cite [this paper](http://nar.oxfordjournals.org/content/early/2016/04/15/nar.gkw247.full).

## Checkout and contribute
* git clone https://github.com/seppinho/mutation-server
* Import Maven project into your favourite IDE
* maven install
