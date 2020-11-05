package politicalnetwork.core;

import gnu.trove.map.hash.TIntObjectHashMap;
import politicalnetwork.rationalnumber.RationalNumber;
import java.util.List;

/**
 * This is the interface supported by the PoliticalNetwork class.
 * 
 * 
 */
public interface IPoliticalNetwork
{

    /**
     * Creates a virtual candidate. Used for virtual party candidates.
     * Virtual candidates can be voted, but cannot be elected.
     * @param identifier An integer to identify this candidate uniquely. 
     * This identifier cannot be zero, since zero is already used for the virtual
     * discard candidate.
     */
    void addVirtualCandidate(int identifier);

    /**
     * Creates a real candidate.
     * @param identifier An integer to identify this candidate uniquely.
     * This identifier cannot be zero, since zero is already used for the virtual
     * discard candidate.
     */    
    void addCandidate(int identifier);

    /**
     * Register that a candidate chose another as a neighbor and sets the percentage of transfer.
     * @param candidateIdentifier Identifier of the candidate that chose the other as a neighbor.
     * @param neighborCandidateIdentifier Identifier of the candidate that was chosen as a neighbor.
     * @param tranferPercentage Percentage of tranfer from the candidate to the neighbor in any 
     *                          direct vote transfers.
     */
    void addNeighborRelationship(int candidateIdentifier, int neighborCandidateIdentifier, RationalNumber tranferPercentage);

    /**
     * Set the number of seats being disputed in the election.
     * @param numberOfSeats 
     */
    void setNumberOfSeats(int numberOfSeats);

    /**
     * Sets the number of votes that a candidate received from voters.
     * @param candidateIdentifier Identifier of the candidate.
     * @param individualVotes Number of votes received.
     */
    void setNumberOfVotes(int candidateIdentifier, int individualVotes);
    
    /**
     * Process the election determining the elected candidates.
     */
    void processElection();
   
    /**
     * Checks if this political network is equal to another political network.
     * Neighbor sets, percentages of transfer, current quota, current status of each candidate,
     * and current number of votes of each candidate are considered.
     * This method is used to test the insensibility to the order of elections and eliminations.
     * 
     * @param o The other political network
     * @return True if the networks are equal.
     */
    boolean equals(Object o);

    /**
     * Finds the data associated to a candidate.
     * @param identifier The indentifier of the candidate.
     * @return The candidate with its data.
     */
    Candidate getCandidate(int identifier);

    /**
     * Find the current quota.
     * @return The current quota.
     */
    RationalNumber getCurrentQuota();
    
    /**
     * Indicates the set of elected candidates.
     * @return The set of elected candidates
     */
    TIntObjectHashMap<Candidate> getElected();

    /**
     * Define the state of candidates regardless of usual election rules.
     * This method is used to test the insensibility to the order of elimination and election. It is also helpful to generate
     * views of the network, where some candidates have beeen removed from vote transfer paths.
     * @param eliminated Candidates to eliminate
     * @param elected Candidates to elect
     */
    public void defineStatusOfArbitraryCandidates(List<Integer> eliminated,List<Integer> elected);
    

    /**
     * This method should be called to release some resources when the method prepareToProcess was called
     * but the method processElection was not.  This happens when a network is created just for structure 
     * analysis.
     */
    void close();

    public void setDefinitionListener(PoliticalNetwork.DefinitionListener definitionListener);
    
    
}
