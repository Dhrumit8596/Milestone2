package cecs429.index;

import java.util.List;

/**
 * A Posting encapulates a document ID associated with a search query component.
 */
public class Posting {

    private int mDocumentId;
    private List<Integer> mPositionList;
    private int mtftd;

    public Posting(int documentId, List<Integer> positionList) {
        mDocumentId = documentId;
        mPositionList = positionList;
    }

    public Posting(int documentId) {
        mDocumentId = documentId;

    }
    
    public Posting(int documentID, int tftd)
    {
        mDocumentId = documentID;
        mtftd = tftd;
    }
    
    public int gettftd()
    {
        return mtftd;
    }

    public int getDocumentId() {
        return mDocumentId;
    }

    public List<Integer> getPositionList() {
        return mPositionList;
    }

    public void setPostionList(List<Integer> position) {
        mPositionList = position;
    }

    @Override
    public boolean equals(Object p) {
        boolean sameSame = false;

        if (p != null && p instanceof Posting) {
            sameSame = this.mDocumentId == ((Posting) p).getDocumentId();
        }

        return sameSame;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + this.mDocumentId;
        return hash;
    }

}
