This is a Java program to test the addition of Crypt4GH to htsjdk. This addition is an extension to the SeekableStream class, which will allow any class that takes a SeekableStream as input (e.g. SamFileReader) to transparently read Crypt4GH files.

Download: git clone https://github.com/AlexanderSenf/htsjdk_crypt4gh_test.git

Then cd htsjdk_crypt4gh_test

Build: ant jar copy-dependencies package-for-store

Test: there are two tests included, one to use SeekableCrypt4GHStream in isolation and test reading a random block by comparing it agains the plain file and the original SeekableStream class. And the second test opens a SamFileReader to verify that the SeekableCrypt4GH class can be used as input source.

The included test file is: htsjdk_crypt4gh_test.git, and an encrypted version is also included.

Test 1:

java -jar store/HtsjdkTest.jar 1 mapt.NA12156.altex.c4gh.john.bam john.sec

Test 2:

java -jar store/HtsjdkTest.jar 2 mapt.NA12156.altex.bam mapt.NA12156.altex.c4gh.john.bam john.sec

There is also a test file that was encrypted with htslib/samtools, as a way to test interoperability between htslib and htsjdk:

java -jar store/HtsjdkTest.jar 1 john.sam john.sec
