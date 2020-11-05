/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package politicalnetwork.tests.keyplayers;

import gnu.trove.map.hash.TIntObjectHashMap;
import politicalnetwork.core.Candidate;
import politicalnetwork.core.KeyPlayersSelector;

/**
 *
 * @author jesjf
 */
public class OrtizConnectivityEntropySelector
{
    public static void main(String argv[])    
    {
        KeyPlayersSelector kps = GenExamples.get4ComponentsExample(); //GenExamples.getBorgattiExample();
        kps.setNumberOfSeats(3);
        kps.prepareToProcess();
        kps.checkConsistency(false);
        kps.close();
        double hco = kps.connectivityEntropy();
        System.out.println("Total connectivity entropy = "+hco);
        for (int i=1; i<=kps.getCandidates().size(); i++)
        {    
            double hcoi = kps.connectivityEntropyWithoutNode(i);
            System.out.println("Connectivity entropy without "+i+ " = " + hcoi + " dif = "+(hco-hcoi));
        }    
        TIntObjectHashMap<Candidate> elected = kps.getHCOElected();
        for (Candidate cand:elected.valueCollection())
            System.out.println("Elected "+cand.getIdentifier());            
    }        
}
