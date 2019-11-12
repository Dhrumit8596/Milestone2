/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RankedRetrieval;

import cecs429.index.Index;

/**
 *
 * @author dhrum
 */
public interface RankingStrategy {
    double getWQT(double N, double dft);
    double getWDT(double tftd, int docID);
    double getLD(int docId);
    
}
