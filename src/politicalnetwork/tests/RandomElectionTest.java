/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package politicalnetwork.tests;

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.Map.Entry;
import politicalnetwork.core.PoliticalNetwork;
import politicalnetwork.core.IPoliticalNetwork;
import politicalnetwork.rationalnumber.DoublePrecisionRationalNumber;
import politicalnetwork.rationalnumber.RationalNumber;
import java.util.Random;
import java.util.TreeMap;
import politicalnetwork.core.Candidate;
import politicalnetwork.core.PoliticalNetwork.DefinitionListener;
import politicalnetwork.core.PoliticalNetwork.IDTierBreaker;
import politicalnetwork.rationalnumber.InfinitePrecisionRationalNumber;

/**
 *
 * @author jesjf22
 */
public class RandomElectionTest
{
    
    static class CandidateOrderKey implements Comparable<CandidateOrderKey>
    {
        int numberOfVotes;
        int identifier;

        public CandidateOrderKey(int numberOfVotes, int identifier)
        {
            this.numberOfVotes = numberOfVotes;
            this.identifier = identifier;
        }

        
        @Override
        public int compareTo(CandidateOrderKey o)
        {
            if (numberOfVotes > o.numberOfVotes)
                return -1;
            if (numberOfVotes < o.numberOfVotes)
                return 1;
            if (identifier < o.identifier)
                return -1;
            if (identifier > o.identifier)
                return 1;
            return 0;
        }
        @Override 
        public String toString()
        {
            return ""+numberOfVotes+","+identifier;
        }        
        
        
    }
    
    static class PartyData
    {
        int totalNumberOfVotesOfParty;
        TreeMap<CandidateOrderKey,Candidate> partyCandidates;
        boolean isSolidCoalition;
        boolean isPartyCoalition;
        int minElected;
        int numEliminated;
        PartyData()
        {
            totalNumberOfVotesOfParty = 0;
            partyCandidates = new TreeMap();            
            numEliminated=0;
        }   
        
        void check(TIntObjectHashMap<Candidate> elected)
        {   
           if (!isSolidCoalition) return;
           int numElected = 0;
           boolean anyOneNotElected = false;
           for (Entry<CandidateOrderKey,Candidate> e:partyCandidates.entrySet())
           {    
               if (elected.contains(e.getValue().getIdentifier()))
               {    
                   if (isPartyCoalition && anyOneNotElected)
                       throw new RuntimeException("Elected memebers of party were not the most voted");           
                   numElected++;
               }    
               else
                   anyOneNotElected = true;
           }    
           if (numElected < minElected)
               throw new RuntimeException("Party did not have enough candidates elected");           
        }        
        void close(long numValidVotes,int numSeats)
        {
              minElected = Math.min(partyCandidates.size(),(int)(((long)totalNumberOfVotesOfParty) * numSeats / numValidVotes));            
        }        
        
        private void registerDefinition(int candidateIdentifier, boolean elected)
        {
            if (elected || !isSolidCoalition)
                return;
            numEliminated++;            
            if (partyCandidates.size()-numEliminated < minElected)
            {    
                double numVotes = 0;
                /*
                for (Candidate cand:partyCandidates.values())
                {
                    numVotes += cand.getNumberOfCurrentVotes().doubleValue();
                    if (cand.getCurrentNeighbors()!=null && cand.getCurrentNeighbors().containsKey(0))
                        throw new RuntimeException("Too many candidates of party eliminated - discard candidate beacame a neighbor");
                }*/
                throw new RuntimeException("Too many candidates of party eliminated "+numVotes+" "+totalNumberOfVotesOfParty);
            }    
        }
    }
    
    static class REDefinitionListener implements DefinitionListener
    {
        PartyData[] partyData;
        THashMap<Integer,PartyData> partyMap;
        
        void setPartyData(PartyData[] partyData)
        {
            this.partyData = partyData;
            partyMap = new THashMap();
            for (int t=0; t<partyData.length; t++)
            {
                for (Candidate cand:partyData[t].partyCandidates.values())
                    partyMap.put(cand.getIdentifier(), partyData[t]);
            }    
        }
                
