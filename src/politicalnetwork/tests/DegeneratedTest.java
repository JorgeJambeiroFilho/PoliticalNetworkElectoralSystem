package politicalnetwork.tests;

import politicalnetwork.core.PoliticalNetwork;
import politicalnetwork.core.IPoliticalNetwork;
import politicalnetwork.rationalnumber.DoublePrecisionRationalNumber;
import politicalnetwork.rationalnumber.RationalNumber;
import java.util.Random;
import politicalnetwork.core.PoliticalNetwork.IDTierBreaker;
import politicalnetwork.rationalnumber.InfinitePrecisionRationalNumber;

/**
 *
 */
public class DegeneratedTest
{
    
    /**
     * This method runs a random election where, at most as many candidates as the number of seats available receive votes.
     * This test the special termination conditions of the election.
     * 
     * @param seed The seed for the random number generator. With the same seed, the same random election is executed.
     */
    public static void runRandomElectionWithDegeneratedNetwork(int seed)
    {   
        int numTh = Runtime.getRuntime().availableProcessors();
        Random rand = new Random(seed);
        int maxCandidates = 1+rand.nextInt(50);
        int votedCandidates = 1+rand.nextInt(maxCandidates);
        int maxVotesPerCand = 1000+rand.nextInt(1000000);
        int numSeats = 1+rand.nextInt(maxCandidates);            

        
        RationalNumber.Factory numberFact = new InfinitePrecisionRationalNumber.Factory();
        IPoliticalNetwork politicalNetwork = new PoliticalNetwork(numberFact,"Degenerated Political Network with Random Percentages seed = "+seed,numTh,false,new IDTierBreaker(),null);       
        
        try
        {    
            // creates candidates and set their votes
            for (int candId=1; candId<=maxCandidates; candId++)   
            {    
                politicalNetwork.addCandidate(candId);
                int numCandVotes = candId > votedCandidates ? 0 : 1+rand.nextInt(maxVotesPerCand-1); 
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
                    if (props[nId-1] < 9000) props[nId-1] = 0;
                    sumProps += props[nId-1];
                }
                for (int nId=1; nId<maxCandidates; nId++)
                {    
                    int neighborId = nId >= candId ? nId+1 : nId;
                    if (props[nId-1]!=0)
                       politicalNetwork.addNeighborRelationship(candId, neighborId, numberFact.valueOf(props[nId-1],sumProps));                   
                }    
            }
            // set the number of seats in dispute
            politicalNetwork.setNumberOfSeats(numSeats);

            // process election with infinite precision
            politicalNetwork.processElection();

            if (politicalNetwork.getElected().size()!=numSeats)
                throw new RuntimeException("Number of elected candidates does not match number of seats");
            
        }
        finally
        {    
            politicalNetwork.close();                
        }
    }        
    public static void main(String argv[])
    {
        DegeneratedTest.runRandomElectionWithDegeneratedNetwork(1246330380);
        Random r = new Random(8);        
        int max = 10000;
        int numFails = 0;
        for (int t=0; t<max; t++)
        {    
            int seed = r.nextInt();
            try
            {    
                DegeneratedTest.runRandomElectionWithDegeneratedNetwork(seed);
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
