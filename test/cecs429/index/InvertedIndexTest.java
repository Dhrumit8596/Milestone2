/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cecs429.index;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.query.BooleanQueryParser;
import cecs429.query.QueryComponent;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.EnglishTokenStream;
import disk.DiskIndexWriter;
import disk.DiskInvertedIndex;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.HashMap;
import static jdk.nashorn.internal.objects.NativeArray.map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Dipesh
 */
public class InvertedIndexTest {

    public InvertedIndexTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetVocabulary() throws IOException {

        System.out.println("Inverted Index Test Case I Complete");
        String query = "256.256.256.256";
        String expResult = "Document5";
        String results = mMethod(query);
        assertEquals(expResult.trim(), results.trim());
    }

    @Test
    public void testGetVocabularyTwo() throws IOException {
        System.out.println("Inverted Index Test Case II Complete");
        String query = "firstdocument";
        String expResult = "Document1";
        String results = mMethod(query);
        assertEquals(expResult.trim(), results.trim());
    }

    @Test
    public void testGetVocabularyThree() throws IOException {
        System.out.println("Inverted Index Test Case III Complete");
        String query = "JSON";
        String expResult = "Document2";
        String results = mMethod(query);
        assertEquals(expResult.trim(), results.trim());
    }

    @Test
    public void testGetVocabularyFour() throws IOException {

        System.out.println("Inverted Test Case IV Complete");
        String query = "Testing";
        String expResult = "Document1Document2";
        String results = mMethod(query);
        assertEquals(expResult.trim(), results.trim());
    }

    private static String mPath = "test\\";

    private static String mMethod(String query) throws IOException {

        DirectoryCorpus corpus = DirectoryCorpus.loadJsonDirectory(Paths.get(mPath).toAbsolutePath(), ".json");
        corpus = DirectoryCorpus.loadJsonDirectory(Paths.get(mPath).toAbsolutePath(), ".json");
        Indexes indexes = indexCorpus(corpus);
        AdvancedTokenProcessor processor = new AdvancedTokenProcessor();

        //writing INDEX on disk
        DiskIndexWriter disk_writer = new DiskIndexWriter();
        List<Long> voc_positions = disk_writer.write_posting(indexes.index, mPath + "\\index\\");
        List<Long> vocab_positions = disk_writer.write_vocab(indexes.index.getVocabulary(), mPath + "\\index\\");
        disk_writer.write_vocab_table(vocab_positions, voc_positions, mPath + "\\index\\");
        DiskInvertedIndex DII = new DiskInvertedIndex(mPath + "\\index\\");
        //writing Biword on disk
        List<Long> voc_positions_biword = disk_writer.write_posting(indexes.biword_index, mPath + "\\index\\biword\\");
        List<Long> vocab_positions_biword = disk_writer.write_vocab(indexes.biword_index.getVocabulary(), mPath + "\\index\\biword\\");
        disk_writer.write_vocab_table(vocab_positions_biword, voc_positions_biword, mPath + "\\index\\biword\\");
        DiskInvertedIndex DII_biword = new DiskInvertedIndex(mPath + "\\index\\biword\\");
        //DiskInvertedIndex[] i = {DII, DII_biword};
        indexes = new Indexes(DII, DII_biword);

        DiskInvertedIndex[] i = {DII, DII_biword};
        String results = "";
        BooleanQueryParser queryparser = new BooleanQueryParser();
        QueryComponent query_component = queryparser.parseQuery(query);
        List<Posting> result_docs = query_component.getPostings(indexes, processor);
        for (Posting p : result_docs) {
            results = results + corpus.getDocument(p.getDocumentId()).getTitle();
        }
        return results;
    }

    private static Indexes indexCorpus(DocumentCorpus corpus) throws IOException {

        //HashSet<String> vocabulary = new HashSet<>();
        List<Double> doc_weights_file = new ArrayList<>();
        double doc_weight = 0;
        double doc_length = 0;
        double byte_size = 0;
        double avg_tftd = 0;
        double doc_length_a = 0;
        double total_length_tftd = 0;
        AdvancedTokenProcessor processor = new AdvancedTokenProcessor();
        InvertedIndex index = new InvertedIndex();
        InvertedIndex biword = new InvertedIndex();
        for (Document d : corpus.getDocuments()) {
            EnglishTokenStream ets = new EnglishTokenStream(d.getContent());
            File doc = new File(d.getFilePath().toString());
            byte_size = (double) doc.length();
            int term_position = 0;
            String previous = "";
            HashMap<String, Integer> map = new HashMap<>();
            for (String s : ets.getTokens()) {
                term_position++;
                List<String> word = processor.processToken(s);

                for (int i = 0; i < word.size(); i++) {

                    if (map.containsKey(word.get(i))) {
                        int count = map.get(word.get(i));
                        map.put(word.get(i), count);
                    } else {
                        map.put(word.get(i), 1);
                    }
                    index.addTerm(word.get(i), d.getId(), term_position);

                    if (previous != "") {
                        biword.addTerm(previous + " " + word.get(i), d.getId(), term_position - 1);
                    }
                    previous = word.get(i);

                }

            }
            doc_length = (double) term_position;
            double w_d_t = 0;
            double ld = 0;
            double total_tftd = 0;
            for (HashMap.Entry<String, Integer> entry : map.entrySet()) {
                int tftd = entry.getValue();
                total_tftd += tftd;
                w_d_t = 1 + Math.log(tftd);
                ld += (w_d_t * w_d_t);
            }
            ld = Math.sqrt(ld);
            doc_weight = ld;
            total_length_tftd += total_tftd;
            avg_tftd = total_tftd / (double) map.size();
            doc_weights_file.add(doc_weight);
            doc_weights_file.add(doc_length);
            doc_weights_file.add(byte_size);
            doc_weights_file.add(avg_tftd);
            ets.close();
        }
        doc_length_a = total_length_tftd / (double) corpus.getCorpusSize();
        doc_weights_file.add(doc_length_a);
        DiskIndexWriter DiskWriter = new DiskIndexWriter();
        DiskWriter.write_doc_weights(doc_weights_file, mPath + "\\index\\");
        Indexes i = new Indexes(index, biword);

        return i;
    }
}
