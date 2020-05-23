/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package htsjdktest;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SamInputResource;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.seekablestream.SeekableFileStream;
import htsjdk.samtools.seekablestream.SeekableStream;
import htsjdk.samtools.util.IOUtil;
import htsjdk.tribble.readers.TabixReader;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFIterator;
import htsjdk.variant.vcf.VCFIteratorBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import no.uio.ifi.crypt4gh.stream.Crypt4GHSeekableStreamInternal;
import no.uio.ifi.crypt4gh.util.KeyUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author asenf
 */
public class HtsjdkTest {
    
    private static final int BUF_SIZE = 1048576; //0x1000; // 4K

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, Exception {
        try {
            // Specify Command line options
            Options options = new Options();
            options.addOption(new Option("t", "test number", true, "Number of test to run"));
            options.addOption("e", false, "encrypted input if specified");
            options.addOption(new Option("if", "input file", true, "Input File"));
            options.addOption(new Option("idx", "index file", true, "Index File"));
            options.addOption(new Option("kf", "key file", true, "Private Key File"));
            options.addOption(new Option("kp", "key password", true, "Private Key Password"));

            // Parse options
            CommandLineParser parser = new DefaultParser();
            CommandLine line = parser.parse(options, args);
            if (line.getOptions().length == 0) {
                throw (new Exception()); // Prints help
            }
            
            // Test Number (to enable multiple tests to be run)
            int testNum = Integer.parseInt( line.getOptionValue("t") );
            System.out.println("Running Test " + testNum);

            boolean encrypted = line.hasOption("e");
            
            // Get Files
            String inputFile = line.getOptionValue("if");
            System.out.println("\tInput File: " + inputFile);
            String indexFile = (line.hasOption("idx"))?line.getOptionValue("idx"):null;
            System.out.println("\tIndex File: " + indexFile);
            String privateKeyFile = null;
            String privateKeyPassword = null;
            if (encrypted) {
                privateKeyFile = line.getOptionValue("kf");
                System.out.println("\tKey File: " + privateKeyFile);
                if (line.hasOption("kp"))
                    privateKeyPassword = line.getOptionValue("kp");
            }
            
            // Open files before passing them off to test functions:
            SeekableStream s_file = new SeekableFileStream(new File(inputFile));
            SeekableStream s_index = (indexFile!=null)?new SeekableFileStream(new File(indexFile)):null;
            if (encrypted) {
                String keyLines = FileUtils.readFileToString(new File(privateKeyFile), Charset.defaultCharset());
                byte[] decodedKey = KeyUtils.getInstance().decodeKey(keyLines);
                PrivateKey pK= KeyUtils.getInstance().readCrypt4GHPrivateKey(decodedKey, privateKeyPassword.toCharArray());
                if (testNum > 1 && s_index == null)
                    s_file = new Crypt4GHSeekableStreamInternal(s_file, pK);
                s_index = (indexFile!=null)?new Crypt4GHSeekableStreamInternal(s_index, pK):null;
            }
            System.out.println("\tFile opend as SeekableStreams.");
            
            int num = 1000;
            long execution_ms = System.currentTimeMillis();
            switch(testNum) {
                case 1:
                        test_0(s_file, s_index);
                        break;
                case 2:
                        test_1(s_file, s_index);
                        break;
                case 3:
                        test_2(s_file, s_index, true, num);
                        break;
                case 4:
                        test_4(s_file);
                        break;
                case 5:
                        test_5(inputFile, s_file, s_index, true, num);
                        break;
                case 6:
                        test_6(s_file);
                        break;
            }
            execution_ms = System.currentTimeMillis() - execution_ms;
            System.out.println("\tTest " + testNum + " completed in. " + execution_ms + " ms.");

        } catch (Throwable t) {
            System.out.println("Error: " + t.toString());
            System.out.println(" ");
            System.out.println("Usage: ");
            System.out.println("  Test 'n': 'n' 'true' 'inputfile_path' 'indexfile_path' 'keyfile_path' 'keyfile passsword'");
            System.out.println("  Test 'n': 'n' 'false' 'inputfile_path' 'indexfile_path'");
            System.out.println("  'true' indicates an encrypted input file; in this case a key file is required.");
            System.out.println("  ");           
            System.out.println("  Available Tests: ");
            System.out.println("        1: Open unencrypted + encrypted files; 5000 seek + compare reads");
            System.out.println("        2: Iterate through all SAMRecords sequentially");
            System.out.println("        3: Query whole BAM file & iterate through all results [1000 times]");
            System.out.println("        4: Simply stream though the file, sequentially");
            System.out.println("        5: Query whole VCF file & iterate through all results [1000 times]");
            System.out.println("        6: Iterate through VCF File");
        }
    }
    
