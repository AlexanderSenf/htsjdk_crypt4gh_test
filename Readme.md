This is a Java program to test the addition of Crypt4GH to htsjdk. This addition is an extension to the SeekableStream class, which will allow any class that takes a SeekableStream as input (e.g. SamFileReader) to transparently read Crypt4GH files.

Download: git clone https://github.com/AlexanderSenf/htsjdk_crypt4gh_test.git

Then cd htsjdk_crypt4gh_test

Build: ant jar copy-dependencies package-for-store

The test file is: 6929_4#44.bam which can be obtained from ENA: https://www.ebi.ac.uk/ena/data/view/ERR065185

[Then create the index file, with samtools: `samtools index 6929_4#44.bam`]
[Then create the encrypted versions of these files: `./encrypt.sh`]

There are 4 tests included; each test can be run on a plain ('false') or on a GA4GH-encrypted file ('true')

java -jar store/HtsjdkTest.jar {Test Num} {Encrypted} {BAM/CRAM File} {BAM/CRAM Index File} {Private Key File}

Test 1:

`java -jar store/HtsjdkTest.jar 1 false 6929_4#44.bam 6929_4#44.bam.enc bob.sec.pem`

Test 2:

`java -jar store/HtsjdkTest.jar 2 false 6929_4#44.bam 6929_4#44.bam.bai`

`java -jar store/HtsjdkTest.jar 2 true 6929_4#44.bam.enc 6929_4#44.bam.enc bob.sec.pem`

This test simply opens the file and iterated through all read record sequentially.

Test 3:

`java -jar store/HtsjdkTest.jar 3 false 6929_4#44.bam 6929_4#44.bam.bai`

`java -jar store/HtsjdkTest.jar 3 true 6929_4#44.bam.enc 6929_4#44.bam.enc bob.sec.pem`

This test performs 1000 queries ove all chromosomes, and then traverses all records in the result set. This is likely the most applicable test to determine the expected real-world speed.

Test 4: [TO FIX - SKIP FOR NOW]

`java -jar store/HtsjdkTest.jar 4 false NA12878.bam NA12878.bam.bai`

`java -jar store/HtsjdkTest.jar 4 true NA12878.bam.c4gh NA12878.bam.bai.c4gh john.sec`

Simply stream-copy the data.

