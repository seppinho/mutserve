## Evaluation
Started from [this paper](https://www.nature.com/articles/srep43169?WT.feed_name=subjects_biotechnology). Other callers to follow...

### Freebayes

[Link](https://github.com/ekg/freebayes)

freebayes -F 0.01 -f <fasta> <file.bam> > <out.vcf>

### VarScan

[Link](https://sourceforge.net/projects/varscan/files/)

samtools mpileup -d 1000000 -f <fasta>_<bam> > <varscan.bcf>
  
java -jar VarScan.v2.3.9.jar mpileup2snp <varscan.bcf> --min-var-freq 0.01  > <varscan.vcf>


### lofreq

[Link](http://csb5.github.io/lofreq/)

./lofreq call -f <fasta> -o lofreq.vcf <file.bam> -s
