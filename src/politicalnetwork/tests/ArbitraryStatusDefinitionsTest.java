/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package politicalnetwork.tests;

import java.util.ArrayList;
import java.util.List;
import politicalnetwork.testimplementationsimp.PoliticalNetwork;
import politicalnetwork.testimplementationsimp.IPoliticalNetwork;
import politicalnetwork.rationalnumber.RationalNumber;
import java.util.Random;
import politicalnetwork.testimplementationsimp.PoliticalNetwork.IDTierBreaker;
import politicalnetwork.rationalnumber.InfinitePrecisionRationalNumber;
import politicalnetwork.testimplementationsimp.Candidate;

/**
 *
 * @author R
 */
public class ArbitraryStatusDefinitionsTest
{
    
    static class RandomStatusDefinitions
    {
        List<Integer> elected;
        List<Integer> eliminated;
        RandomStatusDefinitions()
        {
            elected = new ArrayList();
            eliminated =  new ArrayList();
        }        
    }
    
    static void applyRandomStatusDefinitions(Random rand,int numCandidates,IPoliticalNetwork politicalNetwork,int []defCandidatesOri,boolean defStatusOri[])
    {
        int[] defCandidatesInds = getRandomIntsWithoutRepetitions(defCandidatesOri.length,defCandidatesOri.length, rand);
        int[] defCandidates = new int[defCandidatesOri.length];
        boolean[] defStatus = new boolean[defStatusOri.length];
        
        for (int t=0; t<defCandidates.length; t++)
        {    
           defCandidates[t] = defCandidatesOri[defCandidatesInds[t]-1];
           defStatus[t] = defStatusOri[defCandidatesInds[t]-1];
        }
        RandomStatusDefinitions lastDef = new RandomStatusDefinitions();
        int numDone = 0;        
        while (numDone < defCandidates.length)
        {
            if (rand.nextInt(4)==0)
            {                
                // apply list of simultaneous definitions
                politicalNetwork.defineStatusOfArbitraryCandidates(lastDef.eliminated, lastDef.elected);
                lastDef = new RandomStatusDefinitions();
            }
            if (defStatus[numDone])
                lastDef.elected.add(defCandidates[numDone]);
            else
                lastDef.eliminated.add(defCandidates[numDone]);
            numDone++;
        }    
        politicalNetwork.defineStatusOfArbitraryCandidates(lastDef.eliminated, lastDef.elected);
    }        
    
