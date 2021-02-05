**Columns**|**Description**
:-----|:-----
ID|Sample Identifier
Pos|Position on locus (integer)
Ref|Reference nucleotide (A,C,G,T,N)
Variant|Variant nucleotide (A,C,G,T,D=deletion)
VariantLevel|Variant level or variant allele frequency (applies for heteroplasmic variants, Type=2)
MajorBase|Most observed nucloetide
MajorLevel|Frequency of most observed nucleotide
MinorBase|if present, the minor variant nucloetide
MinorLevel|Frequency of minor observed nucleotide
Coverage|Total coverage 
CoverageFWD|Coverage on Forward Strand
CoverageREV|Coverage on Reverse Strand
Type|1=homoplasmic variant, 2=heteroplasmic variant
Mutation|Position + alternative base
Substitution|transition or transversion
Maplocus|Locus (37 different loci on mtDNA – including one “empty”)
Category|Locus category – CR = Control Region, rRNA=RNR1 or RNR2, tRNA= all tRNA loci, Coding=all coding genes)
Phylotree17\_haplogroups|How many haplogroups include this SNP in Phylotree 17 – of total ~5,500 different haplogroups – see van Oven 2015 https://www.sciencedirect.com/science/article/pii/S1875176815302432 
Phylotree17\_clades|How many different lineages show this SNP
HaploGrep2\_weight|Weight between 1 and 10 (1=rare, 10=high fluctuating) – see HaploGrep 2 Paper – Weissensteiner et al 2016  https://academic.oup.com/nar/article/44/W1/W58/2499296
RSRS\_SNP|Is this SNP differing between rCRS and RSRS – see Behar et al 2012 https://www.sciencedirect.com/science/article/pii/S000292971200208X (0=no, 1=yes)
KGP3\_SNP|Is this SNP present in the 1000 Genome Project Phase 3 samples (0=no, 1=yes)
AAC|Amino Acid Change for SNPs in coding region, e.g. A97V
CodonPosition|Codon position (position 1, 2 or 3), e.g. 2
AminoAcid|One letter amino acid, e.g. A
NewAminoAcid|One letter amino acid, e.g. V
AminoAcid\_pos\_protein|Amino Acid position in specific protein – e.g. 97
MutPred\_Score|MutPred Score – value between 0 and 1. See Pereira et al 2011 https://www.sciencedirect.com/science/article/pii/S000292971100098X
mtDNA\_Selection\_Score|mtDNA Selection Score introduced by Pereira et al, based on MutPred, values between 0 and 3, with values >1  likely pathogenic– see Pereira et al 2011 https://www.sciencedirect.com/science/article/pii/S000292971100098X
CI\_MitoTool|Conservation Index, values between 0 and 1, whereas deleterious or adaptive mutations would have a high CI see Fan & Guo 2011 https://www.sciencedirect.com/science/article/pii/S1567724910001686 and Ruiz-Pesini et al 2004 https://science.sciencemag.org/content/303/5655/223.full 
OXPHOS\_complex|OXPHOS Complex for coding genes: I, III, IV, V 
NuMTs\_dayama|SNP found in nuclear mitochondrial pseudogene (NUMT), based on 1000 Genome Phase 1 data by Dayama et al 2014  https://academic.oup.com/nar/article/42/20/12640/2902626 
Helix\_count\_hom|Homoplasmic variants on this SNP in Helix mtDNA database of ~200,000 samples, see Helix.com/MITO and Bolze et al 2020 https://www.biorxiv.org/content/10.1101/798264v3  
Helix\_count\_het|Heteroplasmic variants on this SNP in Helix mtDNA database of ~200,000 samples, see Helix.com/MITO and Bolze et al 2020 https://www.biorxiv.org/content/10.1101/798264v3  
Helix\_vaf\_hom|Homoplasmic variant allele frequency in Helix mtDNA database of ~200,000 samples, see Helix.com/MITO and Bolze et al 2020 https://www.biorxiv.org/content/10.1101/798264v3  
Helix\_vaf\_het|Heteroplasmic variant allele frequency in Helix mtDNA database of ~200,000 samples, see Helix.com/MITO and Bolze et al 2020 https://www.biorxiv.org/content/10.1101/798264v3  
Helix\_haplogroups|Haplogroroups in Helix mtDNA database containing the SNP in of ~200,000 samples, see Helix.com/MITO and Bolze et al 2020 https://www.biorxiv.org/content/10.1101/798264v3  
rCRS\_Surr\_seq|Nucloebases -5/+5 on around present SNP, on rCRS
LowComplexityRegion|4 or more same nucleobases (homopolymeric stretch)
DuplSeq\_rCRS|Can the sequence in Column “rCRS\_Surr\_seq” be found on rCRS (0=no, othewise number of occurrence)
DuplSeq\_rCRS\_pos|If found, locus for identical 11 base sequence on mitochondrial genome
