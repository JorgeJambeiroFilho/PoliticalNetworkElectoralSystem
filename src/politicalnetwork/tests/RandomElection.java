/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package politicalnetwork.tests;

import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.ArrayList;
import java.util.Map.Entry;
import politicalnetwork.core.PoliticalNetwork;
import politicalnetwork.core.IPoliticalNetwork;
import politicalnetwork.rationalnumber.DoublePrecisionRationalNumber;
import politicalnetwork.rationalnumber.RationalNumber;
import java.util.Random;
import java.util.TreeMap;
import politicalnetwork.core.Candidate;
import politicalnetwork.rationalnumber.InfinitePrecisionRationalNumber;

/**
 *
 * @author jesjf22
 */
public class RandomElection
{
    
    static class PartyData
    {
        int totalNumberOfVotesOfParty;
        TreeMap<Integer/*number of votes*/,Integer/*candidate id*/> partyCandidates;
        boolean isSolidCoalition;
        boolean isPartyCoalition;
        PartyData()
        {
            totalNumberOfVotesOfParty = 0;
            partyCandidates = new TreeMap();            
        }        
        void check(int numValidVotes,int numSeats,TIntObjectHashMap<Candidate> elected)
        {   
           if (!isSolidCoalition) return;
           int minElected = Math.min(partyCandidates.size(),totalNumberOfVotesOfParty * numSeats / numValidVotes);
           
           int numElected = 0;
           boolean anyOneNotElected = false;
           for (Entry<Integer,Integer> candData:partyCandidates.descendingMap().entrySet())
               if (elected.contains(candData.getValue()))
               {    
                   if (isPartyCoalition && anyOneNotElected)
                       throw new RuntimeException("Elected memeber of party were not the most voted");           
                   numElected++;
               }    
               else
                   anyOneNotElected = true;
           if (numElected < minElected)
               throw new RuntimeException("Party did not have enough candidates elected");           
        }        
    }
    
    public static void runRandomElection(int seed)
    {   
        int numTh = Runtime.getRuntime().availableProcessors();
        RationalNumber.Factory rationalFactory = new InfinitePrecisionRationalNumber.Factory();
        IPoliticalNetwork politicalNetwork = new PoliticalNetwork(rationalFactory,"Random Political Network seed = "+seed,numTh,false);
        Random rand = new Random(seed);
        int numParties = 5+rand.nextInt(5);
        int maxCandParty = 1+rand.nextInt(300 / numParties);
        int maxNeighbors = rand.nextInt(100);        
        int maxVotesPerCand = 1000+rand.nextInt(10000000);
        
        
        int numCandidates = 0;
        PartyData[] partyData = new PartyData[numParties];
        int numValidVotes = 0;
        
        for (int p=0; p<numParties; p++)
        {
            partyData[p] = new PartyData();
            int numCandidatesOfParty = 1+rand.nextInt(maxCandParty);            
            int partyId = numCandidates + 1; // create identifiers in a range above previous parties and avoid zero
            boolean isSolidCoalition = rand.nextBoolean();
            boolean isPartyCoalition = rand.nextBoolean();
            partyData[p].isSolidCoalition = isSolidCoalition || isPartyCoalition;
            partyData[p].isPartyCoalition = isPartyCoalition;
            
            politicalNetwork.addVirtualCandidate(partyId);             
            int numPartyVotes = rand.nextInt(maxVotesPerCand); 
            politicalNetwork.setNumberOfVotes(partyId, numPartyVotes); 
            partyData[p].totalNumberOfVotesOfParty += numPartyVotes;
            numValidVotes += numPartyVotes;
            for (int candId=partyId+1; candId<=partyId+numCandidatesOfParty; candId++)   
            {    
                politicalNetwork.addCandidate(candId);
                int numCandVotes = rand.nextInt(maxVotesPerCand); 
                politicalNetwork.setNumberOfVotes(candId, numCandVotes); 
                partyData[p].totalNumberOfVotesOfParty += numCandVotes;
                partyData[p].partyCandidates.put(numCandVotes,candId);                
                numValidVotes += numCandVotes;
            }    

            for (int candId=partyId+1; candId<=partyId+numCandidatesOfParty; candId++)
                politicalNetwork.addNeighborRelationship(partyId, candId, rationalFactory.valueOf(1,numCandidatesOfParty));
                        
            if (isPartyCoalition)
            {
                for (int candId=partyId+1; candId<=partyId+numCandidatesOfParty; candId++)
                    for (int neighborId=partyId+1; neighborId<=partyId+numCandidatesOfParty; neighborId++)
                       if (candId!=neighborId)    
                           politicalNetwork.addNeighborRelationship(candId, neighborId, rationalFactory.valueOf(1,numCandidatesOfParty-1));                   
            }
            else
            {    
                for (int candId=partyId+1; candId<=partyId+numCandidatesOfParty; candId++)
                {  
                   int numNeighbors;
                   numNeighbors = rand.nextInt(Math.min(maxNeighbors+1,numCandidatesOfParty)); 
                   int [] vv = getRandomIntsWithoutRepetitions(numCandidatesOfParty-1,numNeighbors,rand); 
                   int usedPercentage = 0;   
                   if (isSolidCoalition)
                   {
                        int partyPercentage;
                        if (numNeighbors!=0)
                           // we force the coalition transferring a small amount of votes to the virtual party candidate                   
                           partyPercentage = 1;
                        else
                           partyPercentage = 100;
                        politicalNetwork.addNeighborRelationship(candId, partyId, rationalFactory.valueOf(partyPercentage,100));                   
                        usedPercentage = partyPercentage;
                   }    
                   for (int tt=1; tt<=numNeighbors; tt++)
                   {    
                       int neighborId = partyId+vv[tt-1];
                       if (neighborId >= candId) 
                           neighborId++;   // avoids linking the candidate to itself
                       
                       // in this test we use only non zero integer percentages
                       int numNeighborsToGo = numNeighbors-tt;
                       int maxPercentage = 100-numNeighborsToGo-usedPercentage; // leaves at least 1 percent for each lasting neighbor
                       
                       int percentage = tt==numNeighbors ?
                                        maxPercentage :
                                        1+rand.nextInt(maxPercentage); 
                       usedPercentage += percentage;
                       politicalNetwork.addNeighborRelationship(candId, neighborId, rationalFactory.valueOf(percentage,100));                   
                   }   
                   if (!isSolidCoalition && numNeighbors==0)
                   {
                        if (usedPercentage!=0)
                            throw new RuntimeException("Percentages don't sum 100%");                       
                   }    
                   if (usedPercentage!=100 && (isSolidCoalition || numNeighbors!=0))
                       throw new RuntimeException("Percentages don't sum 100%");
                }
            }
            numCandidates += numCandidatesOfParty + 1;                    
        }        
        int numSeats = 1+Math.min(rand.nextInt(200),numCandidates - numParties-1);
        
        politicalNetwork.setNumberOfSeats(numSeats);
        politicalNetwork.processElection();
        politicalNetwork.close();        
        TIntObjectHashMap<Candidate> elected = politicalNetwork.getElected();                
                
        for (int p=0; p<numParties; p++)
            partyData[p].check(numValidVotes, numSeats, elected);
        
        
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
        Random r = new Random(0);
        
        RandomElection.runRandomElection(-713629100);
         
        for (int t=0; t<1000; t++)
        {    
            int seed = r.nextInt();
            System.out.print("Test "+t+"  with seed "+seed+" ");            
            RandomElection.runRandomElection(seed);
            System.out.println("passed");
        }    
    }        
    
}
