package politicalnetwork.core;

import gnu.trove.map.hash.TIntObjectHashMap;
import politicalnetwork.rationalnumber.RationalNumber;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * This class implementats the political network election system.
 * 
 * 
 * @author Removed for blind review
 */
public class PoliticalNetwork implements IPoliticalNetwork
{
    // Data related to the original structure of the political network
    private RationalNumber originalValidNumberOfVotes;
    private RationalNumber numberOfSeats;    
    final private TIntObjectHashMap<Candidate> candidates;
    
    // Data related to the status of the political netowork
    private TIntObjectHashMap<Candidate> remainingCandidates;
    private TIntObjectHashMap<Candidate> electedCandidates;
    final private Candidate virtualDiscardCandidate;    
    private RationalNumber currentQuota;

    // Helper variables to handle rational numbers
    final private RationalNumber.Factory numberFactory;  // To allow numbers to be created using the correct class
    final private RationalNumber zero; // useful constant
    final private RationalNumber one;  // useful constant
    
    // True if this the election should be accompanied by convergence tests and extra consistency tests
    final boolean checkConvergenceAndMakeExtraConsistencyTests;

    public interface TieBreaker
    {
        /**
         * Compares two candidates that have exactly the same number of votes.
         * @param id1
         * @param id2
         * @return -1 if the first candidate is less prefered to be elected than the second
         *         +1 if the first candidate is prefered to be elected than the second
         *          0 only if the two candidates are the same
         */
        int compare(int id1,int id2);
    }        
    public static class IDTierBreaker implements TieBreaker
    {
        @Override
        public int compare(int id1, int id2)
        {
            if (id1 < id2) return -1;
            if (id2 < id1) return 1;
            return 0;
        }
    }
    
    public interface DefinitionListener
    {
        /**
         * Registers that a candidate was elected or eliminated by the election algorithm.
         * This allows external monitoring of the election and thus to helps to debug code, for example,
         * checking for violations of solid coalition guarantees.
         * 
         * @param candidate
         * @param elected True if the candidate was elected. False if eliminated.
         */
        void registerDefinition(Candidate candidate, boolean elected);
    }
    