        @Override
        public void registerDefinition(Candidate candidate, boolean elected)
        {
            if (candidate.isVirtual())
                return;
            PartyData pData = partyMap.get(candidate.getIdentifier());
            pData.registerDefinition(candidate.getIdentifier(), elected);
        }        
    }
    
    /**
     * This method runs a random election and throws an exception if any inconsistency is found.
     * 
     * Elections with at most 200 candidates per party are run twice. Once with finite and once with infinite precision 
     * numbers. If there is a divergence among the two results the method returns true
     * 
     * For elections with at most 50 candidates, vote transfers are also run iteractively to test convergence.
     *       
     * @param seed The seed for the random number generator. With the same seed, the same random election is executed.
     * @return True if there is a divergenge between finite and infinite precision results, what is possible, but very difficult .
     */
    public static boolean runRandomElection(int seed)
    {   
        int numTh = Runtime.getRuntime().availableProcessors();
        Random rand = new Random(seed);
        int maxCandidates = 1500;
        int numParties =  1+rand.nextInt(30);
        int maxCandParty = 1+rand.nextInt(maxCandidates / numParties);
        int minCandParty = 1;         
        int maxNeighbors = rand.nextInt(100);        
        int maxVotesPerCand = 1000+rand.nextInt(1000000);
        
        RationalNumber.Factory doubleFact = new DoublePrecisionRationalNumber.Factory();
        RationalNumber.Factory infFact = new InfinitePrecisionRationalNumber.Factory();
        IPoliticalNetwork politicalNetwork = new PoliticalNetwork(doubleFact,"Finite Precision Random Political Network seed = "+seed,numTh,maxCandParty*numParties<50,new IDTierBreaker(),null);       
        REDefinitionListener defListener = new REDefinitionListener();
        IPoliticalNetwork politicalNetworkInfPrec = new PoliticalNetwork(infFact,"Infinite Precision Random Political Network seed = "+seed,numTh,false,new IDTierBreaker(),defListener);        
        
        try
        {    
            int numCandidates = 0;
            PartyData[] partyData = new PartyData[numParties];
            long numValidVotes = 0;
            int maxCandidatesOfAllParties = 0;
                    
            for (int p=0; p<numParties; p++)
            {                
                partyData[p] = new PartyData();
                int numCandidatesOfParty = minCandParty+rand.nextInt(maxCandParty-minCandParty+1);            
                maxCandidatesOfAllParties = Math.max(maxCandidatesOfAllParties,numCandidatesOfParty);
                int partyId = numCandidates + 1; // create identifiers in a range above previous parties and avoid zero
                boolean isSolidCoalition = rand.nextBoolean();
                boolean isPartyCoalition = rand.nextBoolean();
                partyData[p].isSolidCoalition = isSolidCoalition || isPartyCoalition;
                partyData[p].isPartyCoalition = isPartyCoalition;

                politicalNetwork.addVirtualCandidate(partyId);             
                politicalNetworkInfPrec.addVirtualCandidate(partyId);             
                int numPartyVotes = rand.nextInt(maxVotesPerCand); 
                politicalNetwork.setNumberOfVotes(partyId, numPartyVotes); 
                politicalNetworkInfPrec.setNumberOfVotes(partyId, numPartyVotes); 
                partyData[p].totalNumberOfVotesOfParty += numPartyVotes;
                numValidVotes += numPartyVotes;
                for (int candId=partyId+1; candId<=partyId+numCandidatesOfParty; candId++)   
                {    
                    politicalNetwork.addCandidate(candId);
                    politicalNetworkInfPrec.addCandidate(candId);
                    int numCandVotes = rand.nextInt(maxVotesPerCand); 
                    politicalNetwork.setNumberOfVotes(candId, numCandVotes); 
                    politicalNetworkInfPrec.setNumberOfVotes(candId, numCandVotes); 
                    partyData[p].totalNumberOfVotesOfParty += numCandVotes;
                    partyData[p].partyCandidates.put(new CandidateOrderKey(numCandVotes,candId),politicalNetworkInfPrec.getCandidate(candId));                
                    numValidVotes += numCandVotes;
                }    

                for (int candId=partyId+1; candId<=partyId+numCandidatesOfParty; candId++)
                {    
                    politicalNetwork.addNeighborRelationship(partyId, candId, doubleFact.valueOf(1,numCandidatesOfParty));
                    politicalNetworkInfPrec.addNeighborRelationship(partyId, candId, infFact.valueOf(1,numCandidatesOfParty));
                }  

                if (isPartyCoalition)
                {
                    for (int candId=partyId+1; candId<=partyId+numCandidatesOfParty; candId++)
                        for (int neighborId=partyId+1; neighborId<=partyId+numCandidatesOfParty; neighborId++)
                           if (candId!=neighborId)    
                           {    
                               politicalNetwork.addNeighborRelationship(candId, neighborId, doubleFact.valueOf(1,numCandidatesOfParty-1));                   
                               politicalNetworkInfPrec.addNeighborRelationship(candId, neighborId, infFact.valueOf(1,numCandidatesOfParty-1));                   
                           }    
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
                            politicalNetwork.addNeighborRelationship(candId, partyId, doubleFact.valueOf(partyPercentage,100));                   
                            politicalNetworkInfPrec.addNeighborRelationship(candId, partyId, infFact.valueOf(partyPercentage,100));                   
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
                           politicalNetwork.addNeighborRelationship(candId, neighborId, doubleFact.valueOf(percentage,100));                   
                           politicalNetworkInfPrec.addNeighborRelationship(candId, neighborId, infFact.valueOf(percentage,100));                   
                       }   
                       if (!isSolidCoalition && numNeighbors==0)
                       {
                            if (usedPercentage!=0)
                                throw new RuntimeException("Percentages are not zero, when there are no neighbors");                       
                       }    
                       if (usedPercentage!=100 && (isSolidCoalition || numNeighbors!=0))
                           throw new RuntimeException("Percentages don't sum 100%");
                    }
                }
                numCandidates += numCandidatesOfParty + 1;                    
            }        
            int numSeats = 1+Math.min(rand.nextInt(200),numCandidates - numParties-1);

            
            politicalNetwork.setNumberOfSeats(numSeats);
            politicalNetworkInfPrec.setNumberOfSeats(numSeats);

