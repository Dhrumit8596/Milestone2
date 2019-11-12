/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RankedRetrieval;

import disk.DiskInvertedIndex;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dhrum
 */
public class Tf_IdfRanking implements RankingStrategy{
 DiskInvertedIndex mIndex;
    
    public Tf_IdfRanking(DiskInvertedIndex index)
    {
        mIndex = index;
    }

    @Override
    public double getWQT(double N, double dft) {
        double idft = Math.log(N/dft);
        return idft;
    }

    @Override
    public double getWDT(double tftd, int docID) {
       return tftd;
    }

    @Override
    public double getLD(int docId) {
       double LD=0;
        try {
            LD = mIndex.getdocweights_LD(docId);
        } catch (IOException ex) {
            Logger.getLogger(DefaultRanking.class.getName()).log(Level.SEVERE, null, ex);
        }
     return LD;
    }
    
}
