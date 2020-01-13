This is a Java program to test the addition of Crypt4GH to htsjdk. This addition is an extension to the SeekableStream class, which will allow any class that takes a SeekableStream as input (e.g. SamFileReader) to transparently read Crypt4GH files.

Download: git clone https://github.com/AlexanderSenf/htsjdk_crypt4gh_test.git

Then cd htsjdk_crypt4gh_test

Build: ant jar copy-dependencies package-for-store

[Then create the encrypted versions of these files: `./encrypt.sh`]

There are 4 tests included; each test can be run on a plain ('false') or on a GA4GH-encrypted file ('true')

java -jar store/HtsjdkTest.jar {Test Num} {Encrypted} {BAM/CRAM File} {BAM/CRAM Index File} {Private Key File}

Test 1:

`java -jar store/HtsjdkTest.jar 1 false NA12878.bam NA12878.bam.bai`

`java -jar store/HtsjdkTest.jar 1 true NA12878.bam.c4gh NA12878.bam.bai.c4gh john.sec`

This test simply opens the file and iterated through all read record sequentially.

Test 2:

`java -jar store/HtsjdkTest.jar 2 false NA12878.bam NA12878.bam.bai`

`java -jar store/HtsjdkTest.jar 2 true NA12878.bam.c4gh NA12878.bam.bai.c4gh john.sec`

This test performs 1000 queries in chr20

Test 3:

`java -jar store/HtsjdkTest.jar 3 false NA12878.bam NA12878.bam.bai`

`java -jar store/HtsjdkTest.jar 3 true NA12878.bam.c4gh NA12878.bam.bai.c4gh john.sec`

This test performs 1000 queries in chr20, and then traverses all records in the result set. This is likely the most applicable test to determine the expected real-world speed.

Test 4:

`java -jar store/HtsjdkTest.jar 4 false NA12878.bam NA12878.bam.bai`

`java -jar store/HtsjdkTest.jar 4 true NA12878.bam.c4gh NA12878.bam.bai.c4gh john.sec`

Simply stream-copy the data.


CRAM Files:

To test CRAM files, the existing file must be converted into a CRAM file, which requires there to be a referencde index file to be available.

Getting the reference (requires wget and gunzip): `./getcramref38.sh`

Once the index is availble, the BAM file needs to be converted into a CRAM file, and indexed.

Converting the file (requires samtools): `./makecram.sh`

Then an encrypted version of these files should be created: `encryptcram.sh`

With these file in place tests 5 & 6 can be run, which is a mirror of tests 2 & 3:

Test 5:

`java -jar store/HtsjdkTest.jar 5 false NA12878.cram NA12878.cram.crai`

`java -jar store/HtsjdkTest.jar 5 true NA12878.c4gh.cram NA12878.c4gh.cram.crai john.sec`

This test performs 1000 queries in chr20

Test 6:

`java -jar store/HtsjdkTest.jar 6 false NA12878.cram NA12878.cram.crai`

`java -jar store/HtsjdkTest.jar 6 true NA12878.c4gh.cram NA12878.c4gh.cram.crai john.sec`

This test performs 1000 queries in chr20, and then traverses all records in the result set. This is likely the most applicable test to determine the expected real-world speed.


Additional functionality:

To enable a custom set of test files, it is posible to encrypt data as well:

java -jar store/HtsjdkTest.jar encrypt {file} {private key file} {public key file}

This file can then be accessed with the private key file corresponding to the public key file used for encryption.

(Both a BAM/CRAM file as well as the BAM/CRAM index file must be encrypted, separately.)
