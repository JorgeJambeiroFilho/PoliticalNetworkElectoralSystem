/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package politicalnetwork.testimplementation;

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import politicalnetwork.rationalnumber.RationalNumber;
import java.util.HashMap;

/**
 *
 * @author jesjf
 */
public interface IPoliticalNetwork
{

    /**
     * Creates a virtual candidate. Used for virtual party candidates.
     * @param identifier An integer to identify this candidate uniquely. 
     * This identifier cannot be zero, sic zero is already used for the virtual
     * discard candidate.
     */
    void addVirtualCandidate(int identifier);

    /**
     * Creates a real candidate.
     * @param identifier An integer to identify this candidate uniquely.
     */    
    void addCandidate(int identifier);

    /**
     * Register that a candidate chose another as a neighbor and sets the percentage of tranfer.
     * @param candidateIdentifier Identifier of the candidate that chos ethe other as neighbor.
     * @param neighborCandidateIdentifier Identifier of the candidate that was chosen as a neighbor.
     * @param tranferPercentage Percentage of tranfer 
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
     */
    void prepareToProcess();
    
    /**
     * Process the election determining the elected candidates.
     */
    void processElection();

    /**
     * Performas arbitrary removal of candidates from vote tranfer paths to allow the analysis of the 
     * resulting network structure.
     * @param identifier 
     */
    void removeFromAllNeighborSets(int identifier);
    
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

    /**
     * Indicated the set of elected candidates.
     * @return The set of elected candidates
     */
    TIntObjectHashMap<Candidate> getElected();
    

    /**
     * This method should be called to release some resources when the method prepareToProcess was called
     * but the method processElection was not.  This happens when a network is created just for structure 
     * analysis.
     */
    void close();

    
    
}
