package politicalnetwork.core;

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;
import politicalnetwork.rationalnumber.RationalNumber;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author Removed for Blind Review
 */
public class PoliticalNetwork implements IPoliticalNetwork
{
    private RationalNumber originalValidNumberOfVotes;
//    RationalNumber votosValidosCorrentes;
    final private TIntObjectHashMap<Candidate> candidates;
    private TIntObjectHashMap<Candidate> remainingCandidates;
    private TIntObjectHashMap<Candidate> electedCandidates;
    private TIntObjectHashMap<Candidate> eliminatedCandidates;
    final private Candidate virtualDiscardCandidate;    
    private RationalNumber currentQuota;
    private RationalNumber numberOfSeats;
    final private RationalNumber.Factory numberFactory;  // To allow numbers to be created using the correct class
    final private RationalNumber zero; // useful constant
    final private RationalNumber one;  // useful constant
    final boolean checkConvergence;
    
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        for (Candidate c:candidates.valueCollection())        
            sb.append(c.toStringWithLinks());
        sb.append(virtualDiscardCandidate.toStringWithLinks());
        return sb.toString();
    }        
    @Override
    public boolean equals(Object o)
    {
        PoliticalNetwork rp = (PoliticalNetwork)o;
        if (rp.candidates.size()!=candidates.size())
            return false;
        for (Candidate c:candidates.valueCollection())
        {
            Candidate oc = rp.getCandidate(c.identifier);
            if (oc==null)
                return false;
            if (!c.equals(oc))
                return false;
        }    
        return virtualDiscardCandidate.equals(rp.virtualDiscardCandidate);
    }        
    
    public TIntObjectHashMap<Candidate> getElected()
    {
        return electedCandidates;
    }
    
        
    /**
     * Creates an empty political network.
     * @param numberFactory Allows number to be created using the correct class
     * @param name A name to be displayed when necessary
     * @param numTh The number of thread that should be used when the election starts to be processed.
     * @param checkConvergence If true vote transfer processes are also performed iteractively using the original
     *                         network structure and the results are compared to the exact ones obtained with the
     *                         modified structure to see if they are converging to the right place 
     */
    public PoliticalNetwork(RationalNumber.Factory numberFactory,String name,int numTh,boolean checkConvergence)
    {
        this.checkConvergence = checkConvergence;
        this.numberFactory = numberFactory;
        zero = numberFactory.valueOf(0,1);
        one = numberFactory.valueOf(1,1);
        remainingCandidates = new TIntObjectHashMap();
        electedCandidates = new TIntObjectHashMap();
        eliminatedCandidates = new TIntObjectHashMap();        
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
        // This ends some threads
        removeState = RS_DONE;
        notifyAll();
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
         neighborCandidate.addReverseNeighbor(candidate);
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
        if (true) return;
        RationalNumber currentNumberOfValidVotesFromQuota = currentQuota.times(numberOfSeats);
        RationalNumber currentNumberOfValidVotesFromDiscards = originalValidNumberOfVotes.minus(virtualDiscardCandidate.numberOfCurrentVotes);
        if (!currentNumberOfValidVotesFromDiscards.equals(currentNumberOfValidVotesFromQuota))
            throw new RuntimeException("Inconsistent current quota");
        RationalNumber  currentNumberOfValidVotesFromSum = zero;        
        for (Candidate c: candidates.valueCollection())
        {    
            c.checkConsistency(currentQuota,numberFactory,areTransfersComplete);
            currentNumberOfValidVotesFromSum = currentNumberOfValidVotesFromSum.plus(c.numberOfCurrentVotes);
        }
        if (!currentNumberOfValidVotesFromSum.equals(currentNumberOfValidVotesFromDiscards))
            throw new RuntimeException("Inconsistent number of valid votes");
    }        
    
    
    public void prepareToProcess()
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
        eliminatedCandidates = new TIntObjectHashMap();          // neither eliminated
        currentQuota =  originalValidNumberOfVotes.divide(numberOfSeats);        
    }
            
    public void processElection()
    {
        prepareToProcess();        
        eliminateVirtualCandidatesAndTranferVotes();
        checkConsistency(true);
        identifyElectedAndTransferVotes();
        checkConsistency(true);
        //System.out.println("electedCandidates: "+electedCandidates.size()+" eliminatedCandidates: "+eliminatedCandidates.size()+" remainingCandidates: "+remainingCandidates.size());
        while (!remainingCandidates.isEmpty())
        {
            checkConsistency(true);
            eliminateCandidateWithLeastVotesTranferAndDiscardVotes();
            checkConsistency(false);
            identifyElectedAndTransferVotes();
            RationalNumber votosValidosCorrentes = originalValidNumberOfVotes.minus(virtualDiscardCandidate.getNumberOfCurrentVotes());
            //System.out.println("electedCandidates: "+electedCandidates.size()+" eliminatedCandidates: "+eliminatedCandidates.size()+" remainingCandidates: "+remainingCandidates.size()+" votosValidos "+votosValidosCorrentes);
        }            
        if (electedCandidates.size()!=numberOfSeats.doubleValue())
            throw new RuntimeException("Number of elected candidates does not match the number of seats "+electedCandidates.size() + " <> " + numberOfSeats.doubleValue());
    }        
    
    private HashMap<Integer,Candidate> identifyRecentlyElected()
    {
        boolean last = remainingCandidates.size() + electedCandidates.size() == numberOfSeats.doubleValue(); // this variable compensates rounding errors
        HashMap<Integer,Candidate> recemEleitos = new HashMap();
        for (Iterator<Candidate> i=remainingCandidates.valueCollection().iterator(); i.hasNext(); )
        {
            Candidate c = i.next();
            if (c.numberOfCurrentVotes.compareTo(currentQuota) >= 0 || last)
            {    
                recemEleitos.put(c.identifier,c);
                electedCandidates.put(c.identifier, c);
                c.status = Candidate.ST_ELECTED;
                c.numberOfVotesWhenEliminatedOrElected = c.numberOfCurrentVotes;
                i.remove();
            }    
        }    
        return recemEleitos;        
    }        
            
    
    private void identifyElectedAndTransferVotes()
    {
        HashMap<Integer,Candidate> recentlyElected = identifyRecentlyElected();
        while (!recentlyElected.isEmpty())
        {            
            removeFromNeighborSets(recentlyElected);
            
            //if (numberOfSeats.equals(numberFactory.valueOf(electedCandidates.size(),1)))
            // we could break the election here, since the elected candidates are defined, but
            // we preferred to let the last vote transfer go on and commented out the command
            //    break;
            
            transferVotesAndUpdateQuota(new ArrayList());            
            
            // more candidates can be elected due to vote tranfers and reductions in the current quota
            recentlyElected = identifyRecentlyElected();
        }    
    }        

    private void eliminateCandidatesAndTranferVotes(List<Candidate> recentlyEliminatedList)
    {
        HashMap<Integer,Candidate> recentlyEliminated = new HashMap();
        for (Candidate eliminated:recentlyEliminatedList)
        {    
            recentlyEliminated.put(eliminated.identifier, eliminated);
            eliminated.status = Candidate.ST_BEING_ELIMINATED;  
            // temporary status to separate the recently eliminated candidates from other eliminated candidates
            // what helps during network updates
        }    
        removeFromNeighborSets(recentlyEliminated);               
        for (Candidate eliminated:recentlyEliminatedList)
        {    
            eliminatedCandidates.put(eliminated.identifier, eliminated);
            eliminated.status = Candidate.ST_ELIMINATED;        // set the permanent status of the candidate
            eliminated.numberOfVotesWhenEliminatedOrElected = eliminated.numberOfCurrentVotes;
            remainingCandidates.remove(eliminated.identifier);
            //System.out.println("eliminated votes "+eliminado.numberOfCurrentVotes+"   currentQuota "+currentQuota);
        }                
        transferVotesAndUpdateQuota(recentlyEliminatedList);                    
        
        // since eliminated candidates keep zero votes, future transfers originated on them
        // would only be a waste of time, so we clear their neighbor sets.
        for (Candidate eliminated:recentlyEliminatedList)
           eliminated.currentNeighbors = null;        
    }        

    private void eliminateVirtualCandidatesAndTranferVotes()
    {
        //ArrayList<Candidate> candidates = new ArrayList(remainingCandidates.valueCollection()); 
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
            if (eliminatedCandidate == null || c.numberOfCurrentVotes.compareTo(eliminatedCandidate.numberOfCurrentVotes) < 0)
                eliminatedCandidate = c;
        }    
        ArrayList<Candidate> recentlyEliminatedCandidates = new ArrayList(); 
        recentlyEliminatedCandidates.add(eliminatedCandidate);
        eliminateCandidatesAndTranferVotes(recentlyEliminatedCandidates);                
    }        
    
    private void checkAbsenceInNeighborSets(Candidate c)
    {
        for (Candidate cc:candidates.valueCollection())
        {
            if (cc.currentNeighbors!=null && cc.currentNeighbors.containsKey(c.getIdentifier()))
                throw new RuntimeException("Removed Candidate in neighbor set");
        }    
    }        
          
    private void removeFromNeighborSets(HashMap<Integer,Candidate> recemDefinidos)
    {
        for (Candidate c:recemDefinidos.values())
        {    
              removeFromNeighborSets(c);
              checkAbsenceInNeighborSets(c);
        }
    }        
    
    public void removeFromAllNeighborSets(int identifier)
    {
        Candidate c= candidates.get(identifier);
        removeFromNeighborSets(c);
    }        

    private void removeFromNeighborSets(Candidate c)
    {
        if (c==null)
            throw new RuntimeException("Null candidate");
        if (c.currentReverseNeighbors==null)
            throw new RuntimeException("Candidate being removed from neighbor sets without reverse neighbor set");
        THashSet<Candidate> reverseNeighbors = new THashSet(c.currentReverseNeighbors.valueCollection());

        // prepare to make parallel removals, create one removal list for each thread        
        removeEntries = new ArrayList[numTh];
        for (int t=0; t<numTh; t++)
           removeEntries[t] = new ArrayList();            
        int  p = 0;
        for (Candidate reverseNeighbor:reverseNeighbors)
        {    
            if (reverseNeighbor.status!=Candidate.ST_ELIMINATED) 
            {    
                removeEntries[p].add(new RemoveEntry(c,reverseNeighbor));                
                p = (p+1) % numTh;
            }    
            else
                throw new RuntimeException("Eliminated candidate in reverse neighbor set");
        }               
        // cause threads to remove from neighbor sets but not reverse neighbor sets
        synchronized(this)
        {
            removeState = RS_NEIGHBORS;
            numDone = 0;
            notifyAll();
            try { wait();} catch (InterruptedException ex) {throw new RuntimeException(ex); }
        }    
        
        // prepare to remove again,now from reverse neighbor sets
        removeEntries = new ArrayList[numTh];
        for (int t=0; t<numTh; t++)
           removeEntries[t] = new ArrayList();
        
        p = 0;
        for (NeighborhoodRelation r:c.currentNeighbors.valueCollection())
        {
            Candidate neighbor = r.neighbor;
            if (neighbor.status!=Candidate.ST_ELIMINATED) 
            {    
                removeEntries[p].add(new RemoveEntry(c,neighbor));                
                p = (p+1) % numTh;
            }    
            else
                throw new RuntimeException("Eliminated candidate in neighbor set");
        }       
        // cause threads to remove from reverse neighbor sets
        synchronized(this)
        {
            removeState = RS_REVERSENEIGHBORS;
            numDone = 0;
            notifyAll();
            try { wait();} catch (InterruptedException ex) {throw new RuntimeException(ex); }
        }   
        
        // one the candidate is not a remaining candidate anymore, it will never be remove from neighbor sets again and 
        // the reverse neighbor set can be cleared
        // note that the neighbor set, must be kept for elected candidates, because further vote tranfers can occur due
        // to reductions in the current quota
        c.currentReverseNeighbors = null;                
    }        
    
    int numTh;
    int numDone;
    int removeState;
    static final int RS_WAIT = 0;             // nothing is happening in the removal threads
    static final int RS_NEIGHBORS = 1;         // threads are removing a candidate from neighbor sets
    static final int RS_REVERSENEIGHBORS = 2;   // threads are removing a candidate from reverse neighbor sets
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
           doRemoves(num);
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
            }    
            if (act==RS_DONE)
                break;
            if (act==RS_NEIGHBORS)
            {
                for (RemoveEntry re:removeEntries[tn])
                    removeFromNeighborsetsWithoutAdjustingReverseNeighbors(re.candidateToBeRemoved,re.candidateFromWhereToRemove);
            }    
            if (act==RS_REVERSENEIGHBORS)
            {
                for (RemoveEntry re:removeEntries[tn])
                    removeFromReverseNeighborSets(re.candidateToBeRemoved,re.candidateFromWhereToRemove);
            }                            
            synchronized(this)
            {
                numDone++;
                if (numDone==numTh)
                {
                    removeState=RS_WAIT;
                    notifyAll();
                }    
                else
                    try { wait();} catch (InterruptedException ex) {throw new RuntimeException(ex); }
            }
        }    
    }        

    
    private void removeFromNeighborsetsWithoutAdjustingReverseNeighbors(Candidate candidateToBeRemoved,Candidate candidateFromWhereToRemove)
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
        //checa();
    }        
    
    private void removeFromReverseNeighborSets(Candidate candidateToBeRemoved,Candidate candidateFromWhereToRemove)
    {
        // since  the virtual discard candidates is never removed from neighbor sets we don't need to keep track
        // of which candidates have it as a neighbor
        if (candidateFromWhereToRemove.status==Candidate.ST_VIRTUALDISCARDCANDIDATE)
            return;
        // Eliminated candidates have their reverse neighbor sets cleared, so they cannot appear here.
        if (candidateFromWhereToRemove.status==Candidate.ST_ELIMINATED)
            throw new RuntimeException("Adjusting reverse neighbor set of eliminated candidate");

        if (candidateToBeRemoved.status==Candidate.ST_ELIMINATED)
            throw new RuntimeException("Already eliminated candidate being removed");
        
        // since eliminated candidates are never going to be removed again from neighbor sets we don't need to keep track
        // of which candidates have it as a neighbor        
        if (candidateFromWhereToRemove.status==Candidate.ST_BEING_ELIMINATED)
            return;
                                       
        if (candidateFromWhereToRemove!=virtualDiscardCandidate && !candidateFromWhereToRemove.currentReverseNeighbors.containsKey(candidateToBeRemoved.identifier))
            throw new RuntimeException("Inconsistency in neighbor set");

                
        for (Candidate reverseNeighbor:candidateToBeRemoved.currentReverseNeighbors.valueCollection())            
           if (reverseNeighbor != candidateFromWhereToRemove)           
               candidateFromWhereToRemove.currentReverseNeighbors.put(reverseNeighbor.identifier, reverseNeighbor);

        
        if (candidateToBeRemoved.status==Candidate.ST_BEING_ELIMINATED)
             candidateFromWhereToRemove.currentReverseNeighbors.remove(candidateToBeRemoved.identifier);
        
    }        
        
    
    static int numRep = 0;
    private void transferVotesAndUpdateQuota(List<Candidate> recentlyEliminated)
    {     
        // The parameter recentlyEliminated avoid havinf to loop over all eliminated candidate to find which ones still have votes.
        
        //DadosTemporariosRepasseEstruturaOriginal dt = new DadosTemporariosRepasseEstruturaOriginal();
        
        // Update the 
        
        RationalNumber sumVP = numberFactory.valueOf(0, 1);
        for (Candidate c:recentlyEliminated)
        {    
            RationalNumber v = c.numberOfCurrentVotes;
            NeighborhoodRelation rDiscard = c.currentNeighbors.get(virtualDiscardCandidate.identifier);
            RationalNumber p = rDiscard==null ? zero : rDiscard.transferPercentage;
            RationalNumber vp = v.times(p);
            sumVP = sumVP.plus(vp);
        }
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
        if (sumP.compareTo(numberOfSeats) > 0)
            throw new RuntimeException("Percentuais de descarte de eleitos superam cadeiras");

        if (!remainingCandidates.isEmpty())
            newQuota = originalValidNumberOfVotes.minus(virtualDiscardCandidate.numberOfCurrentVotes).minus(sumVP).divide(numberOfSeats.minus(sumP));
        else
            // in the very end of the election, vote tranfers and discards don't matter anymore
            // the quota could be kept the same or reduced to any non negative number including zero
            // we just cannot use the usual update formula to avoid a 0/0
            // if we set the quota to zero, all elected candidates would discard all votes
            // we prefer to keep the old quota.
            newQuota = currentQuota;
        //if (!novoQuocienteEleitoral.equals(currentQuota))
        //   System.out.println("currentQuota reduced to "+currentQuota);        
        
        transferVotesAccordingToQuota(newQuota, recentlyEliminated);
        
        currentQuota = newQuota;

        //if (numRep++ % 1 ==0)
        //   repassaPelaRedeEAtualizaCoeficienteEstruturaOriginal(dt);
            //System.out.println("Repasee teste");
        
    }    
        
        
    private void transferVotesAccordingToQuota(RationalNumber newQuota, List<Candidate> recentlyEliminated)
    {        
        HashSet<Candidate> candidatesWithTransferrableVotes = new HashSet();
        candidatesWithTransferrableVotes.addAll(recentlyEliminated);
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

    
    
    class DadosTemporariosRepasseEstruturaOriginal
    {
          TIntObjectHashMap<Candidate> candidatos;
          TIntObjectHashMap<Candidate> candidatosFonte;
          RationalNumber quocienteEleitoral;           
          Candidate descarte;
          DadosTemporariosRepasseEstruturaOriginal()
          {              
            quocienteEleitoral = PoliticalNetwork.this.currentQuota;  
            descarte = new Candidate(PoliticalNetwork.this.virtualDiscardCandidate);
            candidatos = new TIntObjectHashMap();
            for (Candidate c:PoliticalNetwork.this.candidates.valueCollection())                
               candidatos.put(c.identifier,new Candidate(c));              
            for (Candidate c:candidatos.valueCollection())
            {
                TIntObjectHashMap<NeighborhoodRelation> vizinhosOriginais = new TIntObjectHashMap();
                for (NeighborhoodRelation r:c.originalNeighbors.valueCollection())
                    vizinhosOriginais.put(r.neighbor.identifier,new NeighborhoodRelation(candidatos.get(r.neighbor.identifier),r.transferPercentage));
                c.originalNeighbors = vizinhosOriginais;
            }    
            candidatosFonte = new TIntObjectHashMap();
            for (Candidate c:candidatos.valueCollection())
                if (
                        c.status==Candidate.ST_ELECTED || 
                        c.status==Candidate.ST_ELIMINATED  ||
                        c.status==Candidate.ST_BEING_ELIMINATED 
                   )
                   candidatosFonte.put(c.identifier, c); 
          }        
    }
    
    private void repassaPelaRedeEAtualizaCoeficienteEstruturaOriginal(DadosTemporariosRepasseEstruturaOriginal dt)
    {
        int nq = electedCandidates.isEmpty() ? 1 : 16;
        
        for (int i=0; i<nq; i++)
        {    
/*            
            RationalNumber maxDif = zero;
            RationalNumber totDif =  zero;            
            RationalNumber totMain = zero;
            RationalNumber totCopy = zero;
            for (Candidate c:dt.candidates.values())
            {
                Candidate cc = candidates.get(c.identifier);
                totMain = totMain.plus(cc.numberOfCurrentVotes);
                totCopy = totCopy.plus(c.numberOfCurrentVotes);
                RationalNumber dif = c.numberOfCurrentVotes.minus(cc.numberOfCurrentVotes);
                totDif = totDif.plus(dif);
                if (dif.compareTo(zero) < 0)
                    dif = zero.minus(dif);                
                if (maxDif.compareTo(dif) < 0)                
                    maxDif = dif;
                if (c.status!=Candidate.ST_REMAINING)
                {    
                    RationalNumber vp = c.status==Candidate.ST_ELECTED ? c.numberOfCurrentVotes.minus(dt.currentQuota) : c.numberOfCurrentVotes;
                    if (!vp.equals(zero))
                        vp = zero;
                }
            }   
            System.out.println("MAXDIF = "+maxDif.doubleValue()+" desc "+dt.virtualDiscardCandidate.numberOfCurrentVotes +"  "+virtualDiscardCandidate.numberOfCurrentVotes+" TOTDIF = "+totDif);
            System.out.println("TOTMAIN = "+totMain+" TOTCOPY "+totCopy);
*/            
            for (int t=0; t<1024; t++)
                for (Candidate c:dt.candidatosFonte.valueCollection())
                    if (c.status!=Candidate.ST_REMAINING)
                    {
                        RationalNumber vp = c.status==Candidate.ST_ELECTED ? c.numberOfCurrentVotes.minus(dt.quocienteEleitoral) : c.numberOfCurrentVotes;
                        RationalNumber vp2 = zero;
                        if (!vp.equals(zero))
                        {   
                            if (!c.originalNeighbors.isEmpty())
                                c.numberOfCurrentVotes = c.numberOfCurrentVotes.minus(vp);
                            for (NeighborhoodRelation r:c.originalNeighbors.valueCollection())
                            {
                                //if (r.neighbor.identifier==28)
                                //    System.out.println("28 "+r.neighbor.numberOfCurrentVotes+" "+c.identifier+" "+vp.times(r.transferPercentage));
                                r.neighbor.numberOfCurrentVotes = r.neighbor.numberOfCurrentVotes.plus(vp.times(r.transferPercentage));
                                vp2 = vp2.plus(vp.times(r.transferPercentage));
                            }   
                            if (vp2.minus(vp).doubleValue() > 0.01)
                                System.out.println("Votos brotaram");
                        }    
                    }    
            for (Candidate c:dt.candidatosFonte.valueCollection())
                if (c.status!=Candidate.ST_REMAINING)
                {
                    RationalNumber vp = c.status==Candidate.ST_ELECTED ? c.numberOfCurrentVotes.minus(dt.quocienteEleitoral) : c.numberOfCurrentVotes;
                    if (!vp.equals(zero))
                    {    
                        c.numberOfCurrentVotes = c.numberOfCurrentVotes.minus(vp);
                        dt.descarte.numberOfCurrentVotes = dt.descarte.numberOfCurrentVotes.plus(vp);
                    }    
                }    
            dt.quocienteEleitoral = originalValidNumberOfVotes.minus(dt.descarte.numberOfCurrentVotes).divide(numberOfSeats);          
            //if (!dt.virtualDiscardCandidate.numberOfCurrentVotes.isZero())
            //    System.out.println("Descartados em convergência "+dt.virtualDiscardCandidate.numberOfCurrentVotes);
        }           
        RationalNumber maxDif = zero;
        RationalNumber totDif =  zero;            
        RationalNumber totMain = zero;
        RationalNumber totCopy = zero;
        for (Candidate c:dt.candidatos.valueCollection())
        {
            Candidate cc = candidates.get(c.identifier);
            totMain = totMain.plus(cc.numberOfCurrentVotes);
            totCopy = totCopy.plus(c.numberOfCurrentVotes);
            RationalNumber dif = c.numberOfCurrentVotes.minus(cc.numberOfCurrentVotes);
            
            if (dif.compareTo(zero) < 0)
                dif = zero.minus(dif);                
            if (maxDif.compareTo(dif) < 0)                
                maxDif = dif;            
            totDif = totDif.plus(dif);
            if (c.status!=Candidate.ST_REMAINING)
            {    
                RationalNumber vp = c.status==Candidate.ST_ELECTED ? c.numberOfCurrentVotes.minus(dt.quocienteEleitoral) : c.numberOfCurrentVotes;
                if (!vp.equals(zero))
                    vp = zero;
            }
        }   
        System.out.println("MAXDIF = "+maxDif.doubleValue()+" desc "+dt.descarte.numberOfCurrentVotes.doubleValue() +"  "+virtualDiscardCandidate.numberOfCurrentVotes.doubleValue()+" TOTDIF = "+totDif.doubleValue());
        System.out.println("TOTMAIN = "+totMain.doubleValue()+" TOTCOPY "+totCopy.doubleValue());
        
    }        
    
}
