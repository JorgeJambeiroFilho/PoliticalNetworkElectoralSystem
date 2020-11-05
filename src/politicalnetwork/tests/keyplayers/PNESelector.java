/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package politicalnetwork.tests.keyplayers;

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import politicalnetwork.core.Candidate;
import politicalnetwork.core.KeyPlayersSelector;
import politicalnetwork.core.PoliticalNetwork;
import politicalnetwork.rationalnumber.RationalNumber;
import politicalnetwork.tests.RandomElectionTest;

/**
 *
 * @author jesjf
 */
public class PNESelector
{
    static class REDefinitionListener implements PoliticalNetwork.DefinitionListener
    {
        @Override
        public void registerDefinition(Candidate candidate, boolean elected,RationalNumber currentQuota)
        {
             System.out.println("Candidate "+candidate.getIdentifier()+" votes "+candidate.getNumberOfCurrentVotes().doubleValue()+" "+elected+" "+currentQuota);
        }        
    }
    
    public static void main(String argv[])    
    {
        KeyPlayersSelector kps =  GenExamples.get4ComponentsExample(); // GenExamples.getBorgattiExample(); //
        kps.setNumberOfSeats(3);
        kps.setDefinitionListener(new REDefinitionListener());
        //kps.randomWalkInit(100);
        //for (Candidate c:kps.getCandidates().valueCollection())
        //   System.out.println("Candidate "+c.getIdentifier()+" votes "+c.getNumberOfCurrentVotes().doubleValue());
        
        kps.processElection();
        kps.close();
        TIntObjectHashMap<Candidate> elected = kps.getElected();
        for (Candidate cand:elected.valueCollection())
            System.out.println("Elected "+cand.getIdentifier());            
    }        
}
