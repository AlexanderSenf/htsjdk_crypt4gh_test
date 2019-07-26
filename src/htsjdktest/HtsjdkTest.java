/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package htsjdktest;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamInputResource;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.seekablestream.SeekableCrypt4GHStream;
import htsjdk.samtools.seekablestream.SeekableFileStream;
import htsjdk.samtools.seekablestream.SeekableStream;
import htsjdk.samtools.util.Crypt4GHOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author asenf
 */
public class HtsjdkTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, Exception {
        try {
            int testNum = Integer.parseInt(args[0]);

            if (testNum==1) {
                String inputFile = args[1];
                String privateKeyFile = args[2];
                
                System.out.println("Running Test 1");
                System.out.println("\tInput File: " + inputFile);
                System.out.println("\tKey File: " + privateKeyFile);
                test1(inputFile, privateKeyFile);
            } else if (testNum==2) {
                String inputFilePlain = args[1];
                String inputFileEncrypted = args[2];
                String privateKeyFile = args[3];
                
                test2(inputFilePlain, inputFileEncrypted, privateKeyFile);
            } else if (testNum==3) {
                String inputFilePlain = args[1];
                String outputFileEncrypted = args[2];
                String sourcePrivateKeyFile = args[3];
                String targetPublicKeyFile = args[4];
                String tagetPrivateKeyFile = args[5];
                
                writeTest(inputFilePlain, outputFileEncrypted, sourcePrivateKeyFile, targetPublicKeyFile, tagetPrivateKeyFile);
            }
        } catch (Throwable t) {
            System.out.println("Usage: ");
            System.out.println("  Test 1: '1' 'inputfile_path' 'keyfile_path' ");
            System.out.println("  Test 2: '2' 'plain_inputfile_path' 'encrypted_inputfile_path' 'keyfile_path' ");           
            System.out.println("  Test 3: '3' 'plain_inputfile_path' 'output_encrypted_path' 'source_private_keyfile_path' 'target_public_keyfile_path' 'tagret_public_keyfile_path'");           
        }
    }
    
    private static void test2(String inputFilePlain, 
                              String inputFileEncrypted, 
                              String privateKeyFile) throws FileNotFoundException, 
                                                            IOException, 
                                                            NoSuchAlgorithmException, 
                                                            InvalidKeySpecException, 
                                                            NoSuchProviderException, 
                                                            Exception {
        // plain
        //SeekableStream s1 = new SeekableFileStream(new File("/home/asenf/Documents/data/mapt.NA12156.altex.bam"));
        SeekableStream s1 = new SeekableFileStream(new File(inputFilePlain));
        // encrypted
        //SeekableStream s2 = new SeekableFileStream(new File("/home/asenf/Documents/data/mapt.NA12156.altex.bam.c4gh"));
        SeekableStream s2 = new SeekableFileStream(new File(inputFileEncrypted));
        //String targetPrivate = "/home/asenf/NetBeansProjects/crypt4gh/plain.sec";
        String targetPrivate = privateKeyFile;
        byte[] targetPK = loadKey(Paths.get(targetPrivate));
        SeekableStream s3 = new SeekableCrypt4GHStream(s2, targetPK);
        
        // Test / Compare
        byte[] block1 = new byte[131072];
        byte[] block2 = new byte[131072];
        
        long seekPos = 0;
        
        s1.seek(seekPos);
        s1.readFully(block1);
        
        s3.seek(seekPos);
        s3.readFully(block2);
        
        boolean equals = Arrays.equals(block1, block2);
        System.out.println(equals);
        
        // Done!
        s1.close();
        s3.close();
    }    
    
    private static void test1(String inputFile,
                              String keyFile) throws FileNotFoundException, 
                                                     IOException, 
                                                     NoSuchAlgorithmException, 
                                                     InvalidKeySpecException, 
                                                     NoSuchProviderException, 
                                                     Exception {
/*        
        SeekableStream s1 = new SeekableFileStream(new File("/home/asenf/Documents/data/mapt.NA12156.altex.bam"));
        
        SamReaderFactory srf = SamReaderFactory.make();
        SamInputResource sir = SamInputResource.of(s1);
        SamReader sr = srf.open(sir);
        
        SAMFileHeader fileHeader = sr.getFileHeader();
        System.out.println(fileHeader.toString());
        
        SAMRecordIterator iter = sr.iterator();
        while (iter.hasNext()) {
            try {
                SAMRecord next = iter.next();
                System.out.println(next.getCigar().toString());
            } catch (Throwable th) {
                System.out.println("**");
            }
        }
        
        sr.close();
        s1.close();
*/        
        // ---------------------------------------------------------------------

        //SeekableStream s2 = new SeekableFileStream(new File("/home/asenf/Documents/data/mapt.NA12156.altex.bam.c4gh"));
        //SeekableStream s2 = new SeekableFileStream(new File("/home/asenf/Documents/data/plain.sam"));
        //SeekableStream s2 = new SeekableFileStream(new File("/home/asenf/Documents/data/john.sam"));
        //String targetPrivate = "/home/asenf/NetBeansProjects/crypt4gh/plain.sec";
        //String targetPrivate = "/home/asenf/NetBeansProjects/crypt4gh/john.sec";

        SeekableStream s2 = new SeekableFileStream(new File(inputFile));
        String targetPrivate = keyFile;
        
        byte[] targetPK = loadKey(Paths.get(targetPrivate));
        SeekableStream s3 = new SeekableCrypt4GHStream(s2, targetPK);
     
        SamInputResource sir3 = SamInputResource.of(s3);
        SamReaderFactory srf_ = SamReaderFactory.make();
        SamReader sr3 = srf_.open(sir3);
        
        SAMFileHeader fileHeader3 = sr3.getFileHeader();
        System.out.println(fileHeader3.toString());
                
        SAMRecordIterator iterator = sr3.iterator();
        while (iterator.hasNext()) {
            try {
                SAMRecord next = iterator.next();
                System.out.println(next.getCigar().toString());
            } catch (Throwable th) {
                System.out.println("**");
            }
        }
        
        sr3.close();
        s3.close();
    }

    public static void writeTest(String inputFile,
                                 String outputFile,
                                 String sourcePrivateKeyFile,
                                 String targetPublicKeyFile,
                                 String targetPrivateKeyFile) throws FileNotFoundException, 
                                                                    IOException, 
                                                                    NoSuchAlgorithmException, 
                                                                    InvalidKeySpecException, 
                                                                    NoSuchProviderException, 
                                                                    GeneralSecurityException, 
                                                                    Exception {
        
        byte[] targetUK = loadKey(Paths.get(targetPublicKeyFile));
        byte[] sourceRK = loadKey(Paths.get(sourcePrivateKeyFile));

        
        OutputStream fOut = Files.newOutputStream(new File(outputFile).toPath());
        OutputStream c4ghout = new Crypt4GHOutputStream(fOut, sourceRK, targetUK);
        
        final SamReader reader = SamReaderFactory.makeDefault().open(new File(inputFile));
        final SAMFileWriter outputSam = new SAMFileWriterFactory().makeBAMWriter(reader.getFileHeader(),
                true, c4ghout);

        for (final SAMRecord samRecord : reader) {
            // Convert read name to upper case.
            samRecord.setReadName(samRecord.getReadName().toUpperCase());
            outputSam.addAlignment(samRecord);
        }

        outputSam.close();        
        reader.close();
        
        /*
         * (Test case - just making sure htsjdk can still read the file we just created!
         */
        SeekableStream s2 = new SeekableFileStream(new File(outputFile));

        byte[] targetPK = loadKey(Paths.get(targetPrivateKeyFile));
        SeekableStream s3 = new SeekableCrypt4GHStream(s2, targetPK);
     
        SamInputResource sir3 = SamInputResource.of(s3);
        SamReaderFactory srf_ = SamReaderFactory.make();
        SamReader sr3 = srf_.open(sir3);
        
        SAMFileHeader fileHeader3 = sr3.getFileHeader();
        System.out.println(fileHeader3.toString());
                
        SAMRecordIterator iterator = sr3.iterator();
        while (iterator.hasNext()) {
            try {
                SAMRecord next = iterator.next();
                System.out.println(next.getCigar().toString());
            } catch (Throwable th) {
                System.out.println("**");
            }
        }
        
        sr3.close();
        s3.close();
        
    }
    
    private static byte[] loadKey(Path keyIn) throws FileNotFoundException, IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        BufferedReader in = new BufferedReader(new FileReader(keyIn.toString()));
        in.readLine();
        String key = in.readLine();
        in.readLine();
        in.close();
        
        Base64 decoder = new Base64(64);
        byte[] decode = decoder.decode(key); //.substring(20));
        
        return decode;
    }
}
