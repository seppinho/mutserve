# Mutserve

[![Mutserve Tests](https://github.com/seppinho/mutserve/actions/workflows/run-tests.yml/badge.svg?event=public)](https://github.com/seppinho/mutserve/actions/workflows/run-tests.yml)
[![Twitter Follow](https://img.shields.io/twitter/follow/mtdnaserver.svg?style=social&label=Follow)](https://twitter.com/mtdnaserver)

Mutserve is a variant caller for the mitochondrial genome to detect homoplasmic and heteroplasmic sites in sequence data. It is used by [haplocheck](https://github.com/genepi/haplocheck) and [mtDNA-Server](https://mitoverse.i-med.ac.at).

## Quick Start
Mutserve requires sorted and indexed CRAM/BAM files as an input.

```
curl -sL mutserve.vercel.app | bash
./mutserve
```

## Documentation
Full documentation for mutserve can be found [here](https://mitoverse.readthedocs.io/mutserve/mutserve/). 

## Limitations
The focus of mutserve is currenly on SNP calling and not on indels.

## Contact
See [here](https://mitoverse.readthedocs.io/contact/).

## Citation

Weissensteiner H, Forer L, Fendt L, Kheirkhah A, Salas A, Kronenberg F, Schoenherr S. 2021. Contamination detection in sequencing studies using the mitochondrial phylogeny.
Genome Res. 31: 309-316

Weissensteiner H, Forer L, Fuchsberger C, Schöpf B, Kloss-Brandstätter A, Specht G, Kronenberg F, Schönherr S. 2016. mtDNA-Server: next-generation sequencing data analysis of human mitochondrial DNA in the cloud. Nucleic Acids Res 44: W64–9.

