/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package politicalnetwork.core;

import gnu.trove.map.hash.TIntObjectHashMap;
import politicalnetwork.rationalnumber.RationalNumber;
import java.util.HashMap;

/**
 * Class to represent a candidate and to store data related to a candidate.
 * 
 * @author Removed for Blind Review
 */
public class Candidate
{
    int identifier;
    public static final int ST_REMAINING = 0;
    public static final int ST_ELECTED = 1;
    public static final int ST_ELIMINATED = 2;
    public static final int ST_VIRTUALDISCARDCANDIDATE = 3; // Special permanet state for the virtual discard candidate
    public static final int ST_BEING_ELIMINATED = 4; // Temporary status for a candidate that has just been choosen for elimination, that lasts till everything reflects the elimination
    int status;
    RationalNumber numberOfIndividualVotes; // number of votes received directly from the electors
    RationalNumber numberOfCurrentVotes; // current number of votes, considering individual votes and transfers
    TIntObjectHashMap<NeighborhoodRelation> originalNeighbors; // relations to neighbors choosen by the candidate
    TIntObjectHashMap<NeighborhoodRelation> currentNeighbors;  // relations to neighbors after network updates
    TIntObjectHashMap<Candidate> originalReverseNeighbors;     // candidates which chose this candidate as a neighbor
    TIntObjectHashMap<Candidate> currentReverseNeighbors;      // candidates to whose neighborsets this candidade belongs after network updates 
                                                               // this set is kept, so that when it is needed to remove the candidate
                                                               // from all neighbor sets it is fast to find in which ones the candidate is
    
    RationalNumber numberOfVotesWhenEliminatedOrElected; // number of votes this candidate had in the moment of its elimination or election
    boolean isVirtual; // true for virtual party candidates

    public String toString()
    {
        return "identifier " + identifier + " status " + status 
                + " votes " + numberOfCurrentVotes.doubleValue()  
                + (numberOfVotesWhenEliminatedOrElected != null ? " votosdef " + numberOfVotesWhenEliminatedOrElected.doubleValue() : "")
                + " numNeigh " + (currentNeighbors==null?0:currentNeighbors.size())   
                + " numRevNeigh " + (currentReverseNeighbors==null ? 0 : currentReverseNeighbors.size())                 
                ;
    }    
    
    /**
     *  Copies a candiate for testting purposes
     * @param c The candidate to be copied
     */
    Candidate(Candidate c)
    {
        identifier = c.identifier;
        status = c.status;
        numberOfIndividualVotes = c.numberOfIndividualVotes;
        numberOfCurrentVotes = c.numberOfCurrentVotes;
        originalNeighbors = new TIntObjectHashMap(c.originalNeighbors);        
        if (currentNeighbors!=null)
           currentNeighbors = new TIntObjectHashMap(c.currentNeighbors);
        originalReverseNeighbors = new TIntObjectHashMap(c.originalReverseNeighbors);
        if (c.currentReverseNeighbors!=null)
            currentReverseNeighbors = new TIntObjectHashMap(c.currentReverseNeighbors);
        numberOfVotesWhenEliminatedOrElected = c.numberOfVotesWhenEliminatedOrElected;
        isVirtual = c.isVirtual;                        
    }
     
    /**
     * Creates a candidate 
     * @param identifier An integer that is unique for each candidate
     * @param isVirtual  True for virtual party candidates
     */
    Candidate(int identifier, boolean isVirtual)
    {
        this.identifier = identifier;
        this.isVirtual = isVirtual;
        originalNeighbors = new TIntObjectHashMap();
        originalReverseNeighbors = new TIntObjectHashMap();
    }

    /**
     * Sets the number of votes this canidate received direclty from voters
     * @param individualVotes The number of votes.
     */
    void setIndividualVotes(RationalNumber individualVotes)
    {
        this.numberOfIndividualVotes = individualVotes;
        numberOfCurrentVotes = this.numberOfIndividualVotes;
    }

    /**
     * Adds a neighbor to this candidate.
     * This method should be called once for each neighbor choosen by this candidate.
     * The sum of the percentages in the calls shoud be one
     * @param neighbor
     * @param percentage 
     */
    void addNeighbor(Candidate neighbor, RationalNumber percentage)
    {
        originalNeighbors.put(neighbor.identifier, new NeighborhoodRelation(neighbor, percentage));
    }

    /**
     * Register the fact that this candidate is a neighbor of some other candidate
     * @param reverseNeighbor The other candidate.
     */
    void addReverseNeighbor(Candidate reverseNeighbor)
    {
        originalReverseNeighbors.put(reverseNeighbor.identifier, reverseNeighbor);
    }

    /**
     * Prepares a normal candidate or a virtual party candidate for the election.
     * Initial current values are stablished.
     * @param virtualDiscardCandidate The virtual discard candidate, for the case it needs to be added to an empty
     *                                neighbor set.
     * @param numberFactory Allows number to be created here using the  correct class
     */
    void prepareForProcessingElection(Candidate virtualDiscardCandidate,RationalNumber.Factory numberFactory)
    {
        status = ST_REMAINING;
        currentNeighbors = new TIntObjectHashMap();
        for (NeighborhoodRelation r:originalNeighbors.valueCollection())
            currentNeighbors.put(r.neighbor.identifier,new NeighborhoodRelation(r));
        if (virtualDiscardCandidate!=null && originalNeighbors.isEmpty())
             currentNeighbors.put(virtualDiscardCandidate.identifier, new NeighborhoodRelation(virtualDiscardCandidate,numberFactory.valueOf(1, 1)));
        currentReverseNeighbors = new TIntObjectHashMap(originalReverseNeighbors);
        
    }
    /**
     * Prepares the virtual discard candidate for the election, seting its special status.
     * @param numberFactory Allows numbers to be created here using the  correct class
     */
    void prepareVirtualDiscardCandidateForProcessingElection(RationalNumber.Factory numberFactory)
    {
        status = ST_VIRTUALDISCARDCANDIDATE;        
        currentNeighbors = new TIntObjectHashMap();
    }
    
