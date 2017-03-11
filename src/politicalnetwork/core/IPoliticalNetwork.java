/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package politicalnetwork.core;

import gnu.trove.map.hash.TIntObjectHashMap;
import politicalnetwork.rationalnumber.RationalNumber;
import java.util.List;

/**
 * This is the interface supported by the PoliticalNetwork class.
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
     * After all information about the network has been registerd by calls to
     * addVirtualCandidate, addCandidate, addNeighborRelationship, setNumberOfSeats and 
     * setNumberOfVotes, a call to  prepareToProcess prepares the network to process an election. 
     * After this call, the refered methods should not be called again.
     * 
     * Calling this method is unnecessary in an election process beacause the method 
     * processElection() already class it. However, for tests that don't actually run the election
     * like 
     * 
     */
    //void prepareToProcess();
    
    /**
     * Process the election determining the elected candidates.
     */
    void processElection();

    /**
     * Performas arbitrary removal of candidates from vote tranfer paths to allow the analysis of the 
     * resulting network structure.
     * @param identifier 
     */
    //void removeFromAllNeighborSets(int identifier);
    
    /**
     * Raises an exception if any inconsistency is detected.
     * @param areTransfersComplete 
     */
    void checkConsistency(boolean areTransfersComplete);

    
    /**
     * Checks if this political network is equal to another political network.
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

    RationalNumber getCurrentQuota();
    
    /**
     * Indicated the set of elected candidates.
     * @return The set of elected candidates
     */
    TIntObjectHashMap<Candidate> getElected();

    /**
     * Define the state of candidates regardless of usual election rules.
     * This method is used to test the insensibility to the order of elimination and election.
     * @param eliminated candidates to eliminate
     * @param eliminated candidates to elect
     */
    public void defineStatusOfArbitraryCandidates(List<Integer> eliminated,List<Integer> elected);
    

    /**
     * This method should be called to release some resources when the method prepareToProcess was called
     * but the method processElection was not.  This happens when a network is created just for structure 
     * analysis.
     */
    void close();

    
    
}
