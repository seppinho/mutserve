# Mutserve2

![Mutserve Tests](https://github.com/seppinho/mutserve/actions/workflows/run-tests.yml/badge.svg)

Mutserve2 is a variant caller for the mitochondrial genome to detect homoplasmic and heteroplasmic sites in sequence data. 

We recommend to use our [Nextflow DSL2 pipeline for mtDNA-Server 2](https://github.com/genepi/mtdna-server-2) which includes mutserve2. 

## Standalone Quick Start
Mutserve requires sorted and indexed CRAM/BAM files as an input.

```
curl -sL mutserve.vercel.app | bash
./mutserve
```

## Documentation

### Quick Start
Mutserve requires sorted and indexed CRAM/BAM files as an input.

```
curl -sL mutserve.vercel.app | bash
./mutserve
```

### Available Tools
Currently two tools are available. 

* **call**: Variant Calling of homoplasmic and heteroplasmic positions. 
* **annotate**:  Annotation of mutserve variants (generated with `mutserve call`). 

### Mutserve Variant Calling

```
wget https://github.com/seppinho/mutserve/raw/master/test-data/mtdna/bam/input/HG00096.mapped.ILLUMINA.bwa.GBR.low_coverage.20101123.bam
curl -sL mutserve.vercel.app | bash
./mutserve call --reference rCRS.fasta --output HG00096.vcf.gz --threads 4 *.bam 
```

Please use [this reference file](https://raw.githubusercontent.com/seppinho/mutserve/master/files/rCRS.fasta) when using BAQ (disabled by default since v2.0.0).

### Mutserve Annotation

Mutserve allows to annotate the variant file (.txt) with a predefined [annotation file](https://raw.githubusercontent.com/seppinho/mutserve/master/files/rCRS_annotation_2020-08-20.txt) 

```
./mutserve annotate --input variantfile.txt --annotation rCRS_annotation_2020-08-20.txt --output AnnotatedVariants.txt
```

### Parameters

| Parameter        | Default Value / Comment          | Command Line Option | 
| ------------- |:-------------:| :-------------:| 
| Input Files     | sorted and indexed BAM/CRAM files | |
| Output Name   | output file; supported: \*.txt, \*.vcf, \*vcf.gz | `--output` |
| Reference  | reference file | `--reference` |
| Threads     | 1 | `--threads`|
| Minimum Heteroplasmy Level     | 0.01 | `--level`|
| Define specific mtDNA contig in whole-genome file     | null | `--contig-name`|
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

## Output Formats

### Tab delimited File
By default (`--output filename` does not end with .vcf or .vcf.gz) we export a TAB-delimited file including *ID, Position, Reference, Variant & VariantLevel*. Please note that the *VariantLevel* always reports the non-reference variant level. The output file also includes the **most** and **second most base** at a specific position (MajorBase + MajorLevel, MinorBase+MinorLevel). The reported variant can be the major or the minor component. The last column includes the type of the variant (1: Homoplasmy, 2: Heteroplasmy or Low-Level Variant, 3: Low-Level Deletion, 4: Deletion, 5: Insertion). See [here](https://raw.githubusercontent.com/seppinho/mutation-server/master/test-data/results/variantsLocal1000G) for an example. 

### VCF
If you want a **VCF** file as an output, please specify `--output filename.vcf.gz`. Heteroplasmies are coded as 1/0 genotypes, the heteroplasmy level is included in the FORMAT using the **AF** attribute (allele frequency) of the first non-reference allele. Please note that indels are currently not included in the VCF.  This VCF file can be used as an input for https://github.com/seppinho/haplogrep-cmd.

## Limitations
The focus of mutserve is currenly on SNP calling and not on indels. Please checkout [mtDNA-Server 2](https://github.com/genepi/mtdna-server-2/) to combine SNV with InDel Calling. 

## Contact
[Sebastian Schoenherr](mailto:sebastian.schoenherr@i-med.ac.at)

## Citation

Weissensteiner H, Forer L, Fendt L, Kheirkhah A, Salas A, Kronenberg F, Schoenherr S. 2021. Contamination detection in sequencing studies using the mitochondrial phylogeny.
Genome Res. 31: 309-316

Weissensteiner H, Forer L, Fuchsberger C, Schöpf B, Kloss-Brandstätter A, Specht G, Kronenberg F, Schönherr S. 2016. mtDNA-Server: next-generation sequencing data analysis of human mitochondrial DNA in the cloud. Nucleic Acids Res 44: W64–9.

