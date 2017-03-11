/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package politicalnetwork.testimplementationsimp;

import politicalnetwork.rationalnumber.RationalNumber;

/**
 *
 * @author jesjf
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
