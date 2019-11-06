/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.index.PostingAccumulator;
import cecs429.text.AdvancedTokenProcessor;
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
    public RankedRetrievals(String query)
    {
        mqueries  = query.split(" ");
        AdvancedTokenProcessor processor = new AdvancedTokenProcessor(); 
        
        for(int i =0; i<mqueries.length;i++)
        {
            List<String> s = new ArrayList(processor.processToken(mqueries[i]));
            mqueries[i] = s.get(s.size() - 1);
        }
    }
    
    public List<PostingAccumulator> getPostings(Index[] index) throws IOException
    {
        List<PostingAccumulator> results = new ArrayList<>();
        HashMap<Integer, PostingAccumulator> map = new HashMap<>();
        double N = mqueries.length;
        for(String s : mqueries)
        {
            List<Posting> postings = index[0].getPostingsWithoutPositions(s);
            double dft = postings.size();
            double wqt = Math.log(1 + (N/dft));
            for(Posting p : postings)
            {
                double wdt = 1 + Math.log(p.gettftd());
                double increment = wdt * wqt;
                if(map.containsKey(p.getDocumentId()))
                {
                    PostingAccumulator postingaccumulator = map.get(p.getDocumentId());
                    double Ad = postingaccumulator.getAccumulator() + increment;
                    postingaccumulator.setAccumulator(increment);
                    map.put(p.getDocumentId(), postingaccumulator);
                }
                else
                {
                    map.put(p.getDocumentId(), new PostingAccumulator(p,increment));
                }
            }
            
        }
        DiskInvertedIndex DII = new DiskInvertedIndex("C:\\Docs\\Study\\SET\\Milestone2\\Milestone2\\index");
        PriorityQueue<PostingAccumulator> PQ = new PriorityQueue<>();
        
        for(HashMap.Entry<Integer, PostingAccumulator> entry: map.entrySet())
        {
            Double LD = DII.getdocweights_LD(entry.getKey());
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