            defListener.setPartyData(partyData);
            for (int p=0; p<numParties; p++)
                partyData[p].close(numValidVotes, numSeats);
            

            TIntObjectHashMap<Candidate> infElected = null;
            if (maxCandidatesOfAllParties < 200) // avoid slow executions
            {    
                politicalNetworkInfPrec.processElection();
                infElected = politicalNetworkInfPrec.getElected();                                
                for (int p=0; p<numParties; p++)
                    partyData[p].check(infElected);
            }    
            
            politicalNetwork.processElection();
            TIntObjectHashMap<Candidate> elected = politicalNetwork.getElected();                                
            for (int p=0; p<numParties; p++)
                partyData[p].check(elected);

            
            if (maxCandidatesOfAllParties < 200) // avoid slow executions
            {                    
                for (Candidate cand:elected.valueCollection())
                    if (!infElected.contains(cand.getIdentifier()))
                        return true;                
            }                    
            return false;
        }
        finally
        {    
            politicalNetwork.close();                
            politicalNetworkInfPrec.close();                
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
        Random r = new Random(9);
        
        //RandomElection.runRandomElection(-1958782853);
        
        int max = 100000;
        int numFails = 0;
        int numDivergences = 0;
        
        
        for (int t=0; t<max; t++)
        {    
            int seed = r.nextInt();
            try
            {    
                if (RandomElectionTest.runRandomElection(seed))
                {    
                    System.out.println("\nDivergence with seed "+seed);
                    numDivergences++;
                }    
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
                System.out.println("\n"+t+" random election already run. Divergenges = "+numDivergences+" fails = "+numFails);                        
            System.out.print(".");            
        }    
        System.out.println("\n"+max+" random election already run. Divergenges = "+numDivergences+" fails = "+numFails);                        
        
    }        
    
}
