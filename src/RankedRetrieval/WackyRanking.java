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
public class WackyRanking implements RankingStrategy{

    DiskInvertedIndex mIndex;
    
    public WackyRanking(DiskInvertedIndex index)
    {
        mIndex = index;
    }
    @Override
    public double getWQT(double N, double dft) {
       double a = Math.log((N-dft)/dft);
       double wqt = Math.max(0, a);
       return wqt;
    }

    @Override
    public double getWDT(double tftd, int docID) {
       double avg_tftd=0;
        try {
            avg_tftd = mIndex.getavgTftd(docID);
        } catch (IOException ex) {
            Logger.getLogger(WackyRanking.class.getName()).log(Level.SEVERE, null, ex);
        }
       double numerator = 1 + Math.log(tftd);
       double denominator = 1 + Math.log(avg_tftd);
       double wdt = numerator/denominator;
       return wdt;
    }

    @Override
    public double getLD(int docId) {
       double bytesize = 0;
        try {
            bytesize = mIndex.getbyteSize(docId);
        } catch (IOException ex) {
            Logger.getLogger(WackyRanking.class.getName()).log(Level.SEVERE, null, ex);
        }
       double LD = Math.sqrt(bytesize);
       return LD;
    }
    
}
