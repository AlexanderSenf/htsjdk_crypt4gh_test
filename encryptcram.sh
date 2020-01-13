java -jar store/HtsjdkTest.jar encrypt NA12878.cram plain.sec john.pub
mv NA12878.cram.c4gh NA12878.c4gh.cram
java -jar store/HtsjdkTest.jar encrypt NA12878.cram.crai plain.sec john.pub
mv NA12878.cram.crai.c4gh NA12878.c4gh.cram.crai
