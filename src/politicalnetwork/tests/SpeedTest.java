package politicalnetwork.tests;

import politicalnetwork.core.PoliticalNetwork;
import politicalnetwork.core.IPoliticalNetwork;
import politicalnetwork.rationalnumber.DoublePrecisionRationalNumber;
import politicalnetwork.rationalnumber.RationalNumber;
import java.util.Random;
import politicalnetwork.core.Candidate;
import politicalnetwork.core.PoliticalNetwork.DefinitionListener;
import politicalnetwork.core.PoliticalNetwork.IDTierBreaker;
import politicalnetwork.rationalnumber.InfinitePrecisionRationalNumber;

/**
 *
 */
public class SpeedTest
{
    static class ProgressDefinitionListener implements DefinitionListener
    {
        int numRegs = 0;
        @Override
        public void registerDefinition(Candidate candidate, boolean elected,RationalNumber currentQuota)
        {
            // virtual candidates don't count here
            if (candidate.isVirtual())
                return;
            numRegs++;
            System.out.print("-");            
            if (numRegs%100==0)
                System.out.println("");            
            
            //System.out.println(numRegs);            
        }                
    }
    
    /**
     * This method runs a random election (in respect to percentages of transfer) using a 
     * completely connected network, (what maximizes running time) and throws an exception 
     * if any inconsistency is found.
     * @param seed The seed for the random number generator. With the same seed, the same random election is executed.
     */
    public static void runRandomElectionWithCompleteNetwork(int seed,boolean infPrec,int maxCandidates)
    {   
        int numTh = Runtime.getRuntime().availableProcessors();
        Random rand = new Random(seed);
        int maxVotesPerCand = 1000+rand.nextInt(1000000);

        RationalNumber.Factory numberFact = infPrec ? new InfinitePrecisionRationalNumber.Factory() : new DoublePrecisionRationalNumber.Factory();
        ProgressDefinitionListener progListener = new ProgressDefinitionListener();
        IPoliticalNetwork politicalNetwork = new PoliticalNetwork(numberFact,"Complete Political Network with Random Percentages seed = "+seed,numTh,false,new IDTierBreaker(),progListener);       
        
        try
        {    
            // creates candidates and set their votes
            for (int candId=1; candId<=maxCandidates; candId++)   
            {    
                politicalNetwork.addCandidate(candId);
                int numCandVotes = rand.nextInt(maxVotesPerCand); 
                politicalNetwork.setNumberOfVotes(candId, numCandVotes); 
            }    
            // sets percentages
            for (int candId=1; candId<=maxCandidates; candId++)
            {  
                int [] props =  new int[maxCandidates-1];
                int sumProps = 0;
                for (int nId=1; nId<maxCandidates; nId++)
                {
                    props[nId-1] = rand.nextInt(10000);
                    sumProps += props[nId-1];
                }
                for (int nId=1; nId<maxCandidates; nId++)
                {    
                    int neighborId = nId >= candId ? nId+1 : nId;
                    politicalNetwork.addNeighborRelationship(candId, neighborId, numberFact.valueOf(props[nId-1],sumProps));                   
                }    
            }
            // set the number of seats in dispute
            int numSeats = 1+Math.min(rand.nextInt(200),maxCandidates-1);            
            politicalNetwork.setNumberOfSeats(numSeats);

            // process election with finite precision
            politicalNetwork.processElection();

        }
        finally
        {    
            politicalNetwork.close();                
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
        long ini = System.currentTimeMillis();
        System.out.println();
        SpeedTest.runRandomElectionWithCompleteNetwork(1,false,2250);        
        long mid = System.currentTimeMillis();
        System.out.println();
        System.out.println("First test "+(mid-ini)/1000+" s");
        SpeedTest.runRandomElectionWithCompleteNetwork(1,true,200);        
        long end = System.currentTimeMillis();
        System.out.println();
        System.out.println("Second test "+(end-mid)/1000+" s");
        
    }        
    
}
