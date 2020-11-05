/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package politicalnetwork.tests.keyplayers;

import politicalnetwork.core.IPoliticalNetwork;
import politicalnetwork.core.KeyPlayersSelector;
import politicalnetwork.core.PoliticalNetwork;
import politicalnetwork.rationalnumber.InfinitePrecisionRationalNumber;
import politicalnetwork.rationalnumber.RationalNumber;

/**
 *
 * @author jesjf
 */
public class GenExamples
{
    
    
    public static KeyPlayersSelector getBorgattiExample()
    {        
        RationalNumber.Factory numberFact = new InfinitePrecisionRationalNumber.Factory();
        KeyPlayersSelector politicalNetwork = new KeyPlayersSelector(numberFact,"Borgatti Example",1,false,new PoliticalNetwork.IDTierBreaker(),null);       
        for (int candId=1; candId<=19; candId++)   
        {    
            politicalNetwork.addCandidate(candId);
            politicalNetwork.setNumberOfVotes(candId, 1); 
        }   
        
        politicalNetwork.addNeighborRelationship(1, 2, numberFact.valueOf(1,3));                   
        politicalNetwork.addNeighborRelationship(1, 4, numberFact.valueOf(1,3));                   
        politicalNetwork.addNeighborRelationship(1, 6, numberFact.valueOf(1,3));                   
        
        politicalNetwork.addNeighborRelationship(2, 1, numberFact.valueOf(1,4));                   
        politicalNetwork.addNeighborRelationship(2, 4, numberFact.valueOf(1,4));                   
        politicalNetwork.addNeighborRelationship(2, 5, numberFact.valueOf(1,4));                   
        politicalNetwork.addNeighborRelationship(2, 7, numberFact.valueOf(1,4));                   
        
        politicalNetwork.addNeighborRelationship(3, 4, numberFact.valueOf(1,2));                   
        politicalNetwork.addNeighborRelationship(3, 8, numberFact.valueOf(1,2));                   
        
        politicalNetwork.addNeighborRelationship(4, 1, numberFact.valueOf(1,6));                   
        politicalNetwork.addNeighborRelationship(4, 2, numberFact.valueOf(1,6));                   
        politicalNetwork.addNeighborRelationship(4, 3, numberFact.valueOf(1,6));                   
        politicalNetwork.addNeighborRelationship(4, 5, numberFact.valueOf(1,6));                   
        politicalNetwork.addNeighborRelationship(4, 6, numberFact.valueOf(1,6));                   
        politicalNetwork.addNeighborRelationship(4, 7, numberFact.valueOf(1,6));                   
        
        politicalNetwork.addNeighborRelationship(5, 2, numberFact.valueOf(1,3));                   
        politicalNetwork.addNeighborRelationship(5, 4, numberFact.valueOf(1,3));                   
        politicalNetwork.addNeighborRelationship(5, 7, numberFact.valueOf(1,3));                   
        
        politicalNetwork.addNeighborRelationship(6, 1, numberFact.valueOf(1,4));                   
        politicalNetwork.addNeighborRelationship(6, 4, numberFact.valueOf(1,4));                   
        politicalNetwork.addNeighborRelationship(6, 7, numberFact.valueOf(1,4));                   
        politicalNetwork.addNeighborRelationship(6, 8, numberFact.valueOf(1,4));                   
        
        politicalNetwork.addNeighborRelationship(7, 2, numberFact.valueOf(1,5));                   
        politicalNetwork.addNeighborRelationship(7, 4, numberFact.valueOf(1,5));                   
        politicalNetwork.addNeighborRelationship(7, 5, numberFact.valueOf(1,5));                   
        politicalNetwork.addNeighborRelationship(7, 6, numberFact.valueOf(1,5));                   
        politicalNetwork.addNeighborRelationship(7, 8, numberFact.valueOf(1,5));                   
        
        politicalNetwork.addNeighborRelationship(8, 3, numberFact.valueOf(1,5));                   
        politicalNetwork.addNeighborRelationship(8, 6, numberFact.valueOf(1,5));                   
        politicalNetwork.addNeighborRelationship(8, 7, numberFact.valueOf(1,5));                   
        politicalNetwork.addNeighborRelationship(8, 9, numberFact.valueOf(1,5));                   
        politicalNetwork.addNeighborRelationship(8, 18, numberFact.valueOf(1,5));                   
        
        politicalNetwork.addNeighborRelationship(9, 8, numberFact.valueOf(1,4));                   
        politicalNetwork.addNeighborRelationship(9, 10, numberFact.valueOf(1,4));                   
        politicalNetwork.addNeighborRelationship(9, 14, numberFact.valueOf(1,4));                   
        politicalNetwork.addNeighborRelationship(9, 16, numberFact.valueOf(1,4));                   
        
        politicalNetwork.addNeighborRelationship(10, 9, numberFact.valueOf(1,6));                   
        politicalNetwork.addNeighborRelationship(10, 11, numberFact.valueOf(1,6));                   
        politicalNetwork.addNeighborRelationship(10, 12, numberFact.valueOf(1,6));                   
        politicalNetwork.addNeighborRelationship(10, 13, numberFact.valueOf(1,6));                   
        politicalNetwork.addNeighborRelationship(10, 15, numberFact.valueOf(1,6));                   
        politicalNetwork.addNeighborRelationship(10, 16, numberFact.valueOf(1,6));                   
        
        politicalNetwork.addNeighborRelationship(11, 9, numberFact.valueOf(1,3));                   
        politicalNetwork.addNeighborRelationship(11, 10, numberFact.valueOf(1,3));                           
        politicalNetwork.addNeighborRelationship(11, 16, numberFact.valueOf(1,3));                   
        
        politicalNetwork.addNeighborRelationship(12, 10, numberFact.valueOf(1,3));                   
        politicalNetwork.addNeighborRelationship(12, 11, numberFact.valueOf(1,3));                   
        politicalNetwork.addNeighborRelationship(12, 13, numberFact.valueOf(1,3));                   
        
        politicalNetwork.addNeighborRelationship(13, 10, numberFact.valueOf(1,3));                   
        politicalNetwork.addNeighborRelationship(13, 12, numberFact.valueOf(1,3));                   
        politicalNetwork.addNeighborRelationship(13, 17, numberFact.valueOf(1,3));                   
        
        politicalNetwork.addNeighborRelationship(14, 9, numberFact.valueOf(1,3));                   
        politicalNetwork.addNeighborRelationship(14, 15, numberFact.valueOf(1,3));                   
        politicalNetwork.addNeighborRelationship(14, 16, numberFact.valueOf(1,3));                   
        
        politicalNetwork.addNeighborRelationship(15, 10, numberFact.valueOf(1,2));                   
        politicalNetwork.addNeighborRelationship(15, 14, numberFact.valueOf(1,2));                   
        
        politicalNetwork.addNeighborRelationship(16, 9, numberFact.valueOf(1,4));                   
        politicalNetwork.addNeighborRelationship(16, 10, numberFact.valueOf(1,4));                   
        politicalNetwork.addNeighborRelationship(16, 11, numberFact.valueOf(1,4));                   
        politicalNetwork.addNeighborRelationship(16, 14, numberFact.valueOf(1,4));                   
        
        politicalNetwork.addNeighborRelationship(17, 13, numberFact.valueOf(1,2));                   
        politicalNetwork.addNeighborRelationship(17, 19, numberFact.valueOf(1,2));                   
        
        politicalNetwork.addNeighborRelationship(18, 8, numberFact.valueOf(1,1));                   
        
        politicalNetwork.addNeighborRelationship(19, 17, numberFact.valueOf(1,1));                   
        
        // a1 b2 c3 d4 e5 f6 g7 h8 i9 j10 k11 l12 m13 n14 o15 p16 q17 r18 s19 t20 u21 v22 w23 x24 y25 z26        

        return politicalNetwork;
        
    }   
    
