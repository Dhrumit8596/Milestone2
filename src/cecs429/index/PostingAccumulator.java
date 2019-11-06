/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cecs429.index;

/**
 *
 * @author dhrum
 */
public class PostingAccumulator implements Comparable<PostingAccumulator>{
    private Posting mdocument;
    private Double mAccumulator;
  public PostingAccumulator(Posting document, double Accumulator)
  {
      mdocument = document;
      mAccumulator = Accumulator;
  }
  public Double getAccumulator()
  {
      return mAccumulator;
  }
  public Posting getPosting()
  {
      return mdocument;
  }
  public void setAccumulator(Double Accumulator)
  {
      mAccumulator = Accumulator;
  }

    @Override
    public int compareTo(PostingAccumulator o) {
        return o.getAccumulator().compareTo(mAccumulator);
    }
  
}
