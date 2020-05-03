This is a Java program to test the addition of Crypt4GH to htsjdk. This addition is an extension to the SeekableStream class, which will allow any class that takes a SeekableStream as input (e.g. SamFileReader) to transparently read Crypt4GH files.

Download: git clone https://github.com/AlexanderSenf/htsjdk_crypt4gh_test.git

Then cd htsjdk_crypt4gh_test

Build: ant jar copy-dependencies package-for-store

The test file is: 6929_4#44.bam which can be obtained from ENA: https://www.ebi.ac.uk/ena/data/view/ERR065185
Then create the index file, with samtools: `samtools index 6929_4#44.bam`

Then create the encrypted versions of these files: `./encrypt.sh`
Keys have been created using `java -jar lib/crypt4gh-2.3.0-shaded.jar -kf Crypt4GH -g alice` (and `[...] -g bob`) with password: `password`.

There are 4 tests included; each test can be run on a plain or on a GA4GH-encrypted file

java -jar store/HtsjdkTest.jar -t {Test Num} [-e] -if {BAM/CRAM File} -idx {BAM/CRAM Index File} -kf {Private Key File} -kp {password}

Test 1:

`java -jar store/HtsjdkTest.jar -t 1 -e -if 6929_4#44.bam -idx 6929_4#44.bam.enc -kf bob.sec.pem -kp password`

Test 2:

`java -jar store/HtsjdkTest.jar -t 2 -if 6929_4#44.bam -idx 6929_4#44.bam.bai`

`java -jar store/HtsjdkTest.jar -t 2 -e -if 6929_4#44.bam.enc -idx 6929_4#44.bam.enc -kf bob.sec.pem -kp password`

This test simply opens the file and iterated through all read record sequentially.

Test 3:

`java -jar store/HtsjdkTest.jar -t 3 -if 6929_4#44.bam -idx 6929_4#44.bam.bai`

`java -jar store/HtsjdkTest.jar -t 3 -e -if 6929_4#44.bam.enc -idx 6929_4#44.bam.enc -kf bob.sec.pem -kp password`

This test performs 1000 queries ove all chromosomes, and then traverses all records in the result set. This is likely the most applicable test to determine the expected real-world speed.

