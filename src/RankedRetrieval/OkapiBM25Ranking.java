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
public class OkapiBM25Ranking implements RankingStrategy{
 DiskInvertedIndex mIndex;
    
    public OkapiBM25Ranking(DiskInvertedIndex index)
    {
        mIndex = index;
    }

    @Override
    public double getWQT(double N, double dft) {
    double a = 0.1;
    double b = Math.log((N-dft+0.5)/(dft+0.5));
    double wqt = Math.max(a, b);
    return wqt;
    }

    @Override
    public double getWDT(double tftd, int docID) {
     double doclength = 0,doclengthA = 0;
        try {
         doclength = mIndex.getdocLength(docID);
         doclengthA = mIndex.getdocLengthA();
     } catch (IOException ex) {
         Logger.getLogger(OkapiBM25Ranking.class.getName()).log(Level.SEVERE, null, ex);
     }
      double numerator = 2.2 * tftd;
      double denominator = 1.2*(0.25 + (0.75)*(doclength/doclengthA)) + tftd;
      double wdt = numerator/denominator;
      return wdt;
    }

    @Override
    public double getLD(int docId) {
      return 1;
    }
    
}