    // Test 0: Simply read stream
    private static void test_0(SeekableStream s_file,
                               SeekableStream s_index) throws IOException {
        
        long length = s_file.length() - 50;
        long length_ = s_index.length() - 50;
        System.out.println(length);
        System.out.println(length_);

        byte[] x = new byte[50], x_ = new byte[50];
        
        Random r = new Random();
        for (int i=0; i<5000; i++) {
            
            long pos = Math.abs(r.nextLong()) % length;
            
            s_file.seek(pos);
            s_index.seek(pos);
            
            int read = s_file.read(x);
            int read_ = s_index.read(x_);
            
            if (!(Arrays.equals(x, x_))) {
                System.out.println("ERROR! at pos " + pos);
                String encodeHexString = Hex.encodeHexString(x);
                String encodeHexString_ = Hex.encodeHexString(x_);
                System.out.println("Plain " + read + ": " + encodeHexString);
                System.out.println("Encry " + read_ + ": " + encodeHexString_);
            }
            
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
                //System.out.println(next.getAlignmentStart() + ": " + next.getCigar().toString());
            } catch (Throwable th) {
                //System.out.println("**");
            }
        }
        
        sr.close();        
    }
    
    // Query all Chr
    private static void test_2(SeekableStream s_file,
                               SeekableStream s_index,
                               boolean iterate,
                               int num) throws IOException, Exception {
        
        SamInputResource sir3 = SamInputResource.of(s_file).index(s_index);
        SamReaderFactory srf_ = SamReaderFactory.make().validationStringency(ValidationStringency.SILENT);
        SamReader sr = srf_.open(sir3);
        
        List<SAMSequenceRecord> sequences = sr.getFileHeader().getSequenceDictionary().getSequences();
        SAMSequenceRecord[] entries = new SAMSequenceRecord[sequences.size()];
        for (int y=0; y<sequences.size(); y++)
            entries[y] = sequences.get(y);
        
        int[] start = new int[num], end = new int[num];
        String chr_[] = new String[num];
        long t3 = System.currentTimeMillis();
        Random rand = new Random();
        for (int i=0; i<num; i++) {
            int iEntry = rand.nextInt(entries.length);
             chr_[i]= entries[iEntry].getSequenceName();
            
            int chr_size = sr.getFileHeader().getSequenceDictionary().getSequence(chr_[i]).getSequenceLength();
            int delta = (chr_size/10)>1000000?1000000:(chr_size/10);
        
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
            SAMRecordIterator queryOverlapping = sr.queryOverlapping(chr_[i], start[i], end[i]);
            if (iterate) {
                while (queryOverlapping.hasNext()) {
                    SAMRecord next = queryOverlapping.next();
                }
            }
            queryOverlapping.close();
        }
        t5 = System.currentTimeMillis() - t5;
        System.out.println("        QueryOverlapping for " + num + " queries - " + t5 + " ms");
        
        // close readers
        sr.close();        
    }
    
    // Simply stream, beginning to end
    private static void test_4(SeekableStream s_file) throws IOException {

        InputStream in = s_file;
        if (IOUtil.isGZIPInputStream(in))
            in = new GZIPInputStream(in);
        OutputStream out = OutputStream.nullOutputStream();

        long bytes = copy(in, out);
        System.out.println("Bytes copies: " + bytes);
    }

    // VCF Stats
    private static void test_5(String filePath,
                               SeekableStream s_file,
                               SeekableStream s_index,
                               boolean iterate,
                               int num) throws IOException {
        String indexPath = filePath.endsWith(".enc")?
                filePath.substring(0, filePath.length()-4) + ".tbi":
                filePath + ".tbi";
        
        TabixReader tabixReader = new TabixReader(filePath, indexPath, s_file);
        Set<String> chromosomes = tabixReader.getChromosomes();
        Object[] entries = chromosomes.toArray();
        int[] start = new int[num], end = new int[num];
        String chr_[] = new String[num];
        long t5 = System.currentTimeMillis();
        Random rand = new Random();
        for (int i=0; i<num; i++) {
            int iEntry = rand.nextInt(entries.length);
            chr_[i]= (String) entries[iEntry];
            
            int[] parseReg = tabixReader.parseReg(chr_[i]);

            int chr_size = parseReg[2];
            int delta = (chr_size/10)>1000000?1000000:(chr_size/10);
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
        t5 = System.currentTimeMillis() - t5;
        System.out.println("    --- Generating ranges: " + t5 + " (ms)");
        
        long t7 = System.currentTimeMillis();
        for (int i=0; i<num; i++) {
            TabixReader.Iterator query = tabixReader.query(chr_[i], start[i], end[i]);
            
            if (iterate) {
                String next = query.next();
                while (next!=null) {

                    next = query.next();
                }
            }
        }
        t7 = System.currentTimeMillis() - t7;
        System.out.println("        Tabix Query for " + num + " queries - " + t7 + " ms");
        
        
        
        tabixReader.close();
    }

    // VCF Iterate
    private static void test_6(SeekableStream s_file) throws IOException {

        InputStream in = s_file;
        VCFIterator v_iter = new VCFIteratorBuilder().open(in);
        
        while (v_iter.hasNext()) {
            VariantContext next = v_iter.next();
            boolean emptyID = next.emptyID();
        }
        
        v_iter.close();
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
