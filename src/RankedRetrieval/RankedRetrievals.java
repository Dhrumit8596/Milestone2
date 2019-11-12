/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RankedRetrieval;

import cecs429.index.Index;
import cecs429.index.Indexes;
import cecs429.index.Posting;
import cecs429.index.PostingAccumulator;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.TokenProcessor;
import disk.DiskInvertedIndex;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

/**
 *
 * @author dhrum
 */
public class RankedRetrievals {
    
    private String mqueries[];
    private String mpath;
    private int msize;
    public RankedRetrievals(String query, String path, int size)
    {
        mpath = path;
        mqueries  = query.split(" ");
        msize = size;
    }
    
    public List<PostingAccumulator> getPostings(Indexes indexes, TokenProcessor processor, RankingStrategy rank_strategy) throws IOException
    {
        for(int i =0; i<mqueries.length;i++)
        {
            List<String> s = new ArrayList(processor.processToken(mqueries[i]));
            mqueries[i] = s.get(s.size() - 1);
        }
        List<PostingAccumulator> results = new ArrayList<>();
        HashMap<Integer, PostingAccumulator> map = new HashMap<>();
        double N = (double)msize;
        for(String s : mqueries)
        {
            List<Posting> postings = indexes.index.getPostingsWithoutPositions(s);
            double dft = postings.size();
            double wqt = rank_strategy.getWQT(N, dft);
            for(Posting p : postings)
            {
                double tftd = p.gettftd();
                double wdt = rank_strategy.getWDT(tftd, p.getDocumentId());
                
                double increment = wdt * wqt;
                if(map.containsKey(p.getDocumentId()))
                {
                    PostingAccumulator postingaccumulator = map.get(p.getDocumentId());
                    double Ad = postingaccumulator.getAccumulator() + increment;
                    postingaccumulator.setAccumulator(Ad);
                    map.put(p.getDocumentId(), postingaccumulator);
                }
                else
                {
                    map.put(p.getDocumentId(), new PostingAccumulator(p,increment));
                }
            }
            
        }
        //DiskInvertedIndex DII = new DiskInvertedIndex(mpath+"\\index\\");
        PriorityQueue<PostingAccumulator> PQ = new PriorityQueue<>();
        
        for(HashMap.Entry<Integer, PostingAccumulator> entry: map.entrySet())
        {
            double LD = rank_strategy.getLD(entry.getKey());
            System.out.println("LD"+LD);
            PostingAccumulator p = entry.getValue();
            Double Ad;
            if(p.getAccumulator() != 0)
            {
            Ad = p.getAccumulator()/LD;
            p.setAccumulator(Ad);
            }
            PQ.add(p);
        }
        
        int size = PQ.size();
        int i =0;
        while(i<10 && i<size)
        {
         results.add(PQ.poll());
         i++;
        }
        
      return results;
    }
    
}
