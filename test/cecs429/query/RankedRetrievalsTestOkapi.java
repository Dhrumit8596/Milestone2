/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cecs429.query;

import RankedRetrieval.DefaultRanking;
import RankedRetrieval.OkapiBM25Ranking;
import RankedRetrieval.RankedRetrievals;
import RankedRetrieval.Tf_IdfRanking;
import RankedRetrieval.WackyRanking;
import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Indexes;
import cecs429.index.InvertedIndex;
import cecs429.index.Posting;
import cecs429.index.PostingAccumulator;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.EnglishTokenStream;
import csulb.DirectorySearch;
import disk.DiskIndexWriter;
import disk.DiskInvertedIndex;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class RankedRetrievalsTestOkapi {

    private static String mPath = "test\\";

    public RankedRetrievalsTestOkapi() {
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

    /**
     * Test of getPostings method, of class RankedRetrievals.
     */
    @Test
    public void testGetPostings() throws IOException {
        System.out.println("Test Case I Complete");
        String query = "this";
        double[] expAccum = new double[10];
        expAccum[0] = 0.118;
        expAccum[1] = 0.118;
        expAccum[2] = 0.086;
        String[] expDocs = new String[10];
        expDocs[0] = "Document3";
        expDocs[1] = "Document5";
        expDocs[2] = "Document1";
        String docs = "";

        int j = 0;
        double accumulator = 0;
        DirectoryCorpus corpus = DirectoryCorpus.loadJsonDirectory(Paths.get(mPath).toAbsolutePath(), ".json");
        int corp_size = corpus.getCorpusSize();
        List<PostingAccumulator> results = mMethod(query);
        for (PostingAccumulator p : results) {
            j++;
            Posting posting = p.getPosting();
            accumulator = p.getAccumulator();
            docs = corpus.getDocument(posting.getDocumentId()).getTitle();
            System.out.println(docs + " :: Accum value - " + accumulator);
            if (docs.equals(expDocs[j])) {
                assertEquals((new DecimalFormat("#0.000").format(accumulator)) + "", expAccum + "");

            }
        }
    }

    @Test
    public void testGetPostingsTwo() throws IOException {
        System.out.println("Test Case II Complete");
        String query = "Hello";
        double expAccum = 0.942;
        String expDocs = "";
        expDocs = "Document1";
        String docs = "";

        int j = 0;
        double accumulator = 0;
        DirectoryCorpus corpus = DirectoryCorpus.loadJsonDirectory(Paths.get(mPath).toAbsolutePath(), ".json");
        int corp_size = corpus.getCorpusSize();
        List<PostingAccumulator> results = mMethod(query);
        for (PostingAccumulator p : results) {
            j++;
            Posting posting = p.getPosting();
            accumulator = p.getAccumulator();
            docs = corpus.getDocument(posting.getDocumentId()).getTitle();
            if (docs.equals(expDocs)) {
                assertEquals((new DecimalFormat("#0.000").format(accumulator)) + "", expAccum + "");
            }
        }
    }

    private static List<PostingAccumulator> mMethod(String query) throws IOException {
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
        RankedRetrievals r = new RankedRetrievals(query, mPath, corpus.getCorpusSize());
        List<PostingAccumulator> Ranking_results = new ArrayList<>();
        OkapiBM25Ranking ranking_strategy = new OkapiBM25Ranking(DII);
        Ranking_results = r.getPostings(indexes, processor, ranking_strategy);
        return Ranking_results;
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