    public static KeyPlayersSelector get4ComponentsExample()
    {        
        RationalNumber.Factory numberFact = new InfinitePrecisionRationalNumber.Factory();
        KeyPlayersSelector politicalNetwork = new KeyPlayersSelector(numberFact,"Borgatti Example",1,false,new PoliticalNetwork.IDTierBreaker(),null);       
        for (int candId=1; candId<=150; candId++)   
        {    
            politicalNetwork.addCandidate(candId);
            politicalNetwork.setNumberOfVotes(candId, 1); 
        }   
        for (int candId=1; candId<=3; candId++)   
            for (int neighborId=candId+1; neighborId<=3; neighborId++)   
            {
                politicalNetwork.addNeighborRelationship(candId, neighborId, numberFact.valueOf(1,59));                   
                politicalNetwork.addNeighborRelationship(neighborId, candId, numberFact.valueOf(1,59));                   
            }    
        for (int candId=1; candId<=3; candId++)   
            for (int neighborId=4; neighborId<=60; neighborId++)   
            {
                politicalNetwork.addNeighborRelationship(candId, neighborId, numberFact.valueOf(1,59));                   
                politicalNetwork.addNeighborRelationship(neighborId, candId,  numberFact.valueOf(1,3));                   
            }            
        for (int neighborId=62; neighborId<=100; neighborId++)   
        {
            politicalNetwork.addNeighborRelationship(61, neighborId, numberFact.valueOf(1,39));                   
            politicalNetwork.addNeighborRelationship(neighborId,61, numberFact.valueOf(1,1));                   
        }    
        for (int neighborId=102; neighborId<=125; neighborId++)   
        {
            politicalNetwork.addNeighborRelationship(101, neighborId, numberFact.valueOf(1,24));                   
            politicalNetwork.addNeighborRelationship(neighborId,101, numberFact.valueOf(1,1));                   
        }    
        for (int neighborId=127; neighborId<=150; neighborId++)   
        {
            politicalNetwork.addNeighborRelationship(126, neighborId, numberFact.valueOf(1,24));                   
            politicalNetwork.addNeighborRelationship(neighborId,126, numberFact.valueOf(1,1));                   
        }            
        return politicalNetwork;
        
    }   
    
}