    public int getIdentifier()
    {
        return identifier;
    }

    public int getStatus()
    {
        return status;
    }

    public RationalNumber getNumberOfIndividualVotes()
    {
        return numberOfIndividualVotes;
    }

    public RationalNumber getNumberOfCurrentVotes()
    {
        return numberOfCurrentVotes;
    }

    public TIntObjectHashMap<NeighborhoodRelation> getOriginalNeighbors()
    {
        return originalNeighbors;
    }

    public TIntObjectHashMap<NeighborhoodRelation> getCurrentNeighbors()
    {
        return currentNeighbors;
    }

    public TIntObjectHashMap<Candidate> getOriginalReverseNeighbors()
    {
        return originalReverseNeighbors;
    }

    public TIntObjectHashMap<Candidate> getCurrentReverseNeighbors()
    {
        return currentReverseNeighbors;
    }
     
    /**
     * Throws an exception if an inconsistency is detected.
     * @param currentQuota The current quota
     * @param numberFactory Allows numbers to be created here using the  correct class
     */
    void checkConsistency(RationalNumber currentQuota, RationalNumber.Factory numberFactory)
    {
        checkConsistency(currentQuota, numberFactory, true);
    }        
    /**
     * Throws an exception if an inconsistency is detected.
     * @param currentQuota The current quota
     * @param numberFactory Allows numbers to be created here using the  correct class
     * @param areTransfersComplete True if the election is in a point where a vote tranfer process has completed.
     *                             There are consistency requirements that only apply in this case. 
     */    
    void checkConsistency(RationalNumber currentQuota, RationalNumber.Factory numberFactory,boolean areTransfersComplete)
    {
        if (status == ST_ELECTED && !numberOfCurrentVotes.equals(currentQuota))
        {
            throw new RuntimeException("Candidate marked as elected, but with number of votes that differs from the current quota");
        }
        else 
        if (status == ST_ELIMINATED && !numberOfCurrentVotes.equals(numberFactory.valueOf(0, 1)))
        {
            throw new RuntimeException("Candidate marked as elected, but with non zero number of votes");
        }
        else 
        if (areTransfersComplete && status == ST_REMAINING && !isVirtual && numberOfCurrentVotes.compareTo(currentQuota) >= 0)
        {
            throw new RuntimeException("Remaining candidate with number of votes greater or equal to the current quota");
        }
        RationalNumber sum = numberFactory.valueOf(0, 1);
        if (currentNeighbors != null)
        {
            for (NeighborhoodRelation r : currentNeighbors.valueCollection())
            {
                sum = sum.plus(r.transferPercentage);
                if (r.neighbor == this)
                    throw new RuntimeException("Neighbor of itself");
                if (r.neighbor.currentReverseNeighbors==null && r.neighbor.status != ST_VIRTUALDISCARDCANDIDATE)
                    throw new RuntimeException("Inconsistency between sets of neighbors and reverse neighbors");
                if (r.neighbor.currentReverseNeighbors!=null && !r.neighbor.currentReverseNeighbors.containsKey(identifier))
                    throw new RuntimeException("Inconsistency between sets of neighbors and reverse neighbors");
                if (r.neighbor.status!=Candidate.ST_REMAINING && r.neighbor.status!=Candidate.ST_VIRTUALDISCARDCANDIDATE)                
                    throw new RuntimeException("Non remaining candidate is still in a neighbor set");
            }
        }
        if (currentReverseNeighbors != null)
        {
            for (Candidate c : currentReverseNeighbors.valueCollection())
            {
                if (c.currentNeighbors != null && !c.currentNeighbors.containsKey(identifier))
                    throw new RuntimeException("Inconsistency between sets of neighbors and reverse neighbors");
                if (c.status==Candidate.ST_ELIMINATED)
                    throw new RuntimeException("Eliminated canddiate kept s a reverse neighbor");
            }
        }
        if (currentNeighbors != null && !currentNeighbors.isEmpty() && !sum.equals(numberFactory.valueOf(1, 1)))
            throw new RuntimeException("Percentages of tranfer don't sum one");
    }

    public String toStringWithLinks()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("" + identifier + "\n");
        for (NeighborhoodRelation r : currentNeighbors.valueCollection())
            sb.append("    " + r.neighbor.identifier + "       " + r.transferPercentage + "\n");
        return sb.toString();
    }

    public boolean equals(Object o)
    {
        Candidate c = (Candidate) o;
        if (c.identifier != identifier)
        {
            return false;
        }
        if (c.status != status)
        {
            return false;
        }
        if (!c.numberOfIndividualVotes.equals(numberOfIndividualVotes))
        {
            return false;
        }
        if (!c.numberOfCurrentVotes.equals(numberOfCurrentVotes))
        {
            return false;
        }
        if (currentNeighbors.size() != c.currentNeighbors.size())
        {
            return false;
        }
        for (NeighborhoodRelation r : currentNeighbors.valueCollection())
        {
            NeighborhoodRelation or = c.currentNeighbors.get(r.neighbor.identifier);
            if (or == null)
            {
                return false;
            }
            if (!or.transferPercentage.equals(r.transferPercentage))
            {
                return false;
            }
        }
        return true;
    }

    public RationalNumber getNumberOfVotesWhenEliminatedOrElected()
    {
        return numberOfVotesWhenEliminatedOrElected;
    }

    public boolean isIsBandeira()
    {
        return isVirtual;
    }
    
}
