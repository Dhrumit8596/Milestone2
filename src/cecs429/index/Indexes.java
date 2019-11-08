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
public class Indexes {
    public static Index index;
    public static Index biword_index;
    
    public Indexes(Index i, Index biword)
    {
        index = i;
        biword_index = biword;
    }
}
