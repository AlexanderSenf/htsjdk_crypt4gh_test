/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package htsjdktest;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamInputResource;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.seekablestream.SeekableCrypt4GHStream;
import htsjdk.samtools.seekablestream.SeekableFileStream;
import htsjdk.samtools.seekablestream.SeekableStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Random;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author asenf
 */
public class HtsjdkTest {
    
    private static final int BUF_SIZE = 0x1000; // 4K

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, Exception {
        try {
            // Test Number (to enable multiple tests to be run)
            int testNum = Integer.parseInt(args[0]);
            System.out.println("Running Test " + testNum);

            boolean encrypted = args[1].equalsIgnoreCase("true");
            
            // Get Files
            String inputFile = args[2];
            System.out.println("\tInput File: " + inputFile);
            String indexFile = args[3];
            System.out.println("\tIndex File: " + indexFile);
            String privateKeyFile = null;
            if (encrypted) {
                privateKeyFile = args[4];
                System.out.println("\tKey File: " + privateKeyFile);
            }
            
            // Open files before passing them off to test functions:
            SeekableStream s_file = new SeekableFileStream(new File(inputFile));
            SeekableStream s_index = new SeekableFileStream(new File(inputFile));
            if (encrypted) {
                byte[] pK = loadKey(Paths.get(privateKeyFile));
                s_file = new SeekableCrypt4GHStream(s_file, pK);
                s_index = new SeekableCrypt4GHStream(s_index, pK);
            }
            System.out.println("\tFile opend as SeekableStreams.");
            
            int num = 1000;
            String chr = "chr20";
            long execution_ms = System.currentTimeMillis();
            switch(testNum) {
                case 1:
                        test_1(s_file, s_index);
                        break;
                case 2:
                        test_2(s_file, s_index, false, num, chr);
                        break;
                case 3:
                        test_2(s_file, s_index, true, num, chr);
                        break;
                case 4:
                        test_4(s_file, s_index);
                        break;
                case 5:
                        break;
                case 6:
                        break;
            }
            execution_ms = System.currentTimeMillis() - execution_ms;
            System.out.println("\tTest " + testNum + " completed in. " + execution_ms + " ms.");

        } catch (Throwable t) {
            System.out.println("Error: " + t.toString());
            System.out.println(" ");
            System.out.println("Usage: ");
            System.out.println("  Test 'n': 'n' 'true' 'inputfile_path' 'indexfile_path' 'keyfile_path'");
            System.out.println("  Test 'n': 'n' 'false' 'inputfile_path' 'indexfile_path'");
            System.out.println("  'true' indicates an encrypted input file; in this case a key file is required.");
            System.out.println("  ");           
            System.out.println("  Available Tests: ");
            System.out.println("        1: Iterate through all SAMRecords sequentially");
            System.out.println("        2: Query chr20 [1000 times]");
            System.out.println("        3: Query chr20 & iterate through all results [1000 times]");
            System.out.println("        4: Simply stream though the file, sequentially");
            System.out.println("        5: [TODO]");
            System.out.println("        6: [TODO]");
        }
    }
    
    // Test 1: Sequentially iterate through all SAMRecords
    private static void test_1(SeekableStream s_file,
                               SeekableStream s_index) throws IOException {
        
        SamInputResource sir3 = SamInputResource.of(s_file);
        SamReaderFactory srf_ = SamReaderFactory.make();
        SamReader sr = srf_.open(sir3);
        
        SAMFileHeader fileHeader3 = sr.getFileHeader();
        //System.out.println(fileHeader3.toString());

        SAMRecordIterator iterator = sr.iterator();
        while (iterator.hasNext()) {
            try {
                SAMRecord next = iterator.next();
                //System.out.println(next.getCigar().toString());
            } catch (Throwable th) {
                //System.out.println("**");
            }
        }
        
        sr.close();        
    }
    
    // Query chr20
    private static void test_2(SeekableStream s_file,
                               SeekableStream s_index,
                               boolean iterate,
                               int num,
                               String chr) throws IOException, Exception {
        
        SamInputResource sir3 = SamInputResource.of(s_file).index(s_index);
        SamReaderFactory srf_ = SamReaderFactory.make();
        SamReader sr = srf_.open(sir3);
        
        if (sr.getFileHeader().getSequenceDictionary().getSequence(chr) == null)
            throw new Exception("chr20 is not in this file");
        
        int chr_size = sr.getFileHeader().getSequenceDictionary().getSequence(chr).getSequenceLength();
        int delta = (chr_size/10)>1000000?1000000:(chr_size/10);
        
        int[] start = new int[num], end = new int[num];
        long t3 = System.currentTimeMillis();
        Random rand = new Random();
        for (int i=0; i<num; i++) {
            int iStart = 1, iEnd = 0;
            while (iStart >= iEnd) {
                iStart = rand.nextInt(chr_size-delta);
                iEnd = iStart + delta;
            }
            start[i] = iStart;
            end[i] = iEnd;
        }
        Arrays.sort(start);
        Arrays.sort(end);
        t3 = System.currentTimeMillis() - t3;
        System.out.println("    --- Generating ranges: " + t3 + " (ms)");

        long t5 = System.currentTimeMillis();
        for (int i=0; i<num; i++) {
            SAMRecordIterator queryOverlapping = sr.queryOverlapping(chr, start[i], end[i]);
            if (iterate) {
                while (queryOverlapping.hasNext()) {
                    SAMRecord next = queryOverlapping.next();
                }
            }
            queryOverlapping.close();
        }
        t5 = System.currentTimeMillis() - t5;
        System.out.println("QueryOverlapping for " + num + " queries - " + t5 + " ms");
        
        // close readers
        sr.close();
        
    }
    
    // Simply stream, beginning to end
    private static void test_4(SeekableStream s_file,
                               SeekableStream s_index) throws IOException {

        InputStream in = s_file;
        OutputStream out = new ByteArrayOutputStream();

        long bytes = copy(in, out);
    }

    private static void test_5(SeekableStream s_file,
                               SeekableStream s_index) {
        // TODO
    }

    private static void test_6(SeekableStream s_file,
                               SeekableStream s_index) {
        // TODO
    }
    
    /*
     * Helper Functions
     */
    
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

    public static long copy(InputStream from, OutputStream to)
          throws IOException {
    
        byte[] buf = new byte[BUF_SIZE];
        long total = 0;
        while (true) {
          int r = from.read(buf);
          if (r == -1) {
            break;
          }
          to.write(buf, 0, r);
          total += r;
        }
        return total;
      }
}