    public static void applySameRandomStatusDefinitionsInDifferentOrders(int seed)
    {   
        int numTh = Runtime.getRuntime().availableProcessors();
        Random rand = new Random(seed);
        int numCandidates = 250;
        int maxNeighbors = rand.nextInt(10);        
        int maxVotesPerCand = 10000; //00+rand.nextInt(10);
        int numDefCand = rand.nextInt(numCandidates / 50);
        int numSeats = 1+rand.nextInt(numCandidates);
        
        RationalNumber.Factory infFact = new InfinitePrecisionRationalNumber.Factory();
        IPoliticalNetwork politicalNetwork1 = new PoliticalNetwork(infFact,"Network 1 seed = "+seed,numTh,false,new IDTierBreaker(),null);       
        IPoliticalNetwork politicalNetwork2 = new PoliticalNetwork(infFact,"Network 2 seed = "+seed,numTh,false,new IDTierBreaker(),null);        
        
        try
        {    
 
            for (int candId=1; candId<=numCandidates; candId++)   
            {    
                politicalNetwork1.addCandidate(candId);
                politicalNetwork2.addCandidate(candId);
                int numCandVotes = rand.nextInt(maxVotesPerCand); 
                politicalNetwork1.setNumberOfVotes(candId, numCandVotes); 
                politicalNetwork2.setNumberOfVotes(candId, numCandVotes); 
            }    
            for (int candId=1; candId<=numCandidates; candId++)
            {  
               int numNeighbors;
               numNeighbors = rand.nextInt(Math.min(maxNeighbors+1,numCandidates)); 
               int [] vv = getRandomIntsWithoutRepetitions(numCandidates-1,numNeighbors,rand); 
               int usedPercentage = 0;   
               for (int tt=1; tt<=numNeighbors; tt++)
               {    
                   int neighborId = vv[tt-1];
                   if (neighborId >= candId) 
                       neighborId++;   // avoids linking the candidate to itself

                   // in this test we use only non zero integer percentages
                   int numNeighborsToGo = numNeighbors-tt;
                   int maxPercentage = 100-numNeighborsToGo-usedPercentage; // leaves at least 1 percent for each lasting neighbor

                   int percentage = tt==numNeighbors ?
                                    maxPercentage :
                                    1+rand.nextInt(maxPercentage); 
                   usedPercentage += percentage;
                   politicalNetwork1.addNeighborRelationship(candId, neighborId, infFact.valueOf(percentage,100));                   
                   politicalNetwork2.addNeighborRelationship(candId, neighborId, infFact.valueOf(percentage,100));                   
               }   
               if (numNeighbors==0 && usedPercentage!=0)
                   throw new RuntimeException("Percentages are not zero, when there are no neighbors");                       
               if (usedPercentage!=100 && numNeighbors!=0)
                   throw new RuntimeException("Percentages don't sum 100%");
            }        
            
            
            politicalNetwork1.setNumberOfSeats(numSeats);
            politicalNetwork2.setNumberOfSeats(numSeats);

            politicalNetwork1.prepareToProcess();
            politicalNetwork2.prepareToProcess();
            
            
            int[] defCandidates = getRandomIntsWithoutRepetitions(numCandidates,numDefCand,rand);
            boolean[] status = new boolean[numDefCand];
            
            RationalNumber q = politicalNetwork1.getCurrentQuota();
            for (int t=0; t<numDefCand; t++)
            {    
                Candidate c = politicalNetwork1.getCandidate(defCandidates[t]);
                if (c==null)
                    throw new RuntimeException("Candidate not found");
                RationalNumber v = c.getNumberOfCurrentVotes();
                status[t] = v.compareTo(q) >= 0;
            }
            
            
            applyRandomStatusDefinitions(rand,numCandidates,politicalNetwork1,defCandidates,status);
            applyRandomStatusDefinitions(rand,numCandidates,politicalNetwork2,defCandidates,status);

            if (!politicalNetwork1.equals(politicalNetwork2))
                throw new RuntimeException("Networks are different");;
            
        }
        finally
        {    
            politicalNetwork1.close();                
            politicalNetwork2.close();                
        }
    }        
    static int [] getRandomIntsWithoutRepetitions(int n,int l, Random r)
    {
        int [] a = new int[n];
        for (int t=0; t<n; t++) 
            a[t] = t;
        int res[] = new int[l]; 
        for (int t=0; t<l; t++)
        {
            int i = r.nextInt(a.length-t);
            res[t] = a[i]+1;
            a[i] = a[a.length-t-1];            
        }    
        return res;
    }
    public static void main(String argv[])
    {
        Random r = new Random(7);
        
        ArbitraryStatusDefinitionsTest.applySameRandomStatusDefinitionsInDifferentOrders(-1850488227);
        
        
        int max = 100000;
        int numFails = 0;
        
        
        for (int t=0; t<max; t++)
        {    
            int seed = r.nextInt();
            try
            {    
                ArbitraryStatusDefinitionsTest.applySameRandomStatusDefinitionsInDifferentOrders(seed);
            }
            catch(RuntimeException e)
            {
                    System.out.println("\nFailure with seed "+seed);                
                    numFails++;
                    e.printStackTrace();
            }    
            if (t%100==0)
                System.out.print("\n");            
            if (t%1000==0)
                System.out.println("\n"+t+" random status definitions already run. Fails = "+numFails);                        
            System.out.print(".");            
        }    
        System.out.println("\n"+max+" random status definitions already run. Fails = "+numFails);                        
        
    }        
    
}
