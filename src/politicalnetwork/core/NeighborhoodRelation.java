package politicalnetwork.core;

import politicalnetwork.rationalnumber.RationalNumber;

/**
 * This class represents a neighborhood relationship. 
 * 
 * Is is just a pointer to the neighbor and the percentage of transfer.
 * 
 * @author Removed for blind review
 */
public class NeighborhoodRelation
{
    Candidate neighbor;
    RationalNumber transferPercentage;

    public NeighborhoodRelation(Candidate relecionado, RationalNumber percentualRepasse)
    {
        this.neighbor = relecionado;
        this.transferPercentage = percentualRepasse;
    }

    NeighborhoodRelation(NeighborhoodRelation r)
    {
        this.neighbor = r.neighbor;
        this.transferPercentage = r.transferPercentage;
    }

    public Candidate getRelacionado()
    {
        return neighbor;
    }

    public RationalNumber getPercentualRepasse()
    {
        return transferPercentage;
    }

    public String toString()
    {
        return neighbor.identifier+" "+transferPercentage;
    }        
    
}
