This is a Java program to test the addition of Crypt4GH to htsjdk. This addition is an extension to the SeekableStream class, which will allow any class that takes a SeekableStream as input (e.g. SamFileReader) to transparently read Crypt4GH files.

* Download: `git clone https://github.com/AlexanderSenf/htsjdk_crypt4gh_test.git`

* Then `cd htsjdk_crypt4gh_test`

* Build: `ant jar copy-dependencies package-for-store` (or use the existing precompiled jar file)

* The BAM test file is: 6929_4#44.bam which can be obtained from ENA: https://www.ebi.ac.uk/ena/data/view/ERR065185
* Then create the index file, with samtools: `samtools index 6929_4#44.bam`

* The VCF test file is `ftp://ftp-trace.ncbi.nih.gov/1000genomes/ftp/release/20130502/ALL.chr13.phase3_shapeit2_mvncall_integrated_v5a.20130502.genotypes.vcf.gz`
* Then create the index file, with tabix: `tabix -p vcf ALL.chr13.phase3_shapeit2_mvncall_integrated_v5a.20130502.genotypes.vcf.gz`

* Then create the encrypted versions of these files: `./encrypt.sh`. (The passwords are: `password`)

(Keys have been created using `java -jar lib/crypt4gh-2.3.0-shaded.jar -kf Crypt4GH -g alice` (and `[...] -g bob`) with password: `password`)

There is one test that simply compares the unencrypted and encrypted files by randomly seeking to byte posititons and comparing the bytes read from each stream. There are 2 tests included for the BAM file, and 2 for the VCF file; each test can be run on a plain or on a GA4GH-encrypted file

`java -jar store/HtsjdkTest.jar -t {Test Num} [-e] -if {BAM File} -idx {BAM Index File} -kf {Private Key File} -kp {password} [-bfn {BED File}] [-is interval_size]`

* If option `-bfn` is chosen (optional), (1) if the file does not exist, the randomly generated search ranges are written to a BED like file (2) if the file exists, existing ranges are loaded from disk. This should allow for reproducible tests.
* Option `-is` allows for specifying the size of each search interval (optional).

Test 1: (generic; randomly access sections of the plain and encrypted file, ensure the data retrieved is identical. The parameter of the index file is used for the encrypted version of the file specufued with `-if`)

`java -jar store/HtsjdkTest.jar -t 1 -e -if 6929_4#44.bam -idx 6929_4#44.bam.enc -kf bob.sec.pem -kp password`

Test 2: (BAM)

`java -jar store/HtsjdkTest.jar -t 2 -if 6929_4#44.bam -idx 6929_4#44.bam.bai`

`java -jar store/HtsjdkTest.jar -t 2 -e -if 6929_4#44.bam.enc -idx 6929_4#44.bam.enc -kf bob.sec.pem -kp password`

This test simply opens the file and iterated through all read record sequentially.

Test 3: (BAM; iterate through all records in this file)

`java -jar store/HtsjdkTest.jar -t 3 -if 6929_4#44.bam -idx 6929_4#44.bam.bai`

`java -jar store/HtsjdkTest.jar -t 3 -e -if 6929_4#44.bam.enc -idx 6929_4#44.bam.bai.enc -kf bob.sec.pem -kp password`

This test performs 1000 queries ove all chromosomes, and then traverses all records in the result set. This is likely the most applicable test to determine the expected real-world speed.

Test 5: (VCF)

`java -jar store/HtsjdkTest.jar -t 5 -if ALL.chr13.phase3_shapeit2_mvncall_integrated_v5a.20130502.genotypes.vcf.gz`

`java -jar store/HtsjdkTest.jar -t 5 -e -if ALL.chr13.phase3_shapeit2_mvncall_integrated_v5a.20130502.genotypes.vcf.gz.enc -kf bob.sec.pem -kp password`

This is an analogy to test 3, but for VCF files: This test performs 1000 queries over all chromosomes, and then traverses all records in the result set.

(This test uses an unencrypted index file, due to limitation in the Tabix reader)

Test 6: (VCF)

`java -jar store/HtsjdkTest.jar -t 6 -if ALL.chr13.phase3_shapeit2_mvncall_integrated_v5a.20130502.genotypes.vcf.gz`

`java -jar store/HtsjdkTest.jar -t 6 -e -if ALL.chr13.phase3_shapeit2_mvncall_integrated_v5a.20130502.genotypes.vcf.gz.enc -kf bob.sec.pem -kp password`

This test is ananalogy to test 2, but for VCF files: This test simply opens the file and iterated through all read record sequentially.


Execution Log (Ubuntu 18 VM, OpenJDK 11, 4 3.5 GHz Xeon CPU cores, 4 GB RAM, SSD)

