[![Build Status](https://travis-ci.org/seppinho/mutserve.svg?branch=master)](https://travis-ci.org/seppinho/mutserve)
[![Twitter Follow](https://img.shields.io/twitter/follow/mtdnaserver.svg?style=social&label=Follow)](https://twitter.com/mtdnaserver)

Mutserve is a library to detect heteroplasmic and homoplasmic sites in mtDNA NGS data. 
It has been integrated in [mtDNA-Server](https://mtdna-server.uibk.ac.at). For scalability reasons, mutserve is parallelized using Hadoop MapReduce but also available as a standalone tool.

## Differences to mtDNA-Server

- mutserve always reports the non-reference level as the heteroplasmy level, while mtDNA-Server reports the minor component.
- mutserve includes a Bayesian model for homoplasmy detection. It uses the 1000G Phase 3 data as a prior and calculates the most likely posterior probability for each genotype. mtDNA-Server only outputs homoplasmic variants with a coverage > 30.


## Standalone Usage
You can run mutserve as a standalone tool starting with CRAM/BAM files and detecting heteroplasmic and homoplasmic sites. By default BAQ is set (``--noBaq`` otherwise).

```
wget https://github.com/seppinho/mutserve/releases/download/v1.3.0/mutserve-1.3.0.jar

java -jar mutserve-1.3.0.jar  analyse-local --input <file/folder> --output <filename.vcf / filename.txt> --reference <fasta> --level 0.01
```
To create a VCF file as an output simple specify `--output filename.vcf.gz`. Please use [this reference file](https://raw.githubusercontent.com/seppinho/mutserve/master/files/rCRS.fasta) when using BAQ.

### Default Parameters

| Parameter        | Default Value           | Command Line Option | 
| ------------- |:-------------:| :-------------:| 
| InputFolder     | <folder> | `--input`|
| Output File   | <filename> (supported: *.txt, *.vcf, *vcf.gz) | `--output` |
| Output Fasta     |  | `--writeFasta`|
| Heteroplasmy Level     | 0.01 | `--level`|
| MappingQuality     | 20 | `--mapQ`|
| BaseQuality     | 20 | `--baseQ`|
| AlignmentQuality     | 30 | `--alignQ`|
| noBaq     | false | `--noBaq`|
| deletions (beta)     | false | `--deletions`|
| insertions (beta)     | false | `--insertions`|


## Output Formats

### Tab delimited File
By default (`--output filename` does not end with .vcf or .vcf.gz) we export a TAB-delimited file including *ID, Position, Reference, Variant & VariantLevel*. Please note that the *VariantLevel* always reports the non-reference variant level. The output file also includes the **most** and **second most base** at a specific position (MajorBase + MajorLevel, MinorBase+MinorLevel). The reported variant can be the major or the minor component. The last column includes the type of the variant (1: Homoplasmy, 2: Heteroplasmy or Low-Level Variant, 3: Low-Level Deletion, 4: Deletion, 5: Insertion). See [here](https://raw.githubusercontent.com/seppinho/mutation-server/master/test-data/results/variantsLocal1000G) for an example. 

### VCF
If you want a **VCF** file as an output, please specify `--output filename.vcf.gz`. Heteroplasmies are coded as 1/0 genotypes, the heteroplasmy level is included in the FORMAT using the **AF** attribute (allele frequency) of the first non-reference allele. Please note that indels are currently not included in the VCF.  This VCF file can be used as an input for https://github.com/seppinho/haplogrep-cmd.

## Current Shortcomings
* The **insertions/deletions calling** is currently in **beta**, there is currently **no** normalization or realignment applied for indel positions. 

## Performance - Sensitivity and Specificity

If you have a mixture model generated, you can use mutserve for checking precision, specificity and sensitivity. The expected mutations (homoplasmic and heteroplasmic) need to be provided as gold standard in form of a text file, with one column, containing the positions expected. The variant from *analyse-local* are used as input file and length needs to be specified (usually 16,569 for human mitochondrial genomes, but as there are different reference sequence, this can vary as well). The value provided in *level* indicates the threshold for heteroplasmic levels to be considered in the analysis.
```
java -jar mutserve-1.2.2.jar  performance --in <variantfile> --gold <expectedmutations> --length <size of reference> --level <threshold for heteroplasmic levels>
```

## Citation
If you use this tool, please cite [this paper](http://nar.oxfordjournals.org/content/early/2016/04/15/nar.gkw247.full).

## Checkout and contribute
* git clone https://github.com/seppinho/mutserve
* Import Maven project into your favourite IDE
* maven install