    final TieBreaker tieBreaker;
    final DefinitionListener definitionListener;
    
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(currentQuota);
        sb.append("\n");
        for (Candidate c:candidates.valueCollection())        
            sb.append(c.toStringWithLinks());
        sb.append(virtualDiscardCandidate.toStringWithLinks());
        return sb.toString();
    }        
    @Override
    public boolean equals(Object o)
    {        
        PoliticalNetwork otherPoliticalNetwork = (PoliticalNetwork)o;
        if (otherPoliticalNetwork.candidates.size()!=candidates.size())
            return false;
        if (!currentQuota.equals(otherPoliticalNetwork.currentQuota))
            return false;
        for (Candidate c:candidates.valueCollection())
        {
            Candidate oc = otherPoliticalNetwork.getCandidate(c.identifier);
            if (oc==null)
                return false;
            if (!c.equals(oc))
                return false;
        }    
        return virtualDiscardCandidate.equals(otherPoliticalNetwork.virtualDiscardCandidate);
    }        
    
    public TIntObjectHashMap<Candidate> getElected()
    {
        return electedCandidates;
    }
    
        
    /**
     * Creates an empty political network.
     * @param numberFactory Allows numbers to be created using the correct class
     * @param name A name to be displayed when necessary
     * @param numTh The number of threads that should be used when the election starts to be processed.
     * @param checkConvergenceAndMakeExtraConsistencyTests If true vote transfer processes are also performed iteractively using the original
     *                                                     network structure and the results are compared to the exact ones obtained with the
     *                                                     modified structure to see if they are converging to the right place .
     *                                                     Extra consistency tests are also performed at each round of the election 
     * @param definitionListener A listener to be warned of elections and eliminations while they happen
     * @param tieBreaker A breaker for eventual ties when candidates are about to be eliminated
     */
    public PoliticalNetwork(RationalNumber.Factory numberFactory,String name,int numTh,boolean checkConvergenceAndMakeExtraConsistencyTests,TieBreaker tieBreaker,DefinitionListener definitionListener)
    {
        this.tieBreaker = tieBreaker;
        this.definitionListener = definitionListener;
        this.checkConvergenceAndMakeExtraConsistencyTests = checkConvergenceAndMakeExtraConsistencyTests;
        this.numberFactory = numberFactory;
        zero = numberFactory.valueOf(0,1);
        one = numberFactory.valueOf(1,1);
        remainingCandidates = new TIntObjectHashMap();
        electedCandidates = new TIntObjectHashMap();
        originalValidNumberOfVotes = zero;
        candidates =  new TIntObjectHashMap();
        virtualDiscardCandidate = new Candidate(0,false);
        virtualDiscardCandidate.setIndividualVotes(numberFactory.valueOf(0, 1));
        this.numTh = numTh;
        // Creates threads to perform edge removals in parallel
        for (int t=0; t<numTh; t++)
            new Thread(new Remover(t),"DoRemoves "+name+" "+t).start();        
    }        
    public synchronized void close()
    {
        // This ends the remover threads
        removeState = RS_DONE;
        numDone=0;
        numActive = 0;
        notifyAll();
    }        

    public RationalNumber getCurrentQuota()
    {
        return currentQuota;
    }
    
    public Candidate getCandidate(int identifier)
    {
        return candidates.get(identifier);
    }        
    public void addCandidate(int identifier)
    {
         candidates.put(identifier, new Candidate(identifier,false));    
    }        
    public void addVirtualCandidate(int identifier)
    {
         candidates.put(identifier, new Candidate(identifier,true));    
    }            
    public void addNeighborRelationship(int candidateIdentifier,int neighborCandidateIdentifier,RationalNumber tranferPercentage)
    {
        if (candidateIdentifier==neighborCandidateIdentifier)
            throw new RuntimeException("Candidate set as neighbor of itself");
        Candidate candidate = candidates.get(candidateIdentifier);
        Candidate neighborCandidate = candidates.get(neighborCandidateIdentifier);
        if (candidate ==null)
            throw new RuntimeException("Inexistent candidate "+candidateIdentifier);
        if (neighborCandidate==null)
            throw new RuntimeException("Inexistent candidate "+neighborCandidateIdentifier);
        
         candidate.addNeighbor(neighborCandidate,tranferPercentage);      
    }        
    public void setNumberOfVotes(int numero,int votosProprios)
    {
        if (!candidates.containsKey(numero))
            throw new RuntimeException("Inexistent candidate "+numero);
        candidates.get(numero).setIndividualVotes(numberFactory.valueOf(votosProprios,1));
    }        
    public void setNumberOfSeats(int numeroDeCadeiras)
    {
        this.numberOfSeats = numberFactory.valueOf(numeroDeCadeiras, 1);
    }        
    
    public void checkConsistency(boolean areTransfersComplete)
    {
        if (!checkConvergenceAndMakeExtraConsistencyTests)
            return;
        RationalNumber currentNumberOfValidVotesFromQuota = currentQuota.times(numberOfSeats);
        RationalNumber currentNumberOfValidVotesFromDiscards = originalValidNumberOfVotes.minus(virtualDiscardCandidate.numberOfCurrentVotes);
        if (!numberFactory.isClose(currentNumberOfValidVotesFromDiscards,currentNumberOfValidVotesFromQuota))
            throw new RuntimeException("Inconsistent current quota");
        RationalNumber  currentNumberOfValidVotesFromSum = zero;        
        for (Candidate c: candidates.valueCollection())
        {    
            c.checkConsistency(currentQuota,numberFactory,areTransfersComplete);
            currentNumberOfValidVotesFromSum = currentNumberOfValidVotesFromSum.plus(c.numberOfCurrentVotes);
        }
        if (!numberFactory.isClose(currentNumberOfValidVotesFromSum,currentNumberOfValidVotesFromDiscards))
            throw new RuntimeException("Inconsistent number of valid votes");
    }        
    
    /**
     * Sets the initial state of the network. Should be called after all parameters have been set.
     */
    private void prepareToProcess()
    {
        originalValidNumberOfVotes = zero;
        for (Candidate c:candidates.valueCollection())
        {    
            originalValidNumberOfVotes = originalValidNumberOfVotes.plus(c.numberOfIndividualVotes);               
            c.prepareForProcessingElection(virtualDiscardCandidate,numberFactory);
        }    
        virtualDiscardCandidate.prepareVirtualDiscardCandidateForProcessingElection(numberFactory);
        remainingCandidates = new TIntObjectHashMap(candidates); // initially all candidates are remaining
        electedCandidates = new TIntObjectHashMap();             // none are elected
        currentQuota =  originalValidNumberOfVotes.divide(numberOfSeats);        
    }
            

    private void identifyElectedAndTransferVotes()
    {
        HashMap<Integer,Candidate> recentlyElected = identifyAndMarkRecentlyElected();
        while (!recentlyElected.isEmpty())
        {            
            //updateNetworkRemovingFromNeighborSets(recentlyElected);                        
            transferVotesAndUpdateQuota(recentlyElected,new ArrayList());                        
            recentlyElected = identifyAndMarkRecentlyElected(); // more candidates can be elected due to vote tranfers and reductions in the current currentQuota
        }    
    }        
    
    
    public void processElection()
    {
        prepareToProcess();        
        eliminateVirtualCandidatesAndTranferVotes();
        checkConsistency(false);
        while (!remainingCandidates.isEmpty())
        {
            
            identifyElectedAndTransferVotes();
            checkConsistency(true);
            if (!remainingCandidates.isEmpty())            
            {
                eliminateCandidateWithLeastVotesTranferAndDiscardVotes();
                checkConsistency(false);
            }
        }            
        if (electedCandidates.size()!=numberOfSeats.doubleValue())
            throw new RuntimeException("Number of elected candidates does not match the number of seats "+electedCandidates.size() + " <> " + numberOfSeats.doubleValue());
    }        
    
    
    private HashMap<Integer,Candidate> identifyAndMarkRecentlyElected()
    {
        boolean last = remainingCandidates.size() + electedCandidates.size() == numberOfSeats.doubleValue(); 
        HashMap<Integer,Candidate> recentlyElected = new HashMap();
        for (Iterator<Candidate> i=remainingCandidates.valueCollection().iterator(); i.hasNext(); )
        {
            Candidate c = i.next();
            if (c.numberOfCurrentVotes.compareTo(currentQuota) >= 0)
            {    
                recentlyElected.put(c.identifier,c);
                electedCandidates.put(c.identifier, c);
                c.status = Candidate.ST_ELECTED;
                i.remove();
                if (definitionListener!=null)
                    definitionListener.registerDefinition(c, true);                
            }    
        }    
        if (recentlyElected.isEmpty() && remainingCandidates.size() + electedCandidates.size() == numberOfSeats.doubleValue())
        {
            // compensates rounding errors using Corollary 7
            for (Iterator<Candidate> i=remainingCandidates.valueCollection().iterator(); i.hasNext(); )
            {
                Candidate c = i.next();
                if (last && !numberFactory.isClose(c.numberOfCurrentVotes, currentQuota))
                    throw new RuntimeException("Last candidates don't have a number of votes equal to the quota");
                recentlyElected.put(c.identifier,c);
                electedCandidates.put(c.identifier, c);
                c.status = Candidate.ST_ELECTED;
                i.remove();
                if (definitionListener!=null)
                    definitionListener.registerDefinition(c, true);
            }                            
        }    
        
        return recentlyElected;        
    }        
            
    

    public void defineStatusOfArbitraryCandidates(List<Integer> eliminated,List<Integer> elected)
    {
        if (currentQuota==null)
            prepareToProcess(); // when this method is called for the first time, the network needs to be prepared.
        HashMap<Integer,Candidate> defined = new HashMap();
        ArrayList<Candidate> recentlyEliminated = new ArrayList();
        for(Integer candId:eliminated)
        {   
            Candidate c = candidates.get(candId);
            defined.put(candId, c);
            c.status = Candidate.ST_ELIMINATED;  
            recentlyEliminated.add(c);
        }
        for(Integer candId:elected)
        {   
            Candidate c = candidates.get(candId);
            defined.put(candId, c);
            electedCandidates.put(c.identifier, c);
            c.status = Candidate.ST_ELECTED;  
        }
        transferVotesAndUpdateQuota(defined,recentlyEliminated);
        for(Integer candId:eliminated)
        {    
           Candidate c = candidates.get(candId); 
           c.currentNeighbors = null;                        
        }
    }        
    
    private void eliminateCandidatesAndTranferVotes(List<Candidate> recentlyEliminatedList)
    {
        HashMap<Integer,Candidate> recentlyEliminated = new HashMap();
        for (Candidate eliminated:recentlyEliminatedList)
        {    
            recentlyEliminated.put(eliminated.identifier, eliminated);
            eliminated.status = Candidate.ST_ELIMINATED;  
            remainingCandidates.remove(eliminated.identifier);
        }    
        transferVotesAndUpdateQuota(recentlyEliminated,recentlyEliminatedList);                    
        
        // since eliminated candidates keep zero votes, future transfers originated on them
        // would only be a waste of time, so we clear their neighbor sets.
        for (Candidate eliminated:recentlyEliminatedList)
           eliminated.currentNeighbors = null;        
    }        

    private void eliminateVirtualCandidatesAndTranferVotes()
    {
        ArrayList<Candidate> recentlyEliminatedCandidates = new ArrayList(); 
        for (Candidate c:candidates.valueCollection())        
            if (c.isVirtual)
                recentlyEliminatedCandidates.add(c);
        eliminateCandidatesAndTranferVotes(recentlyEliminatedCandidates);                
    }        
    
    private void eliminateCandidateWithLeastVotesTranferAndDiscardVotes()
    {
        Candidate eliminatedCandidate = null;
        for (Candidate c:remainingCandidates.valueCollection())        
        { 
            if (eliminatedCandidate == null)
                eliminatedCandidate = c;
            else
            {    
                int cpr = c.numberOfCurrentVotes.compareTo(eliminatedCandidate.numberOfCurrentVotes);
                if (cpr < 0 || cpr==0 && tieBreaker.compare(eliminatedCandidate.identifier,c.identifier ) < 0)
                   eliminatedCandidate = c;
            }    
        }    
        ArrayList<Candidate> recentlyEliminatedCandidates = new ArrayList(); 
        recentlyEliminatedCandidates.add(eliminatedCandidate);
        eliminateCandidatesAndTranferVotes(recentlyEliminatedCandidates);                
    }        
    
    private void checkAbsenceInNeighborSets(Candidate c)
    {
        for (Candidate cc:candidates.valueCollection())
        {
            if ( (cc.status!=Candidate.ST_ELIMINATED ) && cc.currentNeighbors!=null && cc.currentNeighbors.containsKey(c.getIdentifier()))
                throw new RuntimeException("Removed Candidate in neighbor set");
        }    
    }        
          
    private void updateNetworkRemovingFromNeighborSets(HashMap<Integer,Candidate> recentlyDefined)
    {
        for (Candidate c:recentlyDefined.values())
        {    
              removeFromNeighborSets(c);
              checkAbsenceInNeighborSets(c);
        }
    }        

    private void removeFromNeighborSets(Candidate c)
    {
        if (c==null)
            throw new RuntimeException("Null candidate");

        // prepare to make parallel removals, create one removal list for each thread        
        removeEntries = new ArrayList[numTh];
        for (int t=0; t<numTh; t++)
           removeEntries[t] = new ArrayList();            
        int  p = 0;
        for (Candidate reverseNeighbor:candidates.valueCollection())
        {    
            if (reverseNeighbor.currentNeighbors==null || !reverseNeighbor.currentNeighbors.containsKey(c.identifier))
                continue;
            removeEntries[p].add(new RemoveEntry(c,reverseNeighbor));                
            p = (p+1) % numTh;
        }               
        synchronized(this)
        {
            removeState = RS_NEIGHBORS;
            numDone = 0;
            numActive = 0;
            notifyAll();
            while (numDone!=numTh)
                try { wait();} catch (InterruptedException ex) {throw new RuntimeException(ex); }
        }            
    }        
    
    int numTh;
    int numDone;
    int numActive;
    int removeState;
    
    static final int RS_WAIT = 0;             // nothing is happening in the removal threads
    static final int RS_NEIGHBORS = 1;         // threads are removing a candidate from neighbor sets
    static final int RS_DONE = 3;             // election is over, threads have ended or are about to end
    static class RemoveEntry
    {        
        Candidate candidateToBeRemoved;
        Candidate candidateFromWhereToRemove;
        public RemoveEntry(Candidate candidatoASerRemovido, Candidate candidatoDeOndeRemover)
        {
            this.candidateToBeRemoved = candidatoASerRemovido;
            this.candidateFromWhereToRemove = candidatoDeOndeRemover;
        }        
    }
    ArrayList<RemoveEntry>[] removeEntries;
    
    class Remover implements Runnable
    {
        int num;
        public Remover(int num)
        {
            this.num = num;
        }        
        @Override
        public void run()
        {     
           try
           {    
              doRemoves(num);
           }
           catch(RuntimeException e)
           {
               e.printStackTrace();
               System.out.println("Exception in remover");
           }    
        }        
    }
    
    private void doRemoves(int tn)
    {        
        while (true)
        {
            int act;
            synchronized(this)
            {
                while (removeState==RS_WAIT)
                    try { wait();} catch (InterruptedException ex) {throw new RuntimeException(ex); }
                act = removeState;
                numActive++;
            }    
            synchronized(this)
            {
                if (numActive==numTh)
                {    
                    removeState=RS_WAIT;                
                    notifyAll();
                }    
                while (numActive!=numTh)
                    try { wait();} catch (InterruptedException ex) {throw new RuntimeException(ex); }
            }            
            if (act==RS_DONE)
                break;
            if (act==RS_NEIGHBORS)
            {
                for (RemoveEntry re:removeEntries[tn])
                    removeFromNeighborsets(re.candidateToBeRemoved,re.candidateFromWhereToRemove);
            }    
            synchronized(this)
            {
                numDone++;
                if (numDone==numTh)
                    notifyAll();
            }
        }    
    }        

    
    private void removeFromNeighborsets(Candidate candidateToBeRemoved,Candidate candidateFromWhereToRemove)
    {
        // Note that only the neighborset of the candidateFromWhereToRemove is submitted to any changes.
        // This means that this method can be called several time in parallel, as long as the vakue of
        // candidateFromWhereToRemove is different for each call
        
        if (!candidateFromWhereToRemove.currentNeighbors.containsKey(candidateToBeRemoved.identifier))
            throw new RuntimeException("Inconsistency in neighbor set");
        if (
               candidateToBeRemoved.currentNeighbors.size()==1 && 
               candidateToBeRemoved.currentNeighbors.containsKey(candidateFromWhereToRemove.identifier) &&
               candidateFromWhereToRemove.currentNeighbors.size()==1
            )
        {
            candidateFromWhereToRemove.currentNeighbors.remove(candidateToBeRemoved.identifier);            
            candidateFromWhereToRemove.currentNeighbors.put(virtualDiscardCandidate.getIdentifier(), new NeighborhoodRelation(virtualDiscardCandidate,numberFactory.valueOf(1, 1)));            
        }   
        else
        {
            RationalNumber percentageForth = candidateFromWhereToRemove.currentNeighbors.get(candidateToBeRemoved.identifier).transferPercentage;
            RationalNumber percentageBack = zero;            
            if (candidateToBeRemoved.currentNeighbors.containsKey(candidateFromWhereToRemove.identifier))
                   percentageBack = candidateToBeRemoved.currentNeighbors.get(candidateFromWhereToRemove.identifier).transferPercentage;
            
            RationalNumber percentageBackAndForth = percentageForth.times(percentageBack);
            RationalNumber complementarPercentageBackAndForth = one.minus(percentageBackAndForth);
            
            candidateFromWhereToRemove.currentNeighbors.remove(candidateToBeRemoved.identifier);
                            
            for (NeighborhoodRelation r:candidateFromWhereToRemove.currentNeighbors.valueCollection())
                r.transferPercentage = r.transferPercentage.divide(complementarPercentageBackAndForth);   
            
            for (NeighborhoodRelation r:candidateToBeRemoved.currentNeighbors.valueCollection())
            {
                if (r.neighbor==candidateFromWhereToRemove)
                    continue;
                NeighborhoodRelation r2 = candidateFromWhereToRemove.currentNeighbors.get(r.neighbor.identifier);
                if (r2==null)
                {    
                    r2 = new NeighborhoodRelation(r.neighbor,zero);
                    candidateFromWhereToRemove.currentNeighbors.put(r2.neighbor.identifier, r2);   
                }    
                r2.transferPercentage = r2.transferPercentage.plus(percentageForth.times(r.transferPercentage.divide(complementarPercentageBackAndForth)));
            }            
        }            
        
    }        

    /**
     * Transfer votes. Vote discards occur through transfers to the virtual discard candiate.
     * 
     * @param recentlyDefined   The candidates that have just had their stated defined as elected or eliminated.
     * @param recentlyEliminated 
     */
    private void transferVotesAndUpdateQuota(HashMap<Integer,Candidate> recentlyDefined,List<Candidate> recaentlyEliminated)
    {     
        updateNetworkRemovingFromNeighborSets(recentlyDefined);   
        
        TemporaryDataForTranfersUsingOriginalStructure dt = checkConvergenceAndMakeExtraConsistencyTests ? new TemporaryDataForTranfersUsingOriginalStructure(): null;       
        
        
         // The parameter recentlyEliminated avoids having to loop over all eliminated candidates to find which ones still have votes.
        RationalNumber sumVP = numberFactory.valueOf(0, 1);
        for (Candidate c:recentlyDefined.values())  //recentlyEliminated)
        {    
            if (c.status==Candidate.ST_ELIMINATED)
            {    
                RationalNumber v = c.numberOfCurrentVotes;
                NeighborhoodRelation rDiscard = c.currentNeighbors.get(virtualDiscardCandidate.identifier);
                RationalNumber p = rDiscard==null ? zero : rDiscard.transferPercentage;
                RationalNumber vp = v.times(p);
                sumVP = sumVP.plus(vp);
            }
        }
        // It is necessary to loop over all elected candidates, not only the recently elected anyway 
        RationalNumber sumP = numberFactory.valueOf(0, 1);                
        for (Candidate c:electedCandidates.valueCollection())
        {    
            RationalNumber v = c.numberOfCurrentVotes;
            NeighborhoodRelation rDiscard = c.currentNeighbors.get(virtualDiscardCandidate.identifier);
            RationalNumber p = rDiscard==null ? zero : rDiscard.transferPercentage;
            RationalNumber vp = v.times(p);
            sumVP = sumVP.plus(vp);
            sumP = sumP.plus(p);
        }
        RationalNumber newQuota;
        if (sumP.compareTo(numberOfSeats) > 0 && !numberFactory.isClose(sumP, numberOfSeats))
            throw new RuntimeException("The sum of the percentages of discard of electec candidates is greater than the number of available seats");

        if (!remainingCandidates.isEmpty())            
            newQuota = originalValidNumberOfVotes.minus(virtualDiscardCandidate.numberOfCurrentVotes).minus(sumVP).divide(numberOfSeats.minus(sumP));
        else
        {   
            if ( !numberFactory.isClose(sumP, numberOfSeats))
                throw new RuntimeException("The sum of the percentages of discard of electec candidates does not match the number of available seats");
            
            // in the very end of the election, vote tranfers and discards don't matter anymore
            // the currentQuota could be kept the same or reduced to any non negative number including zero
            // we just cannot use the usual update formula to avoid a 0/0
            // if we set the currentQuota to zero, all elected candidates would discard all votes
            // we prefer to keep the old currentQuota.
            newQuota = currentQuota;            
        }    
        
        transferVotesAccordingToQuota(newQuota,recentlyDefined); //recentlyEliminated);
        
        currentQuota = newQuota;

        
        if (dt!=null)
           transferVotesIteractivelyUsingOriginalStructure(dt);
        
        
    }    
        
        
    private void transferVotesAccordingToQuota(RationalNumber newQuota, HashMap<Integer,Candidate> recentlyDefined)//List<Candidate> recentlyEliminated)
    {        
        HashSet<Candidate> candidatesWithTransferrableVotes = new HashSet();
        //candidatesWithTransferrableVotes.addAll(recentlyEliminated);
        for (Candidate c:recentlyDefined.values())
            if (c.status==Candidate.ST_ELIMINATED)
               candidatesWithTransferrableVotes.add(c);
            
        for (Candidate c:electedCandidates.valueCollection())
           if (c.numberOfCurrentVotes.compareTo(newQuota)>=0) 
              candidatesWithTransferrableVotes.add(c);
        
        for (Candidate c:candidatesWithTransferrableVotes) 
        {
            RationalNumber transferrableVotes =  (c.status==Candidate.ST_ELIMINATED) ? c.numberOfCurrentVotes : c.numberOfCurrentVotes.minus(newQuota);
            for (NeighborhoodRelation r:c.currentNeighbors.valueCollection())
            {    
                    r.neighbor.numberOfCurrentVotes = r.neighbor.numberOfCurrentVotes.plus(transferrableVotes.times(r.transferPercentage));
                    if (r.neighbor.status!=Candidate.ST_REMAINING && r.neighbor.status!=Candidate.ST_VIRTUALDISCARDCANDIDATE)
                        throw new RuntimeException("Votes being tranferred to non remanining candidate");
            }        
            c.numberOfCurrentVotes = c.numberOfCurrentVotes.minus(transferrableVotes);
        }            
    }        

    
    /**
     * This class conatins data that allows iteractive vote transfers using the unmodified political network.
     */
    class TemporaryDataForTranfersUsingOriginalStructure
    {
          TIntObjectHashMap<Candidate> candidates;
          TIntObjectHashMap<Candidate> sourceCandidates; // candidates that can be the source of a transfer
          RationalNumber currentQuota;           
          Candidate discardCandidate;
          TemporaryDataForTranfersUsingOriginalStructure()
          {              
            currentQuota = PoliticalNetwork.this.currentQuota;  
            discardCandidate = new Candidate(PoliticalNetwork.this.virtualDiscardCandidate);
            candidates = new TIntObjectHashMap();
            for (Candidate c:PoliticalNetwork.this.candidates.valueCollection())   // copies candidates             
               candidates.put(c.identifier,new Candidate(c));              
            for (Candidate c:candidates.valueCollection())                         // copies relations
            {
                TIntObjectHashMap<NeighborhoodRelation> originalNeighbors = new TIntObjectHashMap();
                for (NeighborhoodRelation r:c.originalNeighbors.valueCollection())
                    originalNeighbors.put(r.neighbor.identifier,new NeighborhoodRelation(candidates.get(r.neighbor.identifier),r.transferPercentage));
                c.originalNeighbors = originalNeighbors;
            }    
            sourceCandidates = new TIntObjectHashMap();
            for (Candidate c:candidates.valueCollection())
                if (
                        c.status==Candidate.ST_ELECTED || 
                        c.status==Candidate.ST_ELIMINATED  
                   )
                   sourceCandidates.put(c.identifier, c); 
          
          }        
          TemporaryDataForTranfersUsingOriginalStructure(TemporaryDataForTranfersUsingOriginalStructure dt)
          {              
            currentQuota = dt.currentQuota;  
            discardCandidate = new Candidate(dt.discardCandidate);
            candidates = new TIntObjectHashMap();
            for (Candidate c:dt.candidates.valueCollection())   // copies candidates             
               candidates.put(c.identifier,new Candidate(c));                          
            for (Candidate c:candidates.valueCollection())                         // copies relations
            {
                TIntObjectHashMap<NeighborhoodRelation> originalNeighbors = new TIntObjectHashMap();
                for (NeighborhoodRelation r:c.originalNeighbors.valueCollection())
                    originalNeighbors.put(r.neighbor.identifier,new NeighborhoodRelation(candidates.get(r.neighbor.identifier),r.transferPercentage));
                c.originalNeighbors = originalNeighbors;
            }    
            sourceCandidates = new TIntObjectHashMap();
            for (Candidate c:candidates.valueCollection())
                if (
                        c.status==Candidate.ST_ELECTED || 
                        c.status==Candidate.ST_ELIMINATED  
                   )
                   sourceCandidates.put(c.identifier, c);           
          }        
          
    }
    /**
     * This method is used to check if an iteractive vote transfer process using the original structure would indeed coverge to
     * the results calculated usign the modified structure.
     * @param dt A structure containing a copy of the original data
     */
    private void transferVotesIteractivelyUsingOriginalStructure(TemporaryDataForTranfersUsingOriginalStructure dto)
    {
        int mul =1; // this variable regulates the number of iteractions
        RationalNumber maxDif;
        do // This loop tries to increase the number of iteractions too see if convergence is confirmed.
           // When the number ofiteractions is increased, the vote transfer process is started from zero.
           // Discarding votes too ealy makes errors permanent, so we need to restart and be more patient. 
           // We could work around this problem detecting transitive closures, but we prefer to be simplistic here. 
        {
            if (mul > 4096) // an arbitrary limit for the multiplier of the number of iteractions
                throw new RuntimeException("Convergence not verified");            
            
            // Copies the initial data to be able to restart latter, if the number of iterations are not enough to confirm convergence.            
            TemporaryDataForTranfersUsingOriginalStructure dt = new TemporaryDataForTranfersUsingOriginalStructure(dto);
            int maxNumberValueForTheQuotaAttempted = electedCandidates.isEmpty() ? 1 : 2 * mul; // when there are no elected candidates, the current quota is never reduced
            int maxRounds = 32 * mul;
            for (int i=0; i<maxNumberValueForTheQuotaAttempted; i++) // tries quota reductions
            {    
                for (int t=0; t<maxRounds; t++) // execute rounds of the transfer process for a fixed quota
                    for (Candidate c:dt.sourceCandidates.valueCollection()) // execute transfers using every possible candidate as a source
                    {    
                        if (c.status==Candidate.ST_REMAINING)
                            throw new RuntimeException("Remaining candidate listed as a source for vote transfers");

                        RationalNumber transferableVotes = c.status==Candidate.ST_ELECTED ? c.numberOfCurrentVotes.minus(dt.currentQuota) : c.numberOfCurrentVotes;
                        if (!transferableVotes.equals(zero))
                        {   
                            if (!c.originalNeighbors.isEmpty())
                                c.numberOfCurrentVotes = c.numberOfCurrentVotes.minus(transferableVotes);
                            for (NeighborhoodRelation r:c.originalNeighbors.valueCollection())
                                r.neighbor.numberOfCurrentVotes = r.neighbor.numberOfCurrentVotes.plus(transferableVotes.times(r.transferPercentage));
                        }    
                    }    
                // after a big number of vote transfers, discard transferrable votes that have not reached remaining candidates                
                for (Candidate c:dt.sourceCandidates.valueCollection())
                    if (c.status!=Candidate.ST_REMAINING)
                    {
                        RationalNumber vp = c.status==Candidate.ST_ELECTED ? c.numberOfCurrentVotes.minus(dt.currentQuota) : c.numberOfCurrentVotes;
                        if (!vp.equals(zero))
                        {    
                            c.numberOfCurrentVotes = c.numberOfCurrentVotes.minus(vp);
                            dt.discardCandidate.numberOfCurrentVotes = dt.discardCandidate.numberOfCurrentVotes.plus(vp);
                        }    
                    }    
                dt.currentQuota = originalValidNumberOfVotes.minus(dt.discardCandidate.numberOfCurrentVotes).divide(numberOfSeats);          
            }           
            maxDif = zero;
            for (Candidate c:dt.candidates.valueCollection())
            {
                Candidate cc = candidates.get(c.identifier);
                RationalNumber dif = c.numberOfCurrentVotes.minus(cc.numberOfCurrentVotes);
                if (dif.compareTo(zero) < 0)
                    dif = zero.minus(dif);                
                if (maxDif.compareTo(dif) < 0)                
                    maxDif = dif;            
            } 
            mul *= 2;
        }    
        while (maxDif.doubleValue() >=1); // 1 is an arbitrary value. A smaller value would need more iteractions            
    }        
    
}
