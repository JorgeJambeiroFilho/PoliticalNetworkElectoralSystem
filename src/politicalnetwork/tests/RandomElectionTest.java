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
 */
public class RandomElectionTest
{
    /**
     * Class to sort by decreasing number of votes, without letting identical numbers
     * result in identical keys.
     */    
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
    
    /**
     * This class keeps data about parties to allow us to check if their guarantees are being
     * met.
     */
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
           // Guarantees only applies if the party forms, at least, a solid coalition. 
           if (!isSolidCoalition) return;
           
           int numElected = 0;
           boolean anyOneNotElected = false;
           for (Entry<CandidateOrderKey,Candidate> e:partyCandidates.entrySet())
           {    
               if (elected.contains(e.getValue().getIdentifier()))
               {    
                   if (isPartyCoalition && anyOneNotElected)
                       throw new RuntimeException("Elected memebers of party coalitions were not the most voted");           
                   numElected++;
               }    
               else
                   anyOneNotElected = true;
           }    
           if (numElected < minElected)
               throw new RuntimeException("Party formed a solid coalition, but did not have enough candidates elected");           
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
                // tests violations of the minimum number of elected candidates in advance, so that if we put a breakpoint here
                // we can genrally see the mistake that caused them loking inside the network
                throw new RuntimeException("Too many candidates of party eliminated "+numVotes+" "+totalNumberOfVotesOfParty);
            }    
        }
    }
    
    /**
     * Class to receive notifications from the political network, informing that certain candidates have been
     * eliminated or elected.
     */
    static class REDefinitionListener implements DefinitionListener
    {
        PartyData[] partyData;
        THashMap<Integer,PartyData> partyMap;
        
        void setPartyData(PartyData[] partyData)
        {
            this.partyData = partyData;
            
            // creates a map to identify the correct party of a candidate
            partyMap = new THashMap();
            for (int t=0; t<partyData.length; t++)
            {
                for (Candidate cand:partyData[t].partyCandidates.values())
                    partyMap.put(cand.getIdentifier(), partyData[t]);
            }    
        }
                
        int numRegs = 0;
        @Override
        public void registerDefinition(Candidate candidate, boolean elected,RationalNumber currentQuota)
        {
            // virtual candidates don't count here
            if (candidate.isVirtual())
                return;
            
            // checks if the floor for the number of elected candidates of the party have already been violated
            PartyData pData = partyMap.get(candidate.getIdentifier());
            pData.registerDefinition(candidate.getIdentifier(), elected);
        }        
    }
    
    /**
     * This method runs a random election and throws an exception if any inconsistency is found.
     * 
     * Elections with at most 200 candidates per party are run twice. Once with finite and once with infinite precision 
     * numbers. If there is a divergence among the two results the method returns true. This is theoretically possible,
     * but difficult. We don't do that for any number of candidates, because of speed.
     * 
     * For elections with at most 50 candidates, vote transfers are also run iteractively to test convergence.
     * We don't do that for any number of candidates, because of speed.      
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
    
        // creates two empty political networks
        RationalNumber.Factory doubleFact = new DoublePrecisionRationalNumber.Factory();
        RationalNumber.Factory infFact = new InfinitePrecisionRationalNumber.Factory();        
        IPoliticalNetwork politicalNetwork = new PoliticalNetwork(doubleFact,"Finite Precision Random Political Network seed = "+seed,numTh,maxCandParty*numParties<50,new IDTierBreaker(),null);       
        REDefinitionListener defListener = new REDefinitionListener();
        IPoliticalNetwork politicalNetworkInfPrec = new PoliticalNetwork(infFact,"Infinite Precision Random Political Network seed = "+seed,numTh,false,new IDTierBreaker(),defListener);        
        
        try
        {    
            // Fills the two networks with the same structure
            
            int numCandidates = 0;
            PartyData[] partyData = new PartyData[numParties];
            long numValidVotes = 0;
            int maxCandidatesOfAllParties = 0;
            
            // Creates the structure, party by party, only allowing neighborhood connections within the same party.
            // If two parties are mixed, they have the guarantees of a single party, so we don't
            // need to worry about allowing connections among parties.
            // When the number of parties is one, this is equivalent to a fully flexible network.
            for (int p=0; p<numParties; p++)
            {                
                partyData[p] = new PartyData();
                
                int numCandidatesOfParty = minCandParty+rand.nextInt(maxCandParty-minCandParty+1);            
                maxCandidatesOfAllParties = Math.max(maxCandidatesOfAllParties,numCandidatesOfParty); // updates the maximum number of candidates in a party
                int partyId = numCandidates + 1; // creates identifiers in a range above previous parties and avoids zero
                
                // decides what kind of coalition, the party will form
                boolean isSolidCoalition = rand.nextBoolean();
                boolean isPartyCoalition = rand.nextBoolean();
                
                
                partyData[p].isSolidCoalition = isSolidCoalition || isPartyCoalition;
                partyData[p].isPartyCoalition = isPartyCoalition;

                // creates a virtual party candidate and set its votes
                politicalNetwork.addVirtualCandidate(partyId);             
                politicalNetworkInfPrec.addVirtualCandidate(partyId);             
                int numPartyVotes = rand.nextInt(maxVotesPerCand); 
                politicalNetwork.setNumberOfVotes(partyId, numPartyVotes); 
                politicalNetworkInfPrec.setNumberOfVotes(partyId, numPartyVotes); 
                partyData[p].totalNumberOfVotesOfParty += numPartyVotes;
                numValidVotes += numPartyVotes;
                
                // creates the real candidates and set their votes
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

                // sets every real candidate as neighbor of the virtual party candidate with equal percentages of transfer
                for (int candId=partyId+1; candId<=partyId+numCandidatesOfParty; candId++)
                {    
                    politicalNetwork.addNeighborRelationship(partyId, candId, doubleFact.valueOf(1,numCandidatesOfParty));
                    politicalNetworkInfPrec.addNeighborRelationship(partyId, candId, infFact.valueOf(1,numCandidatesOfParty));
                }  

                if (isPartyCoalition)
                {
                    // for party coalitions, makes every real candidate, neighbor of every real candidate with equal percentages
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
                    // chooses neighbors of the real candidates for non party coalitions and set the percentages of transfer
                    for (int candId=partyId+1; candId<=partyId+numCandidatesOfParty; candId++)
                    {  
                       // chooses the number of neighbors 
                       int numNeighbors;
                       numNeighbors = rand.nextInt(Math.min(maxNeighbors+1,numCandidatesOfParty)); 
                       
                       
                       
                       // chooses the neighbors, sampling without replacements from a range of the number of members of the party minus 1
                       int [] neighborIds = getRandomIntsWithoutRepetitions(numCandidatesOfParty-1,numNeighbors,rand); 
                       
                       int usedPercentage = 0;   // this variables allow us to force the percentages to sum one
                       
                       if (isSolidCoalition)
                       {
                            // If its the case, we force the existence of the solid coalition transferring a small amount of votes 
                            // to the virtual party candidate. For candidates without neighbors, the party candidates gets all
                            // transfers
                            int partyPercentage = (numNeighbors!=0) ? 1 : 100;
                            politicalNetwork.addNeighborRelationship(candId, partyId, doubleFact.valueOf(partyPercentage,100));                   
                            politicalNetworkInfPrec.addNeighborRelationship(candId, partyId, infFact.valueOf(partyPercentage,100));                   
                            usedPercentage = partyPercentage;
                       }    
                       // set the neighbors and percentages
                       for (int tt=1; tt<=numNeighbors; tt++)
                       {    
                           int neighborId = partyId+neighborIds[tt-1];
                           if (neighborId >= candId) 
                               neighborId++;   // avoids linking the candidate to itself, creating an offset of one starting from the candidate's position

                           // in this test we use only non zero integer percentages
                           int numNeighborsToGo = numNeighbors-tt;
                           int maxPercentage = 100-numNeighborsToGo-usedPercentage; // leaves at least 1 percent for each lasting neighbor                           
                           int percentage = tt==numNeighbors ?  maxPercentage :  1+rand.nextInt(maxPercentage);  // chooses the percentage                          
                           usedPercentage += percentage;
                           politicalNetwork.addNeighborRelationship(candId, neighborId, doubleFact.valueOf(percentage,100));                   
                           politicalNetworkInfPrec.addNeighborRelationship(candId, neighborId, infFact.valueOf(percentage,100));                   
                       }   
                       if (!isSolidCoalition && numNeighbors==0 && usedPercentage!=0)
                            throw new RuntimeException("Percentages are not zero, when there are no neighbors");                       
                       if (usedPercentage!=100 && (isSolidCoalition || numNeighbors!=0))
                           throw new RuntimeException("Percentages don't sum 100%");
                    }
                }
                numCandidates += numCandidatesOfParty + 1;                    
            }        
            // set the number of seats in dispute
            int numSeats = 1+Math.min(rand.nextInt(200),numCandidates - numParties-1);            
            politicalNetwork.setNumberOfSeats(numSeats);
            politicalNetworkInfPrec.setNumberOfSeats(numSeats);

            // prepares to monitor the eliminations of each party for debuging purposes
            defListener.setPartyData(partyData);
            for (int p=0; p<numParties; p++)
                partyData[p].close(numValidVotes, numSeats);
            

            TIntObjectHashMap<Candidate> infElected = null;
            if (maxCandidatesOfAllParties < 200) // avoid slow executions
            {    
                // process election with infinite precision
                politicalNetworkInfPrec.processElection();
                infElected = politicalNetworkInfPrec.getElected();                                
                for (int p=0; p<numParties; p++)
                    partyData[p].check(infElected);
            }    
            
            // process election with finite precision
            politicalNetwork.processElection();
            TIntObjectHashMap<Candidate> elected = politicalNetwork.getElected();                                
            for (int p=0; p<numParties; p++)
                partyData[p].check(elected);

            // compares the finit and infinte precision results if available
            if (maxCandidatesOfAllParties < 200) 
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
        Random r = new Random(16);
        int max = 10000;
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
                System.out.println("\n"+t+" random elections already run. Divergenges = "+numDivergences+" fails = "+numFails);                        
            System.out.print(".");            
        }    
        System.out.println("\n"+max+" random elections already run. Divergenges = "+numDivergences+" fails = "+numFails);                        
    }        
    
}