```
asenf@asenf-virtual-machine:~/NetBeansProjects/htsjdk_crypt4gh_test$ java -jar store/HtsjdkTest.jar -t 1 -e -if 6929_4#44.bam -idx 6929_4#44.bam.enc -kf bob.sec.pem -kp password
Running Test 1
	Input File: 6929_4#44.bam
	Index File: 6929_4#44.bam.enc
	Key File: bob.sec.pem
	File opend as SeekableStreams.
1003733655
1003733655
	Test 1 completed in: 9666 ms.
asenf@asenf-virtual-machine:~/NetBeansProjects/htsjdk_crypt4gh_test$ java -jar store/HtsjdkTest.jar -t 2 -if 6929_4#44.bam -idx 6929_4#44.bam.bai
Running Test 2
	Input File: 6929_4#44.bam
	Index File: 6929_4#44.bam.bai
	File opend as SeekableStreams.
	Test 2 completed in: 22131 ms.
asenf@asenf-virtual-machine:~/NetBeansProjects/htsjdk_crypt4gh_test$ java -jar store/HtsjdkTest.jar -t 2 -e -if 6929_4#44.bam.enc -idx 6929_4#44.bam.enc -kf bob.sec.pem -kp password
Running Test 2
	Input File: 6929_4#44.bam.enc
	Index File: 6929_4#44.bam.enc
	Key File: bob.sec.pem
	File opend as SeekableStreams.
	Test 2 completed in: 29427 ms.
asenf@asenf-virtual-machine:~/NetBeansProjects/htsjdk_crypt4gh_test$ java -jar store/HtsjdkTest.jar -t 3 -if 6929_4#44.bam -idx 6929_4#44.bam.bai -bfn bamRanges.bed
Running Test 3
	Input File: 6929_4#44.bam
	Index File: 6929_4#44.bam.bai
	File opend as SeekableStreams.
    --- Generating ranges: 5 (ms)
    --- Saving ranges to file: 49 (ms)
        QueryOverlapping for 1000 queries - 103517 ms
	Test 3 completed in: 103665 ms.
asenf@asenf-virtual-machine:~/NetBeansProjects/htsjdk_crypt4gh_test$ java -jar store/HtsjdkTest.jar -t 3 -e -if 6929_4#44.bam.enc -idx 6929_4#44.bam.bai.enc -kf bob.sec.pem -kp password -bfn bamRanges.bed
Running Test 3
	Input File: 6929_4#44.bam.enc
	Index File: 6929_4#44.bam.bai.enc
	Key File: bob.sec.pem
	File opend as SeekableStreams.
    --- Loading ranges from file: 3 (ms)
        QueryOverlapping for 1000 queries - 145111 ms
	Test 3 completed in: 145229 ms.
asenf@asenf-virtual-machine:~/NetBeansProjects/htsjdk_crypt4gh_test$ java -jar store/HtsjdkTest.jar -t 5 -if ALL.chr13.phase3_shapeit2_mvncall_integrated_v5a.20130502.genotypes.vcf.gz -bfn vcfRanges.bed
Running Test 5
	Input File: ALL.chr13.phase3_shapeit2_mvncall_integrated_v5a.20130502.genotypes.vcf.gz
	Index File: null
	File opend as SeekableStreams.
    --- Generating ranges: 3 (ms)
    --- Saving ranges to file: 58 (ms)
        Tabix Query for 1000 queries - 18643 ms
	Test 5 completed in: 18756 ms.
asenf@asenf-virtual-machine:~/NetBeansProjects/htsjdk_crypt4gh_test$ java -jar store/HtsjdkTest.jar -t 5 -e -if ALL.chr13.phase3_shapeit2_mvncall_integrated_v5a.20130502.genotypes.vcf.gz.enc -kf bob.sec.pem -kp password -bfn vcfRanges.bed
Running Test 5
	Input File: ALL.chr13.phase3_shapeit2_mvncall_integrated_v5a.20130502.genotypes.vcf.gz.enc
	Index File: null
	Key File: bob.sec.pem
	File opend as SeekableStreams.
    --- Loading ranges from file: 6 (ms)
        Tabix Query for 1000 queries - 18790 ms
	Test 5 completed in: 18839 ms.
asenf@asenf-virtual-machine:~/NetBeansProjects/htsjdk_crypt4gh_test$ java -jar store/HtsjdkTest.jar -t 6 -if ALL.chr13.phase3_shapeit2_mvncall_integrated_v5a.20130502.genotypes.vcf.gz
Running Test 6
	Input File: ALL.chr13.phase3_shapeit2_mvncall_integrated_v5a.20130502.genotypes.vcf.gz
	Index File: null
	File opend as SeekableStreams.
	Test 6 completed in: 96251 ms.
asenf@asenf-virtual-machine:~/NetBeansProjects/htsjdk_crypt4gh_test$ java -jar store/HtsjdkTest.jar -t 6 -e -if ALL.chr13.phase3_shapeit2_mvncall_integrated_v5a.20130502.genotypes.vcf.gz.enc -kf bob.sec.pem -kp password
Running Test 6
	Input File: ALL.chr13.phase3_shapeit2_mvncall_integrated_v5a.20130502.genotypes.vcf.gz.enc
	Index File: null
	Key File: bob.sec.pem
	File opend as SeekableStreams.
	Test 6 completed in: 97830 ms.
```

The results show BAM processing slightly slower in encrypted mode, while VCF file processing is essentially identical when using Crypt4GH files.
