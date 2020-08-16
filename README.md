# Mutserve

[![Build Status](https://travis-ci.org/seppinho/mutserve.svg?branch=master)](https://travis-ci.org/seppinho/mutserve)
[![Twitter Follow](https://img.shields.io/twitter/follow/mtdnaserver.svg?style=social&label=Follow)](https://twitter.com/mtdnaserver)

Mutserve is a variant caller for the mitochondrial genome to detect homoplasmic and heteroplasmic sites in sequence data. It has been integrated into [haplocheck](https://github.com/genepi/haplocheck) and [mtDNA-Server](https://mtdna-server.uibk.ac.at).

## Getting started
Mutserve requires sorted and indexed CRAM/BAM files as an input.
```
mkdir mutserve
cd mutserve
curl -sL mutserve.vercel.app | bash
./mutserve --reference rCRS.fasta --output s4.vcf.gz --threads 4 *.cram 
```

Please use [this reference file](https://raw.githubusercontent.com/seppinho/mutserve/master/files/rCRS.fasta) when using BAQ (disabled by default since v2.0.0).

### Parameters

| Parameter        | Default Value / Comment          | Command Line Option | 
| ------------- |:-------------:| :-------------:| 
| Input Files     | sorted and indexed BAM/CRAM files | |
| Output Name   | output file; supported: \*.txt, \*.vcf, \*vcf.gz | `--output` |
| Reference  | reference file | `--reference` |
| Threads     | 1 | `--threads`|
| Minimum Heteroplasmy Level     | 0.01 | `--level`|
| Define specific mtDNA contig in whole-genome file     | null | `--contig`|
| Output Fasta     | false | `--writeFasta`|
| Output Raw File     | false | `--writeRaw`|
| MappingQuality     | 20 | `--mapQ`|
| BaseQuality     | 20 | `--baseQ`|
| AlignmentQuality     | 30 | `--alignQ`|
| Enable Base Alignment Quality (BAQ)     | false | `--baq`|
| Disale 1000 Genomes Frequence File     | false | `--noFreq`|
| Call deletions (beta)     | false | `--deletions`|
| Call insertions (beta)     | false | `--insertions`|
| Disable ANSI output     |  | `--no-ansi`|
| Show version     |  | `--version`|
| Show help     |  | `--help`|

## Differences to mtDNA-Server

The previous version of mutserve has been integrated in [mtDNA-Server](https://mtdna-server.uibk.ac.at). For scalability reasons, mutserve is parallelized using Hadoop MapReduce but also available as a standalone tool.

- mutserve always reports the non-reference level as the heteroplasmy level, while mtDNA-Server reports the minor component.
- mutserve includes a Bayesian model for homoplasmy detection. It uses the 1000G Phase 3 data as a prior and calculates the most likely posterior probability for each genotype. mtDNA-Server only outputs homoplasmic variants with a coverage > 30.

### BAM Preperation
Best Practice Pipelines recommend the following steps for BAM files preperation:
- Remove Duplicates (*java -jar picard-tools-2.5.0/picard.jar MarkDuplicates*), 
- Local realignment around indels (*GenomeAnalysisTK.jar -T RealignerTargetCreator*, *java -jar GenomeAnalysisTK.jar -T IndelRealigner*) 
- BQSR (*GenomeAnalysisTK.jar -T BaseRecalibrator*).

## Output Formats

### Tab delimited File
By default (`--output filename` does not end with .vcf or .vcf.gz) we export a TAB-delimited file including *ID, Position, Reference, Variant & VariantLevel*. Please note that the *VariantLevel* always reports the non-reference variant level. The output file also includes the **most** and **second most base** at a specific position (MajorBase + MajorLevel, MinorBase+MinorLevel). The reported variant can be the major or the minor component. The last column includes the type of the variant (1: Homoplasmy, 2: Heteroplasmy or Low-Level Variant, 3: Low-Level Deletion, 4: Deletion, 5: Insertion). See [here](https://raw.githubusercontent.com/seppinho/mutation-server/master/test-data/results/variantsLocal1000G) for an example. 

### VCF
If you want a **VCF** file as an output, please specify `--output filename.vcf.gz`. Heteroplasmies are coded as 1/0 genotypes, the heteroplasmy level is included in the FORMAT using the **AF** attribute (allele frequency) of the first non-reference allele. Please note that indels are currently not included in the VCF.  This VCF file can be used as an input for https://github.com/seppinho/haplogrep-cmd.

## Current Shortcomings
* The **insertions/deletions calling** is currently in **beta**.

## Mixture-Module and Performance - Sensitivity and Specificity

As with v1.3.3 you can generate your gold standard given 2 variant files from the source files, which were used to generate mixtures in lab. With those files (call mutserve on the two samples) you can calculate the expected sites (parameter **generate-gold**) given a mixture ratio and subsequently compare the results from the lab-mixture to the this gold-standard with the **performance** parameter.  

### Generate Gold-Standard
Provide the text files from mutserve output of the two files (the txt-variant files from *analyse-local* are used as input files - file1 for the major component and file2 for the minor mixture component), as well as the level of the mixture (value between 0 and 1) and the output file - which is the resulting gold-standard and input file for the next step - performance calculation - see below:
```
java -jar mutserve-1.3.4.jar  generate-gold --file1 <variantfileMajorComponent.txt> --file2 <variantfileMinorComponent.txt> --level <mixture levels (e.g. 0.01 for 1%)> --output <expectedvariants.txt>
```


### Performance 
If you have a mixture model generated, you can use mutserve for checking precision, specificity and sensitivity. The expected variants (homoplasmic and heteroplasmic) need to be provided as gold standard in form of a text file, with one column, containing the positions expected (this can now be calculated -see previous step). The txt-variant file from *analyse-local* is used as input file and length needs to be specified (typically 16569 for human mitochondrial genomes, but as there are different reference sequence, this can vary as well). The value provided in *level* indicates the threshold at which heteroplasmic levels are considered in the analysis.
```
java -jar mutserve-1.3.4.jar  performance --in <variantfile.txt> --gold <expectedvariants.txt> --length <size of reference (e.g. 16569)> --level <threshold for heteroplasmic levels (e.g. 0.01)>
```

## Citation
If you use this tool, please cite [this paper](http://nar.oxfordjournals.org/content/early/2016/04/15/nar.gkw247.full).

## Checkout and contribute
* git clone https://github.com/seppinho/mutserve
* Import Maven project into your favourite IDE
* maven install
