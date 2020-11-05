/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package politicalnetwork.core;

import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.Arrays;
import java.util.Comparator;
import politicalnetwork.rationalnumber.RationalNumber;

/**
 *
 * @author jesjf
 */
public class KeyPlayersSelector extends PoliticalNetwork
{
    public KeyPlayersSelector(RationalNumber.Factory numberFactory, String name, int numTh, boolean checkConvergenceAndMakeExtraConsistencyTests, TieBreaker tieBreaker, DefinitionListener definitionListener)
    {
        super(numberFactory, name, numTh, checkConvergenceAndMakeExtraConsistencyTests, tieBreaker, definitionListener);
    }
 
    static double log2(double x)
    {
        return Math.log(x)/Math.log(2);
    }        
    
    public double connectivityEntropy()
    {
        TIntObjectHashMap<Candidate> cands = getCandidates();
        int numEdgesX2 = 0;
        for (Candidate cand:cands.valueCollection())
        {    
            int deg = cand.getCurrentNeighbors().size();
            numEdgesX2 += deg;
        }   
        double Hco = 0;
        for (Candidate cand:cands.valueCollection())
        {    
            int deg = cand.getCurrentNeighbors().size();
            double conn = (double)deg / numEdgesX2;
            Hco -= conn * log2(conn);
        }    
        return Hco;
    }        
    /**
     * This measure is for undirected graphs, so it assumes that all relations are symmetric
     * @param id
     * @return 
     */
    public double connectivityEntropyWithoutNode(int id)
    {
        Candidate removed = getCandidate(id);
        TIntObjectHashMap<Candidate> cands = getCandidates();
        int numEdgesX2 = 0;
        for (Candidate cand:cands.valueCollection())
        {    
            if (cand.identifier!=id)
            {                
                int deg = cand.getCurrentNeighbors().size();
                if (removed.currentNeighbors.containsKey(cand.identifier))
                   deg--;
                numEdgesX2 += deg;                
            }    
        }   
        double Hco = 0;
        for (Candidate cand:cands.valueCollection())
        {    
            if (cand.identifier!=id)
            {                            
                int deg = cand.getCurrentNeighbors().size();
                if (removed.currentNeighbors.containsKey(cand.identifier))
                   deg--;                
                double conn = (double)deg / numEdgesX2;
                Hco -= deg==0 ? 0 : conn * log2(conn);
            }    
        }    
        return Hco;
    }        
    
    class HCOComparator implements Comparator<Candidate>
    {
        @Override
        public int compare(Candidate o1, Candidate o2)
        {
            double c = connectivityEntropyWithoutNode(o1.identifier) - connectivityEntropyWithoutNode(o2.identifier);            
            if (c < 0)
                return -1; 
            if (c > 0)
                return 1;
            return 0;        
        }        
    }
    
    public TIntObjectHashMap<Candidate> getHCOElected()
    {        
        Candidate cands[] = new Candidate[candidates.size()];
        int p=0;
        for (Candidate cand:candidates.valueCollection())
            cands[p++] =  cand;
        Arrays.sort(cands, new HCOComparator());
        TIntObjectHashMap<Candidate> res = new TIntObjectHashMap();
        for (int t=0; t<getNumberOfSeats(); t++)
            res.put(cands[t].identifier, cands[t]);
        return res; 
    }        
    
    public void randomWalkInit(int numSteps)
    {
        prepareToProcess();
        for (int s=0; s<numSteps; s++)
        {
            for (Candidate c:candidates.valueCollection())
                c.numberOfCurrentVotes = zero;            
            for (Candidate c:candidates.valueCollection())
                for (NeighborhoodRelation r:c.currentNeighbors.valueCollection())
                    r.neighbor.numberOfCurrentVotes = r.neighbor.numberOfCurrentVotes.plus(c.numberOfIndividualVotes.times(r.transferPercentage));
            for (Candidate c:candidates.valueCollection())
                c.numberOfIndividualVotes = c.numberOfCurrentVotes;
        }            
    }        
    
}
