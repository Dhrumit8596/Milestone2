package disk;

import java.util.*;

import cecs429.index.Index;
import cecs429.index.Posting;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import java.util.*;

import cecs429.index.Index;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiskInvertedIndex implements Index {

    private String mPath;
    private RandomAccessFile mVocabList;
    private RandomAccessFile mPostings;
    private RandomAccessFile mDocWeights;
    private long[] mVocabTable;

    // Opens a disk inverted index that was constructed in the given path.
    public DiskInvertedIndex(String path) {
        try {
            mPath = path;
            mVocabList = new RandomAccessFile(new File(path, "vocab.bin"), "r");
            mPostings = new RandomAccessFile(new File(path, "postings.bin"), "r");
            mVocabTable = readVocabTable(path);

            //mFileNames = readFileNames(path);
        } catch (FileNotFoundException ex) {
            System.out.println(ex.toString());
        }
    }

    // Locates the byte position of the postings for the given term.
    // For example, binarySearchVocabulary("angel") will return the byte position
    // to seek to in postings.bin to find the postings for "angel".
    private long binarySearchVocabulary(String term) {
        // do a binary search over the vocabulary, using the vocabTable and the file vocabList.
        int i = 0, j = mVocabTable.length / 2 - 1;
        while (i <= j) {
            try {
                int m = (i + j) / 2;
                long vListPosition = mVocabTable[m * 2];
                int termLength;
                if (m == mVocabTable.length / 2 - 1) {
                    termLength = (int) (mVocabList.length() - mVocabTable[m * 2]);
                } else {
                    termLength = (int) (mVocabTable[(m + 1) * 2] - vListPosition);
                }

                mVocabList.seek(vListPosition);

                byte[] buffer = new byte[termLength];
                mVocabList.read(buffer, 0, termLength);
                String fileTerm = new String(buffer, "ASCII");

                int compareValue = term.compareTo(fileTerm);
                if (compareValue == 0) {
                    //System.out.print("found it!");
                    return mVocabTable[m * 2 + 1];
                } else if (compareValue < 0) {
                    j = m - 1;
                } else {
                    i = m + 1;
                }
            } catch (IOException ex) {
                System.out.println(ex.toString());
            }
        }
        return -1;
    }

    // Reads the file vocabTable.bin into memory.
    private static long[] readVocabTable(String indexName) {
        try {
            long[] vocabTable;

            RandomAccessFile tableFile = new RandomAccessFile(
                    new File(indexName, "vocabTable.bin"),
                    "r");
           int tableIndex = 0;
            vocabTable = new long[((int) tableFile.length() / 16) * 2];
            byte[] byteBuffer = new byte[8];
           
            while (tableFile.read(byteBuffer, 0, byteBuffer.length) > 0) { // while we keep reading 4 bytes

                vocabTable[tableIndex] = ByteBuffer.wrap(byteBuffer).getLong();
                tableIndex++;
            }
            tableFile.close();
            return vocabTable;
        } catch (FileNotFoundException ex) {
            System.out.println(ex.toString());
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }
        return null;
    }

    public int getTermCount() {
        return mVocabTable.length / 2;
    }

    @Override
    public List<Posting> getPostingsWithPositions(String term) {

        long term_position = binarySearchVocabulary(term);
        List<Posting> posting_list = new ArrayList<>();
        if (term_position == -1) {
            return posting_list;
        }
        try {
            mPostings.seek(term_position);
        } catch (IOException ex) {
            Logger.getLogger(DiskInvertedIndex.class.getName()).log(Level.SEVERE, null, ex);
        }

        byte[] byteBuffer = new byte[4];
        try {
            mPostings.read(byteBuffer, 0, byteBuffer.length);
            ByteBuffer wrapped = ByteBuffer.wrap(byteBuffer);
            int dft = wrapped.getInt();
            int prev = 0;
            int doc_id;
            for (int i = 0; i < dft; i++) {
                mPostings.read(byteBuffer, 0, byteBuffer.length);
                doc_id = prev + ByteBuffer.wrap(byteBuffer).getInt();
                prev = ByteBuffer.wrap(byteBuffer).getInt();
                mPostings.read(byteBuffer, 0, byteBuffer.length);
                int tftd = ByteBuffer.wrap(byteBuffer).getInt();
                int position = 0;
                List<Integer> position_list = new ArrayList();
                for (int j = 0; j < tftd; j++) {
                    mPostings.read(byteBuffer, 0, byteBuffer.length);
                    position = position + ByteBuffer.wrap(byteBuffer).getInt();
                    position_list.add(position);
                }

                Posting p = new Posting(doc_id, position_list);
                posting_list.add(p);
            }

        } catch (IOException ex) {
            Logger.getLogger(DiskInvertedIndex.class.getName()).log(Level.SEVERE, null, ex);
        }

        return posting_list;
    }

    public double getdocweights_LD(int docID) throws IOException {
        mDocWeights = new RandomAccessFile(new File(mPath, "docWeights.bin"), "r");
        long weight_position = docID * (8 * 4);
        mDocWeights.seek(weight_position);
        byte[] byteBuffer = new byte[8];
        mDocWeights.read(byteBuffer, 0, byteBuffer.length);
        ByteBuffer wrapped = ByteBuffer.wrap(byteBuffer);
        double weight = wrapped.getDouble();

        return weight;
    }

    public double getdocLength(int docID) throws FileNotFoundException, IOException {
        mDocWeights = new RandomAccessFile(new File(mPath, "docWeights.bin"), "r");
        long weight_position = (docID) * 8 * 4 + 8;

        mDocWeights.seek(weight_position);
        byte[] byteBuffer = new byte[8];
        mDocWeights.read(byteBuffer, 0, byteBuffer.length);
        ByteBuffer wrapped = ByteBuffer.wrap(byteBuffer);
        double doclength = wrapped.getDouble();

        return doclength;
    }

    public double getbyteSize(int docID) throws FileNotFoundException, IOException {
        mDocWeights = new RandomAccessFile(new File(mPath, "docWeights.bin"), "r");
        long weight_position = (docID) * 8 * 4 + 16;
        mDocWeights.seek(weight_position);
        byte[] byteBuffer = new byte[8];
        mDocWeights.read(byteBuffer, 0, byteBuffer.length);
        ByteBuffer wrapped = ByteBuffer.wrap(byteBuffer);
        double bytesize = wrapped.getDouble();

        return bytesize;
    }

    public double getavgTftd(int docID) throws FileNotFoundException, IOException {
        mDocWeights = new RandomAccessFile(new File(mPath, "docWeights.bin"), "r");
        long weight_position = (docID) * 8 * 4 + 24;
        mDocWeights.seek(weight_position);
        byte[] byteBuffer = new byte[8];
        mDocWeights.read(byteBuffer, 0, byteBuffer.length);
        ByteBuffer wrapped = ByteBuffer.wrap(byteBuffer);
        double avgTftd = wrapped.getDouble();
        return avgTftd;
    }

    public double getdocLengthA() throws FileNotFoundException, IOException {
        mDocWeights = new RandomAccessFile(new File(mPath, "docWeights.bin"), "r");
        long position = mDocWeights.length() - 8;
        mDocWeights.seek(position);
        byte[] byteBuffer = new byte[8];
        mDocWeights.read(byteBuffer, 0, byteBuffer.length);
        ByteBuffer wrapped = ByteBuffer.wrap(byteBuffer);
        double docLengthA = wrapped.getDouble();
        return docLengthA;
    }

    @Override
    public List<String> getVocabulary() {
        List<String> vocab = new ArrayList<>(); 
        int i = 0, j = mVocabTable.length / 2 - 1;
        while(i<=j)
        {   
                
                long vListPosition = mVocabTable[i * 2];
                int termLength = 0;
                if (i == mVocabTable.length / 2 - 1) {
                    try {
                        termLength = (int) (mVocabList.length() - mVocabTable[i * 2]);
                    } catch (IOException ex) {
                        Logger.getLogger(DiskInvertedIndex.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    termLength = (int) (mVocabTable[(i + 1) * 2] - vListPosition);
                }

            try {
                mVocabList.seek(vListPosition);
            } catch (IOException ex) {
                Logger.getLogger(DiskInvertedIndex.class.getName()).log(Level.SEVERE, null, ex);
            }

                byte[] buffer = new byte[termLength];
            try {
                mVocabList.read(buffer, 0, termLength);
            } catch (IOException ex) {
                Logger.getLogger(DiskInvertedIndex.class.getName()).log(Level.SEVERE, null, ex);
            }
                String fileTerm = null;
            try {
                fileTerm = new String(buffer, "ASCII");
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(DiskInvertedIndex.class.getName()).log(Level.SEVERE, null, ex);
            }

            vocab.add(fileTerm);
            i++;
            
        }
        return vocab;
    }

    @Override
    public List<Posting> getPostingsWithoutPositions(String term) {
        long term_position = binarySearchVocabulary(term);
        List<Posting> posting_list = new ArrayList<>();
        if (term_position == -1) {
            return posting_list;
        }
        try {
            mPostings.seek(term_position);
        } catch (IOException ex) {
            Logger.getLogger(DiskInvertedIndex.class.getName()).log(Level.SEVERE, null, ex);
        }

        
        byte[] byteBuffer = new byte[4];
        try {
            mPostings.read(byteBuffer, 0, byteBuffer.length);
            ByteBuffer wrapped = ByteBuffer.wrap(byteBuffer);
            int dft = wrapped.getInt();
            int prev = 0;
            int doc_id;
            for (int i = 0; i < dft; i++) {
                mPostings.read(byteBuffer, 0, byteBuffer.length);
                doc_id = prev + ByteBuffer.wrap(byteBuffer).getInt();
                prev = ByteBuffer.wrap(byteBuffer).getInt();
                mPostings.read(byteBuffer, 0, byteBuffer.length);
                int tftd = ByteBuffer.wrap(byteBuffer).getInt();

                int position = tftd * byteBuffer.length;
                mPostings.skipBytes(position);
                Posting p = new Posting(doc_id, tftd);
                posting_list.add(p);
            }

        } catch (IOException ex) {
            Logger.getLogger(DiskInvertedIndex.class.getName()).log(Level.SEVERE, null, ex);
        }

        return posting_list;
    }
}
