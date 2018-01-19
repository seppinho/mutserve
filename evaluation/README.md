## Evaluation
Started from [this paper](https://www.nature.com/articles/srep43169?WT.feed_name=subjects_biotechnology). Other callers + better docu to follow...

### Freebayes

[Link](https://github.com/ekg/freebayes)

freebayes -F 0.01 -f <fasta> <file.bam> > <out.vcf>


/home/seb/tools/freebayes/bin/freebayes -f /data2/git/mutation-server/test-data/dna/mixtures/reference/kiv2_6.fasta /data2/git/mutation-server/test-data/dna/plasmids/plasmid13/input/LPA-Plasmid-13_S18_L001_R1_001.gz_KIV2_6\(-\)5104bp.bam > plasmid13_freebayes.vcf 

### lofreq

[Link](http://csb5.github.io/lofreq/)

./lofreq call -f <fasta> -o lofreq.vcf <file.bam> -s

/home/seb/tools/lofreq/src/lofreq/lofreq call -f /data2/git/mutation-server/test-data/dna/mixtures/reference/kiv2_6.fasta -o plasmid12_lofreq.vcf /data2/git/mutation-server/test-data/dna/plasmids/plasmid12/input/LPA-Plasmid-12_S17_L001_R1_001.gz_KIV2_6\(-\)5104bp.bam -s 

## GATK

java -jar /home/seb/tools/picard/build/libs/picard.jar AddOrReplaceReadGroups I=/data2/git/mutation-server/test-data/dna/plasmids/plasmid12/input/LPA-Plasmid-12_S17_L001_R1_001.gz_KIV2_6\(-\)5104bp.bam O=/data2/git/mutation-server/test-data/dna/plasmids/plasmid12/input/LPA-Plasmid-12_S17_L001_R1_001.gz_KIV2_6\(-\)5104bp_RG.bam RGPL=illumina RGLB=lib1  RGPU=unit1  RGSM=2

java -jar /home/seb/tools/picard/build/libs/picard.jar  CreateSequenceDictionary R=/data2/git/mutation-server/test-data/dna/mixtures/reference/kiv2_6.fasta O=/data2/git/mutation-server/test-data/dna/mixtures/reference/kiv2_6.dict

/home/seb/tools/samtools-1.2/samtools index /data2/git/mutation-server/test-data/dna/plasmids/plasmid12/input/LPA-Plasmid-12_S17_L001_R1_001.gz_KIV2_6\(-\)5104bp_RG.bam

java -jar GenomeAnalysisTK.jar -T HaplotypeCaller -R /data2/git/mutation-server/test-data/dna/mixtures/reference/kiv2_6.fasta -I /data2/git/mutation-server/test-data/dna/plasmids/plasmid12/input/LPA-Plasmid-12_S17_L001_R1_001.gz_KIV2_6\(-\)5104bp_RG.bam --genotyping_mode DISCOVERY  -o raw_variants.vcf

### VarScan

[Link](https://sourceforge.net/projects/varscan/files/)

samtools mpileup -d 1000000 -f <fasta>_<bam> > <varscan.bcf>
  
java -jar VarScan.v2.3.9.jar mpileup2snp <varscan.bcf> --min-var-freq 0.01  > <varscan.vcf>
